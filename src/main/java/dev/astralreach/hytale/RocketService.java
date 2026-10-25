package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.*;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.util.ChunkUtil;
import dev.astralreach.core.*;
import java.util.*;
import java.util.concurrent.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** One owned anchored launch vehicle and a paid native cross-world route. No free fuel or inventory grants. */
final class RocketService {
 private record Departure(PlayerRef player,Cell cell,PlayerFlightState state,World orbit,long deadline,Set<String> cues){}
 private static final Map<UUID,Departure> departures=new ConcurrentHashMap<>();
 private static final Set<UUID> preparing=ConcurrentHashMap.newKeySet();static final Set<UUID> transferring=ConcurrentHashMap.newKeySet();
 static RocketRegistry data(Store<EntityStore>s){return s.getResource(AstralReachPlugin.rocketType);}
 static String vehicleState(RocketRegistry.Rocket r){return r.needsRebuild?"Away":r.traveller==null?"default":departures.containsKey(r.traveller)?"Launching":"Away";}
 static double departureElapsed(RocketRegistry.Rocket r){var d=r.traveller==null?null:departures.get(r.traveller);return d==null?0:Math.clamp(FlightTimeline.ORBIT-(d.deadline-System.nanoTime())/1_000_000_000d,0,FlightTimeline.ORBIT);}
 static void abort(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){if(r==null||!r.isValid()||r.getStore()!=s)return;var f=s.getComponent(r,AstralReachPlugin.flightType);if(f!=null&&f.trip!=null&&f.trip.phase()==Journey.Phase.OUTBOUND&&f.trip.homeWorld().equals(s.getExternalData().getWorld().getWorldConfig().getUuid()))cancel(s,p,f);}
 static boolean near(Store<EntityStore>s,Ref<EntityStore>ref,Cell c){var t=s.getComponent(ref,TransformComponent.getComponentType());if(t==null)return false;var p=t.getPosition();return p.distanceSquared(c.x()+.5,c.y()+.5,c.z()+.5)<=64;}
 static boolean controlRange(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell c){return near(s,ref,c)||LaunchYardService.nearConsole(s,ref,p,c);}
 static boolean safe(World w,double x,double y,double z){int bx=(int)Math.floor(x),by=(int)Math.floor(y),bz=(int)Math.floor(z);return NativeBlocks.empty(w,new Cell(bx,by,bz))&&NativeBlocks.empty(w,new Cell(bx,by+1,bz))&&!NativeBlocks.empty(w,new Cell(bx,by-1,bz))&&NativeBlocks.get(w,new Cell(bx,by-1,bz))!=null;}
 static void cue(Store<EntityStore>s,PlayerRef p,String id,Cell at){var m=SoundEvent.getAssetMap();if(m.getAsset(id)!=null)SoundUtil.playSoundEvent3dToPlayer(p.getReference(),m.getIndex(id),SoundCategory.SFX,new org.joml.Vector3d(at.x()+.5,at.y()+.5,at.z()+.5),s);}
 static RocketRegistry.Rocket usable(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at){
  var r=data(s).rockets.get(at);if(r==null||!r.owner.equals(p.getUuid())||!NativeBlocks.rocket(s.getExternalData().getWorld(),at)||!controlRange(s,ref,p,at))throw new IllegalArgumentException("Stand beside your own rocket anchor or its construction console.");
  if(r.traveller!=null)throw new IllegalArgumentException("This rocket has a paid mission in progress.");return r;
 }
 static void fuel(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at,boolean fill){
  try{var r=usable(s,ref,p,at);if(fill?!r.tank.canFill():!r.tank.canDrain())throw new IllegalArgumentException(fill?"Tank full. Drain a cell first.":"Tank contains less than 1,000 units.");
   var player=s.getComponent(ref,Player.getComponentType());if(player==null)return;var inv=player.getInventory().getCombinedHotbarFirst();
   int count=0;int limit=fill?Math.max(0,(PropellantTank.ROUND_TRIP_COST-r.tank.units())/1000):1;
   while(count<limit&&(fill?r.tank.canFill():r.tank.canDrain())&&FuelInventory.swap(inv,fill?"AR_PropellantCell":"AR_EmptyFuelCell",fill?"AR_EmptyFuelCell":"AR_PropellantCell")){if(fill)r.tank.fillCell();else r.tank.drainCell();count++;}
   if(count==0)throw new IllegalArgumentException(fill?(r.tank.units()>=9000?"Round-trip fuel is ready.":"Carry filled propellant cells and room for their empty casings."):"Carry an empty cell and room for the filled cell.");cue(s,p,"AR_metal_handle",at);AstralReachPlugin.message(p,(fill?"Deposited ":"Recovered ")+(count*1000)+" units. Tank: "+r.tank.units()+" /16,000.");
  }catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}
 }
 static void rebuildLegacy(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at){try{var r=usable(s,ref,p,at);if(!r.needsRebuild)return;var missing=RocketRecovery.missing(r.plan,r.boosterLanded);var inv=s.getComponent(ref,Player.getComponentType()).getInventory().getCombinedStorageHotbarBackpack();for(var need:missing.entrySet()){int n=0;for(short slot=0;slot<inv.getCapacity();slot++){var item=inv.getItemStack(slot);if(!com.hypixel.hytale.server.core.inventory.ItemStack.isEmpty(item)&&item.getItemId().equals(need.getKey()))n+=item.getQuantity();}if(n<need.getValue())throw new IllegalArgumentException("Carry all missing parts: "+missing);}
  var paid=YardInventory.take(inv,missing);if(paid.stream().mapToInt(com.hypixel.hytale.server.core.inventory.ItemStack::getQuantity).sum()!=missing.values().stream().mapToInt(Integer::intValue).sum())throw new IllegalStateException("Rebuild inventory changed");r.needsRebuild=false;r.boosterLanded=false;NativeBlocks.state(s.getExternalData().getWorld(),at,"default");cue(s,p,"AR_suit_bench",at);
 }catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}}
 static void clearColumn(World w,Cell at,int radius,int height,boolean includeOrigin){
  // Resolve each loaded section once instead of performing a store lookup for every voxel.
  var chunks=w.getChunkStore();var sections=new HashMap<String,com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection>();
  for(int x=at.x()-radius;x<=at.x()+radius;x++)for(int z=at.z()-radius;z<=at.z()+radius;z++)for(int y=at.y()+(includeOrigin?0:1);y<=at.y()+height;y++){
   String key=(x>>5)+","+(y>>5)+","+(z>>5);var section=sections.get(key);if(section==null){var ref=chunks.getChunkSectionReferenceAtBlock(x,y,z);if(ref!=null&&ref.isValid())section=chunks.getStore().getComponent(ref,com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection.getComponentType());if(section==null)throw new IllegalArgumentException("Load and clear the "+(radius*2+1)+"×"+(radius*2+1)+" launch column, "+height+" blocks high.");sections.put(key,section);}
   if(section.get(x&31,y&31,z&31)!=BlockType.EMPTY_ID||section.getFiller(x&31,y&31,z&31)!=0)throw new IllegalArgumentException("Clear the "+(radius*2+1)+"×"+(radius*2+1)+" launch column, "+height+" blocks high.");
  }
 }
 static void clearance(World w,Cell at){
  var s=w.getEntityStore().getStore();var r=data(s).rockets.get(at);var plan=r==null?LaunchPlan.ODYSSEY:r.plan;int height=plan.sweptHeight()+2,radius=plan==LaunchPlan.KESTREL?64:(int)Math.ceil(Math.max(plan.width,plan.depth)/2);
  if(at.y()<2||at.y()+height>=320)throw new IllegalArgumentException("This blueprint needs "+height+" blocks of vertical launch clearance.");
  boolean yard=false;for(var e:LaunchYardService.data(s).yards.entrySet())if(LaunchYardService.anchor(e.getKey(),e.getValue()).equals(at)&&e.getValue().progress.phase()==ConstructionProgress.Phase.READY&&LaunchYardService.formed(s,e.getKey(),e.getValue())){yard=true;break;}
  if(!yard&&!NativeBlocks.base(NativeBlocks.get(w,new Cell(at.x(),at.y()-1,at.z()))).equals("AR_LaunchDeck"))throw new IllegalArgumentException("Restore the formed launch platform underneath this rocket.");
  clearColumn(w,at,radius,height,false);
 }
 static CompletableFuture<Void> load(World w,int x,int z){return w.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunk(x>>5,z>>5)).thenApply(r->null);}
 static CompletableFuture<World> moonLanding(){return AstralReachPlugin.world(true).thenCompose(w->load(w,16,16).thenCompose(v->{var f=new CompletableFuture<World>();w.execute(()->{
  try{if(!safe(w,16.5,64,16.5))throw new IllegalStateException("Lune landing at 16,64,16 is obstructed; preserve the site and clear it before retrying.");var at=new Cell(20,64,16);var block=NativeBlocks.get(w,at);
   if(NativeBlocks.empty(w,at)){if(!NativeBlocks.set(w,at,BlockType.getAssetMap().getAsset("AR_ReturnBeacon")))throw new IllegalStateException("Could not place the mission return beacon");}
   else if(!NativeBlocks.base(block).equals("AR_ReturnBeacon"))throw new IllegalStateException("The return beacon site is occupied; existing blocks are preserved.");f.complete(w);
  }catch(Exception ex){f.completeExceptionally(ex);}
 });return f;}));}
 static void launch(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at){
  var w=s.getExternalData().getWorld();try{
   if(w.getName().equals("AstralLune")||w.getName().equals(SpaceFlight.ORBIT_WORLD))throw new IllegalArgumentException("Use your protected return. New missions launch from a home world.");
   var r=usable(s,ref,p,at);if(r.needsRebuild)throw new IllegalArgumentException("Rebuild the missing stages before launching again. Returned assemblies are credited in the construction console.");var existing=s.getComponent(ref,AstralReachPlugin.flightType);if(existing!=null&&existing.trip!=null)throw new IllegalArgumentException("Complete your existing paid journey first.");
   ExpeditionSuit.require(s,ref,1);OxygenService.requireDeparture(s,ref);
   if(CrewBoarding.occupied(s,ref))throw new IllegalArgumentException("Leave your current mount before boarding the rocket.");
   if(!r.tank.canRoundTrip())throw new IllegalArgumentException("Deposit nine full propellant cells: 6,000 outbound + 3,000 protected for return.");clearance(w,at);
   if(!preparing.add(p.getUuid()))return;AstralReachPlugin.message(p,"Preparing orbital arrival and checking Lune's landing site. Fuel is charged only after preflight.");
   LunarFlight.prepare(p,r.plan).thenCombine(SpaceFlight.orbitLanding(p),(moon,orbit)->orbit).whenComplete((orbit,error)->w.execute(()->{preparing.remove(p.getUuid());if(error!=null){AstralReachPlugin.message(p,"Flight preparation failed; no fuel charged: "+error.getMessage());return;}
    if(!ref.isValid()||ref.getStore()!=s)return;
    try{var rocket=usable(s,ref,p,at);ExpeditionSuit.require(s,ref,1);OxygenService.requireDeparture(s,ref);if(!rocket.tank.canRoundTrip())throw new IllegalArgumentException("Tank changed before departure.");clearance(w,at);
     var pos=s.getComponent(ref,TransformComponent.getComponentType()).getPosition();if(!safe(w,pos.x,pos.y,pos.z))throw new IllegalArgumentException("Stand on solid ground in two clear air blocks before departure.");
     var state=new PlayerFlightState();state.homeWorldName=w.getName();state.plan=rocket.plan;state.x=at.x();state.y=at.y();state.z=at.z();state.trip=new Journey(w.getWorldConfig().getUuid(),pos.x,pos.y,pos.z,PropellantTank.LEG_COST,Journey.Phase.OUTBOUND);state.boarded=true;state.departureX=pos.x;state.departureY=pos.y;state.departureZ=pos.z;
     rocket.tank.reserveRoundTrip();rocket.traveller=p.getUuid();s.putComponent(ref,AstralReachPlugin.flightType,state);
     departures.put(p.getUuid(),new Departure(p,at,state,orbit,System.nanoTime()+TimeUnit.SECONDS.toNanos((long)FlightTimeline.ORBIT),new HashSet<>()));
     cue(s,p,"AR_rocket_hatch",at);SpaceFlight.begin(s,ref,p);SpaceFlight.ascent(s,ref,p,at,rocket.plan,0);
     teleport(s,ref,p,w,new Transform(at.x()+.5,at.y()+FlightTimeline.upperBase(rocket.plan)+FlightTimeline.crewHeight(rocket.plan),at.z()+.5),()->{});
    }catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}
   }));
  }catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}
 }
 private static void cancel(Store<EntityStore>s,PlayerRef p,PlayerFlightState state){
  departures.remove(p.getUuid());
  var at=new Cell(state.x,state.y,state.z);var rocket=data(s).rockets.get(at);
  var ref=p.getReference();
  if(rocket==null)return;
  if(rocket.traveller==null){state.trip=null;recoverBoarding(s,ref,p,state);return;} // Already refunded on disconnect.
  if(!p.getUuid().equals(rocket.traveller))return;int paid=state.trip.reservedUnits();rocket.tank.cancelReservation(paid);rocket.traveller=null;rocket.boosterLanded=false;state.trip=null;NativeBlocks.state(s.getExternalData().getWorld(),at,"default");recoverBoarding(s,ref,p,state);AstralReachPlugin.message(p,"Departure stopped before orbit. All "+paid+" reserved units restored to your rocket.");
 }
 private static void recoverBoarding(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,PlayerFlightState f){
  if(r==null||!r.isValid()||r.getStore()!=s||transferring.contains(p.getUuid()))return;
  if(!f.boarded){SpaceFlight.end(s,r,p);return;}var w=s.getExternalData().getWorld();if(!w.getName().equals(f.homeWorldName)||!safe(w,f.departureX,f.departureY,f.departureZ))return;
  SpaceFlight.end(s,r,p);teleport(s,r,p,w,new Transform(f.departureX,f.departureY,f.departureZ),()->f.boarded=false);
 }
 static void teleport(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,World target,Transform destination,Runnable completed){
  if(!transferring.add(p.getUuid()))return;var done=new CompletableFuture<Void>();var teleport=Teleport.createForPlayer(target,destination);teleport.setOnComplete(done);
  done.whenComplete((v,e)->{transferring.remove(p.getUuid());if(e==null)target.execute(completed);else AstralReachPlugin.message(p,"Travel paused; your paid journey marker is retained. Reconnect or use /astral-return.");});
  try{s.putComponent(ref,Teleport.getComponentType(),teleport);}catch(RuntimeException ex){transferring.remove(p.getUuid());throw ex;}
 }
 static void returnHome(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p){
  var state=s.getComponent(ref,AstralReachPlugin.flightType);var w=s.getExternalData().getWorld();
  if(state==null||state.trip==null){AstralReachPlugin.message(p,"No paid return reserve. Launch a funded round trip from your home rocket first.");return;}
  var trip=state.trip;if(w.getWorldConfig().getUuid().equals(trip.homeWorld())){if(trip.phase()==Journey.Phase.OUTBOUND)cancel(s,p,state);else if(trip.phase()==Journey.Phase.RETURNING&&state.leg==PlayerFlightState.Leg.NONE&&!state.boarded)finishReturn(w,p);return;}
  if(!w.getName().equals("AstralLune")&&!w.getName().equals(SpaceFlight.ORBIT_WORLD)){AstralReachPlugin.message(p,"This paid reserve returns from space or Lune.");return;}
  if(trip.phase()==Journey.Phase.OUTBOUND){state.trip=w.getName().equals(SpaceFlight.ORBIT_WORLD)?trip.orbit():trip.arrive();trip=state.trip;}
  if(LunarFlight.returnFromShip(s,ref,p))return;
  if(transferring.contains(p.getUuid())||!preparing.add(p.getUuid()))return;
  final Journey paid=trip;
  var loaded=Universe.get().getWorld(trip.homeWorld());
  var u=Universe.get();var homeFuture=loaded!=null?CompletableFuture.completedFuture(loaded):u.isWorldLoadable(state.homeWorldName)?u.loadWorld(state.homeWorldName):CompletableFuture.<World>failedFuture(new IllegalStateException("Saved home world is unavailable; preserve its files before retrying"));
  homeFuture.thenCompose(home->{if(!home.getWorldConfig().getUuid().equals(paid.homeWorld()))return CompletableFuture.failedFuture(new IllegalStateException("Saved home identity differs; return reserve preserved"));return load(home,(int)Math.floor(paid.x()),(int)Math.floor(paid.z())).thenApply(v->home);}).whenComplete((home,e)->{
   if(e!=null){preparing.remove(p.getUuid());AstralReachPlugin.message(p,"Home preparation failed; paid reserve retained: "+e.getMessage());return;}
   home.execute(()->{
   boolean clear=e==null&&safe(home,paid.x(),paid.y(),paid.z());w.execute(()->{
    preparing.remove(p.getUuid());if(!ref.isValid()||ref.getStore()!=s||state.trip!=paid)return;
    if(!clear){AstralReachPlugin.message(p,"Your home landing is blocked or unloaded. Return reserve retained; clear the departure point.");return;}
    if(state.trip.phase()==Journey.Phase.LUNE||state.trip.phase()==Journey.Phase.ORBIT)state.trip=state.trip.beginReturn();
    SpaceFlight.end(s,ref,p);
    var pos=s.getComponent(ref,TransformComponent.getComponentType()).getPosition();cue(s,p,"AR_rocket_ignition",new Cell((int)pos.x,(int)pos.y,(int)pos.z));
    teleport(s,ref,p,home,new Transform(paid.x(),paid.y(),paid.z()),()->finishReturn(home,p));
   });
  });});
 }
 static void finishReturn(World home,PlayerRef p){var ref=p.getReference();if(ref==null||!ref.isValid())return;var s=ref.getStore();if(s.getExternalData().getWorld()!=home)return;var state=s.getComponent(ref,AstralReachPlugin.flightType);
  if(state==null||state.trip==null||state.trip.phase()!=Journey.Phase.RETURNING||!state.trip.homeWorld().equals(home.getWorldConfig().getUuid()))return;
  var at=new Cell(state.x,state.y,state.z);var r=data(s).rockets.get(at);if(r!=null&&p.getUuid().equals(r.traveller)){r.traveller=null;r.needsRebuild=true;NativeBlocks.state(home,at,"Away");LaunchYardService.recover(s,at,r);}int spent=state.trip.returnUnits();state.trip=null;state.boarded=false;state.leg=PlayerFlightState.Leg.NONE;state.legSeconds=0;AstralReachPlugin.message(p,"Returned home. The prepaid "+spent+"-unit reserve is spent. Returned stages are credited. Rebuild the missing parts at your launch console before the next mission.");
 }
 static void chooseLune(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p){try{ExpeditionSuit.require(s,ref,1);LunarFlight.descend(s,ref,p);}catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}}
 static final class Tick extends TickingSystem<EntityStore>{
  @Override public void tick(float dt,int i,Store<EntityStore>s){var w=s.getExternalData().getWorld();w.execute(()->tickWorld(w,dt));}
 }
 private static void tickWorld(World w,float dt){var s=w.getEntityStore().getStore();var d=data(s);
  for(var e:d.rockets.entrySet())if(NativeBlocks.rocket(w,e.getKey()))NativeBlocks.state(w,e.getKey(),vehicleState(e.getValue()).equals("Launching")&&departureElapsed(e.getValue())<FlightTimeline.BOARDING?"default":vehicleState(e.getValue()));
  for(var p:w.getPlayerRefs()){
   var ref=p.getReference();if(ref==null||!ref.isValid()||ref.getStore()!=s)continue;var state=s.getComponent(ref,AstralReachPlugin.flightType);if(state==null)continue;if(state.trip==null){if(state.boarded)recoverBoarding(s,ref,p,state);continue;}
   var depart=departures.get(p.getUuid());
   if(depart!=null){
    var rocket=d.rockets.get(depart.cell);if(rocket==null||!NativeBlocks.rocket(w,depart.cell)){cancel(s,p,state);continue;}
    double elapsed=departureElapsed(rocket);SpaceFlight.ascent(s,ref,p,depart.cell,rocket.plan,elapsed);
    if(elapsed>=FlightTimeline.BOARDING&&depart.cues.add("Ignition")){cue(s,p,"AR_rocket_ignition",depart.cell);ParticleUtil.spawnParticleEffect("AR_IgnitionSmoke",new org.joml.Vector3d(depart.cell.x()+.5,depart.cell.y()+.3,depart.cell.z()+.5),s);}
    if(elapsed>=FlightTimeline.SEPARATION&&depart.cues.add("Separation"))cue(s,p,"AR_stage_separation",depart.cell);
    if(rocket.plan==LaunchPlan.KESTREL&&elapsed>=FlightTimeline.BOOSTER_LANDED&&depart.cues.add("Touchdown")){cue(s,p,"AR_rocket_touchdown",depart.cell);ParticleUtil.spawnParticleEffect("AR_IgnitionSmoke",new org.joml.Vector3d(depart.cell.x()+.5,depart.cell.y()+.3,depart.cell.z()+.5),s);}
    if(System.nanoTime()<depart.deadline)continue;
    try{clearance(w,depart.cell);}catch(IllegalArgumentException ex){departures.remove(p.getUuid());cancel(s,p,state);AstralReachPlugin.message(p,ex.getMessage());continue;}
    departures.remove(p.getUuid());NativeBlocks.state(w,depart.cell,"Away");if(rocket!=null&&rocket.plan==LaunchPlan.KESTREL)rocket.boosterLanded=true;SpaceFlight.handoff(s,ref,p);
    teleport(s,ref,p,depart.orbit,new Transform(SpaceFlight.slot(p)+.5,202+FlightTimeline.crewHeight(state.plan),.5),()->{var r=p.getReference();if(r==null||!r.isValid())return;var f=r.getStore().getComponent(r,AstralReachPlugin.flightType);if(f!=null&&f.trip!=null&&f.trip.phase()==Journey.Phase.OUTBOUND)f.trip=f.trip.orbit();});continue;
   }
   if(transferring.contains(p.getUuid())||preparing.contains(p.getUuid()))continue;
   if(w.getWorldConfig().getUuid().equals(state.trip.homeWorld())){if(state.trip.phase()==Journey.Phase.OUTBOUND)cancel(s,p,state);else if(state.trip.phase()==Journey.Phase.RETURNING&&state.leg==PlayerFlightState.Leg.NONE&&!state.boarded)finishReturn(w,p);}
   else if(w.getName().equals("AstralLune")&&state.trip.phase()==Journey.Phase.OUTBOUND)state.trip=state.trip.arrive();
  }
  // A disconnect during countdown stops the actual mechanism and refunds the exact paid reservation.
  for(var e:new ArrayList<>(departures.entrySet())){var dep=e.getValue();if(dep.state.trip==null){departures.remove(e.getKey(),dep);continue;}if(!dep.state.trip.homeWorld().equals(w.getWorldConfig().getUuid()))continue;var ref=dep.player.getReference();if(ref==null||!ref.isValid()){departures.remove(e.getKey());cancel(s,dep.player,dep.state);}}
  RocketVisuals.sync(s);
  LunarFlight.tick(s,dt);
  LunarEnvironment.tick(s,dt);
  OxygenService.tick(s,dt);
  SpaceFlight.tickOrbit(s);
  CrewBoarding.cleanup(s);
  OrbitalMapPage.cleanup(s);
  var yards=LaunchYardService.data(s);yards.elapsedMillis+=Math.max(0,Math.round(dt*1000));if(yards.elapsedMillis>=500){var elapsed=yards.elapsedMillis;yards.elapsedMillis=0;LaunchYardService.tick(s,Math.min(1000,elapsed));}
 }
 static final class Placement extends EntityEventSystem<EntityStore,PlaceBlockEvent>{
  Placement(){super(PlaceBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,PlaceBlockEvent e){if(e.isCancelled()||e.getItemInHand()==null)return;String id=e.getItemInHand().getItemId();if(!id.equals("AR_OdysseyHeavy")&&!id.equals("AR_KestrelStack"))return;var plan=id.equals("AR_KestrelStack")?LaunchPlan.KESTREL:LaunchPlan.ODYSSEY;var p=c.getComponent(i,PlayerRef.getComponentType());var t=e.getTargetBlock();var at=new Cell(t.x,t.y,t.z);var w=s.getExternalData().getWorld();
   if(w.getName().equals("AstralLune")||data(s).rockets.containsKey(at)){e.setCancelled(true);AstralReachPlugin.message(p,"Place new mission rockets in a home world, clear of existing anchors.");return;}
   w.execute(()->{if(NativeBlocks.rocket(w,at))data(s).rockets.putIfAbsent(at,new RocketRegistry.Rocket(p.getUuid(),0,plan));});
  }
 }
 static final class Breaking extends EntityEventSystem<EntityStore,BreakBlockEvent>{
  Breaking(){super(BreakBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,BreakBlockEvent e){if(e.isCancelled())return;var t=e.getTargetBlock();var at=new Cell(t.x,t.y,t.z);var w=s.getExternalData().getWorld();var p=c.getComponent(i,PlayerRef.getComponentType());
   if(NativeBlocks.base(e.getBlockType()).equals("AR_ReturnBeacon")){e.setCancelled(true);AstralReachPlugin.message(p,"Keep the Lune return beacon available for paid expeditions.");return;}
   if(!NativeBlocks.rocket(w,at))return;var r=data(s).rockets.get(at);
   if(r==null||!r.owner.equals(p.getUuid())||r.traveller!=null||r.needsRebuild||r.tank.units()!=0){e.setCancelled(true);AstralReachPlugin.message(p,"Only the owner may remove an idle rocket. Drain all fuel and finish its mission first.");return;}
   w.execute(()->{var block=NativeBlocks.get(w,at);if(block!=null&&!NativeBlocks.rocket(w,at))data(s).rockets.remove(at);});
  }
 }
}
