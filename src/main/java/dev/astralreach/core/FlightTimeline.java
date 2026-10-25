package dev.astralreach.core;

/** Seconds and world coordinates shared by craft, physical crew seats, VFX and camera. */
public final class FlightTimeline {
 public static final double BOARDING=3,IGNITION=7,SPACE=26,SEPARATION=30;
 public static final double FREEFALL=40,LANDING_BURN=64,BOOSTER_LANDED=84,ORBIT=90,ORBIT_VIEW=8;
 public static final double DESCENT=24,RETURN_ASCENT=28;
 private FlightTimeline(){}
 public static double ease(double t,double a,double b){double q=Math.clamp((t-a)/(b-a),0,1);return q*q*(3-2*q);}
 public static double height(double t){return 105*ease(t,IGNITION,SEPARATION);}
 public static double boosterHeight(double t){
  if(t<=SEPARATION)return height(t);
  if(t<FREEFALL)return 105;
  if(t<LANDING_BURN){double q=(t-FREEFALL)/(LANDING_BURN-FREEFALL);return 105-45*q*q;}
  if(t>=BOOSTER_LANDED)return 0;
  double q=(t-LANDING_BURN)/(BOOSTER_LANDED-LANDING_BURN);
  // Match the falling velocity, then progressively brake to zero at the pad.
  return (2*q*q*q-3*q*q+1)*60+(q*q*q-2*q*q+q)*(-75);
 }
 public static double boosterX(double t){return 24*ease(t,SEPARATION,FREEFALL)*(1-ease(t,58,80));}
 public static double boosterRoll(double t){return Math.PI*ease(t,SEPARATION,36)+Math.PI*ease(t,54,LANDING_BURN);}
 public static boolean boosterPowered(double t){return t>=BOARDING&&t<SEPARATION||t>=LANDING_BURN&&t<BOOSTER_LANDED;}
 public static double upperX(LaunchPlan plan,double t){return plan==LaunchPlan.KESTREL?-48*ease(t,SEPARATION,FREEFALL):0;}
 public static double upperBase(LaunchPlan p){return p==LaunchPlan.KESTREL?71.1885:67.325;}
 public static double upperHeight(LaunchPlan p){return p==LaunchPlan.KESTREL?52:30;}
 public static double landingClearance(LaunchPlan p){return p==LaunchPlan.KESTREL?3:9;}
 public static double crewHeight(LaunchPlan p){return p==LaunchPlan.KESTREL?38:20;}
 public static double descentHeight(double t){double q=Math.clamp(t/DESCENT,0,1);return 90*(1-q)*(1-q);}
 public static double returnHeight(double t){return 90*ease(t,IGNITION,RETURN_ASCENT);}
 public static String stage(double t,LaunchPlan plan){
  if(t<BOARDING)return "BOARDING · CREW SECURED";
  if(t<IGNITION)return "IGNITION";
  if(t<SPACE)return "ASCENT";
  if(t<SEPARATION)return "SPACE · STAGE SEPARATION";
  if(plan==LaunchPlan.KESTREL){
   if(t<FREEFALL)return "BOOSTER SEPARATION AND FLIP";
   if(t<54)return "BOOSTER · BALLISTIC FALL TO ORBIS";
   if(t<LANDING_BURN)return "BOOSTER · PAD ALIGNMENT";
   if(t<BOOSTER_LANDED)return "BOOSTER · LANDING BURN";
   if(t<86)return "BOOSTER · TOUCHDOWN";
  }
  return "ORBITAL INSERTION";
 }
}
