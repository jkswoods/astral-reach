package dev.astralreach.core;
/** Conserves charge within a loaded station. No offline accrual or partial free consumer ticks. */
public final class StationPower{
 public static final double CAPACITY=2400,SOLAR=4,LOAD=3,CYCLE=720;
 public record Step(double stored,int supplied,double generated,double consumed){}
 public static boolean sunlit(double seconds){return ((seconds%CYCLE)+CYCLE)%CYCLE<CYCLE*.6;}
 public static Step advance(double stored,int batteries,int panels,int consumers,double seconds,boolean lit){
  if(!Double.isFinite(stored)||stored<0||batteries<0||panels<0||consumers<0||!Double.isFinite(seconds)||seconds<0||seconds>1)throw new IllegalArgumentException("Invalid power budget");
  double capacity=batteries*CAPACITY,generated=lit?panels*SOLAR*seconds:0,available=Math.min(stored,capacity)+generated,cost=LOAD*seconds;int n=cost==0?consumers:Math.min(consumers,(int)Math.floor((available+1e-9)/cost));double consumed=n*cost;
  return new Step(Math.clamp(available-consumed,0,capacity),n,generated,consumed);
 }
 private StationPower(){}
}
