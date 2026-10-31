package dev.astralreach.core;
import java.util.UUID;
/** Compressed circular orbit and reflected sunlight, expressed in the observer's local sky. */
public final class StationPass {
 public static final double PERIOD=720,RADIUS=3200,ALTITUDE=800,PROJECTION=96;
 public record Vector(double x,double y,double z){public double dot(Vector v){return x*v.x+y*v.y+z*v.z;}public double length(){return Math.sqrt(dot(this));}public Vector unit(){double d=length();return new Vector(x/d,y/d,z/d);}}
 public record Pass(Vector direction,double elevation,double brightness){}
 private static double fraction(long v){return (v>>>11)*0x1.0p-53;}
 private static long mix(long x){x=(x^(x>>>30))*0xbf58476d1ce4e5b9L;x=(x^(x>>>27))*0x94d049bb133111ebL;return x^(x>>>31);}
 private static double fade(double low,double high,double value){double t=Math.clamp((value-low)/(high-low),0,1);return t*t*(3-2*t);}
 /** Native 0.7 sky's unmodified solar arc, before its separate shadow-lighting correction. */
 public static Vector sun(double hour){hour=((hour%24)+24)%24;double a=hour<4.8?(hour-4.8)/19.2*Math.TAU:hour>19.2?(hour-28.8)/19.2*Math.TAU:(hour-4.8)/28.8*Math.TAU;return new Vector(Math.cos(a),2*Math.sin(a),Math.sin(a)).unit();}
 public static Pass sample(UUID station,double utcSeconds,double x,double z,double hour){
  if(station==null||!Double.isFinite(utcSeconds)||!Double.isFinite(x)||!Double.isFinite(z)||!Double.isFinite(hour))throw new IllegalArgumentException("Invalid station observer");
  long seed=mix(station.getMostSignificantBits()^station.getLeastSignificantBits());double phase=Math.TAU*(utcSeconds%PERIOD/PERIOD+fraction(seed));double node=Math.TAU*fraction(mix(seed)),inclination=.35+.8*fraction(mix(seed+1));double r=RADIUS+ALTITUDE;
  double a=r*Math.cos(phase),b=r*Math.sin(phase)*Math.cos(inclination);var stationPosition=new Vector(a*Math.cos(node)-b*Math.sin(node),r*Math.sin(phase)*Math.sin(inclination),a*Math.sin(node)+b*Math.cos(node));
  double longitude=x/16000+Math.TAU*(utcSeconds%(PERIOD*4))/(PERIOD*4),latitude=Math.clamp(z/16000,-.75,.75);var up=new Vector(Math.cos(latitude)*Math.cos(longitude),Math.sin(latitude),Math.cos(latitude)*Math.sin(longitude));var east=new Vector(-Math.sin(longitude),0,Math.cos(longitude));var north=new Vector(-Math.sin(latitude)*Math.cos(longitude),Math.cos(latitude),-Math.sin(latitude)*Math.sin(longitude));
  var relative=new Vector(stationPosition.dot(east),stationPosition.dot(up)-RADIUS,-stationPosition.dot(north));var direction=relative.unit();double elevation=Math.asin(Math.clamp(direction.y,-1,1));var sunlight=sun(hour);var planetLocal=new Vector(relative.x,relative.y+RADIUS,relative.z);double along=planetLocal.dot(sunlight),across=Math.sqrt(Math.max(0,planetLocal.dot(planetLocal)-along*along));double lit=along>=0?1:fade(RADIUS,RADIUS+70,across);
  double contrast=1-fade(-.03,.30,sunlight.y);double horizon=fade(Math.toRadians(4),Math.toRadians(11),elevation);double range=Math.clamp(ALTITUDE/relative.length(),.18,1);return new Pass(direction,elevation,lit*contrast*horizon*(.5+.5*range));
 }
 public record Prediction(double waitSeconds,double bearing,double maxElevation,boolean visible,double riseSeconds,double setSeconds){}
 /** Forecast the next lit pass. Native paused game-time has zero solar speed; UTC motion remains live. */
 public static Prediction next(UUID id,double seconds,double x,double z,double hour,double hoursPerSecond){
  boolean visible=sample(id,seconds,x,z,hour).brightness()>=.015;double start=-1,bearing=0,peak=0,end=3600;
  for(int t=0;t<=3600;t+=2){var p=sample(id,seconds+t,x,z,hour+t*hoursPerSecond);if(p.brightness()>=.015){if(start<0){start=t;bearing=bearing(p.direction());}peak=Math.max(peak,p.elevation());}else if(start>=0){end=t;break;}}
  double rise=start;if(visible)for(int t=2;t<=720;t+=2){var p=sample(id,seconds-t,x,z,hour-t*hoursPerSecond);if(p.brightness()<.015)break;rise=-t;peak=Math.max(peak,p.elevation());}
  return new Prediction(start,bearing,Math.toDegrees(peak),visible,rise,end);
 }
 public static double bearing(Vector v){return (Math.toDegrees(Math.atan2(v.x,-v.z))+360)%360;}
 public static String compass(double degrees){return new String[]{"N","NE","E","SE","S","SW","W","NW"}[(int)Math.floor((degrees+22.5)/45)%8];}
 /** Brief deterministic solar-panel specular reflection, never an arbitrary blinking beacon. */
 public static double reflectionGain(UUID id,double seconds){double phase=((seconds+fraction(mix(id.getLeastSignificantBits()))*PERIOD*3)%(PERIOD*3)+PERIOD*3)%(PERIOD*3);double d=Math.min(phase,PERIOD*3-phase);return 1+1.4*Math.pow(Math.max(0,1-d/1.6),2);}
 private StationPass(){}
}
