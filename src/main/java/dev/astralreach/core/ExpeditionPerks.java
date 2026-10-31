package dev.astralreach.core;
import java.util.*;

/** Stable expedition-type rewards. Generated copies of a mission never stack its permanent perk. */
public final class ExpeditionPerks {
 public static final String LOST_SURVEY="lost_survey_shelter";
 public static final String FUEL_TITLE="Trajectory Optimisation";
 private ExpeditionPerks(){}
 public static boolean completedSurvey(Set<String>claims){
  for(String key:claims)if(key.matches("-?[0-9]+:-?[0-9]+:-?[0-9]+/0")&&claims.contains(key.substring(0,key.length()-1)+"1"))return true;
  return false;
 }
 /** Also reconciles completed journals from builds predating permanent rewards. */
 public static boolean reconcile(Set<String>claims,Set<String>perks){return !perks.contains(LOST_SURVEY)&&completedSurvey(claims)&&perks.add(LOST_SURVEY);}
 public static int fuelReduction(Set<String>perks){return perks.contains(LOST_SURVEY)?5:0;}
 public static String description(Set<String>perks){return fuelReduction(perks)>0?FUEL_TITLE+" · 5% less rocket fuel":"Complete both archives at one site to unlock 5% less rocket fuel";}
}
