package dev.astralreach.core;
/** Native model axes: upper stages point along local+Y; docking rotates them nose-in along world+Z. */
public final class StationDocking {
 public static final double SECONDS=12,DEPARTURE_SECONDS=16,APPROACH=40;
 public static double duration(boolean departing){return departing?DEPARTURE_SECONDS:SECONDS;}
 public record Point(double x,double y,double z){}
 public record Pose(Point base,Point crew,Point camera,Point target,double pitch){}
 public static Pose pose(StationLayout l,int port,LaunchPlan plan,double seconds,boolean departing){
  StationLayout.checkPort(port);double t=Math.clamp(seconds/SECONDS,0,1),smooth=t*t*(3-2*t);double separation=(departing?smooth:1-smooth)*APPROACH;double x=l.contactX(port),y=l.contactY(),nose=l.contactZ()-separation,base=nose-plan.upperHeight;
  var origin=new Point(x,y,base);var crew=new Point(x,y-.9,base+plan.crewHeight);var target=new Point(x,y,base+plan.upperHeight*.5);double d=Math.max(24,plan.upperHeight*.85);var camera=new Point(x+d,y+d*.32,target.z-d*.25);double pitch=Math.PI/2;
  if(departing&&seconds>SECONDS){double q=FlightTimeline.ease(seconds,SECONDS,DEPARTURE_SECONDS);pitch=Math.PI/2*(1-q);double cy=Math.cos(pitch),sy=Math.sin(pitch);origin=new Point(x,y-plan.upperHeight*.5*cy,target.z-plan.upperHeight*.5*sy);crew=new Point(x,origin.y+plan.crewHeight*cy-.9*sy,origin.z+plan.crewHeight*sy);camera=new Point(target.x+d*(1-q)-55*q,target.y+d*.32*(1-q)-110*q,target.z-d*.25*(1-q)-120*q);}
  return new Pose(origin,crew,camera,target,pitch);
 }
 public static boolean lanesClear(StationLayout l){for(int a=0;a<StationLayout.PORTS;a++)for(int b=a+1;b<StationLayout.PORTS;b++)for(var p:LaunchPlan.values())for(var q:LaunchPlan.values())if(Math.abs(l.contactX(a)-l.contactX(b))<(Math.max(p.width,p.depth)+Math.max(q.width,q.depth))/2+2)return false;return true;}
 private StationDocking(){}
}
