package dev.astralreach.core;
import java.util.Set;

/** Exact route quotes. The chosen contract is persisted at payment, independent of later perk changes. */
public record FlightFuel(int outbound,int returning) {
 public static final FlightFuel STANDARD=new FlightFuel(6000,3000),SURVEY=new FlightFuel(5700,2850),LEGACY=new FlightFuel(4000,4000);
 public FlightFuel{if(!known(outbound,returning))throw new IllegalArgumentException("Unknown paid fuel contract");}
 private static boolean known(int out,int back){return out==6000&&back==3000||out==5700&&back==2850||out==4000&&back==4000;}
 public int total(){return outbound+returning;}
 public int cellsNeeded(int stored){if(stored<0||stored>PropellantTank.CAPACITY)throw new IllegalArgumentException("Invalid stored fuel");return Math.ceilDiv(Math.max(0,total()-stored),PropellantTank.CELL_UNITS);}
 public static FlightFuel forPerks(Set<String>perks){return ExpeditionPerks.fuelReduction(perks)==5?SURVEY:STANDARD;}
 public static FlightFuel legacy(int returning){return switch(returning){case 3000->STANDARD;case 4000->LEGACY;default->throw new IllegalArgumentException("Unknown legacy return reserve");};}
 public static boolean knownTotal(int paid){return paid==9000||paid==8550||paid==8000;}
}
