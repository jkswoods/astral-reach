package dev.astralreach.core;

/** Keep the existing paid-journey gates independent of a client screen's selected state. */
public final class DestinationAccess {
 private DestinationAccess(){}
 public static boolean canTravel(String body,boolean paidOrbit,int suitTier){
  if(!paidOrbit)return false;
  return "Orbis".equals(body)||"Lune".equals(body)&&suitTier>=1;
 }
}
