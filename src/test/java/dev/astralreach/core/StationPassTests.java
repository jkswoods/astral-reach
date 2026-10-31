package dev.astralreach.core;
import java.util.*;
final class StationPassTests {
 static int count;static void check(boolean v,String why){count++;if(!v)throw new AssertionError(why);}
 static int run(){
  count=0;check(StationPass.sun(12).y()>.8&&StationPass.sun(0).y()<-.8,"Solar visibility uses the actual sky arc, not the native corrected shadow direction");int lit=0,hidden=0;var seen=new HashSet<String>();
  for(var id:List.of(UUID.fromString("8197ffa5-62e5-48cb-a29d-0989b74ce7af"),new UUID(1,7),new UUID(2,9))){double highest=-1;for(int second=0;second<720;second++){
   var pass=StationPass.sample(id,second,1.5,18.5,18.8);var repeat=StationPass.sample(id,second+2880,1.5,18.5,18.8);check(pass.direction().dot(repeat.direction())>.999999999,"Saved identity and UTC phase repeat over four orbits/one planet rotation; no randomized restart");check(Math.abs(pass.direction().length()-1)<1e-10&&Double.isFinite(pass.brightness())&&pass.brightness()>=0&&pass.brightness()<=1,"Projection finite and unit length");check(StationPass.sample(id,second,1.5,18.5,12).brightness()==0,"No bright station light in midday sky");if(pass.elevation()<Math.toRadians(4))check(pass.brightness()==0,"No light through the ground/horizon");if(pass.brightness()>.05)lit++;else hidden++;highest=Math.max(highest,pass.elevation());
   var next=StationPass.sample(id,second+.1,1.5,18.5,18.8);check(pass.direction().dot(next.direction())>.9999,"Steady sub-degree motion between updates, without teleporting horizon jumps");var nearby=StationPass.sample(id,second,2.5,18.5,18.8);check(pass.direction().dot(nearby.direction())>.99999,"Nearby observers agree on the orbital pass");
  }seen.add(String.format(Locale.ROOT,"%.5f",highest));}
  check(lit>20&&hidden>lit,"Real intervals above the horizon and in sunlight; not an always-on fake star");check(seen.size()>1,"Different deployed identities have distinct ground tracks");var id=new UUID(1,7);double midnightPeak=0;for(int t=0;t<720;t++)midnightPeak=Math.max(midnightPeak,StationPass.sample(id,t,0,0,0).brightness());check(midnightPeak==0,"Planet shadow hides unlit stations in deep midnight");return count;
 }
}
