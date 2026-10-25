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
 private static Map<LaunchPlan,List<Nozzle>> load(){
  try(var in=RocketExhaust.class.getResourceAsStream("/astral-engine-nozzles.json")){
   if(in==null)throw new IllegalStateException("Missing authored engine positions");var doc=BsonDocument.parse(new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));var result=new EnumMap<LaunchPlan,List<Nozzle>>(LaunchPlan.class);
   for(var plan:LaunchPlan.values()){var list=new ArrayList<Nozzle>();for(var value:doc.getArray(plan.name())){var n=value.asDocument();list.add(new Nozzle(n.getNumber("x").doubleValue(),n.getNumber("y").doubleValue(),n.getNumber("z").doubleValue(),n.getBoolean("ship").getValue()));}result.put(plan,List.copyOf(list));}return Map.copyOf(result);
  }catch(java.io.IOException e){throw new IllegalStateException("Cannot read authored engine positions",e);}
 }
 static int count(LaunchPlan plan){return engines.get(plan).size();}
 static void emit(Store<EntityStore>s,RocketRegistry.Cell at,LaunchPlan plan,double seconds,double yaw){
  if(seconds<FlightTimeline.BOARDING)return;
  var data=RocketService.data(s);long now=System.nanoTime();if(now-data.exhaustAt.getOrDefault(at,0L)<160_000_000L)return;data.exhaustAt.put(at,now);
  var audience=s.getExternalData().getWorld().getPlayerRefs().stream().map(p->p.getReference()).filter(r->r!=null&&r.isValid()).toList();double cy=Math.cos(yaw),sy=Math.sin(yaw);
  if(plan==LaunchPlan.ODYSSEY&&seconds>=FlightTimeline.SEPARATION){upper(s,plan,new org.joml.Vector3d(at.x()+.5,at.y()+FlightTimeline.height(seconds)+FlightTimeline.upperBase(plan),at.z()+.5).add(cy*FlightTimeline.upperX(plan,seconds),0,-sy*FlightTimeline.upperX(plan,seconds)),yaw);return;}
  for(var n:engines.get(plan)){
   boolean separated=plan==LaunchPlan.KESTREL&&seconds>=FlightTimeline.SEPARATION;
   if(plan==LaunchPlan.KESTREL&&(n.ship&&!separated||!n.ship&&!FlightTimeline.boosterPowered(seconds)))continue;
   double x=n.x,y=n.y,z=n.z,roll=0,lift=FlightTimeline.height(seconds);
   double wx,wy,wz;
   if(separated&&!n.ship){var position=BoosterMotion.nozzle(at,yaw,seconds,x,y,z);wx=position.x;wy=position.y;wz=position.z;roll=FlightTimeline.boosterRoll(seconds);}
   else{if(separated&&n.ship)x+=FlightTimeline.upperX(plan,seconds);wx=at.x()+.5+cy*x+sy*z;wy=at.y()+lift+y;wz=at.z()+.5-sy*x+cy*z;}

   ParticleUtil.spawnParticleEffect("AR_EngineFire",wx,wy,wz,(float)yaw,0,(float)roll,1,null,null,audience,s,384);
  }
 }
 static void upper(Store<EntityStore>s,LaunchPlan plan,org.joml.Vector3d origin,double yaw){
  var audience=s.getExternalData().getWorld().getPlayerRefs().stream().map(p->p.getReference()).filter(r->r!=null&&r.isValid()).toList();
  if(plan==LaunchPlan.ODYSSEY){ParticleUtil.spawnParticleEffect("AR_EngineFire",origin.x,origin.y-1.632,origin.z,(float)yaw,0,0,1,null,null,audience,s,384);return;}
  double cy=Math.cos(yaw),sy=Math.sin(yaw);for(var n:engines.get(plan))if(n.ship)ParticleUtil.spawnParticleEffect("AR_EngineFire",origin.x+cy*n.x+sy*n.z,origin.y+n.y-FlightTimeline.upperBase(plan),origin.z-sy*n.x+cy*n.z,(float)yaw,0,0,1,null,null,audience,s,384);
 }
}
