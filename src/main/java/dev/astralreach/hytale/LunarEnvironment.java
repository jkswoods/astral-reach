package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateAudioCategories;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import java.util.*;

/** Surface-only native movement, bounded cosmetic tracks and per-listener helmet attenuation. */
final class LunarEnvironment {
 private static final class Walker{Vector3d last;boolean left;float jump,fall;MovementManager manager;boolean lunar,muffled,vacuum,grounded;long audioAt,stepAt,mineAt;}
 private record Print(Ref<EntityStore> ref,long expires){}
 private static final Map<UUID,Walker> walkers=new HashMap<>();
 private static final Map<Store<EntityStore>,ArrayDeque<Print>> tracks=new IdentityHashMap<>();
 static void tick(Store<EntityStore>s,float dt){var w=s.getExternalData().getWorld();boolean moon=w.getName().equals("AstralLune");
  if(moon&&(!"AR_LunarSurface".equals(w.getWorldConfig().getForcedWeather())||w.getWorldConfig().isGameTimePaused())){
   var cfg=w.getWorldConfig();cfg.setForcedWeather("AR_LunarSurface");cfg.setGameplayConfig("AR_Lunar");cfg.setGameTimePaused(false);cfg.markChanged();s.getResource(com.hypixel.hytale.builtin.weather.resources.WeatherResource.getResourceType()).setForcedWeather("AR_LunarSurface");
  }
  for(var p:w.getPlayerRefs()){
   var r=p.getReference();if(r==null||!r.isValid()||r.getStore()!=s)continue;var walker=walkers.computeIfAbsent(p.getUuid(),id->new Walker());var f=s.getComponent(r,AstralReachPlugin.flightType);boolean foot=moon&&(f==null||!f.boarded)&&!RocketService.transferring.contains(p.getUuid());
   var manager=s.getComponent(r,MovementManager.getComponentType());
   if(foot&&manager!=null){if(!walker.lunar||walker.manager!=manager){walker.manager=manager;walker.jump=manager.getSettings().jumpForce;walker.fall=manager.getSettings().variableJumpFallForce;walker.lunar=true;manager.getSettings().jumpForce=8.5f;manager.getSettings().variableJumpFallForce=0;manager.update(p.getPacketHandler());}}
   else if(walker.lunar){if(manager==walker.manager){if(manager.getSettings().jumpForce==8.5f)manager.getSettings().jumpForce=walker.jump;if(manager.getSettings().variableJumpFallForce==0)manager.getSettings().variableJumpFallForce=walker.fall;manager.update(p.getPacketHandler());}walker.lunar=false;walker.last=null;}
   boolean helmet=ExpeditionSuit.helmet(s,r);if(walker.muffled!=helmet||walker.vacuum!=moon){attenuate(p,helmet,moon);walker.vacuum=moon;walker.muffled=helmet;if(helmet){var t=s.getComponent(r,TransformComponent.getComponentType());if(t!=null)RocketService.cue(s,p,"AR_suit_latch",new RocketRegistry.Cell((int)t.getPosition().x,(int)t.getPosition().y,(int)t.getPosition().z));}}
   if(!foot)continue;var movement=s.getComponent(r,MovementStatesComponent.getComponentType());if(movement==null)continue;var m=movement.getMovementStates();
   // Native velocity instructions are acknowledged by the controlling client. No position correction loop.
   if(!m.onGround&&!m.flying&&!m.climbing&&!m.inFluid&&!m.mantling&&!m.gliding){var velocity=s.getComponent(r,Velocity.getComponentType());if(velocity!=null)velocity.addInstruction(new Vector3d(0,(32d*5/6)*Math.clamp(dt,0,.1),0),null,ChangeVelocityType.Add);}
   var transform=s.getComponent(r,TransformComponent.getComponentType());if(transform==null)continue;var pos=transform.getPosition();if(!m.onGround||m.flying){walker.last=null;walker.grounded=false;continue;}if(!walker.grounded){walker.grounded=true;if(helmet&&System.nanoTime()-walker.stepAt>350_000_000L){walker.stepAt=System.nanoTime();interior(s,r,p,"AR_lunar_step");}}
   if(walker.last==null){walker.last=new Vector3d(pos);continue;}double distance=pos.distance(walker.last);if(distance>3){walker.last=new Vector3d(pos);continue;}if(distance<.68)continue;
   if(helmet&&System.nanoTime()-walker.stepAt>300_000_000L){walker.stepAt=System.nanoTime();interior(s,r,p,"AR_lunar_step");}
   var direction=new Vector3d(pos).sub(walker.last);walker.last.set(pos);walker.left=!walker.left;double yaw=Math.atan2(-direction.x,-direction.z),side=walker.left?-.14:.14;double x=pos.x+Math.cos(yaw)*side,z=pos.z-Math.sin(yaw)*side;int ground=(int)Math.floor(pos.y-.12);var at=new RocketRegistry.Cell((int)Math.floor(x),ground,(int)Math.floor(z));var block=NativeBlocks.get(w,at);
   if(block==null||!NativeBlocks.base(block).equals("AR_LuneRegolith")||Math.abs(pos.y-(ground+1))>.22)continue;
   var ref=RocketVisuals.spawn(s,Model.createScaledModel(ModelAsset.getAssetMap().getAsset("AR_Bootprint"),1),new Vector3d(x,ground+1.006,z),new Rotation3f(0,(float)yaw,0));
   if(ref!=null){var queue=tracks.computeIfAbsent(s,key->new ArrayDeque<>());queue.addLast(new Print(ref,System.nanoTime()+900_000_000_000L));while(queue.size()>192)remove(s,queue.removeFirst());}
  }
  var queue=tracks.get(s);if(queue!=null){long now=System.nanoTime();while(!queue.isEmpty()&&(queue.peekFirst().expires<now||!queue.peekFirst().ref.isValid()))remove(s,queue.removeFirst());if(queue.isEmpty())tracks.remove(s);}
 }
 static void interior(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,String cue){var map=com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap();if(map.getAsset(cue)!=null)com.hypixel.hytale.server.core.universe.world.SoundUtil.playSoundEvent2dToPlayer(p,map.getIndex(cue),com.hypixel.hytale.protocol.SoundCategory.SFX);}
 static final class Mining extends com.hypixel.hytale.component.system.EntityEventSystem<EntityStore,com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent>{
  Mining(){super(com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent.class);}
  @Override public com.hypixel.hytale.component.query.Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent e){
   if(e.isCancelled()||e.getDamage()<=0||!s.getExternalData().getWorld().getName().equals("AstralLune"))return;var p=c.getComponent(i,PlayerRef.getComponentType());s.getExternalData().getWorld().execute(()->{var r=p.getReference();if(e.isCancelled()||e.getDamage()<=0||r==null||!r.isValid()||r.getStore()!=s||!ExpeditionSuit.helmet(s,r))return;var walker=walkers.computeIfAbsent(p.getUuid(),id->new Walker());long now=System.nanoTime();if(now-walker.mineAt<200_000_000L)return;walker.mineAt=now;interior(s,r,p,"AR_lunar_mine");});
  }
 }
 private static void remove(Store<EntityStore>s,Print p){if(p.ref.isValid())s.removeEntity(p.ref,RemoveReason.REMOVE);}
 private static void attenuate(PlayerRef p,boolean helmet,boolean vacuum){var map=AudioCategory.getAssetMap();var values=new HashMap<Integer,com.hypixel.hytale.protocol.AudioCategory>();for(var e:Map.of("AudioCat_Ambient",-80f,"AudioCat_BlockSounds",vacuum?-80f:-20f,"AudioCat_Footsteps",vacuum?-80f:-18f,"AudioCat_Abilities",vacuum?-80f:-16f,"AudioCat_Weapons",vacuum?-80f:-22f).entrySet()){var asset=map.getAsset(e.getKey());if(asset==null)continue;var packet=asset.toPacket();if(helmet)packet.volume+=e.getValue();values.put(map.getIndex(e.getKey()),packet);}if(!values.isEmpty())p.getPacketHandler().writeNoCache(new UpdateAudioCategories(UpdateType.AddOrUpdate,map.getNextIndex(),values));}
}
