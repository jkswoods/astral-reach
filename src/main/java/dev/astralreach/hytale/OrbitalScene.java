package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.asset.type.model.config.*;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import java.util.*;

/** Top-down native globes, separated from the cabin and its third-person ship. */
final class OrbitalScene {
 private record Body(String name,double x,double z,double radius){}
 private static final List<Body> bodies=List.of(new Body("Sol",-45,0,8),new Body("Orbis",-23,-6,5),new Body("Lune",-14,-14,3),new Body("Rustreach",-20,29,4),new Body("Nacre",-8,-40,5.5),new Body("Vesper",16,35,7),new Body("Umbra",27,-50,3.8));
 private final List<Ref<EntityStore>> refs=new ArrayList<>();private final Map<Integer,String> targets=new HashMap<>();private Ref<EntityStore> marker;private final Store<EntityStore> store;private final PlayerRef player;private final Vector3d center;
 double zoom=70,panX,panZ;
 OrbitalScene(Store<EntityStore>s,PlayerRef p){store=s;player=p;center=new Vector3d(SpaceFlight.slot(p),242,-160);
  refs.add(RocketVisuals.spawn(s,Model.createScaledModel(ModelAsset.getAssetMap().getAsset("AR_MapBackdrop"),14.4f),new Vector3d(center).add(0,-1.5,0),new Rotation3f()));
  for(var b:bodies){var asset=ModelAsset.getAssetMap().getAsset("AR_Map"+b.name);var ref=RocketVisuals.spawn(s,Model.createScaledModel(asset,(float)(14.4*b.radius)),new Vector3d(center).add(b.x*.45,0,b.z*.45),new Rotation3f());refs.add(ref);if(ref!=null){s.tryRemoveComponent(ref,Intangible.getComponentType());s.putComponent(ref,Interactable.getComponentType(),Interactable.INSTANCE);s.putComponent(ref,Nameplate.getComponentType(),new Nameplate(b.name));var id=s.getComponent(ref,NetworkId.getComponentType());if(id!=null)targets.put(id.getId(),b.name);}}
  refs.add(RocketVisuals.spawn(s,Model.createScaledModel(ModelAsset.getAssetMap().getAsset("AR_MapPaths"),14.4f),new Vector3d(center).add(-20.25,-.36,0),new Rotation3f()));
  marker=RocketVisuals.spawn(s,Model.createScaledModel(ModelAsset.getAssetMap().getAsset("AR_MapSelection"),14.4f),new Vector3d(center),new Rotation3f());refs.add(marker);select("Lune");
 }
 String hit(int networkId){return targets.get(networkId);}
 String hitPlane(org.joml.Vector3ic point){if(point==null)return null;String result=null;double nearest=Double.POSITIVE_INFINITY;for(var b:bodies){if(b.name.equals("Sol"))continue;double dx=point.x()+.5-center.x-b.x*.45,dz=point.z()+.5-center.z-b.z*.45,d=Math.hypot(dx,dz);if(d<=b.radius*.45+.75&&d<nearest){result=b.name;nearest=d;}}return result;}
 void dragPlane(double dx,double dz){panX=Math.clamp(panX-dx,-45,45);panZ=Math.clamp(panZ-dz,-45,45);view();}
 void select(String name){if(marker==null||!marker.isValid())return;for(var b:bodies)if(b.name.equals(name)){store.getComponent(marker,TransformComponent.getComponentType()).setPosition(new Vector3d(center).add(b.x*.45,-.225,b.z*.45));break;}}
 void pan(int x,int z){drag(-x*70,-z*70);}
 void drag(double dx,double dy){panX=Math.clamp(panX-dx*zoom/1000,-45,45);panZ=Math.clamp(panZ-dy*zoom/1000,-45,45);view();}
 void reset(){zoom=70;panX=panZ=0;view();}
 void view(){zoom=Math.clamp(zoom,32,130);var target=new Vector3d(center).add(panX+7,0,panZ);var eye=new Vector3d(target).add(0,zoom,.001);var look=Rotation3f.lookAt(new Vector3d(target).sub(eye));var c=new ServerCameraSettings();c.positionLerpSpeed=.25f;c.rotationLerpSpeed=.25f;c.attachedToType=AttachedToType.None;c.positionType=PositionType.Custom;c.position=new Position(eye.x,eye.y,eye.z);c.rotationType=RotationType.Custom;c.rotation=new Direction(look.yaw(),look.pitch(),0);c.applyLookType=ApplyLookType.LocalPlayerLookOrientation;c.positionOffset=new Position();c.positionDistanceOffsetType=PositionDistanceOffsetType.None;c.isFirstPerson=false;c.skipCharacterPhysics=false;c.hideHeldItem=true;c.displayReticle=false;c.displayCursor=true;c.sendMouseMotion=true;c.mouseInputTargetType=MouseInputTargetType.Any;c.mouseInputType=MouseInputType.LookAtPlane;c.planeNormal=new org.joml.Vector3f(0,1,0);c.allowPitchControls=false;c.movementForceRotationType=MovementForceRotationType.Custom;c.movementForceRotation=new Direction();c.movementMultiplier=new org.joml.Vector3f();c.baseFov=60f;player.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom,true,c));}
 void close(){for(var r:refs)if(r!=null&&r.isValid())store.removeEntity(r,RemoveReason.REMOVE);refs.clear();targets.clear();}
}
