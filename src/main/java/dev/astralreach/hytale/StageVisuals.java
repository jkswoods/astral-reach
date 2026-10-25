package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import java.util.*;

/** One transient stage at an owned mission footprint, including refs restored by native section activation. */
final class StageVisuals {
 static List<Ref<EntityStore>> at(Store<EntityStore>s,String model,Vector3d position){
  var refs=new ArrayList<Ref<EntityStore>>();
  s.forEachChunk(Query.and(ModelComponent.getComponentType(),TransformComponent.getComponentType(),PropComponent.getComponentType(),Intangible.getComponentType()),(java.util.function.BiConsumer<ArchetypeChunk<EntityStore>,CommandBuffer<EntityStore>>)(c,b)->{
   for(int i=0;i<c.size();i++){
    var m=c.getComponent(i,ModelComponent.getComponentType());var t=c.getComponent(i,TransformComponent.getComponentType());var p=t.getPosition();
    if(model.equals(m.getModel().getModelAssetId())&&Math.abs(p.x-position.x)<.25&&Math.abs(p.z-position.z)<.25)refs.add(c.getReferenceTo(i));
   }
  });return refs;
 }
 static Ref<EntityStore> one(Store<EntityStore>s,String model,Vector3d position,Ref<EntityStore> preferred){
  var refs=at(s,model,position);var keep=preferred!=null&&preferred.isValid()&&preferred.getStore()==s?preferred:null;
  if(keep==null)keep=refs.stream().min(Comparator.comparingDouble(r->s.getComponent(r,TransformComponent.getComponentType()).getPosition().distanceSquared(position))).orElse(null);
  for(var r:refs)if(r!=keep&&r.isValid())s.removeEntity(r,RemoveReason.REMOVE);
  if(keep==null)keep=RocketVisuals.spawn(s,model,position,new Rotation3f());
  if(keep!=null&&keep.isValid())s.getComponent(keep,TransformComponent.getComponentType()).setPosition(position);
  return keep;
 }
 static void clear(Store<EntityStore>s,String model,Vector3d position){for(var r:at(s,model,position))if(r.isValid())s.removeEntity(r,RemoveReason.REMOVE);}
}
