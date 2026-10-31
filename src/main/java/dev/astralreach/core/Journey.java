package dev.astralreach.core;
import java.util.UUID;

/** Finite route state. A paid quote stays immutable even if a perk is unlocked during the mission. */
public record Journey(UUID homeWorld,double x,double y,double z,int returnUnits,Phase phase,int outboundUnits) {
 public enum Phase { OUTBOUND, ORBIT, LUNE, RETURNING }
 public Journey {
  if(homeWorld==null||phase==null||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Invalid paid journey");
  new FlightFuel(outboundUnits,returnUnits);
 }
 /** Source/saved compatibility for historical 6,000/3,000 and 4,000/4,000 contracts. */
 public Journey(UUID homeWorld,double x,double y,double z,int returnUnits,Phase phase){this(homeWorld,x,y,z,returnUnits,phase,FlightFuel.legacy(returnUnits).outbound());}
 public int reservedUnits(){return outboundUnits+returnUnits;}
 public Journey orbit(){if(phase!=Phase.OUTBOUND)throw new IllegalStateException("Orbit follows ascent");return new Journey(homeWorld,x,y,z,returnUnits,Phase.ORBIT,outboundUnits);}
 public Journey arrive(){if(phase!=Phase.OUTBOUND&&phase!=Phase.ORBIT)throw new IllegalStateException("Already arrived");return new Journey(homeWorld,x,y,z,returnUnits,Phase.LUNE,outboundUnits);}
 public Journey beginReturn(){if(phase!=Phase.LUNE&&phase!=Phase.ORBIT)throw new IllegalStateException("Return only from space or Lune");return new Journey(homeWorld,x,y,z,returnUnits,Phase.RETURNING,outboundUnits);}
 public String snapshot(){return String.join("|",homeWorld.toString(),Double.toString(x),Double.toString(y),Double.toString(z),Integer.toString(returnUnits),phase.name(),Integer.toString(outboundUnits));}
 public static Journey restore(String encoded){String[]x=encoded.split("\\|",-1);if(x.length!=6&&x.length!=7)throw new IllegalArgumentException("Malformed journey");return new Journey(UUID.fromString(x[0]),Double.parseDouble(x[1]),Double.parseDouble(x[2]),Double.parseDouble(x[3]),Integer.parseInt(x[4]),Phase.valueOf(x[5]),x.length==6?FlightFuel.legacy(Integer.parseInt(x[4])).outbound():Integer.parseInt(x[6]));}
}
