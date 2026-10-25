package dev.astralreach.core;
import java.util.*;
/** Credits actual returned assemblies; expendable cores/solid boosters never receive a credit. */
public final class RocketRecovery{
 private RocketRecovery(){}
 public static Map<String,Integer> recovered(LaunchPlan plan,boolean boosterReturned){
  if(plan==LaunchPlan.ODYSSEY)return Map.of("AR_PressureHullPart",2,"AR_OrbitalEnginePart",1,"AR_FlightComputer",1,"AR_FramePlate",4);
  return boosterReturned?Map.of("AR_PressureHullPart",6,"AR_OrbitalEnginePart",6,"AR_FlightComputer",2,"AR_FramePlate",20,"AR_ThermalShield",1):Map.of("AR_PressureHullPart",3,"AR_OrbitalEnginePart",3,"AR_FlightComputer",1,"AR_FramePlate",8,"AR_ThermalShield",1);
 }
 public static Map<String,Integer> missing(LaunchPlan plan,boolean boosterReturned){var n=new HashMap<>(plan.materials());for(var e:recovered(plan,boosterReturned).entrySet())n.merge(e.getKey(),-e.getValue(),Integer::sum);n.values().removeIf(v->v==0);return Map.copyOf(n);}
}
