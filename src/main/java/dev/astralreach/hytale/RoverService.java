package dev.astralreach.hytale;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.builtin.mounts.MountSystems;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.player.*;
import com.hypixel.hytale.server.core.modules.entity.teleport.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Rotation3f;
import dev.astralreach.core.RoverPower;
import java.util.*;
import org.bson.*;
import org.joml.Vector3d;

/** Native seated rendering with server-owned off-road motion and finite power. */
final class RoverService {
 static final String ITEM="AR_LunarRover",CHARGE="AstralRoverChargeV1";
 static ComponentType<EntityStore,RoverBody>bodyType;static ComponentType<EntityStore,RoverSeat>seatType;
 static MountedComponent mount(Ref<EntityStore>vehicle,boolean passenger){return new MountedComponent(vehicle,RoverView.offset(passenger),MountController.Minecart);}
 static boolean wantsExit(MovementStates states){return states!=null&&states.crouching&&!states.forcedCrouching;}
 static void trace(String reason){AstralReachPlugin.instance.getLogger().atInfo().log("AR_ROVER_SEAT "+reason);}
 static boolean riding(Store<EntityStore>s,Ref<EntityStore>r){return seatType!=null&&s.getComponent(r,seatType)!=null;}
 static void say(Store<EntityStore>s,Ref<EntityStore>r,String text){var p=s.getComponent(r,PlayerRef.getComponentType());if(p!=null)AstralReachPlugin.message(p,text);}
 static boolean near(Store<EntityStore>s,Ref<EntityStore>r,Vector3d p,double distance){var t=s.getComponent(r,TransformComponent.getComponentType());return t!=null&&t.getPosition().distanceSquared(p)<=distance*distance;}
 static long charge(ItemStack kit){var m=kit.getMetadata();var value=m==null?null:m.get(CHARGE);if(value==null)return 0;if(!value.isNumber())throw new IllegalArgumentException("Invalid rover charge");double d=value.asNumber().doubleValue();long n=value.asNumber().longValue();if(!Double.isFinite(d)||d!=n||n<0||n>RoverPower.CAPACITY)throw new IllegalArgumentException("Invalid rover charge");return n;}
 static ItemStack kit(RoverBody body){var m=body.kit.getMetadata()==null?new BsonDocument():body.kit.getMetadata().clone();m.put(CHARGE,new BsonInt64(body.power.units()));return body.kit.withMetadata(m).withMaxDurability(1800).withDurability(body.power.seconds());}
 static void deploy(Store<EntityStore>s,Ref<EntityStore>r,Vector3d at,ItemContainer inv,short slot,ItemStack held){
  try{
   if(held.getQuantity()!=1||riding(s,r))throw new IllegalArgumentException("Stand on foot to deploy one rover kit.");
   var head=s.getComponent(r,HeadRotation.getComponentType());float yaw=head==null?0:head.getRotation().yaw();var w=s.getExternalData().getWorld();var p=RoverTerrain.ground(w,at,yaw);
   if(p==null||!near(s,r,p,6)||!RoverTerrain.clear(w,p,yaw))throw new IllegalArgumentException("Deploy on clear supported terrain: allow three by four blocks, with room for the mast.");
   var asset=ModelAsset.getAssetMap().getAsset(ITEM);if(asset==null)throw new IllegalArgumentException("Rover model is unavailable; keep the kit.");long units=charge(held);
   var h=EntityStore.REGISTRY.newHolder();var model=Model.createScaledModel(asset,1);var body=new RoverBody();body.owner=s.getComponent(r,PlayerRef.getComponentType()).getUuid();body.power=new RoverPower(units);body.kit=YardInventory.copy(held);
   h.addComponent(TransformComponent.getComponentType(),new TransformComponent(p,new Rotation3f(0,yaw,0)));h.ensureComponent(UUIDComponent.getComponentType());h.ensureComponent(Dirty.getComponentType());h.addComponent(NetworkId.getComponentType(),new NetworkId(s.getExternalData().takeNextNetworkId()));h.addComponent(PersistentModel.getComponentType(),new PersistentModel(model.toReference()));h.addComponent(ModelComponent.getComponentType(),new ModelComponent(model));h.addComponent(BoundingBox.getComponentType(),new BoundingBox(model.getBoundingBox()));h.addComponent(ModelRotateBoundingBoxComponent.getComponentType(),new ModelRotateBoundingBoxComponent(true));h.ensureComponent(Interactable.getComponentType());h.addComponent(Interactions.getComponentType(),new Interactions(Map.of(InteractionType.Use,"AR_RoverUse")));h.addComponent(bodyType,body);
   if(!inv.replaceItemStackInSlot(slot,held,null).succeeded())throw new IllegalArgumentException("Rover kit moved; try again.");
   try{s.addEntity(h,AddReason.SPAWN);}catch(RuntimeException ex){if(!inv.replaceItemStackInSlot(slot,inv.getItemStack(slot),held).succeeded())throw new IllegalStateException("Rover deployment rollback refused; retain player inventory",ex);throw ex;}
   LunarEnvironment.interior(s,r,s.getComponent(r,PlayerRef.getComponentType()),"AR_Blueprint_Service");say(s,r,"Rover deployed. Hold a Beacon Cell and use F to load power; use F to drive. Empty and crouch-F to pack it.");
  }catch(IllegalArgumentException ex){say(s,r,ex.getMessage());}
 }
 static void use(Store<EntityStore>s,Ref<EntityStore>r,Ref<EntityStore>target){
  try{
   var body=s.getComponent(target,bodyType);var t=s.getComponent(target,TransformComponent.getComponentType());var p=s.getComponent(r,PlayerRef.getComponentType());var player=s.getComponent(r,Player.getComponentType());
   if(body==null||t==null||p==null||player==null||!near(s,r,t.getPosition(),4)||s.getComponent(r,DeathComponent.getComponentType())!=null)return;
   boolean owner=body.owner.equals(p.getUuid());var held=player.getInventory().getItemInHand();
   if(owner&&!ItemStack.isEmpty(held)&&held.getItemId().equals("AR_BeaconCell")){
    if(Math.abs(body.speed)>.05)throw new IllegalArgumentException("Stop before changing a power cell.");if(!body.power.canLoad())throw new IllegalArgumentException("A full cell will not fit yet. Use more power first.");
    var hotbar=player.getInventory().getHotbar();short slot=player.getInventory().getActiveHotbarSlot();if(slot<0||!held.equals(hotbar.getItemStack(slot))||!hotbar.replaceItemStackInSlot(slot,held,held.getQuantity()==1?null:held.withQuantity(held.getQuantity()-1)).succeeded())throw new IllegalArgumentException("Power cell moved; try again.");body.power.load();var dirty=s.getComponent(target,Dirty.getComponentType());if(dirty!=null)dirty.markDirty();LunarEnvironment.interior(s,r,p,"AR_Blueprint_Service");say(s,r,"Power cell loaded: "+NativeUi.duration(body.power.seconds())+" of moving power.");return;
   }
   if(riding(s,r)){var seat=s.getComponent(r,seatType);if(target.equals(seat.vehicle))exit(s,r,target,body);return;}
   var move=s.getComponent(r,MovementStatesComponent.getComponentType());boolean crouched=move!=null&&move.getMovementStates().crouching;
   if(owner&&crouched){if(body.driver!=null||body.passenger!=null||Math.abs(body.speed)>.05)throw new IllegalArgumentException("Stop and empty both seats before packing the rover.");if(!YardInventory.refund(player.getInventory().getCombinedStorageHotbarBackpack(),List.of(kit(body))))throw new IllegalArgumentException("Make room for the rover kit; its charge stays here.");s.removeEntity(target,RemoveReason.REMOVE);LunarEnvironment.interior(s,r,p,"AR_Blueprint_Service");return;}
   var flight=s.getComponent(r,AstralReachPlugin.flightType);if(flight!=null&&flight.boarded||CrewBoarding.occupied(s,r)||s.getComponent(r,MountedComponent.getComponentType())!=null||s.getComponent(r,Teleport.getComponentType())!=null)throw new IllegalArgumentException("Leave your current seat before boarding the rover.");
   boolean guest=!owner;if(guest&&(body.driver==null||body.passenger!=null))throw new IllegalArgumentException("The owner must drive before a visitor can occupy the passenger seat.");if(owner&&body.driver!=null)throw new IllegalArgumentException("Driver seat occupied.");if(owner&&body.power.units()==0)throw new IllegalArgumentException("Load a Beacon Cell first: hold it and use F on your rover.");
   if(guest)body.passenger=r;else{body.driver=r;body.inputAge=10;body.throttle=0;body.steer=0;body.brake=0;body.wish=false;}
   s.putComponent(r,seatType,new RoverSeat(target,guest,s.getExternalData().getWorld().getName()));RoverTransport.bind(p.getUuid(),s.getExternalData().getWorld(),r,target,guest);AnimationUtils.playAnimation(r,AnimationSlot.Action,"Sit",true,s);var at=RoverView.seat(t,guest);s.putComponent(r,Teleport.getComponentType(),Teleport.createForPlayer(at,new Rotation3f(0,t.getRotation().yaw(),0)));RoverView.prepare(p);player.getHudManager().addCustomHud(p,new RoverHud(p));LunarEnvironment.interior(s,r,p,"AR_Blueprint_Service");say(s,r,guest?"Passenger aboard. Your suit still supplies oxygen. Dismount (F) to exit.":"W/S drive, A/D steer, Space brake, F exit. Your suit still supplies oxygen.");
  }catch(IllegalArgumentException ex){say(s,r,ex.getMessage());}
 }
 static void clearRider(Store<EntityStore>s,Ref<EntityStore>r,CommandBuffer<EntityStore>b){
  if(r==null)return;RoverTransport.unbind(r);if(!r.isValid()||r.getStore()!=s)return;var player=s.getComponent(r,Player.getComponentType());var p=s.getComponent(r,PlayerRef.getComponentType());var seat=s.getComponent(r,seatType);var mounted=s.getComponent(r,MountedComponent.getComponentType());boolean ours=seat!=null&&mounted!=null&&seat.vehicle!=null&&seat.vehicle.equals(mounted.getMountedToEntity());if(ours){if(b==null)s.tryRemoveComponent(r,MountedComponent.getComponentType());else b.tryRemoveComponent(r,MountedComponent.getComponentType());}if(player!=null&&p!=null){player.getHudManager().removeCustomHud(p,RoverHud.KEY);RoverView.reset(p);}if(b==null){s.tryRemoveComponent(r,seatType);AnimationUtils.stopAnimation(r,AnimationSlot.Action,s);}else{b.tryRemoveComponent(r,seatType);AnimationUtils.stopAnimation(r,AnimationSlot.Action,b);}
 }
 static boolean exit(Store<EntityStore>s,Ref<EntityStore>r,Ref<EntityStore>vehicle,RoverBody body){
  var t=s.getComponent(vehicle,TransformComponent.getComponentType());body.speed=0;body.throttle=0;var at=RoverTerrain.exit(s.getExternalData().getWorld(),t.getPosition(),t.getRotation().yaw());if(at==null){say(s,r,"Exit blocked. Rover stopped; clear a space beside it.");return false;}
  if(r.equals(body.driver))body.driver=null;if(r.equals(body.passenger))body.passenger=null;clearRider(s,r,null);s.putComponent(r,Teleport.getComponentType(),Teleport.createForPlayer(at,new Rotation3f(0,t.getRotation().yaw(),0)));return true;
 }
 static boolean valid(Store<EntityStore>s,Ref<EntityStore>r,Ref<EntityStore>vehicle){return r!=null&&r.isValid()&&r.getStore()==s&&s.getComponent(r,DeathComponent.getComponentType())==null&&s.getComponent(r,seatType)!=null&&vehicle.equals(s.getComponent(r,seatType).vehicle);}
 static final class Lifecycle extends HolderSystem<EntityStore>{
  @Override public Query<EntityStore>getQuery(){return bodyType;}@Override public Set<Dependency<EntityStore>>getDependencies(){return RootDependency.lastSet();}
  @Override public void onEntityAdd(Holder<EntityStore>h,AddReason reason,Store<EntityStore>s){var asset=ModelAsset.getAssetMap().getAsset(ITEM);if(asset==null)throw new IllegalStateException("Retain rover: model unavailable");var model=Model.createScaledModel(asset,1);if(h.getComponent(NetworkId.getComponentType())==null)h.addComponent(NetworkId.getComponentType(),new NetworkId(s.getExternalData().takeNextNetworkId()));if(h.getComponent(ModelComponent.getComponentType())==null)h.addComponent(ModelComponent.getComponentType(),new ModelComponent(model));if(h.getComponent(BoundingBox.getComponentType())==null)h.addComponent(BoundingBox.getComponentType(),new BoundingBox(model.getBoundingBox()));h.ensureComponent(Dirty.getComponentType());h.ensureComponent(Interactable.getComponentType());if(h.getComponent(Interactions.getComponentType())==null)h.addComponent(Interactions.getComponentType(),new Interactions(Map.of(InteractionType.Use,"AR_RoverUse")));if(h.getComponent(ModelRotateBoundingBoxComponent.getComponentType())==null)h.addComponent(ModelRotateBoundingBoxComponent.getComponentType(),new ModelRotateBoundingBoxComponent(true));}
  @Override public void onEntityRemoved(Holder<EntityStore>h,RemoveReason reason,Store<EntityStore>s){var body=h.getComponent(bodyType);var riders=new ArrayList<Ref<EntityStore>>();riders.add(body.driver);riders.add(body.passenger);for(var player:riders){RoverTransport.unbind(player);if(player!=null&&player.isValid()){s.getExternalData().getWorld().execute(()->{if(player.isValid()&&player.getStore()==s)clearRider(s,player,null);});}}}
 }
 static final class Inputs extends EntityTickingSystem<EntityStore>{
  @Override public Query<EntityStore>getQuery(){return Query.and(seatType,PlayerInput.getComponentType());}
  @Override public Set<Dependency<EntityStore>>getDependencies(){return Set.of(new SystemDependency<>(Order.BEFORE,MountSystems.HandleMountInput.class),new SystemDependency<>(Order.BEFORE,PlayerSystems.ProcessPlayerInput.class));}
  @Override public void tick(float dt,int i,ArchetypeChunk<EntityStore>chunk,Store<EntityStore>s,CommandBuffer<EntityStore>b){
   var r=chunk.getReferenceTo(i);var seat=chunk.getComponent(i,seatType);var vehicle=seat.vehicle;
   if(vehicle==null||!vehicle.isValid()||vehicle.getStore()!=s||s.getComponent(vehicle,bodyType)==null){
    if(seat.world.equals(s.getExternalData().getWorld().getName())){var t=s.getComponent(r,TransformComponent.getComponentType());var at=RoverTerrain.exit(s.getExternalData().getWorld(),t.getPosition(),t.getRotation().yaw());if(at==null)return;b.putComponent(r,Teleport.getComponentType(),Teleport.createForPlayer(at,t.getRotation()));}trace("recovery vehicle="+(vehicle==null?"null":vehicle.isValid()?"valid":"invalid"));clearRider(s,r,b);return;
   }
   var body=s.getComponent(vehicle,bodyType);if(seat.nativeAttached&&s.getComponent(r,MountedComponent.getComponentType())==null){s.getExternalData().getWorld().execute(()->{if(r.isValid()&&vehicle.isValid()&&r.getStore()==s)exit(s,r,vehicle,body);});return;}var input=chunk.getComponent(i,PlayerInput.getComponentType());var queue=input.getMovementUpdateQueue();boolean leave=false,crouch=false,forced=false;PlayerInput.WishMovement intent=null;Vector3d velocity=null;
   if(seat.nativeAttached&&seat.inputTraces==0){seat.inputTraces++;trace("native input tick route="+RoverTransport.bound(r));}
   if(seat.nativeAttached&&RoverTransport.bound(r)){queue.removeIf(u->u instanceof PlayerInput.WishMovement||u instanceof PlayerInput.RelativeMovement||u instanceof PlayerInput.AbsoluteMovement||u instanceof PlayerInput.SetBody||u instanceof PlayerInput.SetClientVelocity);return;}
   if(!queue.isEmpty()&&!seat.passenger)body.inputAge=0;
   for(var u:queue){if(u instanceof PlayerInput.WishMovement v)intent=v;if(u instanceof PlayerInput.SetClientVelocity v)velocity=v.getVelocity();var states=u instanceof PlayerInput.SetRiderMovementStates v?v.movementStates():u instanceof PlayerInput.SetMovementStates v?v.movementStates():null;if(states!=null){crouch|=states.crouching;forced|=states.forcedCrouching;if(!seat.passenger)body.brake=states.jumping?1:0;if(wantsExit(states))leave=true;}}
   boolean motion=intent!=null&&Math.hypot(intent.getX(),intent.getZ())>.01||velocity!=null&&Math.hypot(velocity.x,velocity.z)>.01;
   long traceNow=System.nanoTime();if((motion||crouch)&&seat.inputTraces<6&&traceNow-seat.inputTraceAt>1_000_000_000L){seat.inputTraceAt=traceNow;seat.inputTraces++;trace("input "+queue.stream().map(u->u.getClass().getSimpleName()).distinct().toList()+" mount="+input.getMountId()+" expected="+s.getComponent(vehicle,NetworkId.getComponentType()).getId()+" wish="+(intent==null?"absent":intent.getX()+","+intent.getZ())+" velocity="+velocity+" crouch="+crouch+" forced="+forced);}
   // Prefer a wish from this batch; a previous wish must not suppress later mounted velocity input.
   if(!seat.passenger){body.wish=intent!=null;if(intent!=null||velocity!=null){double x=intent!=null?intent.getX():velocity.x,z=intent!=null?intent.getZ():velocity.z;if(Double.isFinite(x)&&Double.isFinite(z)){double length=Math.hypot(x,z);if(length>.12){x/=length;z/=length;}else{x=z=0;}double yaw=s.getComponent(vehicle,TransformComponent.getComponentType()).getRotation().yaw();body.throttle=Math.clamp(-x*Math.sin(yaw)-z*Math.cos(yaw),-1,1);body.steer=Math.clamp(x*Math.cos(yaw)-z*Math.sin(yaw),-1,1);}}}
   queue.removeIf(u->u instanceof PlayerInput.WishMovement||u instanceof PlayerInput.RelativeMovement||u instanceof PlayerInput.AbsoluteMovement||u instanceof PlayerInput.SetBody||u instanceof PlayerInput.SetClientVelocity);
   if(leave){trace("intentional crouch exit input");s.getExternalData().getWorld().execute(()->{if(r.isValid()&&vehicle.isValid()&&r.getStore()==s)exit(s,r,vehicle,body);});}
  }
 }
 static final class Cars extends EntityTickingSystem<EntityStore>{
  @Override public Query<EntityStore>getQuery(){return Query.and(bodyType,TransformComponent.getComponentType());}
  @Override public Set<Dependency<EntityStore>>getDependencies(){return Set.of(new SystemDependency<>(Order.AFTER,Inputs.class),new SystemDependency<>(Order.BEFORE,com.hypixel.hytale.server.core.modules.entity.system.TransformSystems.EntityTrackerUpdate.class));}
  @Override public void tick(float elapsed,int i,ArchetypeChunk<EntityStore>chunk,Store<EntityStore>s,CommandBuffer<EntityStore>b){
   var vehicle=chunk.getReferenceTo(i);var body=chunk.getComponent(i,bodyType);var t=chunk.getComponent(i,TransformComponent.getComponentType());double dt=Math.clamp(elapsed,0,.1);
   if(body.driver!=null&&!valid(s,body.driver,vehicle))trace("driver invalid; live="+body.driver.isValid());
   if(!valid(s,body.driver,vehicle)){clearRider(s,body.driver,b);body.driver=null;body.speed=0;body.throttle=0;}
   if(!valid(s,body.passenger,vehicle)){clearRider(s,body.passenger,b);body.passenger=null;}
   body.inputAge+=dt;if(body.inputAge>.75){body.throttle=0;body.steer=0;}
   if(body.driver!=null&&dt>0){
    if(body.brake>.5||body.power.units()==0)body.speed=approach(body.speed,0,10*dt);else if(Math.abs(body.throttle)>.05)body.speed=Math.clamp(body.speed+body.throttle*3.5*dt,-2.5,7.5);else body.speed=approach(body.speed,0,2*dt);
    double yaw=t.getRotation().yaw()-body.steer*dt*Math.clamp(body.speed/3,-1,1)*1.3;var next=new Vector3d(t.getPosition()).add(-Math.sin(yaw)*body.speed*dt,0,-Math.cos(yaw)*body.speed*dt);var at=RoverTerrain.ground(s.getExternalData().getWorld(),next,yaw);
    if(at!=null&&RoverTerrain.clear(s.getExternalData().getWorld(),at,yaw)){
     if(Math.abs(body.speed)>.05){double powered=body.power.consume(dt,true);if(powered>0){t.setPosition(new Vector3d(t.getPosition()).lerp(at,powered));t.setRotation(new Rotation3f(0,(float)yaw,0));var dirty=s.getComponent(vehicle,Dirty.getComponentType());if(dirty!=null)dirty.markDirty();}if(powered<1)body.speed=0;}
    }else body.speed=0;
   }
   boolean moving=body.driver!=null&&body.power.units()>0&&Math.abs(body.speed)>.05;
   if(moving!=body.moving){var audio=new AudioComponent();var map=SoundEvent.getAssetMap();if(moving){if(map.getAsset("AR_Rover_Drive")!=null)audio.addSound(map.getIndex("AR_Rover_Drive"));AnimationUtils.playAnimation(vehicle,AnimationSlot.Movement,"Drive",true,b);}else AnimationUtils.stopAnimation(vehicle,AnimationSlot.Movement,b);b.putComponent(vehicle,AudioComponent.getComponentType(),audio);body.moving=moving;}
   follow(s,body.driver,t,body,false,b);follow(s,body.passenger,t,body,true,b);
  }
  private void follow(Store<EntityStore>s,Ref<EntityStore>r,TransformComponent t,RoverBody body,boolean passenger,CommandBuffer<EntityStore>b){if(r==null||!r.isValid()||r.getStore()!=s)return;var rt=s.getComponent(r,TransformComponent.getComponentType());if(rt!=null){rt.setPosition(RoverView.seat(t,passenger));rt.setRotation(new Rotation3f(0,t.getRotation().yaw(),0));}var seat=s.getComponent(r,seatType);
   // Native attachment holds the local/observer body still; movement stays server owned.
   if(seat!=null&&!seat.nativeAttached&&s.getComponent(r,Teleport.getComponentType())==null&&s.getComponent(r,PendingTeleport.getComponentType())==null){var attached=s.getComponent(r,MountedComponent.getComponentType());if(attached==null){seat.nativeAttached=true;b.putComponent(r,MountedComponent.getComponentType(),mount(seat.vehicle,passenger));}else if(seat.vehicle.equals(attached.getMountedToEntity()))seat.nativeAttached=true;}

   var p=s.getComponent(r,PlayerRef.getComponentType());if(seat!=null&&seat.nativeAttached&&seat.vehicle!=null&&seat.vehicle.isValid()){var network=s.getComponent(seat.vehicle,NetworkId.getComponentType());var input=s.getComponent(r,PlayerInput.getComponentType());if(network!=null&&input!=null&&input.getMountId()==network.getId())RoverView.update(p,t,network.getId());}var player=s.getComponent(r,Player.getComponentType());if(player!=null&&p!=null){var old=player.getHudManager().getCustomHud(RoverHud.KEY);RoverHud hud;if(old instanceof RoverHud h)hud=h;else{hud=new RoverHud(p);player.getHudManager().addCustomHud(p,hud);}hud.show(body,passenger);}}
 }
 static double approach(double v,double goal,double step){return v<goal?Math.min(goal,v+step):Math.max(goal,v-step);}
 private RoverService(){}
}
