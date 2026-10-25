package dev.astralreach.hytale;
import com.hypixel.hytale.math.vector.Rotation3f;
import dev.astralreach.core.FlightTimeline;
import org.joml.Vector3d;
/** Entity and nozzles share the same native pitch/yaw/roll quaternion and center of mass. */
final class BoosterMotion{
 static Rotation3f rotation(double yaw,double t){return new Rotation3f(0,(float)yaw,(float)FlightTimeline.boosterRoll(t));}
 static Vector3d center(RocketRegistry.Cell at,double yaw,double t){var offset=new Rotation3f(0,(float)yaw,0).transform(new Vector3d(FlightTimeline.boosterX(t),0,0));return offset.add(at.x()+.5,at.y()+36+FlightTimeline.boosterHeight(t),at.z()+.5);}
 static Vector3d nozzle(RocketRegistry.Cell at,double yaw,double t,double x,double y,double z){return rotation(yaw,t).transform(new Vector3d(x,y-36,z)).add(center(at,yaw,t));}
}
