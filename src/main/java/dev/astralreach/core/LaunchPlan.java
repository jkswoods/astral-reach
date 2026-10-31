package dev.astralreach.core;

import java.util.Map;

/** Saved blueprint identities and measured original art profiles; all currently use the paid Lune route. */
public enum LaunchPlan {
 ODYSSEY("Odyssey Heavy","AR_OdysseyHeavy","Odyssey",16.96,97.325,8.615,67.325,30,20,9,
   Map.of("AR_PressureHullPart",6,"AR_OrbitalEnginePart",4,"AR_FlightComputer",1,"AR_FramePlate",16)),
 KESTREL("Kestrel Stack","AR_KestrelStack","Kestrel",19.3109,123.1885,12.9,71.1885,52,38,3,
   Map.of("AR_PressureHullPart",6,"AR_OrbitalEnginePart",6,"AR_FlightComputer",2,"AR_FramePlate",24,"AR_ThermalShield",1)),
 SATURN_V("Saturn V","AR_SaturnV","SaturnV",11.5,110.6245,11.5,81.6245,29.0,24,3,
   Map.of("AR_PressureHullPart",8,"AR_OrbitalEnginePart",5,"AR_FlightComputer",1,"AR_FramePlate",24)),
 SOYUZ("Soyuz","AR_Soyuz","Soyuz",9.11,48.8988,9.11,37.39875,11.5,5,3,
   Map.of("AR_PressureHullPart",4,"AR_OrbitalEnginePart",5,"AR_FlightComputer",1,"AR_FramePlate",12)),
 SPACE_SHUTTLE("Space Shuttle","AR_SpaceShuttle","SpaceShuttle",19.992,55.5612,18.0008,4.56125,37.5,30,3,
   Map.of("AR_PressureHullPart",6,"AR_OrbitalEnginePart",5,"AR_FlightComputer",2,"AR_FramePlate",24));

 public final String title,artId,assetPrefix;
 public final double width,height,depth,upperBase,upperHeight,crewHeight,landingClearance;
 private final Map<String,Integer> materials;
 LaunchPlan(String title,String artId,String assetPrefix,double width,double height,double depth,double upperBase,double upperHeight,double crewHeight,double landingClearance,Map<String,Integer> materials){
  this.title=title;this.artId=artId;this.width=width;this.height=height;this.depth=depth;this.materials=Map.copyOf(materials);this.assetPrefix=assetPrefix;this.upperBase=upperBase;this.upperHeight=upperHeight;this.crewHeight=crewHeight;this.landingClearance=landingClearance;
 }
 public String asset(String suffix){return "AR_"+assetPrefix+suffix;}
 public static LaunchPlan fromArt(String id){for(var p:values())if(p.artId.equals(id))return p;return null;}
 public Map<String,Integer> materials(){return materials;}
 public double serviceMargin(){return (LaunchPadLayout.WIDTH-Math.max(width,depth))/2;}
 public int sweptHeight(){return (int)Math.ceil(height+105);}
 public static LaunchPlan restore(String id){try{return valueOf(id);}catch(RuntimeException e){throw new IllegalArgumentException("Unknown rocket blueprint",e);}}
}
