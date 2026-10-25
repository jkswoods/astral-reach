package dev.astralreach.core;

/** Gameplay oxygen budget in seconds of normal breathing, not a physical gas-volume simulation. */
public final class OxygenSupply {
 public static final double CAPACITY=1200,RADIUS=8,STATION_BASE=.3,STATION_PERSON=.6;
 private OxygenSupply(){}
 public static double rate(boolean submerged,boolean sprinting){return submerged?2:sprinting?1.5:1;}
 public static double stationRate(int people){if(people<0)throw new IllegalArgumentException("Negative occupants");return STATION_BASE+STATION_PERSON*people;}
 public static double remaining(double before,double seconds,double rate){if(!Double.isFinite(before)||before<0||before>CAPACITY||!Double.isFinite(seconds)||seconds<0||!Double.isFinite(rate)||rate<0)throw new IllegalArgumentException("Invalid oxygen budget");return Math.max(0,before-seconds*rate);}
}
