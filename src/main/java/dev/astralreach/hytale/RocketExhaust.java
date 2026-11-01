package dev.astralreach.hytale;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;
import org.bson.*;
import java.util.*;

/** Finite native bursts at the same authored engine transforms as the stage animations. */
final class RocketExhaust {
 private record Nozzle(double x,double y,double z,boolean ship){}
 private static final Map<LaunchPlan,List<Nozzle>> engines=load();
 private static final Map<LaunchPlan,List<Nozzle>> upperEngines=loadUpper();
 private static Map<LaunchPlan,List<Nozzle>> loadUpper(){try(var in=RocketExhaust.class.getResourceAsStream("/astral-upper-nozzles.json")){var doc=BsonDocument.parse(new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));var result=new EnumMap<LaunchPlan,List<Nozzle>>(LaunchPlan.class);for(var plan:LaunchPlan.values()){var list=new ArrayList<Nozzle>();for(var value:doc.getArray(plan.name())){var n=value.asDocument();list.add(new Nozzle(n.getNumber("x").doubleValue(),n.getNumber("y").doubleValue(),n.getNumber("z").doubleValue(),true));}result.put(plan,List.copyOf(list));}return Map.copyOf(result);}catch(java.io.IOException e){throw new IllegalStateException("Standalone stage engine anchors unavailable",e);}}
 private static Map<LaunchPlan,List<Nozzle>> load(){
  try(var in=RocketExhaust.class.getResourceAsStream("/astral-engine-nozzles.json")){
   if(in==null)throw new IllegalStateException("Missing authored engine positions");var doc=BsonDocument.parse(new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));var result=new EnumMap<LaunchPlan,List<Nozzle>>(LaunchPlan.class);
   for(var plan:LaunchPlan.values()){var list=new ArrayList<Nozzle>();for(var value:doc.getArray(plan.name())){var n=value.asDocument();list.add(new Nozzle(n.getNumber("x").doubleValue(),n.getNumber("y").doubleValue(),n.getNumber("z").doubleValue(),n.getBoolean("ship").getValue()));}result.put(plan,List.copyOf(list));}return Map.copyOf(result);
  }catch(java.io.IOException e){throw new IllegalStateException("Cannot read authored engine positions",e);}
 }
 private static final Map<String,com.hypixel.hytale.server.core.asset.type.model.config.Model> poweredStages=new java.util.HashMap<>();
 static void power(Store<EntityStore>s,com.hypixel.hytale.component.Ref<EntityStore>r,LaunchPlan plan,boolean powered){
  power(s,r,plan,powered,plan.asset("ShipLanded"));
 }
 static void power(Store<EntityStore>s,com.hypixel.hytale.component.Ref<EntityStore>r,LaunchPlan plan,boolean powered,String asset){
  var m=RocketVisuals.named(asset);
  if(powered)m=poweredStages.computeIfAbsent(asset,key->{var base=RocketVisuals.named(key);var particles=upperEngines.get(plan).stream().flatMap(n->java.util.stream.Stream.of(stream(base,n,"AR_EngineMouthStream",2.05),stream(base,n,"AR_EngineFireStream",9.05))).toArray(com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle[]::new);
   return new com.hypixel.hytale.server.core.asset.type.model.config.Model(base.getModelAssetId(),base.getScale(),base.getRandomAttachmentIds(),base.getAttachments(),base.getBoundingBox(),base.getModel(),base.getTexture(),base.getGradientSet(),base.getGradientId(),base.getEyeHeight(),base.getCrouchOffset(),base.getSittingOffset(),base.getSleepingOffset(),base.getAnimationSetMap(),base.getCamera(),base.getLight(),particles,base.getTrails(),base.getPhysicsValues(),base.getDetailBoxes(),base.getPhobia(),base.getPhobiaModelAssetId());});
  s.putComponent(r,com.hypixel.hytale.server.core.modules.entity.component.ModelComponent.getComponentType(),new com.hypixel.hytale.server.core.modules.entity.component.ModelComponent(m));
 }
 // Place quad centres in the same scaled model space as their engine mouth. Spawner
 // EmitOffset is affected differently by native attached-particle scaling.
 private static com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle stream(com.hypixel.hytale.server.core.asset.type.model.config.Model model,Nozzle n,String system,double below){return new com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle(system,com.hypixel.hytale.protocol.EntityPart.Self,"Root",null,1/model.getScale(),new org.joml.Vector3f((float)n.x/model.getScale(),(float)(n.y-below)/model.getScale(),(float)n.z/model.getScale()),new com.hypixel.hytale.protocol.Direction(0,0,0),false,true);}
 static int count(LaunchPlan plan){return engines.get(plan).size();}
 static void emit(Store<EntityStore>s,RocketRegistry.Cell at,LaunchPlan plan,double seconds,double yaw){
  if(seconds<FlightTimeline.BOARDING)return;
  var data=RocketService.data(s);long now=System.nanoTime();if(now-data.exhaustAt.getOrDefault(at,0L)<160_000_000L)return;data.exhaustAt.put(at,now);
  var audience=s.getExternalData().getWorld().getPlayerRefs().stream().map(p->p.getReference()).filter(r->r!=null&&r.isValid()).toList();double cy=Math.cos(yaw),sy=Math.sin(yaw);
  if(plan!=LaunchPlan.KESTREL&&seconds>=FlightTimeline.SEPARATION)return; // Upper-stage particles follow the animated model Root.
  for(var n:engines.get(plan)){
   if(plan==LaunchPlan.SOYUZ&&seconds>=FlightTimeline.SPACE&&!n.ship&&(Math.abs(n.x)>1||Math.abs(n.z)>1))continue;
   boolean separated=plan==LaunchPlan.KESTREL&&seconds>=FlightTimeline.SEPARATION;
   if(separated&&n.ship)continue;
   if(n.ship&&!separated&&plan!=LaunchPlan.SPACE_SHUTTLE||!n.ship&&!FlightTimeline.boosterPowered(seconds))continue;
   double x=n.x,y=n.y,z=n.z,roll=0,lift=FlightTimeline.height(seconds);
   double wx,wy,wz;
   if(separated&&!n.ship){var position=BoosterMotion.nozzle(at,yaw,seconds,x,y,z);wx=position.x;wy=position.y;wz=position.z;roll=FlightTimeline.boosterRoll(seconds);}
   else{if(separated&&n.ship)x+=FlightTimeline.upperX(plan,seconds);wx=at.x()+.5+cy*x+sy*z;wy=at.y()+lift+y;wz=at.z()+.5-sy*x+cy*z;}

   ParticleUtil.spawnParticleEffect("AR_EngineFire",wx,wy,wz,(float)yaw,0,(float)roll,1,null,null,audience,s,384);
  }
 }
 static void upper(Store<EntityStore>s,LaunchPlan plan,org.joml.Vector3d origin,double yaw){
  var audience=s.getExternalData().getWorld().getPlayerRefs().stream().map(p->p.getReference()).filter(r->r!=null&&r.isValid()).toList();
  double cy=Math.cos(yaw),sy=Math.sin(yaw);for(var n:upperEngines.get(plan))ParticleUtil.spawnParticleEffect("AR_EngineFire",origin.x+cy*n.x+sy*n.z,origin.y+n.y,origin.z-sy*n.x+cy*n.z,(float)yaw,0,0,1,null,null,audience,s,384);
 }
}
