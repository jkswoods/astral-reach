package dev.astralreach.core;

/** Exact finite conditioned hydrocarbon units. No elapsed-time production or implicit refills. */
public final class PropellantTank {
 public static final int CELL_UNITS=1000, CAPACITY=16000, OUTBOUND_COST=6000, RETURN_COST=3000, LEG_COST=RETURN_COST, ROUND_TRIP_COST=OUTBOUND_COST+RETURN_COST;
 private int units;
 public PropellantTank(){this(0);}
 public PropellantTank(int units){if(units<0||units>CAPACITY)throw new IllegalArgumentException("Invalid propellant storage");this.units=units;}
 public int units(){return units;}
 public boolean canFill(){return units<=CAPACITY-CELL_UNITS;}
 public void fillCell(){if(!canFill())throw new IllegalStateException("Propellant tank is full");units=Math.addExact(units,CELL_UNITS);}
 public boolean canDrain(){return units>=CELL_UNITS;}
 public void drainCell(){if(!canDrain())throw new IllegalStateException("Less than one cell of stored propellant");units-=CELL_UNITS;}
 public boolean canRoundTrip(){return units>=ROUND_TRIP_COST;}
 /** Called only after full native preflight. 6,000 is paid outbound; 3,000 becomes an exclusive return reserve. */
 public int reserveRoundTrip(){if(!canRoundTrip())throw new IllegalStateException("Store 9 filled propellant cells before launch");units-=ROUND_TRIP_COST;return LEG_COST;}
 public void cancelReservation(){cancelReservation(ROUND_TRIP_COST);}
 public void cancelReservation(int paid){if(paid!=ROUND_TRIP_COST&&paid!=8000)throw new IllegalArgumentException("Unknown paid reservation");if(units>CAPACITY-paid)throw new IllegalStateException("Cannot restore into a changed tank");units+=paid;}
}
