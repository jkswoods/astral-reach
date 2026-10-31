package dev.astralreach.hytale;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.joml.Vector3d;

final class RoverView {
 // Stock player pelvis51 and Sit delta-45 leave6/64 above root; .90 seats it on the .965 cushion.
 static org.joml.Vector3f offset(boolean passenger){return new org.joml.Vector3f(passenger?.43f:-.43f,.90f,.13f);}
 static Vector3d seat(TransformComponent rover,boolean passenger){double yaw=rover.getRotation().yaw();var offset=offset(passenger);double x=offset.x,z=offset.z;return new Vector3d(rover.getPosition()).add(x*Math.cos(yaw)+z*Math.sin(yaw),offset.y,-x*Math.sin(yaw)+z*Math.cos(yaw));}
 private static final java.util.Set<java.util.UUID> active=java.util.concurrent.ConcurrentHashMap.newKeySet();
 // Complete the player teleport/native mount acknowledgement before enabling an entity camera.
 static void prepare(PlayerRef p){if(p!=null)p.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.ThirdPerson,true,null));}
 static void update(PlayerRef p,TransformComponent rover,int vehicleId){
  if(p==null||vehicleId<=0||!active.add(p.getUuid()))return;
  // Configure the native player-attached third-person view. Entity-attached Custom
  // changes the client's mounted control context and hides the driver (native50/51).
  var c=new ServerCameraSettings();c.isFirstPerson=false;c.hideHeldItem=true;c.eyeOffset=true;
  c.distance=5.5f;c.positionOffset=new Position(0,.3,0);c.rotationOffset=new Direction(0,-.24f,0);
  c.allowPitchControls=true;c.positionDistanceOffsetType=PositionDistanceOffsetType.DistanceOffset;
  p.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.ThirdPerson,true,c));
 }
 static void reset(PlayerRef p){if(p!=null){active.remove(p.getUuid());p.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.FirstPerson,false,null));}}
 static void forget(java.util.UUID id){active.remove(id);}
 static void clear(){active.clear();}
 private RoverView(){}
}
