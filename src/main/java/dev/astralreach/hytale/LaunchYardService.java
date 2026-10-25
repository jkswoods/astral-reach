package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;
import java.util.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** A physical joined pad, a separate console and one server-owned construction transaction. */
final class LaunchYardService {
 static LaunchYardRegistry data(Store<EntityStore>s){return s.getResource(AstralReachPlugin.yardType);}
 static LaunchPadLayout.Cell core(Cell p){return new LaunchPadLayout.Cell(p.x(),p.y(),p.z());}
 static Cell nativeCell(LaunchPadLayout.Cell p){return new Cell(p.x(),p.y(),p.z());}
 static Cell center(Cell console,LaunchYardRegistry.Yard yard){return nativeCell(LaunchPadLayout.center(core(console),yard.facing));}
 static Cell anchor(Cell console,LaunchYardRegistry.Yard yard){var p=center(console,yard);return new Cell(p.x(),p.y()+1,p.z());}
 static boolean console(World w,Cell p){return NativeBlocks.base(NativeBlocks.get(w,p)).equals("AR_LaunchConsole")&&NativeBlocks.filler(w,p)==0;}
 static List<LaunchPadLayout.Cell> modules(Cell at,LaunchYardRegistry.Yard y){return LaunchPadLayout.modules(core(center(at,y)));}
 static boolean overlap(Store<EntityStore>s,Cell at,LaunchYardRegistry.Yard yard){var center=center(at,yard);for(var e:data(s).yards.entrySet()){
  if(e.getKey().equals(at))continue;var other=center(e.getKey(),e.getValue());if(center.y()==other.y()&&Math.abs((long)center.x()-other.x())<25&&Math.abs((long)center.z()-other.z())<25)return true;
 }return false;}
 static int present(World w,Cell at,LaunchYardRegistry.Yard y){int n=0;for(var p:modules(at,y)){var root=nativeCell(p);if(NativeBlocks.base(NativeBlocks.get(w,root)).equals("AR_LaunchPadModule")&&NativeBlocks.filler(w,root)==0)n++;}return n;}
 static boolean formed(Store<EntityStore>s,Cell at,LaunchYardRegistry.Yard y){
  var w=s.getExternalData().getWorld();if(!console(w,at)||!NativeBlocks.ticking(w,at)||overlap(s,at,y)||present(w,at,y)!=25)return false;
  for(var p:modules(at,y))for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
   var cell=new Cell(p.x()+x,p.y(),p.z()+z);if(!nativeCell(p).equals(NativeBlocks.root(w,cell)))return false;
  }return true;
 }
 static LaunchYardRegistry.Yard usable(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef player,Cell at){
  var y=data(s).yards.get(at);if(y==null||!y.owner.equals(player.getUuid())||!console(s.getExternalData().getWorld(),at)||!RocketService.near(s,ref,at))throw new IllegalArgumentException("Stand beside your own construction console.");return y;
 }
 static boolean nearConsole(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef player,Cell rocket){for(var e:data(s).yards.entrySet()){
  var y=e.getValue();if(y.owner.equals(player.getUuid())&&y.progress.phase()==ConstructionProgress.Phase.READY&&anchor(e.getKey(),y).equals(rocket)&&console(s.getExternalData().getWorld(),e.getKey())&&RocketService.near(s,ref,e.getKey()))return true;
 }return false;}
 /** Converts a returned complete yard into a discounted, still paid reconstruction. */
 static boolean recover(Store<EntityStore>s,Cell rocket,RocketRegistry.Rocket r){for(var e:data(s).yards.entrySet()){var y=e.getValue();if(!y.owner.equals(r.owner)||!anchor(e.getKey(),y).equals(rocket)||y.progress.phase()!=ConstructionProgress.Phase.READY)continue;
  if(!NativeBlocks.set(s.getExternalData().getWorld(),rocket,BlockType.getAssetMap().getAsset("Empty")))return false;
  y.recovered.clear();y.recovered.putAll(RocketRecovery.recovered(r.plan,r.boosterLanded));y.heldFuel=r.tank.units();y.progress=new ConstructionProgress(r.plan);for(var c:y.recovered.entrySet())y.progress.accept(c.getKey(),c.getValue());y.selected=true;RocketService.data(s).rockets.remove(rocket);return true;
 }return false;}
 static void action(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef player,Cell at,String action){try{
  var y=usable(s,ref,player,at);var p=y.progress;var w=s.getExternalData().getWorld();
  if(action.equals("Odyssey")||action.equals("Kestrel")){
   if(p.phase()!=ConstructionProgress.Phase.PREVIEW||!y.escrow.isEmpty()||!y.recovered.isEmpty())throw new IllegalArgumentException("Cancel and recover supplied materials before changing the blueprint.");
   p.select(action.equals("Odyssey")?LaunchPlan.ODYSSEY:LaunchPlan.KESTREL);y.selected=true;RocketService.cue(s,player,"AR_metal_handle",at);
  }else if(action.equals("Rotate")){
   if(y.selected||!y.escrow.isEmpty()||p.phase()!=ConstructionProgress.Phase.PREVIEW)throw new IllegalArgumentException("Cancel the blueprint before changing the pad direction.");
   var old=y.facing;var prior=modules(at,y);y.facing=LaunchPadLayout.Facing.values()[(old.ordinal()+1)%4];if(overlap(s,at,y)){y.facing=old;throw new IllegalArgumentException("That footprint overlaps another console's pad.");}
   for(var root:prior)NativeBlocks.stateOf(w,nativeCell(root),"AR_LaunchPadModule","default");LaunchYardVisuals.remove(s,data(s).decks.remove(at));
  }else if(action.equals("Cancel")){
   if(p.phase()==ConstructionProgress.Phase.READY)throw new IllegalArgumentException("The completed rocket owns these materials. Drain and remove it before starting another blueprint.");
   var inv=s.getComponent(ref,Player.getComponentType()).getInventory().getCombinedHotbarFirst();
   if(!YardInventory.refund(inv,y.escrow))throw new IllegalArgumentException("Make room for all supplied materials before cancelling. Your exact items remain in the console.");
   y.escrow.clear();p.resetAfterRefund();for(var credit:y.recovered.entrySet())p.accept(credit.getKey(),credit.getValue());y.selected=!y.recovered.isEmpty();RocketService.cue(s,player,"AR_metal_handle",at);
  }else if(action.equals("Deposit")){
   if(!y.selected||p.phase()!=ConstructionProgress.Phase.PREVIEW)throw new IllegalArgumentException("Select a blueprint before feeding materials.");
   if(!formed(s,at,y))throw new IllegalArgumentException("Finish all 25 five-block modules before supplying materials.");
   var needed=new HashMap<String,Integer>();for(var id:p.plan().materials().keySet())needed.put(id,p.remaining(id));
   var inv=s.getComponent(ref,Player.getComponentType()).getInventory().getCombinedHotbarFirst();var paid=YardInventory.take(inv,needed);
   for(var item:paid){p.accept(item.getItemId(),item.getQuantity());y.escrow.add(item);}if(!paid.isEmpty())RocketService.cue(s,player,"AR_metal_handle",at);
   if(p.funded())start(s,at,y);else if(paid.isEmpty())throw new IllegalArgumentException("Carry the remaining blueprint materials; unrelated items stay in your inventory.");
  }else if(action.equals("Build")){start(s,at,y);}
  LaunchYardVisuals.sync(s);
 }catch(IllegalArgumentException|IllegalStateException ex){AstralReachPlugin.message(player,ex.getMessage());}}
 private static void start(Store<EntityStore>s,Cell at,LaunchYardRegistry.Yard y){
  if(!y.selected||!y.progress.funded())throw new IllegalArgumentException("Supply every listed material before construction.");
  if(!formed(s,at,y))throw new IllegalArgumentException("The full25×25 pad must be formed and loaded.");
  clearance(s,at,y);y.progress.start(true,true);
 }
 static void clearance(Store<EntityStore>s,Cell console,LaunchYardRegistry.Yard y){
  var w=s.getExternalData().getWorld();var at=anchor(console,y);var plan=y.progress.plan();int h=plan.sweptHeight()+2,radius=(int)Math.ceil(Math.max(plan.width,plan.depth)/2);
  if(at.y()<2||at.y()+h>=320)throw new IllegalArgumentException("Choose a lower launch site: this blueprint needs "+h+" clear blocks above the pad.");
  if(!NativeBlocks.empty(w,at)||RocketService.data(s).rockets.containsKey(at))throw new IllegalArgumentException("The central rocket anchor is occupied.");
  RocketService.clearColumn(w,at,radius,h,true);
 }
 static void tick(Store<EntityStore>s,long millis){tickCelebrations(s,millis);var w=s.getExternalData().getWorld();for(var e:new ArrayList<>(data(s).yards.entrySet())){
  var at=e.getKey();var y=e.getValue();var p=y.progress;boolean formed=formed(s,at,y),active=false;
  if(p.phase()==ConstructionProgress.Phase.READY){var a=anchor(at,y);if(NativeBlocks.get(w,a)!=null&&!NativeBlocks.rocket(w,a)&&!RocketService.data(s).rockets.containsKey(a)){y.progress=new ConstructionProgress(p.plan());y.selected=false;p=y.progress;}}
  if(p.phase()==ConstructionProgress.Phase.ASSEMBLING&&formed){try{clearance(s,at,y);active=true;}catch(IllegalArgumentException ignored){}p.advance(millis,active);
   if(p.due()&&active){var a=anchor(at,y);if(NativeBlocks.set(w,a,BlockType.getAssetMap().getAsset(p.plan().artId))){
    RocketService.data(s).rockets.put(a,new RocketRegistry.Rocket(y.owner,y.heldFuel,p.plan()));p.complete();y.escrow.clear();y.recovered.clear();y.heldFuel=0;active=false;
    // Persist the claimed flag before emitting any finite, non-authoritative visual/audio effect.
    if(p.claimCelebration())celebrate(s,at,y);
   }}
  }
  if(console(w,at))NativeBlocks.stateOf(w,at,"AR_LaunchConsole",active?"Active":"default");
  if(!overlap(s,at,y))for(var root:modules(at,y))NativeBlocks.stateOf(w,nativeCell(root),"AR_LaunchPadModule",formed?"Formed":"default");
 }
 LaunchYardVisuals.sync(s);
 for(var player:w.getPlayerRefs()){var ref=player.getReference();if(ref!=null&&ref.isValid()&&ref.getStore()==s){var p=s.getComponent(ref,Player.getComponentType());if(p!=null&&p.getPageManager().getCustomPage() instanceof LaunchYardPage page)page.refresh(ref,s);}}
 }
 private static void celebrate(Store<EntityStore>s,Cell at,LaunchYardRegistry.Yard y){var c=center(at,y);
  burst(s,c,0);data(s).celebrations.add(new LaunchYardRegistry.Celebration(c));
  for(var player:s.getExternalData().getWorld().getPlayerRefs())RocketService.cue(s,player,"AR_Rocket_Build_Complete",new Cell(c.x(),c.y()+4,c.z()));
 }
 private static void burst(Store<EntityStore>s,Cell c,int index){int[] dx={-11,11,0},dy={10,12,15},dz={10,10,12};ParticleUtil.spawnParticleEffect("Firework_GS",new org.joml.Vector3d(c.x()+.5+dx[index],c.y()+dy[index],c.z()+.5+dz[index]),s);}
 private static void tickCelebrations(Store<EntityStore>s,long millis){for(var it=data(s).celebrations.iterator();it.hasNext();){var volley=it.next();volley.elapsed+=millis;while(volley.next<3&&volley.elapsed>=volley.next*1000L){burst(s,volley.center,volley.next++);}if(volley.next==3)it.remove();}}
 static final class Placement extends EntityEventSystem<EntityStore,PlaceBlockEvent>{
  Placement(){super(PlaceBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>chunk,Store<EntityStore>s,CommandBuffer<EntityStore>b,PlaceBlockEvent e){
   if(e.isCancelled()||e.getItemInHand()==null||!e.getItemInHand().getItemId().equals("AR_LaunchConsole"))return;
   var player=chunk.getComponent(i,PlayerRef.getComponentType());var t=e.getTargetBlock();var at=new Cell(t.x,t.y,t.z);var w=s.getExternalData().getWorld();
   if(w.getName().equals("AstralLune")||data(s).yards.containsKey(at)){e.setCancelled(true);return;}
   w.execute(()->{if(!console(w,at))return;var y=new LaunchYardRegistry.Yard(player.getUuid(),LaunchPadLayout.Facing.NORTH);
    for(var facing:LaunchPadLayout.Facing.values()){y.facing=facing;if(present(w,at,y)==25&&!overlap(s,at,y))break;}if(present(w,at,y)!=25)y.facing=LaunchPadLayout.Facing.NORTH;
    data(s).yards.putIfAbsent(at,y);
   });
  }
 }
 static final class Breaking extends EntityEventSystem<EntityStore,BreakBlockEvent>{
  Breaking(){super(BreakBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>chunk,Store<EntityStore>s,CommandBuffer<EntityStore>b,BreakBlockEvent e){if(e.isCancelled())return;
   var t=e.getTargetBlock();var at=new Cell(t.x,t.y,t.z);var w=s.getExternalData().getWorld();var player=chunk.getComponent(i,PlayerRef.getComponentType());
   var y=data(s).yards.get(at);if(y!=null){if(!y.owner.equals(player.getUuid())||!y.escrow.isEmpty()||!y.recovered.isEmpty()||y.heldFuel!=0||y.progress.phase()!=ConstructionProgress.Phase.PREVIEW){e.setCancelled(true);AstralReachPlugin.message(player,"Cancel and recover materials, or drain and remove the completed rocket, before removing its console.");return;}
    w.execute(()->{if(NativeBlocks.get(w,at)!=null&&!console(w,at)){for(var root:modules(at,y))NativeBlocks.stateOf(w,nativeCell(root),"AR_LaunchPadModule","default");data(s).yards.remove(at);LaunchYardVisuals.sync(s);}});return;}
   if(!NativeBlocks.base(e.getBlockType()).equals("AR_LaunchPadModule"))return;
   for(var entry:data(s).yards.entrySet())if(LaunchPadLayout.contains(core(center(entry.getKey(),entry.getValue())),core(at))&&!entry.getValue().owner.equals(player.getUuid())){e.setCancelled(true);AstralReachPlugin.message(player,"This launch platform belongs to another player's console.");return;}
  }
 }
}
