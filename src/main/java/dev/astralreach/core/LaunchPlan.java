package dev.astralreach.core;

import java.util.Map;

/** The first two supported yard blueprints; destination travel remains the paid Lune route. */
public enum LaunchPlan {
 ODYSSEY("Odyssey Heavy","AR_OdysseyHeavy",16.96,97.325,8.615,
   Map.of("AR_PressureHullPart",6,"AR_OrbitalEnginePart",4,"AR_FlightComputer",1,"AR_FramePlate",16)),
 KESTREL("Kestrel Stack","AR_KestrelStack",19.3109,123.1885,12.9,
   Map.of("AR_PressureHullPart",6,"AR_OrbitalEnginePart",6,"AR_FlightComputer",2,"AR_FramePlate",24,"AR_ThermalShield",1));

 public final String title,artId;
 public final double width,height,depth;
 private final Map<String,Integer> materials;
 LaunchPlan(String title,String artId,double width,double height,double depth,Map<String,Integer> materials){
  this.title=title;this.artId=artId;this.width=width;this.height=height;this.depth=depth;this.materials=Map.copyOf(materials);
 }
 public Map<String,Integer> materials(){return materials;}
 public double serviceMargin(){return (LaunchPadLayout.WIDTH-Math.max(width,depth))/2;}
 public int sweptHeight(){return (int)Math.ceil(height+105);}
 public static LaunchPlan restore(String id){try{return valueOf(id);}catch(RuntimeException e){throw new IllegalArgumentException("Unknown rocket blueprint",e);}}
}
