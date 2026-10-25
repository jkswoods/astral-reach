package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.AnimationSlot;
import org.joml.Vector3d;
import java.util.*;
import dev.astralreach.core.LaunchPlan;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Transient decorative exterior. The placed block remains the sole control/payment authority. */
final class RocketVisuals {
 private static final Map<String,Model> models=new HashMap<>();
 static Model model(){return model(LaunchPlan.ODYSSEY);}
 static Model model(LaunchPlan plan){return named(plan==LaunchPlan.ODYSSEY?"AR_OdysseyExterior":"AR_KestrelExterior");}
 static Model named(String name){return models.computeIfAbsent(name,key->{var asset=ModelAsset.getAssetMap().getAsset(key);if(asset==null)throw new IllegalStateException("Missing rocket model "+key);return Model.createScaledModel(asset,32);});}
 static Ref<EntityStore> spawn(Store<EntityStore>s,String name,Vector3d position,Rotation3f rotation){
  return spawn(s,named(name),position,rotation);
 }
 static Ref<EntityStore> spawn(Store<EntityStore>s,Model visual,Vector3d position,Rotation3f rotation){
  return spawn(s,visual,position,rotation,false);
 }
 private static Ref<EntityStore> spawn(Store<EntityStore>s,Model visual,Vector3d position,Rotation3f rotation,boolean launch){
  // Native insertion into an inactive section may return null while retaining a holder
  // for later activation. Retrying that insertion used to create untracked sky copies.
  if(!NativeBlocks.ticking(s.getExternalData().getWorld(),new Cell((int)Math.floor(position.x),(int)Math.floor(position.y),(int)Math.floor(position.z))))return null;
  var holder=s.getRegistry().newHolder();holder.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.generateVersion3UUID());
  // A spawn-time PlayAnimation broadcast has no viewers yet. The native tracker
  // includes this component when the new exterior first becomes visible.
  if(launch){var animation=new ActiveAnimationComponent();animation.setPlayingAnimation(AnimationSlot.Status,"Launching");holder.addComponent(ActiveAnimationComponent.getComponentType(),animation);}
  holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(position,rotation));holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(visual));holder.addComponent(ModelRotateBoundingBoxComponent.getComponentType(),new ModelRotateBoundingBoxComponent(true));holder.addComponent(PropComponent.getComponentType(),PropComponent.get());holder.addComponent(Intangible.getComponentType(),Intangible.INSTANCE);holder.addComponent(s.getRegistry().getNonSerializedComponentType(),NonSerialized.get());return s.addEntity(holder,new Ref<>(s),AddReason.SPAWN);
 }
 static void sync(Store<EntityStore>s){
  var data=RocketService.data(s);var w=s.getExternalData().getWorld();
  data.exhaustAt.keySet().removeIf(at->!data.rockets.containsKey(at));
  var existing=new HashSet<>(data.exteriors.keySet());existing.addAll(data.boosters.keySet());
  for(var at:existing)if(!data.rockets.containsKey(at)){remove(s,data.exteriors.remove(at));data.exteriorStates.remove(at);remove(s,data.boosters.remove(at));data.boosterStates.remove(at);}
  for(var e:data.rockets.entrySet()){
   var at=e.getKey();var rocket=e.getValue();String state=RocketService.vehicleState(rocket);double elapsed=RocketService.departureElapsed(rocket);
   if(!NativeBlocks.rocket(w,at)||!NativeBlocks.ticking(w,at)){remove(s,data.exteriors.remove(at));data.exteriorStates.remove(at);remove(s,data.boosters.remove(at));data.boosterStates.remove(at);continue;}
   var rotation=new Rotation3f();RotationTuple.get(NativeBlocks.rotation(w,at)).applyRotationTo(rotation);var position=new Vector3d(at.x()+.5,at.y(),at.z()+.5);
   String name=state.equals("Away")?null:state.equals("Launching")?(rocket.plan==LaunchPlan.ODYSSEY?(elapsed<dev.astralreach.core.FlightTimeline.SEPARATION?"AR_OdysseyAscent":"AR_OdysseyShip"):elapsed<dev.astralreach.core.FlightTimeline.SEPARATION?"AR_KestrelAscent":"AR_KestrelShip"):(rocket.plan==LaunchPlan.ODYSSEY?"AR_OdysseyExterior":"AR_KestrelExterior");
   syncPart(s,data.exteriors,data.exteriorStates,at,name,position,rotation,state.equals("Launching"));
   sound(s,data.exteriors.get(at),state.equals("Launching")&&elapsed>=dev.astralreach.core.FlightTimeline.BOARDING);
   String booster=null; // Odyssey core/solid boosters are expendable; they do not sit restored on the pad.
   if(rocket.plan==LaunchPlan.KESTREL&&(state.equals("Launching")&&elapsed>=dev.astralreach.core.FlightTimeline.SEPARATION||state.equals("Away")&&rocket.boosterLanded)){
    if(elapsed>=dev.astralreach.core.FlightTimeline.BOOSTER_LANDED)rocket.boosterLanded=true;
    booster=rocket.boosterLanded?"AR_KestrelBoosterIdle":"AR_KestrelBoosterFollow";
   }
   syncPart(s,data.boosters,data.boosterStates,at,booster,position,rotation,false);
   if("AR_KestrelBoosterFollow".equals(booster)){var ref=data.boosters.get(at);if(ref!=null&&ref.isValid()){var pose=s.getComponent(ref,TransformComponent.getComponentType());pose.setPosition(BoosterMotion.center(at,rotation.yaw(),elapsed));pose.setRotation(BoosterMotion.rotation(rotation.yaw(),elapsed));}}
   sound(s,data.boosters.get(at),"AR_KestrelBoosterFollow".equals(booster)&&dev.astralreach.core.FlightTimeline.boosterPowered(elapsed));
   if(state.equals("Launching"))RocketExhaust.emit(s,at,rocket.plan,elapsed,rotation.yaw());else data.exhaustAt.remove(at);
  }
 }
 private static void sound(Store<EntityStore>s,Ref<EntityStore>r,boolean powered){if(r==null||!r.isValid())return;var type=AudioComponent.getComponentType();if(powered){if(s.getComponent(r,type)==null){var a=new AudioComponent();a.addSound(com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex("AR_rocket_thrust_loop"));s.putComponent(r,type,a);}}else s.tryRemoveComponent(r,type);}
 private static void syncPart(Store<EntityStore>s,Map<Cell,Ref<EntityStore>>refs,Map<Cell,String>states,Cell at,String name,Vector3d position,Rotation3f rotation,boolean animate){
  var ref=refs.get(at);if(name==null||!name.equals(states.get(at))){remove(s,refs.remove(at));states.remove(at);ref=null;}
  if(name!=null&&(ref==null||!ref.isValid())){ref=spawn(s,named(name),position,rotation,animate);if(ref!=null){refs.put(at,ref);states.put(at,name);}}
 }
 private static void remove(Store<EntityStore>s,Ref<EntityStore>ref){if(ref!=null&&ref.isValid())s.removeEntity(ref,RemoveReason.REMOVE);}
}
