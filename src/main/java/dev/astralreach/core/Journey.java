package dev.astralreach.core;

import java.util.UUID;

/** Finite route state, persisted with the player before native travel. */
public record Journey(UUID homeWorld,double x,double y,double z,int returnUnits,Phase phase) {
 public enum Phase { OUTBOUND, ORBIT, LUNE, RETURNING }
 public Journey {
  if(homeWorld==null||phase==null||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||(returnUnits!=PropellantTank.RETURN_COST&&returnUnits!=4000))
   throw new IllegalArgumentException("Invalid paid journey");
 }
 /** Existing 4,000-unit reservations keep their original paid contract. */
 public int reservedUnits(){return returnUnits==4000?8000:PropellantTank.ROUND_TRIP_COST;}
 public Journey orbit(){if(phase!=Phase.OUTBOUND)throw new IllegalStateException("Orbit follows ascent");return new Journey(homeWorld,x,y,z,returnUnits,Phase.ORBIT);}
 public Journey arrive(){if(phase!=Phase.OUTBOUND&&phase!=Phase.ORBIT)throw new IllegalStateException("Already arrived");return new Journey(homeWorld,x,y,z,returnUnits,Phase.LUNE);}
 public Journey beginReturn(){if(phase!=Phase.LUNE&&phase!=Phase.ORBIT)throw new IllegalStateException("Return only from space or Lune");return new Journey(homeWorld,x,y,z,returnUnits,Phase.RETURNING);}
 public String snapshot(){return String.join("|",homeWorld.toString(),Double.toString(x),Double.toString(y),Double.toString(z),Integer.toString(returnUnits),phase.name());}
 public static Journey restore(String encoded){String[] x=encoded.split("\\|",-1);if(x.length!=6)throw new IllegalArgumentException("Malformed journey");return new Journey(UUID.fromString(x[0]),Double.parseDouble(x[1]),Double.parseDouble(x[2]),Double.parseDouble(x[3]),Integer.parseInt(x[4]),Phase.valueOf(x[5]));}
}
