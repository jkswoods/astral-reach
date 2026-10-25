package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.*;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;
import org.joml.Vector3d;
import java.util.*;
import java.util.concurrent.*;
import static dev.astralreach.hytale.PlayerFlightState.Leg;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Physical upper-stage travel and saved lunar parking; never creates a second fuel reserve. */
final class LunarFlight {
 private static final Map<UUID,Actor> actors=new ConcurrentHashMap<>();
 private static final Set<UUID> pending=ConcurrentHashMap.newKeySet();
 private static final class Actor {Ref<EntityStore> ref;Leg leg;final String model;final Vector3d footprint;long fireAt,retryAt;boolean powered;final Set<String> cues=new HashSet<>();Actor(Ref<EntityStore> r,Leg l,String model,Vector3d at){ref=r;leg=l;this.model=model;footprint=new Vector3d(at);}}
 static LandedShipRegistry data(Store<EntityStore>s){return s.getResource(AstralReachPlugin.landedType);}
 static CompletableFuture<World> prepare(PlayerRef p,LaunchPlan plan){
  return AstralReachPlugin.world(true).thenCompose(w->{var done=new CompletableFuture<World>();w.execute(()->{
   var s=w.getEntityStore().getStore();var d=data(s);var previous=d.ships.get(p.getUuid());
   int slot=Math.floorMod(p.getUuid().hashCode(),4096),x=0,z=0,y=0;
   boolean retain=previous!=null&&!previous.departed;
   if(retain){x=previous.x;y=previous.y;z=previous.z;}
   else{
    boolean found=false;for(int attempt=0;attempt<4096;attempt++,slot=(slot+1)%4096){int sx=LunarSurface.landingX(slot),sz=LunarSurface.landingZ(slot);boolean used=d.ships.values().stream().anyMatch(v->!v.owner.equals(p.getUuid())&&v.x==sx&&v.z==sz);if(!used){x=sx;z=sz;y=LunarSurface.height(91023,x,z);found=true;break;}}
    if(!found){done.completeExceptionally(new IllegalStateException("No lunar berth is available"));return;}
   }
   final int siteX=x,siteY=y,siteZ=z;var site=retain?previous:new LandedShipRegistry.Ship(p.getUuid(),plan,x,y,z);
   var loads=new ArrayList<CompletableFuture<Void>>();for(int dx=-32;dx<=32;dx+=32)for(int dz=-32;dz<=32;dz+=32)loads.add(RocketService.load(w,siteX+dx,siteZ+dz));
   CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).whenComplete((v,error)->w.execute(()->{try{
    if(error!=null)throw new IllegalStateException("Lunar chunks unavailable",error);
    if(!RocketService.safe(w,siteX+14.5,siteY,siteZ+.5))throw new IllegalStateException("Lunar disembarkation point obstructed; existing blocks preserved");
    RocketService.clearColumn(w,new Cell(siteX,siteY,siteZ),7,(int)(FlightTimeline.upperHeight(plan)+FlightTimeline.landingClearance(plan)+92),true);
    var beacon=new Cell(siteX+12,siteY,siteZ);if(NativeBlocks.empty(w,beacon))NativeBlocks.set(w,beacon,BlockType.getAssetMap().getAsset("AR_ReturnBeacon"));else if(!NativeBlocks.base(NativeBlocks.get(w,beacon)).equals("AR_ReturnBeacon"))throw new IllegalStateException("Lunar return control is obstructed");
    if(previous==null||previous.departed)d.ships.put(p.getUuid(),site);
    done.complete(w);
   }catch(Exception ex){done.completeExceptionally(ex);}}));
  });return done;});
 }
 static void descend(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  if(!SpaceFlight.inOrbit(s,r)||!pending.add(p.getUuid()))return;var f=s.getComponent(r,AstralReachPlugin.flightType);var source=s.getExternalData().getWorld();
  prepare(p,f.plan).whenComplete((moon,error)->source.execute(()->{
   pending.remove(p.getUuid());if(error!=null){AstralReachPlugin.message(p,"Lune arrival unavailable; paid reserve retained: "+error.getMessage());return;}
   if(!r.isValid()||r.getStore()!=s||!SpaceFlight.inOrbit(s,r))return;
   moon.execute(()->{var site=data(moon.getEntityStore().getStore()).ships.get(p.getUuid());source.execute(()->{
    if(!r.isValid()||r.getStore()!=s||!SpaceFlight.inOrbit(s,r))return;
    f.leg=Leg.LUNE_DESCENT;f.legSeconds=0;f.boarded=true;SpaceFlight.handoff(s,r,p);
    RocketService.teleport(s,r,p,moon,new Transform(site.x+.5,site.y+FlightTimeline.landingClearance(f.plan)+90+FlightTimeline.crewHeight(f.plan),site.z+.5),()->{});
   });});
  }));
 }
 static CompletableFuture<World> home(PlayerFlightState f){
  var trip=f.trip;var u=Universe.get();var loaded=u.getWorld(trip.homeWorld());
  var future=loaded!=null?CompletableFuture.completedFuture(loaded):u.isWorldLoadable(f.homeWorldName)?u.loadWorld(f.homeWorldName):CompletableFuture.<World>failedFuture(new IllegalStateException("Saved home unavailable"));
  return future.thenCompose(w->{if(!w.getWorldConfig().getUuid().equals(trip.homeWorld()))return CompletableFuture.failedFuture(new IllegalStateException("Saved home identity differs"));
   var loads=new ArrayList<CompletableFuture<Void>>();for(int dx=-64;dx<=64;dx+=32)for(int dz=-64;dz<=64;dz+=32)loads.add(RocketService.load(w,f.x+dx,f.z+dz));
   return CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new)).thenCompose(v->{var done=new CompletableFuture<World>();w.execute(()->{try{
    if(!RocketService.safe(w,trip.x(),trip.y(),trip.z()))throw new IllegalStateException("Saved exit is obstructed");
    var rocket=RocketService.data(w.getEntityStore().getStore()).rockets.get(new Cell(f.x,f.y,f.z));if(rocket==null||!NativeBlocks.rocket(w,new Cell(f.x,f.y,f.z)))throw new IllegalStateException("Saved rocket berth unavailable");
    RocketService.clearColumn(w,new Cell(f.x,(int)Math.ceil(f.y+FlightTimeline.upperBase(f.plan)),f.z),9,(int)(FlightTimeline.upperHeight(f.plan)+92),true);done.complete(w);
   }catch(Exception ex){done.completeExceptionally(ex);}});return done;});});
 }
 static boolean returnFromShip(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  var f=s.getComponent(r,AstralReachPlugin.flightType);if(f==null||f.trip==null)return false;
  if(f.leg!=Leg.NONE){AstralReachPlugin.message(p,"Your ship is already in flight.");return true;}
  var w=s.getExternalData().getWorld();boolean orbit=SpaceFlight.inOrbit(s,r);var ship=data(s).ships.get(p.getUuid());
  if(!orbit&&(ship==null||!ship.grounded||ship.departed))return false; // Legacy paid expeditions retain their return route.
  if(!orbit&&!RocketService.near(s,r,new Cell(ship.x+12,ship.y,ship.z))){AstralReachPlugin.message(p,"Board your landed upper stage using the control beside its landing legs.");return true;}
  if(!pending.add(p.getUuid()))return true;var paid=f.trip;
  home(f).whenComplete((dest,error)->w.execute(()->{
   pending.remove(p.getUuid());if(error!=null){AstralReachPlugin.message(p,"Return preparation failed; paid reserve retained: "+error.getMessage());return;}
   if(!r.isValid()||r.getStore()!=s||f.trip!=paid)return;
   if(f.trip.phase()==Journey.Phase.LUNE||f.trip.phase()==Journey.Phase.ORBIT)f.trip=f.trip.beginReturn();
   f.boarded=true;f.legSeconds=0;RocketService.cue(s,p,"AR_rocket_hatch",orbit?new Cell(SpaceFlight.slot(p),202,0):new Cell(ship.x,ship.y,ship.z));
   SpaceFlight.handoff(s,r,p);
   if(orbit){f.leg=Leg.HOME_DESCENT;RocketService.teleport(s,r,p,dest,new Transform(f.x+.5,f.y+FlightTimeline.upperBase(f.plan)+90+FlightTimeline.crewHeight(f.plan),f.z+.5),()->{});}
   else{f.leg=Leg.LUNE_ASCENT;ship.departed=true;ship.grounded=false;remove(s,data(s).visuals.remove(p.getUuid()));RocketService.teleport(s,r,p,w,new Transform(ship.x+.5,ship.y+FlightTimeline.landingClearance(f.plan)+FlightTimeline.crewHeight(f.plan),ship.z+.5),()->{});}
  }));return true;
 }
 private static void remove(Store<EntityStore>s,Ref<EntityStore>r){if(r!=null&&r.isValid()&&r.getStore()==s)s.removeEntity(r,RemoveReason.REMOVE);}
 private static void actorEnd(Store<EntityStore>s,PlayerRef p){var a=actors.get(p.getUuid());if(a!=null&&(a.ref==null||a.ref.getStore()==s)){actors.remove(p.getUuid(),a);StageVisuals.clear(s,a.model,a.footprint);remove(s,a.ref);}}
 private static void exit(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,PlayerFlightState f,LandedShipRegistry.Ship site){
  var w=s.getExternalData().getWorld();double x=site==null?f.trip.x():site.x+14.5,y=site==null?f.trip.y():site.y,z=site==null?f.trip.z():site.z+.5;
  if(!RocketService.safe(w,x,y,z)){AstralReachPlugin.message(p,"Ship landed; clear the saved exit point before disembarking.");return;}
  SpaceFlight.end(s,r,p);RocketService.teleport(s,r,p,w,new Transform(x,y,z),()->{var ref=p.getReference();if(ref==null||!ref.isValid())return;var state=ref.getStore().getComponent(ref,AstralReachPlugin.flightType);if(state==null)return;state.boarded=false;if(site==null)RocketService.finishReturn(w,p);else AstralReachPlugin.message(p,"Lune touchdown. Your upper stage is parked here; board at its landing control for the paid return.");});
 }
 static void tick(Store<EntityStore>s,float dt){
  var w=s.getExternalData().getWorld();var d=data(s);var active=new HashSet<UUID>();
  for(var p:w.getPlayerRefs()){
   var r=p.getReference();if(r==null||!r.isValid()||r.getStore()!=s||RocketService.transferring.contains(p.getUuid()))continue;var f=s.getComponent(r,AstralReachPlugin.flightType);if(f==null||f.trip==null)continue;
   var site=d.ships.get(p.getUuid());
   if(f.leg==Leg.NONE){if(f.boarded&&f.trip.phase()==Journey.Phase.LUNE&&site!=null&&site.grounded)exit(s,r,p,f,site);continue;}
   boolean lunar=w.getName().equals("AstralLune");if(f.leg==Leg.HOME_DESCENT?!w.getWorldConfig().getUuid().equals(f.trip.homeWorld()):!lunar)continue;
   if(lunar&&site==null)continue;
   active.add(p.getUuid());var a=actors.get(p.getUuid());String model="AR_"+(f.plan==LaunchPlan.KESTREL?"Kestrel":"Odyssey")+"Ship"+(lunar?"Landed":"Idle");
   double seconds=f.legSeconds;double altitude=f.leg==Leg.LUNE_ASCENT?FlightTimeline.returnHeight(seconds):FlightTimeline.descentHeight(seconds);
   var origin=lunar?new Vector3d(site.x+.5,site.y+FlightTimeline.landingClearance(f.plan)+altitude,site.z+.5):new Vector3d(f.x+.5,f.y+FlightTimeline.upperBase(f.plan)+altitude,f.z+.5);
   if(a==null||a.ref==null||!a.ref.isValid()||a.ref.getStore()!=s||a.leg!=f.leg){actorEnd(s,p);var spawned=StageVisuals.one(s,model,origin,null);if(spawned==null)continue;a=new Actor(spawned,f.leg,model,origin);actors.put(p.getUuid(),a);}
   s.getComponent(a.ref,TransformComponent.getComponentType()).setPosition(origin);
   boolean powered=f.leg!=Leg.LUNE_ASCENT||seconds>=FlightTimeline.BOARDING;
   if(powered!=a.powered){a.powered=powered;if(powered){var audio=new AudioComponent();audio.addSound(SoundEvent.getAssetMap().getIndex("AR_rocket_thrust_loop"));s.putComponent(a.ref,AudioComponent.getComponentType(),audio);}else s.tryRemoveComponent(a.ref,AudioComponent.getComponentType());}
   long now=System.nanoTime();if(powered&&now-a.fireAt>=160_000_000L){a.fireAt=now;RocketExhaust.upper(s,f.plan,origin,0);}
   if(powered&&a.cues.add("Ignition"))RocketService.cue(s,p,"AR_rocket_ignition",new Cell((int)origin.x,(int)origin.y,(int)origin.z));
   var target=new Vector3d(origin).add(0,FlightTimeline.upperHeight(f.plan)*.5,0);String label=f.leg==Leg.LUNE_ASCENT?(seconds<3?"BOARDING · LUNE RETURN":"LUNE · UPPER-STAGE ASCENT"):lunar?"LUNE · CONTROLLED DESCENT":"ORBIS · APPROACHING LAUNCH PAD";
   SpaceFlight.cinematic(s,r,p,new Vector3d(origin).add(0,FlightTimeline.crewHeight(f.plan),0),new Vector3d(target).add(45,-3,80),target,label);
   f.legSeconds=Math.min(120,seconds+Math.clamp(dt,0,.25));
   double duration=f.leg==Leg.LUNE_ASCENT?FlightTimeline.RETURN_ASCENT:FlightTimeline.DESCENT;
   if(f.legSeconds<duration)continue;
   if(f.leg==Leg.LUNE_ASCENT){if(now<a.retryAt||!pending.add(p.getUuid()))continue;a.retryAt=now+5_000_000_000L;home(f).whenComplete((dest,error)->w.execute(()->{pending.remove(p.getUuid());if(!r.isValid()||r.getStore()!=s)return;if(error!=null){AstralReachPlugin.message(p,"Return approach paused; saved home must be clear. Your paid marker is retained.");return;}actorEnd(s,p);SpaceFlight.handoff(s,r,p);f.leg=Leg.HOME_DESCENT;f.legSeconds=0;RocketService.teleport(s,r,p,dest,new Transform(f.x+.5,f.y+FlightTimeline.upperBase(f.plan)+90+FlightTimeline.crewHeight(f.plan),f.z+.5),()->{});}));}
   else{
    RocketService.cue(s,p,"AR_rocket_touchdown",new Cell((int)origin.x,(int)origin.y,(int)origin.z));actorEnd(s,p);f.leg=Leg.NONE;f.legSeconds=0;
    if(lunar){site.grounded=true;site.departed=false;if(f.trip.phase()==Journey.Phase.ORBIT)f.trip=f.trip.arrive();}
    exit(s,r,p,f,lunar?site:null);
   }
  }
  for(var e:new ArrayList<>(actors.entrySet()))if(e.getValue().ref!=null&&e.getValue().ref.getStore()==s&&!active.contains(e.getKey())){StageVisuals.clear(s,e.getValue().model,e.getValue().footprint);remove(s,e.getValue().ref);actors.remove(e.getKey(),e.getValue());}
  for(var e:d.ships.entrySet()){
   var ship=e.getValue();var visual=d.visuals.get(e.getKey());var position=new Vector3d(ship.x+.5,ship.y+FlightTimeline.landingClearance(ship.plan),ship.z+.5);String model="AR_"+(ship.plan==LaunchPlan.KESTREL?"Kestrel":"Odyssey")+"ShipLanded";
   boolean show=ship.grounded&&!ship.departed&&!active.contains(e.getKey())&&NativeBlocks.ticking(w,new Cell(ship.x,ship.y,ship.z));
   if(!show){remove(s,d.visuals.remove(e.getKey()));if(!active.contains(e.getKey()))StageVisuals.clear(s,model,position);continue;}
   var single=StageVisuals.one(s,model,position,visual);if(single!=null)d.visuals.put(e.getKey(),single);
  }

 }
}
