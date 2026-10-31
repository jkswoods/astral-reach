package dev.astralreach.core;
/** Native model axes: upper stages point along local+Y; docking rotates them nose-in along world+Z. */
public final class StationDocking {
 public static final double SECONDS=12,APPROACH=40;
 public record Point(double x,double y,double z){}
 public record Pose(Point base,Point crew,Point camera,Point target,double pitch){}
 public static Pose pose(StationLayout l,int port,LaunchPlan plan,double seconds,boolean departing){StationLayout.checkPort(port);double t=Math.clamp(seconds/SECONDS,0,1),smooth=t*t*(3-2*t);double separation=(departing?smooth:1-smooth)*APPROACH;double x=l.contactX(port),y=l.contactY(),nose=l.contactZ()-separation,base=nose-plan.upperHeight;var origin=new Point(x,y,base);var crew=new Point(x,y-.9,base+plan.crewHeight);var target=new Point(x,y,base+plan.upperHeight*.5);double d=Math.max(24,plan.upperHeight*.85);var camera=new Point(x+d,y+d*.32,target.z-d*.25);return new Pose(origin,crew,camera,target,Math.PI/2);}
 public static boolean lanesClear(StationLayout l){for(int a=0;a<StationLayout.PORTS;a++)for(int b=a+1;b<StationLayout.PORTS;b++)for(var p:LaunchPlan.values())for(var q:LaunchPlan.values())if(Math.abs(l.contactX(a)-l.contactX(b))<(Math.max(p.width,p.depth)+Math.max(q.width,q.depth))/2+2)return false;return true;}
 private StationDocking(){}
}
