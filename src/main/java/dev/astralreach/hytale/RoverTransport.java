package dev.astralreach.hytale;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.entities.MountMovement;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.protocol.packets.interaction.DismountNPC;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.io.handlers.GenericPacketHandler;
import com.hypixel.hytale.lib.quiche.QuicheChannel;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Native mounted packets provide bounded intent, never authoritative position or rotation. */
final class RoverTransport {
 private static final ConcurrentHashMap<UUID,Route> routes=new ConcurrentHashMap<>();
 private static final ConcurrentHashMap<Ref<EntityStore>,Route> riders=new ConcurrentHashMap<>();
 private static PacketFilter filter;
 private static final java.util.Set<String> handlers=ConcurrentHashMap.newKeySet();
 private static final java.util.concurrent.atomic.AtomicInteger gameTraces=new java.util.concurrent.atomic.AtomicInteger();
 private static final class Route {
  final UUID id;final World world;final Ref<EntityStore> rider,vehicle;final boolean passenger;
  final AtomicLong receivedAt=new AtomicLong(),rawReceivedAt=new AtomicLong();final AtomicBoolean exiting=new AtomicBoolean();
  final java.util.Set<String> observed=ConcurrentHashMap.newKeySet();
  volatile QuicheChannel channel;volatile RoverPacketBridge bridge;
  int rawTraces;long rawTraceAt,rawUntil;boolean absolutePrimed;double x,z;
  Route(UUID id,World world,Ref<EntityStore> rider,Ref<EntityStore> vehicle,boolean passenger,double x,double z){this.id=id;this.world=world;this.rider=rider;this.vehicle=vehicle;this.passenger=passenger;this.x=x;this.z=z;}
 }
 private record Intent(boolean present,boolean wish,double x,double z,boolean states,boolean jump,boolean crouch,boolean forced,int mounted,boolean relative,double rx,double rz,boolean absolute,double ax,double az){}
 private record Sample(boolean position,double x,double z,boolean states,boolean jump,boolean crouch,boolean forced){}
 static void start(){if(filter==null){filter=(handler,packet)->{
  String kind=handler.getClass().getSimpleName();if(handlers.size()<6&&handlers.add(kind))RoverService.trace("adapter handler="+kind);
  if(!(handler instanceof GamePacketHandler game))return false;
  if(gameTraces.getAndIncrement()<3)RoverService.trace("adapter game packet="+packet.getClass().getSimpleName()+" routes="+routes.size());
  return receive(game.getPlayerRef().getUuid(),packet);
 };PacketAdapters.registerInbound(filter);RoverService.trace("transport registered");}}
 static void stop(){for(var route:routes.values())detach(route);if(filter!=null){PacketAdapters.deregisterInbound(filter);filter=null;}routes.clear();riders.clear();handlers.clear();gameTraces.set(0);RoverView.clear();RoverService.trace("transport stopped");}
 static void bind(UUID id,World world,Ref<EntityStore> rider,Ref<EntityStore> vehicle,boolean passenger){
  unbind(rider);var at=vehicle.getStore().getComponent(vehicle,TransformComponent.getComponentType()).getPosition();
  var route=new Route(id,world,rider,vehicle,passenger,at.x,at.z);var old=routes.put(id,route);if(old!=null)riders.remove(old.rider,old);riders.put(rider,route);RoverService.trace("transport bound passenger="+passenger+" world="+world.getName());
  var player=rider.getStore().getComponent(rider,PlayerRef.getComponentType());
  if(player!=null&&player.getPacketHandler() instanceof GenericPacketHandler handler&&handler.getChannel() instanceof QuicheChannel channel){
   route.channel=channel;channel.execute(()->{if(routes.get(id)!=route)return;try{route.bridge=RoverPacketBridge.attach(handler,packet->receive(id,packet),new int[]{MountMovement.PACKET_ID,DismountNPC.PACKET_ID,ClientMovement.PACKET_ID});RoverService.trace("direct Quiche bridge installed");}catch(RuntimeException ex){RoverService.trace("direct bridge failed "+ex.getClass().getSimpleName());schedule(route,()->{if(rider.isValid()&&vehicle.isValid()){var body=vehicle.getStore().getComponent(vehicle,RoverService.bodyType);if(body!=null)RoverService.exit(vehicle.getStore(),rider,vehicle,body);}});}});
  }
 }
 static boolean bound(Ref<EntityStore> rider){var route=riders.get(rider);return route!=null&&routes.get(route.id)==route;}
 static void unbind(Ref<EntityStore> rider){if(rider==null)return;var route=riders.remove(rider);if(route!=null){routes.remove(route.id,route);detach(route);RoverView.forget(route.id);RoverService.trace("transport unbound");}}
 private static void forget(Route route){riders.remove(route.rider,route);routes.remove(route.id,route);detach(route);RoverView.forget(route.id);RoverService.trace("transport revoked");}
 private static void detach(Route route){var channel=route.channel;if(channel!=null)try{channel.execute(()->{var bridge=route.bridge;if(bridge!=null){bridge.close();route.bridge=null;RoverService.trace("direct Quiche bridge restored");}});}catch(RuntimeException ignored){/* Closed connection; inactive wrappers delegate original handlers. */}}
 static boolean receive(UUID id,Packet packet){
  var route=routes.get(id);if(route==null)return false;
  String kind=packet.getClass().getSimpleName();if(route.observed.size()<12&&route.observed.add(kind))RoverService.trace("bound packet "+kind);
  if(packet instanceof ClientMovement movement){
   long now=System.nanoTime(),last=route.rawReceivedAt.get();
   if(now-last>=20_000_000L&&route.rawReceivedAt.compareAndSet(last,now)){
    var wish=movement.wishMovement;var velocity=movement.velocity;var relative=movement.relativePosition;var absolute=movement.absolutePosition;var states=movement.riderMovementStates!=null?movement.riderMovementStates:movement.movementStates;
    var input=new Intent(wish!=null||velocity!=null,wish!=null,wish!=null?wish.x:velocity!=null?velocity.x:0,wish!=null?wish.z:velocity!=null?velocity.z:0,states!=null,(movement.movementStates!=null&&movement.movementStates.jumping)||(movement.riderMovementStates!=null&&movement.riderMovementStates.jumping),states!=null&&states.crouching,states!=null&&states.forcedCrouching,movement.mountedTo,relative!=null,relative==null?0:relative.x/10000.0,relative==null?0:relative.z/10000.0,absolute!=null,absolute==null?0:absolute.x,absolute==null?0:absolute.z);
    schedule(route,()->applyIntent(route,input));
   }
   // Preserve native teleport acknowledgements and unrelated movement state handling.
   // Inputs strips client pose/wish/velocity from the queue while this rover route is bound.
   return false;
  }
  if(!(packet instanceof MountMovement)&&!(packet instanceof DismountNPC))return false;
  if(packet instanceof DismountNPC leave){
   int expected=leave.mountEntityId;if(route.exiting.compareAndSet(false,true))schedule(route,()->{try{var body=validate(route);if(body==null)return;var s=route.vehicle.getStore();int actual=s.getComponent(route.vehicle,NetworkId.getComponentType()).getId();if(expected<=0||expected==actual)RoverService.exit(s,route.rider,route.vehicle,body);}finally{route.exiting.set(false);}});
   return true;
  }
  var movement=(MountMovement)packet;long now=System.nanoTime(),last=route.receivedAt.get();
  if(now-last<20_000_000L||!route.receivedAt.compareAndSet(last,now))return true;
  var at=movement.absolutePosition;var states=movement.movementStates;
  var sample=new Sample(at!=null,at==null?0:at.x,at==null?0:at.z,states!=null,states!=null&&states.jumping,states!=null&&states.crouching,states!=null&&states.forcedCrouching);
  schedule(route,()->apply(route,sample));return true;
 }
 private static void schedule(Route route,Runnable action){try{route.world.execute(action);}catch(RuntimeException ex){forget(route);}}
 private static RoverBody validate(Route route){
  if(routes.get(route.id)!=route||riders.get(route.rider)!=route)return null;
  var s=route.world.getEntityStore().getStore();
  if(!route.rider.isValid()||!route.vehicle.isValid()||route.rider.getStore()!=s||route.vehicle.getStore()!=s){forget(route);return null;}
  var seat=s.getComponent(route.rider,RoverService.seatType);var body=s.getComponent(route.vehicle,RoverService.bodyType);
  if(seat==null||body==null||seat.vehicle!=route.vehicle||seat.passenger!=route.passenger||!(route.passenger?route.rider.equals(body.passenger):route.rider.equals(body.driver)&&route.id.equals(body.owner))){forget(route);return null;}
  // Movement can arrive while the initial player teleport still awaits acknowledgement.
  // Retain the valid route until Cars attaches the native seat; do not revoke it in this window.
  if(!seat.nativeAttached)return null;
  return body;
 }
 private static void applyIntent(Route route,Intent input){
  var body=validate(route);if(body==null)return;var store=route.vehicle.getStore();long now=System.nanoTime();
  if(route.rawTraces<8&&(route.rawTraces<2||Math.hypot(input.x,input.z)>.12||input.jump||input.crouch||input.relative&&Math.hypot(input.rx,input.rz)>.002||input.absolute&&route.absolutePrimed&&Math.hypot(input.ax-route.x,input.az-route.z)>.002)&&(now-route.rawTraceAt>1_000_000_000L||input.jump||input.crouch)){route.rawTraces++;route.rawTraceAt=now;RoverService.trace("raw input present="+input.present+" wish="+input.wish+" x="+input.x+" z="+input.z+" mount="+input.mounted+" expected="+store.getComponent(route.vehicle,NetworkId.getComponentType()).getId()+" jump="+input.jump+" crouch="+input.crouch+" forced="+input.forced+" rel="+input.relative+":"+input.rx+","+input.rz+" abs="+input.absolute+":"+input.ax+","+input.az);}
  if(input.crouch&&!input.forced){RoverService.exit(store,route.rider,route.vehicle,body);return;}
  if(route.passenger)return;if(input.states)body.brake=input.jump?1:0;
  double x=input.x,z=input.z;boolean pose=false;
  int mount=store.getComponent(route.vehicle,NetworkId.getComponentType()).getId();
  // Native Minecart sends mounted displacement with zero rider velocity; explicit wishes still win.
  if(!input.wish&&Math.hypot(x,z)<=.12&&input.mounted==mount){
   if(input.relative){x=input.rx;z=input.rz;pose=true;}
   else if(input.absolute){if(!route.absolutePrimed){route.x=input.ax;route.z=input.az;route.absolutePrimed=true;x=0;z=0;}else{x=input.ax-route.x;z=input.az-route.z;route.x=input.ax;route.z=input.az;}pose=true;}
  }
  if(!input.present&&!pose)return;
  double length=Math.hypot(x,z);if(!Double.isFinite(length)||pose&&length>8){body.throttle=0;body.steer=0;body.brake=1;return;}
  route.rawUntil=now+1_000_000_000L;body.inputAge=0;
  if(length>(pose?.002:.12)){x/=length;z/=length;}else{x=0;z=0;}
  double yaw=store.getComponent(route.vehicle,TransformComponent.getComponentType()).getRotation().yaw();body.throttle=Math.clamp(-x*Math.sin(yaw)-z*Math.cos(yaw),-1,1);body.steer=Math.clamp(x*Math.cos(yaw)-z*Math.sin(yaw),-1,1);
 }
 private static void apply(Route route,Sample input){
  var body=validate(route);if(body==null)return;var s=route.vehicle.getStore();
  if(input.crouch&&!input.forced){RoverService.exit(s,route.rider,route.vehicle,body);return;}
  if(route.passenger||System.nanoTime()<route.rawUntil)return;
  if(input.states)body.brake=input.jump?1:0;
  if(!input.position||!Double.isFinite(input.x)||!Double.isFinite(input.z)){body.throttle=0;body.steer=0;return;}
  double x=input.x-route.x,z=input.z-route.z;route.x=input.x;route.z=input.z;double length=Math.hypot(x,z);
  if(!Double.isFinite(length)||length>8){body.throttle=0;body.steer=0;body.brake=1;return;}
  body.inputAge=0;
  if(length>.002){x/=length;z/=length;}else{x=0;z=0;}
  double yaw=s.getComponent(route.vehicle,TransformComponent.getComponentType()).getRotation().yaw();
  body.throttle=Math.clamp(-x*Math.sin(yaw)-z*Math.cos(yaw),-1,1);body.steer=Math.clamp(x*Math.cos(yaw)-z*Math.sin(yaw),-1,1);
  var seat=s.getComponent(route.rider,RoverService.seatType);long now=System.nanoTime();
  if((length>.002||body.brake>.5)&&seat.inputTraces<6&&now-seat.inputTraceAt>1_000_000_000L){seat.inputTraceAt=now;seat.inputTraces++;RoverService.trace("mount transport throttle="+body.throttle+" steer="+body.steer+" brake="+body.brake);}
 }
 private RoverTransport(){}
}
