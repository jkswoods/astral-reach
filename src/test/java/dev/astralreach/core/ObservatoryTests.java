package dev.astralreach.core;
import java.util.*;import static dev.astralreach.core.CoreTests.*;
final class ObservatoryTests{
 static void run(){
  var id=UUID.fromString("8197ffa5-62e5-48cb-a29d-0989b74ce7af");
  check(StationPower.advance(0,0,0,1,1,true).supplied()==0,"No free life support without solar or charge");
  var direct=StationPower.advance(0,0,1,1,1,true);check(direct.supplied()==1&&direct.consumed()==3&&direct.stored()==0,"Direct solar supplies a full load, surplus requires battery");
  check(StationPower.advance(2.9,1,0,1,1,false).supplied()==0,"Insufficient charge cannot grant a free full oxygen tick");
  double charge=2400;for(int t=0;t<288;t++)charge=StationPower.advance(charge,1,0,1,1,false).stored();check(charge==1536,"One battery covers a complete eclipse at3/s");
  check(StationPower.advance(2399,1,2,0,1,true).stored()==2400,"Solar overflow clamps to real installed capacity");
  check(StationPower.advance(2400,0,0,0,1,false).stored()==0,"Removed batteries cannot leave charge in a station-wide free pool");
  for(int panels=0;panels<5;panels++)for(int loads=0;loads<5;loads++)for(int tick=1;tick<=10;tick++){double seconds=tick/10d;var p=StationPower.advance(500,1,panels,loads,seconds,true);check(Math.abs(p.stored()+p.consumed()-500-p.generated())<1e-8,"Power accounting conserves all non-overflow charge");check(p.supplied()<=loads&&p.stored()>=0&&p.stored()<=2400,"Grants and storage remain bounded");}
  reject(()->StationPower.advance(Double.NaN,1,1,1,1,true),"Corrupt charge rejected");reject(()->StationPower.advance(1,1,1,1,2,true),"Offline elapsed cannot mint power");
  check(StationPower.sunlit(0)&&!StationPower.sunlit(500)&&StationPower.sunlit(720),"Solar alternates with orbital eclipse independently of paused world clock");
  check(StationPass.bearing(new StationPass.Vector(0,0,-1))==0&&StationPass.bearing(new StationPass.Vector(1,0,0))==90,"Tracker bearings match native north and east");
  for(int t=0;t<3600;t+=37){var p=StationPass.next(id,1793260000+t,1,18,4.6,0);check(p.waitSeconds()>=-1&&p.waitSeconds()<=3600,"Bounded pass search");if(p.waitSeconds()>=0)check(StationPass.sample(id,1793260000+t+p.waitSeconds(),1,18,4.6).brightness()>=.015,"Countdown predicts an actual sunlit visible pass");double g=StationPass.reflectionGain(id,t);check(g>=1&&g<=2.4,"Panel reflection is bounded");check(Math.abs(g-StationPass.reflectionGain(id,t+2160))<1e-8,"Reflection schedule stable across cycles");}
  check(StationPass.next(id,1793260000,1,18,12,0).waitSeconds()<0,"Paused midday cannot promise a visible station pass");
 }
}
