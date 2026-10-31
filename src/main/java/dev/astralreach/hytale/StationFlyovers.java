package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.StationPass;
import org.joml.Vector3d;
import java.util.*;
import java.util.concurrent.*;
/** Listener-local native cosmetic projections. Never spawn physical station copies or override weather. */
final class StationFlyovers {
 private record Light(int networkId){}
 private static final class View{final Store<EntityStore>store;final PlayerRef player;final Object connection;final Map<UUID,Light>lights=new HashMap<>();long sent;View(Store<EntityStore>s,PlayerRef p){store=s;player=p;connection=p.getPacketHandler();}}
 private record Candidate(UUID station,StationPass.Pass pass){}
 private static final Map<UUID,View>views=new ConcurrentHashMap<>();private static volatile com.hypixel.hytale.protocol.Model model,detailed;
 static boolean surface(com.hypixel.hytale.server.core.universe.world.World w){return w==Universe.get().getDefaultWorld()||w.getName().equals("AstralOrbis");}
 static void tick(Store<EntityStore>s){var world=s.getExternalData().getWorld();boolean surface=surface(world);if(surface&&!world.getPlayerRefs().isEmpty())StationSkyCatalog.refresh();
  for(var p:world.getPlayerRefs()){var ref=p.getReference();if(ref==null||!ref.isValid()||ref.getStore()!=s)continue;var old=views.get(p.getUuid());if(old!=null&&(old.store!=s||old.player!=p||old.connection!=p.getPacketHandler())){views.remove(p.getUuid(),old);old=null;}var flight=s.getComponent(ref,AstralReachPlugin.flightType);if(!surface||flight!=null&&flight.boarded){if(old!=null)clear(old,true);continue;}if(old==null){old=new View(s,p);views.put(p.getUuid(),old);}long now=System.nanoTime();if(now-old.sent<100_000_000L)continue;old.sent=now;
   var transform=s.getComponent(ref,TransformComponent.getComponentType());if(transform==null)continue;var at=transform.getPosition();var weather=s.getComponent(ref,WeatherTracker.getComponentType());var asset=weather==null?null:Weather.getAssetMap().getAsset(weather.getWeatherIndex());boolean precipitation=asset!=null&&asset.getParticle()!=null;double hour=s.getResource(WorldTimeResource.getResourceType()).getDayProgress()*24,seconds=System.currentTimeMillis()/1000d;var selected=new ArrayList<Candidate>();if(!precipitation)for(var id:StationSkyCatalog.owners()){var pass=StationPass.sample(id,seconds,at.x,at.z,hour);if(pass.brightness()<.015)continue;selected.add(new Candidate(id,pass));selected.sort(Comparator.comparingDouble((Candidate c)->c.pass.brightness()).reversed().thenComparing(Candidate::station));if(selected.size()>3)selected.removeLast();}
   var wanted=new HashSet<UUID>();var removed=new ArrayList<Integer>();var updates=new ArrayList<EntityUpdate>();for(var candidate:selected){var id=candidate.station;wanted.add(id);var light=old.lights.get(id);boolean fresh=light==null;if(fresh){light=new Light(s.getExternalData().takeNextNetworkId());old.lights.put(id,light);}var direction=candidate.pass.direction();var position=new Vector3d(at.x+direction.x()*StationPass.PROJECTION,at.y+1.6+direction.y()*StationPass.PROJECTION,at.z+direction.z()*StationPass.PROJECTION);var angle=com.hypixel.hytale.math.vector.Rotation3f.lookAt(new Vector3d(at).add(0,1.6,0).sub(position));var rotation=new Direction(angle.yaw(),angle.pitch(),0);var parts=new ArrayList<ComponentUpdate>();if(fresh){parts.add(new NewSpawnUpdate());parts.add(new PropUpdate());parts.add(new IntangibleUpdate());}boolean scoped=Observatory.scoping(p.getUuid());parts.add(new ModelUpdate(scoped?detailed():model(),(float)(scoped?.022:.9*Math.sqrt(candidate.pass.brightness())*StationPass.reflectionGain(id,seconds))));parts.add(new TransformUpdate(new ModelTransform(new Position(position.x,position.y,position.z),rotation,rotation)));updates.add(new EntityUpdate(light.networkId,null,parts.toArray(ComponentUpdate[]::new)));}
   for(var entry:new ArrayList<>(old.lights.entrySet()))if(!wanted.contains(entry.getKey())){removed.add(entry.getValue().networkId);old.lights.remove(entry.getKey());}if(!updates.isEmpty()||!removed.isEmpty())p.getPacketHandler().writeNoCache(new EntityUpdates(removed.stream().mapToInt(Integer::intValue).toArray(),updates.toArray(EntityUpdate[]::new)));
  }
  for(var v:new ArrayList<>(views.values()))if(v.store==s){var ref=v.player.getReference();if(ref==null||!ref.isValid()||ref.getStore()!=s||Universe.get().getPlayer(v.player.getUuid())!=v.player)views.remove(v.player.getUuid(),v);}
 }
 private static synchronized com.hypixel.hytale.protocol.Model model(){if(model==null){var asset=ModelAsset.getAssetMap().getAsset("AR_StationGlint");if(asset==null)throw new IllegalStateException("Missing station sky glint");model=com.hypixel.hytale.server.core.asset.type.model.config.Model.createScaledModel(asset,1).toPacket();}return model;}
 private static synchronized com.hypixel.hytale.protocol.Model detailed(){if(detailed==null)detailed=com.hypixel.hytale.server.core.asset.type.model.config.Model.createScaledModel(ModelAsset.getAssetMap().getAsset("AR_StationDistant"),1).toPacket();return detailed;}
 private static void clear(View view,boolean remove){if(remove&&!view.lights.isEmpty())view.player.getPacketHandler().writeNoCache(new EntityUpdates(view.lights.values().stream().mapToInt(Light::networkId).toArray(),null));view.lights.clear();}
 static void shutdown(){for(var view:new ArrayList<>(views.values()))view.store.getExternalData().getWorld().execute(()->{var ref=view.player.getReference();if(ref!=null&&ref.isValid()&&ref.getStore()==view.store&&view.player.getPacketHandler()==view.connection)clear(view,true);});views.clear();}
 private StationFlyovers(){}
}
