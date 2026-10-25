package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.ConstructionProgress;
import org.joml.Vector3d;
import java.util.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Joined deck and full-size intangible preview. Neither can accept fuel, fly or become saved entities. */
final class LaunchYardVisuals {
 private static Model deck;
 static Model deckModel(){if(deck==null){var asset=ModelAsset.getAssetMap().getAsset("AR_FormedLaunchPad");if(asset==null)throw new IllegalStateException("Missing formed launch pad model");deck=Model.createScaledModel(asset,32);}return deck;}
 static Ref<EntityStore> spawn(Store<EntityStore>s,Model model,Vector3d position,boolean ghost){
  var holder=s.getRegistry().newHolder();holder.addComponent(com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType(),com.hypixel.hytale.server.core.entity.UUIDComponent.generateVersion3UUID());
  holder.addComponent(TransformComponent.getComponentType(),new TransformComponent(position,new Rotation3f()));holder.addComponent(ModelComponent.getComponentType(),new ModelComponent(model));holder.addComponent(ModelRotateBoundingBoxComponent.getComponentType(),new ModelRotateBoundingBoxComponent(true));holder.addComponent(PropComponent.getComponentType(),PropComponent.get());holder.addComponent(Intangible.getComponentType(),Intangible.INSTANCE);holder.addComponent(s.getRegistry().getNonSerializedComponentType(),NonSerialized.get());
  if(ghost)holder.addComponent(EffectControllerComponent.getComponentType(),new EffectControllerComponent());
  var ref=s.addEntity(holder,new Ref<>(s),AddReason.SPAWN);
  if(ref!=null&&ghost){var effects=EntityEffect.getAssetMap();var effect=effects.getAsset("AR_ConstructionGhost");if(effect==null||!s.getComponent(ref,EffectControllerComponent.getComponentType()).addInfiniteEffect(ref,effects.getIndex("AR_ConstructionGhost"),effect,s)){remove(s,ref);throw new IllegalStateException("Native construction ghost effect could not be applied");}}
  return ref;
 }
 static void remove(Store<EntityStore>s,Ref<EntityStore>ref){if(ref!=null&&ref.isValid())s.removeEntity(ref,RemoveReason.REMOVE);}
 static void sync(Store<EntityStore>s){var data=LaunchYardService.data(s);
  for(var at:new ArrayList<>(data.decks.keySet()))if(!data.yards.containsKey(at)){remove(s,data.decks.remove(at));remove(s,data.ghosts.remove(at));data.ghostPlans.remove(at);}
  for(var e:data.yards.entrySet()){
   var at=e.getKey();var yard=e.getValue();boolean formed=LaunchYardService.formed(s,at,yard);
   if(!formed){remove(s,data.decks.remove(at));remove(s,data.ghosts.remove(at));data.ghostPlans.remove(at);continue;}
   var center=LaunchYardService.center(at,yard);var deckRef=data.decks.get(at);
   if(deckRef==null||!deckRef.isValid()){var added=spawn(s,deckModel(),new Vector3d(center.x()+.5,center.y(),center.z()+.5),false);if(added!=null)data.decks.put(at,added);}
   var p=yard.progress;boolean ghost=yard.selected&&p.phase()!=ConstructionProgress.Phase.READY;String key=p.plan().name();var ghostRef=data.ghosts.get(at);
   if(!ghost||!key.equals(data.ghostPlans.get(at))){remove(s,data.ghosts.remove(at));data.ghostPlans.remove(at);ghostRef=null;}
   if(ghost&&(ghostRef==null||!ghostRef.isValid())){var a=LaunchYardService.anchor(at,yard);var added=spawn(s,RocketVisuals.model(p.plan()),new Vector3d(a.x()+.5,a.y(),a.z()+.5),true);if(added!=null){data.ghosts.put(at,added);data.ghostPlans.put(at,key);}}
  }
 }
}
