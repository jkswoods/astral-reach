package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.FlatWorldGenProvider;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Rotation3f;
import dev.astralreach.core.*;
import org.joml.Vector3d;
import java.util.*;
import java.util.concurrent.*;

/** Native third-person presentation. Fuel and journey authority remain in RocketService. */
final class SpaceFlight {
 static final String ORBIT_WORLD="AstralOrbit";
 private static final Map<UUID,View> views=new ConcurrentHashMap<>();
 private static final class View {
  Set<HudComponent> hud;long cameraAt,arrival=System.nanoTime();String weather="";boolean mapShown;Ref<EntityStore> vehicle;AscentPage page;
 }
 static int slot(PlayerRef p){return Math.floorMod(p.getUuid().hashCode(),4096)*128;}
 static CompletableFuture<World> orbitLanding(PlayerRef p){
  var u=Universe.get();var w=u.getWorld(ORBIT_WORLD);CompletableFuture<World> ready;
  if(w!=null)ready=CompletableFuture.completedFuture(w);else if(u.isWorldLoadable(ORBIT_WORLD))ready=u.loadWorld(ORBIT_WORLD);else{
   var cfg=new WorldConfig();cfg.setDisplayName("Astral Reach — Low Orbit");cfg.setWorldGenProvider(new FlatWorldGenProvider(new Color((byte)0,(byte)0,(byte)0),new FlatWorldGenProvider.Layer[]{new FlatWorldGenProvider.Layer(0,1,null,"Rock_Stone")}));cfg.setForcedWeather("AR_Space");cfg.setGameTime(java.time.Instant.parse("0001-01-01T10:00:00Z"));cfg.setGameTimePaused(true);cfg.setSpawningNPC(false);cfg.setPvpEnabled(false);cfg.setSavingPlayers(true);cfg.setSaveNewChunks(true);cfg.setSpawnProvider(new GlobalSpawnProvider(new Transform(.5,201,.5)));ready=u.makeWorld(ORBIT_WORLD,u.getWorldsPath().resolve(ORBIT_WORLD),cfg);
  }
  return ready.thenCompose(world->CompletableFuture.allOf(RocketService.load(world,slot(p),0),RocketService.load(world,slot(p)-32,-192),RocketService.load(world,slot(p),-160),RocketService.load(world,slot(p)+32,-128)).thenCompose(v->{var f=new CompletableFuture<World>();world.execute(()->{try{int x=slot(p);for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(NativeBlocks.empty(world,new RocketRegistry.Cell(x+dx,200,dz)))NativeBlocks.set(world,new RocketRegistry.Cell(x+dx,200,dz),BlockType.getAssetMap().getAsset("Rock_Stone"));if(!RocketService.safe(world,x+.5,201,.5))throw new IllegalStateException("Orbital arrival platform is obstructed");f.complete(world);}catch(Exception ex){f.completeExceptionally(ex);}});return f;}));
 }
 static boolean inOrbit(Store<EntityStore>s,Ref<EntityStore>r){var f=s.getComponent(r,AstralReachPlugin.flightType);return s.getExternalData().getWorld().getName().equals(ORBIT_WORLD)&&f!=null&&f.trip!=null&&f.trip.phase()==Journey.Phase.ORBIT&&f.leg==PlayerFlightState.Leg.NONE;}
 static void begin(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  begin(s,r,p,true);
 }
 static void begin(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,boolean cancellable){
  end(s,r,p);var v=new View();var player=s.getComponent(r,Player.getComponentType());if(player!=null){v.hud=Set.copyOf(player.getHudManager().getVisibleHudComponents());player.getHudManager().setVisibleHudComponents(p,Set.of());}views.put(p.getUuid(),v);
  v.page=new AscentPage(p,cancellable);if(player!=null)player.getPageManager().openCustomPage(r,s,v.page);
 }
 static void handoff(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){var v=views.get(p.getUuid());if(v!=null&&v.page!=null)v.page.handoff=true;end(s,r,p);}
 static void end(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  OrbitalMapPage.close(p);
  CrewBoarding.release(s,r,p);
  var v=views.remove(p.getUuid());if(v!=null){if(v.vehicle!=null&&v.vehicle.isValid())s.removeEntity(v.vehicle,RemoveReason.REMOVE);if(v.page!=null){v.page.handoff=true;v.page.finish();}}
  if(r==null||!r.isValid()||r.getStore()!=s)return;var player=s.getComponent(r,Player.getComponentType());if(player!=null&&v!=null&&v.hud!=null)player.getHudManager().setVisibleHudComponents(p,v.hud);
  var camera=s.getComponent(r,CameraManager.getComponentType());if(camera!=null)camera.resetCamera(p);var weather=s.getComponent(r,WeatherTracker.getComponentType());if(v!=null&&!v.weather.isEmpty()&&weather!=null&&weather.releaseOverride(r))weather.clear();
 }
 private static void weather(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,View v,String name,float seconds){
  if(v.weather.equals(name))return;var t=s.getComponent(r,WeatherTracker.getComponentType());if(t==null)return;
  if(name.isEmpty()){if(t.releaseOverride(r))t.clear();v.weather="";return;}int index=Weather.getAssetMap().getIndex(name);if(index>0&&t.tryClaimOverride(r,index)){t.sendWeatherIndex(p,index,seconds);v.weather=name;}
 }
 static void ascent(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,RocketRegistry.Cell at,LaunchPlan plan,double seconds){
  var v=views.get(p.getUuid());if(v==null){begin(s,r,p);v=views.get(p.getUuid());}if(v==null)return;
  if(plan==LaunchPlan.KESTREL&&seconds>=56&&seconds<86)weather(s,r,p,v,"",4);
  else if(plan==LaunchPlan.KESTREL&&seconds>=40&&seconds<56)weather(s,r,p,v,"AR_ThinAtmosphere",12);
  else if(seconds>=86)weather(s,r,p,v,"AR_Space",4);
  else if(seconds>=18)weather(s,r,p,v,"AR_Space",8);
  else if(seconds>=10)weather(s,r,p,v,"AR_ThinAtmosphere",8);

  long now=System.nanoTime();if(now-v.cameraAt<100_000_000L)return;v.cameraAt=now;
  var rotation=new Rotation3f();RotationTuple.get(NativeBlocks.rotation(s.getExternalData().getWorld(),at)).applyRotationTo(rotation);double yaw=rotation.yaw();
  double upperX=FlightTimeline.upperX(plan,seconds);CrewBoarding.hold(s,r,p,new Vector3d(at.x()+.5+Math.cos(yaw)*upperX,at.y()+FlightTimeline.upperBase(plan)+FlightTimeline.crewHeight(plan)+FlightTimeline.height(seconds),at.z()+.5-Math.sin(yaw)*upperX));
  boolean booster=plan==LaunchPlan.KESTREL&&seconds>=33&&seconds<86;double lift=booster?FlightTimeline.boosterHeight(seconds):FlightTimeline.height(seconds);double center=booster?36:seconds>=FlightTimeline.SEPARATION?FlightTimeline.upperBase(plan)+FlightTimeline.upperHeight(plan)*.5:plan.height*.5;double dx=booster?FlightTimeline.boosterX(seconds):upperX;
  var target=new Vector3d(at.x()+.5+Math.cos(yaw)*dx,at.y()+lift+center,at.z()+.5-Math.sin(yaw)*dx);double distance=booster?82:seconds>=FlightTimeline.SEPARATION?95:155;
  var camera=booster?new Vector3d(target.x-Math.cos(yaw)*distance*.52+Math.sin(yaw)*distance,target.y+14,target.z+Math.sin(yaw)*distance*.52+Math.cos(yaw)*distance):new Vector3d(target.x+Math.cos(yaw)*distance*.52+Math.sin(yaw)*distance,target.y-6,target.z-Math.sin(yaw)*distance*.52+Math.cos(yaw)*distance);
  camera(p,camera,target);if(v.page!=null)v.page.status(FlightTimeline.stage(seconds,plan),Math.min(100,(int)(seconds/FlightTimeline.ORBIT*100)));
 }
 static void cinematic(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,Vector3d crew,Vector3d position,Vector3d target,String label){
  var v=views.get(p.getUuid());if(v==null){begin(s,r,p,false);v=views.get(p.getUuid());}if(v==null)return;
  CrewBoarding.hold(s,r,p,crew);if(s.getExternalData().getWorld().getName().equals("AstralLune"))weather(s,r,p,v,"AR_LunarSurface",2);
  long now=System.nanoTime();if(now-v.cameraAt<100_000_000L)return;v.cameraAt=now;camera(p,position,target);if(v.page!=null)v.page.status(label,0);
 }
 static void camera(PlayerRef p,Vector3d position,Vector3d target){
  var c=new ServerCameraSettings();c.positionLerpSpeed=.15f;c.rotationLerpSpeed=.15f;c.attachedToType=AttachedToType.None;c.positionType=PositionType.Custom;c.position=new Position(position.x,position.y,position.z);c.rotationType=RotationType.Custom;var view=Rotation3f.lookAt(new Vector3d(target).sub(position));c.rotation=new Direction(view.yaw(),view.pitch(),0);c.applyLookType=ApplyLookType.Rotation;c.positionOffset=new Position(0,0,0);c.positionDistanceOffsetType=PositionDistanceOffsetType.None;c.isFirstPerson=false;c.skipCharacterPhysics=true;c.hideHeldItem=true;c.displayReticle=false;c.displayCursor=false;c.allowPitchControls=false;c.movementMultiplier=new org.joml.Vector3f();c.lookMultiplier=new org.joml.Vector2f();c.baseFov=60f;p.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom,true,c));
 }
 static void tickOrbit(Store<EntityStore>s){
  var world=s.getExternalData().getWorld();if(!world.getName().equals(ORBIT_WORLD))return;
  for(var p:world.getPlayerRefs()){
   var r=p.getReference();if(r==null||!r.isValid()||!inOrbit(s,r))continue;var state=s.getComponent(r,AstralReachPlugin.flightType);var v=views.get(p.getUuid());
   if(v==null){v=new View();var player=s.getComponent(r,Player.getComponentType());if(player!=null){v.hud=Set.copyOf(player.getHudManager().getVisibleHudComponents());player.getHudManager().setVisibleHudComponents(p,Set.of(HudComponent.Chat));}views.put(p.getUuid(),v);}
   if(OrbitalMapPage.viewing(p.getUuid())){if(v.vehicle!=null&&v.vehicle.isValid())s.removeEntity(v.vehicle,RemoveReason.REMOVE);v.vehicle=null;}
   else if(v.vehicle==null||!v.vehicle.isValid()){String model=state.plan==LaunchPlan.KESTREL?"AR_KestrelShipIdle":"AR_OdysseyShipIdle";v.vehicle=RocketVisuals.spawn(s,model,new Vector3d(slot(p)+.5,202,.5),new Rotation3f());}
   state.boarded=true;CrewBoarding.hold(s,r,p,new Vector3d(slot(p)+.5,202+FlightTimeline.crewHeight(state.plan),.5));
   long now=System.nanoTime();if(!OrbitalMapPage.viewing(p.getUuid())&&now-v.cameraAt>=100_000_000L){v.cameraAt=now;double h=FlightTimeline.upperHeight(state.plan);var target=new Vector3d(slot(p)+.5,202+h*.5,.5);camera(p,new Vector3d(target.x-55,target.y-110,target.z-120),target);}
   if(!v.mapShown&&now-v.arrival>=8_000_000_000L){v.mapShown=true;OrbitalMapPage.open(s,r,p);}
  }
  for(var id:new ArrayList<>(views.keySet())){var v=views.get(id);if(v==null||v.vehicle==null)continue;boolean present=world.getPlayerRefs().stream().anyMatch(p->p.getUuid().equals(id)&&p.getReference()!=null&&inOrbit(s,p.getReference()));if(!present){if(v.vehicle.isValid())s.removeEntity(v.vehicle,RemoveReason.REMOVE);views.remove(id);}}
 }
}
