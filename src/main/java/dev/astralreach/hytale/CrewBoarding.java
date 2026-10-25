package dev.astralreach.hytale;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.teleport.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Actual player travel inside the cabin, using native acknowledged movement rather than a client-driven minecart. */
final class CrewBoarding {
 private static final class Seat {
  final PlayerRef player;final Vector3d footprint;Ref<EntityStore> entity;Vector3d sent;long moveAt;
  Seat(PlayerRef p,Vector3d at){player=p;footprint=new Vector3d(at);}
 }
 private static final Map<UUID,Seat> seats=new ConcurrentHashMap<>();
 static boolean occupied(Store<EntityStore>s,Ref<EntityStore>r){return s.getComponent(r,MountedComponent.getComponentType())!=null;}
 static void hold(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,Vector3d position){
  if(s.getComponent(r,Teleport.getComponentType())!=null||RocketService.transferring.contains(p.getUuid()))return;
  var seat=seats.get(p.getUuid());
  if(seat!=null&&seat.entity!=null&&seat.entity.getStore()!=s){release(s,r,p);seat=null;}
  if(seat==null){seat=new Seat(p,position);seats.put(p.getUuid(),seat);}
  // Decorative seat activation must never gate the passenger's movement across a section boundary.
  if(seat.entity==null||!seat.entity.isValid())seat.entity=StageVisuals.one(s,"AR_CrewSeat",position,null);
  if(seat.entity!=null&&seat.entity.isValid())s.getComponent(seat.entity,TransformComponent.getComponentType()).setPosition(position);
  long now=System.nanoTime();var transform=s.getComponent(r,TransformComponent.getComponentType());
  if(now-seat.moveAt<100_000_000L||s.getComponent(r,PendingTeleport.getComponentType())!=null)return;
  if(seat.sent!=null&&seat.sent.distanceSquared(position)<.0025&&transform.getPosition().distanceSquared(position)<.25)return;
  // Ordinary Transform updates intentionally skip the controlling client. Native Teleport
  // corrects that client's real body too and waits for its acknowledgment before sending another.
  var head=s.getComponent(r,HeadRotation.getComponentType());
  seat.sent=new Vector3d(position);seat.moveAt=now;
  s.putComponent(r,Teleport.getComponentType(),Teleport.createExact(position,transform.getRotation(),head==null?transform.getRotation():head.getRotation()));
 }
 static void release(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  var seat=seats.remove(p.getUuid());if(seat==null)return;
  if(r!=null&&r.isValid()&&r.getStore()==s){var mount=s.getComponent(r,MountedComponent.getComponentType());if(mount!=null&&seat.entity!=null&&mount.getMountedToEntity()==seat.entity)s.tryRemoveComponent(r,MountedComponent.getComponentType());}
  if(seat.entity==null||seat.entity.getStore()==s){StageVisuals.clear(s,"AR_CrewSeat",seat.footprint);if(seat.entity!=null&&seat.entity.isValid())s.removeEntity(seat.entity,RemoveReason.REMOVE);}
 }
 static void cleanup(Store<EntityStore>s){
  for(var seat:new ArrayList<>(seats.values())){if(seat.entity!=null&&seat.entity.getStore()!=s)continue;var r=seat.player.getReference();if(r==null||!r.isValid()||r.getStore()!=s)release(s,r,seat.player);}
 }
}
