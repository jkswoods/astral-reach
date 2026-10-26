package dev.astralreach.core;
import java.util.*;
public final class CoreTests {
 private static int checks;
 static void check(boolean b,String msg){checks++;if(!b)throw new AssertionError(msg);}
 static void reject(Runnable r,String msg){checks++;try{r.run();throw new AssertionError(msg);}catch(IllegalArgumentException|IllegalStateException expected){}}
 public static void main(String[] args){
  check(DestinationAccess.canTravel("Orbis",true,0),"Protected home return stays available with a missing suit or depleted oxygen");
  check(!DestinationAccess.canTravel("Lune",true,0)&&DestinationAccess.canTravel("Lune",true,1),"Lunar landing preserves its existing complete Survey suit gate");
  check(!DestinationAccess.canTravel("Orbis",false,3)&&!DestinationAccess.canTravel("Lune",false,3),"UI selection never grants an unpaid journey");
  for(String body:List.of("Rustreach","Nacre","Vesper","Umbra","untrusted"))check(!DestinationAccess.canTravel(body,true,3),"Future and unknown destinations cannot become active travel from UI events");
  check(OxygenSupply.remaining(1200,600,OxygenSupply.rate(false,false))==600,"Half a twenty-minute canister after ten minutes of normal breathing");
  check(OxygenSupply.remaining(1200,600,OxygenSupply.rate(true,false))==0&&OxygenSupply.remaining(1200,800,OxygenSupply.rate(false,true))==0,"Underwater ten-minute and sprint thirteen-minute-twenty budgets");
  check(Math.abs(1200/OxygenSupply.stationRate(1)-1333.333333)<.001&&OxygenSupply.stationRate(3)>OxygenSupply.stationRate(1),"Diffuser upkeep and real occupants consume a finite pool");
  reject(()->OxygenSupply.remaining(Double.NaN,1,1),"Invalid oxygen metadata cannot create a budget");
  for(var plan:LaunchPlan.values())for(boolean booster:new boolean[]{false,true}){var costs=plan.materials();var recovered=RocketRecovery.recovered(plan,booster);var missing=RocketRecovery.missing(plan,booster);check(!missing.isEmpty(),"Every returned vehicle needs actual service or replacement material");for(var cost:costs.entrySet())check(cost.getValue()==recovered.getOrDefault(cost.getKey(),0)+missing.getOrDefault(cost.getKey(),0),"Recovered plus replacement equals original assembly cost");}
  check(RocketRecovery.missing(LaunchPlan.ODYSSEY,false).get("AR_OrbitalEnginePart")==3&&RocketRecovery.missing(LaunchPlan.KESTREL,true).equals(Map.of("AR_FramePlate",4)),"Expendable Odyssey engines are lost while recovered Kestrel needs service plates");
  for(int t=0;t<=900;t++){double seconds=t/10.;check(Math.abs(FlightTimeline.upperX(LaunchPlan.KESTREL,seconds))+7<64&&FlightTimeline.upperX(LaunchPlan.ODYSSEY,seconds)==0,"Upper-stage path stays within each blueprint preflight clearance");}
  check(FlightTimeline.upperX(LaunchPlan.KESTREL,30)==0&&FlightTimeline.upperX(LaunchPlan.KESTREL,40)==-48,"Upper stage coasts away inside the preflight-cleared corridor");
  var t=new PropellantTank();reject(t::reserveRoundTrip,"No free launch");check(t.units()==0,"Failed launch cannot consume");
  for(int i=0;i<8;i++)t.fillCell();reject(t::reserveRoundTrip,"Reserve round trip before departure");check(t.units()==8000,"Failed preflight preserved tank");
  t.fillCell();check(t.reserveRoundTrip()==3000,"Finite exclusive return reserve");check(t.units()==0,"Six thousand outbound plus three thousand return paid once");reject(t::reserveRoundTrip,"Cannot double spend");
  t.cancelReservation();check(t.units()==9000,"Cancelled preflight restores exact original payment");
  t.drainCell();check(t.units()==8000,"A drained cell is exactly 1000 units");t.fillCell();
  var empty=new PropellantTank();reject(empty::drainCell,"Cannot create a cell from an empty tank");check(empty.units()==0,"Rejected drain preserves empty tank");
  for(int i=0;i<7;i++)t.fillCell();check(!t.canFill(),"Capacity bounded");reject(t::fillCell,"No overflow");check(t.units()==16000,"Rejected fill preserves quantities");
  reject(()->new PropellantTank(-1),"Negative fuel");reject(()->new PropellantTank(16001),"Over-capacity fuel");
  var j=new Journey(UUID.randomUUID(),12.5,81,33.5,4000,Journey.Phase.OUTBOUND);check(Journey.restore(j.snapshot()).equals(j),"Paid outbound persisted");j=j.arrive();check(Journey.restore(j.snapshot()).equals(j),"Moon reserve persisted");var moon=j;reject(moon::arrive,"No duplicate arrival");j=j.beginReturn();check(j.returnUnits()==4000,"Only paid reserve returns");check(Journey.restore(j.snapshot()).equals(j),"Return intent persisted");
  reject(()->new Journey(UUID.randomUUID(),Double.NaN,0,0,4000,Journey.Phase.LUNE),"No invalid return coordinates");reject(()->Journey.restore("bad"),"Reject damaged saved state");
  var ascent=new Journey(UUID.randomUUID(),12.5,81,33.5,4000,Journey.Phase.OUTBOUND);var orbit=ascent.orbit();check(Journey.restore(orbit.snapshot()).equals(orbit),"Paid orbit phase retains home and protected reserve");check(orbit.beginReturn().returnUnits()==4000,"Orbital return consumes the same paid reserve");check(orbit.arrive().phase()==Journey.Phase.LUNE,"Orbit can select the first moon without a second fuel charge");reject(orbit::orbit,"Orbit entry cannot repeat");check(FlightTimeline.ORBIT>=40&&FlightTimeline.BOOSTER_LANDED<FlightTimeline.ORBIT,"Reusable booster returns before orbital map access");check(FlightTimeline.height(0)==0&&FlightTimeline.height(4)==0&&FlightTimeline.height(40)==105,"Ignition holds the rocket before sustained ascent");check(FlightTimeline.boosterHeight(84)==0&&FlightTimeline.boosterX(84)==0,"Booster returns to its original pad");
  check(FlightTimeline.SEPARATION>FlightTimeline.SPACE&&FlightTimeline.FREEFALL>FlightTimeline.SEPARATION,"Separation occurs after reaching black sky, before the coast and fall");
  check(FlightTimeline.boosterHeight(50)<FlightTimeline.boosterHeight(45)&&FlightTimeline.boosterHeight(50)>FlightTimeline.boosterHeight(60),"Ballistic booster falls toward Orbis");
  double v0=(FlightTimeline.boosterHeight(64)-FlightTimeline.boosterHeight(63.99))/.01,v1=(FlightTimeline.boosterHeight(64.01)-FlightTimeline.boosterHeight(64))/.01;
  check(Math.abs(v0-v1)<.01&&Math.abs(FlightTimeline.boosterHeight(83.99)/.01)<.01,"Landing burn joins falling velocity and brakes to zero at the pad");
  check(Math.abs(FlightTimeline.boosterRoll(64)-Math.PI*2)<1e-8&&!FlightTimeline.boosterPowered(50)&&FlightTimeline.boosterPowered(70),"Booster aligns upright before its actual braking burn");
  check(FlightTimeline.descentHeight(0)==90&&FlightTimeline.descentHeight(24)==0&&FlightTimeline.returnHeight(28)==90,"Upper-stage descent lands and return ascent reaches transfer altitude");
  int lo=999,hi=-999;for(int x=1000;x<1800;x+=3)for(int z=1000;z<1800;z+=3){int h=LunarSurface.height(91023,x,z);lo=Math.min(lo,h);hi=Math.max(hi,h);check(Math.abs(h-LunarSurface.height(91023,x+1,z))<=3&&Math.abs(h-LunarSurface.height(91023,x,z+1))<=3,"Continuous lunar relief across voxel and chunk boundaries");}
  check(hi-lo>=25,"Lune has broad crater relief rather than a flat sheet");
  check(LunarSurface.height(91023,16,16)==64,"Original lunar landing plateau remains compatible");
  var berths=new java.util.HashSet<String>();for(int slot=0;slot<4096;slot++){int x=LunarSurface.landingX(slot),z=LunarSurface.landingZ(slot);check(berths.add(x+","+z)&&Math.max(Math.abs(x),Math.abs(z))<14000,"Unique bounded lunar berths avoid far-coordinate jitter");}
  for(int slot:new int[]{0,1,123,4095}){int x=LunarSurface.landingX(slot),z=LunarSurface.landingZ(slot),h=LunarSurface.height(91023,x,z);for(int dx=-12;dx<=14;dx++)for(int dz=-8;dz<=8;dz++)if(Math.hypot(dx,dz)<=18)check(LunarSurface.height(91023,x+dx,z+dz)==h,"Owned landing legs and exit use a small level regolith clearing");}

  var newTrip=new Journey(UUID.randomUUID(),1,80,1,3000,Journey.Phase.OUTBOUND);check(newTrip.reservedUnits()==9000&&Journey.restore(newTrip.snapshot()).returnUnits()==3000,"Asymmetric route persists exact new cost");
  var oldTrip=new Journey(UUID.randomUUID(),1,80,1,4000,Journey.Phase.OUTBOUND);check(oldTrip.reservedUnits()==8000,"Existing paid trip keeps its historical contract");var legacyTank=new PropellantTank();legacyTank.cancelReservation(oldTrip.reservedUnits());check(legacyTank.units()==8000,"Legacy abort refunds only original eight thousand");
  int tubes=0,skylights=0;for(int x=-64;x<800;x+=2)for(int z=0;z<700;z+=2){int h=LunarSurface.height(91023,x,z);var c=LunarTubes.column(91023,x,z,h);if(c.present()){tubes++;check(c.floor()>=8&&c.ceiling()-c.floor()>=4,"Walkable tube with preserved bedrock");if(c.skylight())skylights++;else check(c.ceiling()<=h-5,"Intact vaulted roofs remain underground");}if(LunarTubes.protectedSite(x,z))check(!c.present(),"Landing and original landing plateau cannot be hollowed");}
  check(tubes>100&&skylights>20,"Deterministic lunar channels include collapsed skylights");
  for(var facing:LaunchPadLayout.Facing.values()){
   var console=new LaunchPadLayout.Cell(-19,80,33);var center=LaunchPadLayout.center(console,facing);var occupied=LaunchPadLayout.occupiedCells(center);
   check(LaunchPadLayout.modules(center).size()==25&&occupied.size()==625&&new HashSet<>(occupied).size()==625,"Joined modules cover exactly625 distinct physical cells");
   check(!LaunchPadLayout.contains(center,console)&&occupied.stream().allMatch(c->LaunchPadLayout.contains(center,c)),"Separate console remains outside the full25-wide footprint in every facing");
  }
  for(var plan:LaunchPlan.values()){
   check(LaunchPadLayout.supports(plan)&&plan.serviceMargin()>2,"Pad is wider than the complete rocket including boosters/fins");
   var build=new ConstructionProgress(plan);check(!build.start(true,true),"A ghost grants no free rocket");
   for(var e:plan.materials().entrySet())build.accept(e.getKey(),e.getValue());
   var supplied=build.supplied();var paidBuild=build;reject(()->paidBuild.accept("AR_FramePlate",1),"Cannot overpay a funded blueprint");check(build.supplied().equals(supplied),"Rejected overpayment preserves all supplied materials");
   reject(()->paidBuild.select(LaunchPlan.ODYSSEY),"Cannot switch away from paid escrow");check(!build.start(false,true)&&!build.start(true,false),"Incomplete platform or blocked clearance cannot start paid construction");
   check(build.start(true,true),"Exactly funded valid construction begins once");build.advance(4100,true);build.advance(999999,false);check(build.workedMillis()==4100,"Unloaded or invalid construction makes no progress");
   build=ConstructionProgress.restore(plan,build.supplied(),build.phase(),build.workedMillis(),false);check(build.workedMillis()==4100&&!build.claimCelebration(),"Paid partial construction reload retains work without completion effects");
   build.advance(Long.MAX_VALUE,true);check(build.due()&&!build.claimCelebration(),"Elapsed time caps without overflow; no effects before world commit");build.complete();check(build.claimCelebration()&&!build.claimCelebration(),"One committed construction produces one celebration");
   var ready=ConstructionProgress.restore(plan,build.supplied(),build.phase(),build.workedMillis(),build.celebrated());check(!ready.claimCelebration(),"Normal reload cannot replay completed fireworks");reject(ready::resetAfterRefund,"Completed rocket cannot also refund its construction materials");
  }
  check(!LaunchPlan.ODYSSEY.materials().containsKey("AR_ThermalShield")&&LaunchPlan.KESTREL.materials().get("AR_ThermalShield")==1,"First launch bootstraps at home; Kestrel uses the obtainable Lune upgrade");
  for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)for(boolean m:new boolean[]{false,true}){
   var veins=VeinPlan.plan(91023,x,z,m);check(veins.equals(VeinPlan.plan(91023,x,z,m)),"Reproducible vein seed");check(veins.size()==8,"Finite deposit budget");
   for(var v:veins){check(v.x()>=3&&v.x()<29&&v.z()>=3&&v.z()<29,"No cross-chunk writes");check(v.depth()>=5&&v.depth()<(m?15:23),"Mineable bounded depth");check(m?v.block().equals("AR_TitaniumOre")||v.block().equals("AR_AluminiumOre"):v.block().equals("AR_NickelOre")||v.block().equals("AR_OilShale"),"No circular home ore requirement");}
  }
  System.out.println("Astral Reach core: "+checks+" checks passed");
 }
}
