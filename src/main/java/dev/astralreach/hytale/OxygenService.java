package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.*;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.OxygenSupply;
import org.joml.Vector3d;
import java.util.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Native breathing authority, real portable item charge and canister-backed placed fields. */
final class OxygenService {
 private static final String PACK="Items/AstralReach/AR_O2Pack";
 private static final Map<UUID,Long> warnings=new java.util.concurrent.ConcurrentHashMap<>();
 static OxygenRegistry data(Store<EntityStore>s){return s.getResource(AstralReachPlugin.oxygenType);}
 static boolean vacuum(Store<EntityStore>s){String w=s.getExternalData().getWorld().getName();return w.equals("AstralLune")||w.equals("AstralOrbit");}
 static boolean device(Store<EntityStore>s,Cell at){return NativeBlocks.base(NativeBlocks.get(s.getExternalData().getWorld(),at)).equals("AR_OxygenDiffuser");}
 static OxygenRegistry.Diffuser usable(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,Cell at){var d=data(s).diffusers.get(at);if(d==null||!d.owner.equals(p.getUuid())||!device(s,at)||!RocketService.near(s,r,at))throw new IllegalArgumentException("Stand beside your own oxygen diffuser.");return d;}
 static void action(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,Cell at,String action){try{var d=usable(s,r,p,at);var inv=s.getComponent(r,Player.getComponentType()).getInventory().getCombinedStorageHotbarBackpack();
  switch(action){case "Load"->{if(d.items.size()>=4)throw new IllegalArgumentException("Collect a canister before loading another. The diffuser holds four.");short slot=OxygenCanisters.active(inv);if(slot<0)throw new IllegalArgumentException("Carry a filled oxygen canister. The Oxygen Collector fills reusable empty canisters using gathered fuel on Orbis.");var stack=inv.getItemStack(slot);if(!inv.replaceItemStackInSlot(slot,stack,null).succeeded())throw new IllegalArgumentException("Canister moved; try again.");d.items.add(YardInventory.copy(stack));}case "Collect"->{if(!YardInventory.refund(inv,d.items))throw new IllegalArgumentException("Make room for every canister. Their exact partial charges remain stored here.");d.items.clear();d.powered=false;}case "Toggle"->d.enabled=!d.enabled;default->{return;}}
  RocketService.cue(s,p,"AR_oxygen_valve",at);
 }catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}}
 static boolean submerged(Store<EntityStore>s,Ref<EntityStore>r){return com.hypixel.hytale.math.util.MathUtil.unpackRight(LivingEntity.getPackedMaterialAndFluidAtBreathingHeight(r,s))!=0;}
 private static OxygenRegistry.Diffuser shelter(Store<EntityStore>s,Vector3d head){OxygenRegistry.Diffuser nearest=null;double best=OxygenSupply.RADIUS*OxygenSupply.RADIUS;for(var e:data(s).diffusers.entrySet()){var d=e.getValue();if(!d.powered)continue;double distance=head.distanceSquared(e.getKey().x()+.5,e.getKey().y()+1,e.getKey().z()+.5);if(distance<best){best=distance;nearest=d;}}return nearest;}
 private static Vector3d head(Store<EntityStore>s,Ref<EntityStore>r){var t=s.getComponent(r,TransformComponent.getComponentType());return t==null?null:new Vector3d(t.getPosition()).add(0,ModelComponent.getEyeHeight(r,s),0);}
 private static void burn(OxygenRegistry.Diffuser d,double seconds){double cost=seconds*OxygenSupply.stationRate(d.people);for(int i=0;i<d.items.size()&&cost>0;i++){var before=d.items.get(i);double n=OxygenCanisters.remaining(before);if(n<=0)continue;double used=Math.min(n,cost);d.items.set(i,OxygenCanisters.charge(before,n-used));cost-=used;}d.powered=d.oxygen()>0;}
 static void tick(Store<EntityStore>s,float dt){var data=data(s);data.elapsed+=Math.max(0,Math.min(1,dt));if(data.elapsed<1)return;double elapsed=data.elapsed;data.elapsed=0;var w=s.getExternalData().getWorld();
  for(var e:data.diffusers.entrySet()){var at=e.getKey();var d=e.getValue();d.people=0;d.powered=device(s,at)&&NativeBlocks.ticking(w,at)&&d.enabled&&d.oxygen()>0;}
  // Assign overlaps to a single nearest active device; never charge two stations for one player.
  for(var p:w.getPlayerRefs()){var r=p.getReference();if(r==null||!r.isValid()||r.getStore()!=s||submerged(s,r))continue;var head=head(s,r);var d=head==null?null:shelter(s,head);if(d!=null)d.people++;}
  for(var e:data.diffusers.entrySet()){var at=e.getKey();var d=e.getValue();if(d.powered)burn(d,elapsed);if(device(s,at))NativeBlocks.stateOf(w,at,"AR_OxygenDiffuser",d.powered?"Active":"default");
   if(d.powered){if(d.boundary==null||!d.boundary.isValid())d.boundary=RocketVisuals.spawn(s,Model.createScaledModel(ModelAsset.getAssetMap().getAsset("AR_OxygenBoundary"),32),new Vector3d(at.x()+.5,at.y()+1-8,at.z()+.5),new com.hypixel.hytale.math.vector.Rotation3f());}
   else if(d.boundary!=null){if(d.boundary.isValid())s.removeEntity(d.boundary,RemoveReason.REMOVE);d.boundary=null;}
  }
  for(var p:w.getPlayerRefs()){var r=p.getReference();if(r==null||!r.isValid()||r.getStore()!=s)continue;var player=s.getComponent(r,Player.getComponentType());if(player==null)continue;var inv=player.getInventory().getCombinedStorageHotbarBackpack();boolean helmet=ExpeditionSuit.helmet(s,r),sealed=ExpeditionSuit.tier(s,r)>0,wet=submerged(s,r);var head=head(s,r);boolean bubble=!wet&&head!=null&&shelter(s,head)!=null;boolean demand=(vacuum(s)||wet)&&!bubble;short slot=OxygenCanisters.active(inv);double available=slot<0?0:OxygenCanisters.remaining(inv.getItemStack(slot));
   var movement=s.getComponent(r,MovementStatesComponent.getComponentType());boolean sprint=movement!=null&&movement.getMovementStates().sprinting;double rate=OxygenSupply.rate(wet,sprint);
   if(demand&&sealed&&available>0){OxygenCanisters.consume(inv,slot,elapsed*rate);available=OxygenCanisters.remaining(inv.getItemStack(slot));}
   boolean canister=available>0;if(!canister){slot=OxygenCanisters.active(inv);available=slot<0?0:OxygenCanisters.remaining(inv.getItemStack(slot));canister=available>0;}
   int band=sealed&&canister?(int)Math.ceil(available/120):-1;backpack(s,r,band);
   if(helmet){var old=player.getHudManager().getCustomHud(OxygenHud.KEY);OxygenHud hud;if(old instanceof OxygenHud o)hud=o;else{hud=new OxygenHud(p);player.getHudManager().addCustomHud(p,hud);}int seconds=(int)(available/(demand&&sealed?rate:1));String text=canister?"O₂  "+Math.round(available/12)+"% · "+(seconds/60)+":"+String.format(java.util.Locale.ROOT,"%02d",seconds%60):"O₂  NO CANISTER";String mode=!sealed?"Suit unsealed · equip all four pieces":bubble?"Oxygen field · canister on standby":wet?"Underwater · double consumption":vacuum(s)?(sprint?"Vacuum · sprinting":"Vacuum · portable oxygen"):"Breathable air · canister on standby";hud.status(text,available,mode);
    if(demand&&sealed&&available<=120&&System.nanoTime()-warnings.getOrDefault(p.getUuid(),0L)>30_000_000_000L){warnings.put(p.getUuid(),System.nanoTime());LunarEnvironment.interior(s,r,p,"AR_oxygen_warning");AstralReachPlugin.message(p,available<=0?"Oxygen empty. Reach a powered diffuser or breathable air now.":"Oxygen below 10%. Carry a spare or return to your diffuser.");}
   }else if(player.getHudManager().getCustomHud(OxygenHud.KEY)!=null)player.getHudManager().removeCustomHud(p,OxygenHud.KEY);
   // Native material changes alone do not recheck canisters/field boundaries. Force its public evaluator once per second.
   var breathing=s.getComponent(r,BreathingComponent.getComponentType());if(breathing!=null)EntityUtils.processEntityBreathing(r,breathing,s.getComponent(r,CachedStatsComponent.getComponentType()),s.getComponent(r,Invulnerable.getComponentType())!=null,true,s);
   if(player.getPageManager().getCustomPage() instanceof OxygenPage page)page.refresh(s);
  }
 }
 private static void backpack(Store<EntityStore>s,Ref<EntityStore>r,int band){var component=s.getComponent(r,ModelComponent.getComponentType());if(component==null)return;var m=component.getModel();var old=m.getAttachments();String desired=band<0?null:PACK+band+".blockymodel";var attachments=new ArrayList<ModelAttachment>();String present=null;if(old!=null)for(var a:old){if(a.getModel()!=null&&a.getModel().startsWith(PACK))present=a.getModel();else attachments.add(a);}if(Objects.equals(desired,present))return;if(desired!=null)attachments.add(new ModelAttachment(desired,"Items/AstralReach/AR_Survey_Chest.png",null,null,1));
  var copy=new Model(m.getModelAssetId(),m.getScale(),m.getRandomAttachmentIds(),attachments.toArray(ModelAttachment[]::new),m.getBoundingBox(),m.getModel(),m.getTexture(),m.getGradientSet(),m.getGradientId(),m.getEyeHeight(),m.getCrouchOffset(),m.getSittingOffset(),m.getSleepingOffset(),m.getAnimationSetMap(),m.getCamera(),m.getLight(),m.getParticles(),m.getTrails(),m.getPhysicsValues(),m.getDetailBoxes(),m.getPhobia(),m.getPhobiaModelAssetId());s.putComponent(r,ModelComponent.getComponentType(),new ModelComponent(copy));
 }
 static void requireDeparture(Store<EntityStore>s,Ref<EntityStore>r){var p=s.getComponent(r,Player.getComponentType());if(p==null||OxygenCanisters.total(p.getInventory().getCombinedStorageHotbarBackpack())<240)throw new IllegalArgumentException("Carry at least four minutes of oxygen before departure. Fill reusable canisters at an Oxygen Collector on Orbis; one full canister lasts20 minutes of normal breathing.");}
 static final class Breathing extends EntityEventSystem<EntityStore,BreathingCheckEvent>{
  Breathing(){super(BreathingCheckEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,BreathingCheckEvent e){var r=c.getReferenceTo(i);boolean vacuum=vacuum(s);if(!vacuum&&e.canBreathe())return;var player=c.getComponent(i,Player.getComponentType());if(player==null)return;boolean dry=e.getFluidId()==0,empty=e.getBreathingMaterial()==com.hypixel.hytale.protocol.BlockMaterial.Empty;var h=head(s,r);boolean field=dry&&empty&&h!=null&&shelter(s,h)!=null;boolean tank=empty&&ExpeditionSuit.tier(s,r)>0&&OxygenCanisters.active(player.getInventory().getCombinedStorageHotbarBackpack())>=0;
   // A suit supplies inhalable oxygen in fluid, but does not make solid blocks breathable.
   e.setCanBreathe(empty&&(field||tank||(!vacuum&&dry)));
  }
 }
 static final class Placement extends EntityEventSystem<EntityStore,PlaceBlockEvent>{
  Placement(){super(PlaceBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,PlaceBlockEvent e){if(e.isCancelled()||e.getItemInHand()==null)return;String id=e.getItemInHand().getItemId();if(id.equals("AR_OxygenCollector")&&vacuum(s)){e.setCancelled(true);AstralReachPlugin.message(c.getComponent(i,PlayerRef.getComponentType()),"The collector needs an oxygen atmosphere. Fill canisters on Orbis and bring them to Lune.");return;}if(!id.equals("AR_OxygenDiffuser"))return;var p=c.getComponent(i,PlayerRef.getComponentType());var t=e.getTargetBlock();var at=new Cell(t.x,t.y,t.z);s.getExternalData().getWorld().execute(()->{if(device(s,at))data(s).diffusers.putIfAbsent(at,new OxygenRegistry.Diffuser(p.getUuid()));});}
 }
 static final class Breaking extends EntityEventSystem<EntityStore,BreakBlockEvent>{
  Breaking(){super(BreakBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,BreakBlockEvent e){var t=e.getTargetBlock();var at=new Cell(t.x,t.y,t.z);var d=data(s).diffusers.get(at);if(d==null)return;var p=c.getComponent(i,PlayerRef.getComponentType());if(!d.owner.equals(p.getUuid())||!d.items.isEmpty()){e.setCancelled(true);AstralReachPlugin.message(p,"Only the owner can remove this diffuser. Collect all canisters first.");return;}s.getExternalData().getWorld().execute(()->{if(!device(s,at)){if(d.boundary!=null&&d.boundary.isValid())s.removeEntity(d.boundary,RemoveReason.REMOVE);data(s).diffusers.remove(at,d);}});}
 }
}
