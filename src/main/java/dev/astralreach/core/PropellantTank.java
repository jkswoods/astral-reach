package dev.astralreach.core;

/** Exact finite conditioned hydrocarbon units. No elapsed-time production or implicit refills. */
public final class PropellantTank {
 public static final int CELL_UNITS=1000, CAPACITY=16000, OUTBOUND_COST=6000, RETURN_COST=3000, LEG_COST=RETURN_COST, ROUND_TRIP_COST=OUTBOUND_COST+RETURN_COST;
 private int units;
 public PropellantTank(){this(0);}
 public PropellantTank(int units){if(units<0||units>CAPACITY)throw new IllegalArgumentException("Invalid propellant storage");this.units=units;}
 public int units(){return units;}
 public boolean canFill(){return canFill(CELL_UNITS);}
 public boolean canFill(int charge){return charge>0&&charge<=CELL_UNITS&&units<=CAPACITY-charge;}
 public void fillCell(){fillCell(CELL_UNITS);}
 public void fillCell(int charge){if(!canFill(charge))throw new IllegalStateException("Propellant tank is full or cell charge is invalid");units=Math.addExact(units,charge);}
 public boolean canDrain(){return units>=CELL_UNITS;}
 public void drainCell(){drainCell(CELL_UNITS);}
 public void drainCell(int charge){if(charge<=0||charge>CELL_UNITS||units<charge)throw new IllegalStateException("Invalid or unavailable fuel charge");units-=charge;}
 public boolean canRoundTrip(){return canRoundTrip(FlightFuel.STANDARD);}
 public boolean canRoundTrip(FlightFuel quote){return units>=quote.total();}
 /** Called only after full native preflight. 6,000 is paid outbound; 3,000 becomes an exclusive return reserve. */
 public int reserveRoundTrip(){return reserveRoundTrip(FlightFuel.STANDARD);}
 public int reserveRoundTrip(FlightFuel quote){if(!canRoundTrip(quote))throw new IllegalStateException("Store "+quote.total()+" propellant units before launch");units-=quote.total();return quote.returning();}
 public void cancelReservation(){cancelReservation(ROUND_TRIP_COST);}
 public void cancelReservation(int paid){if(!FlightFuel.knownTotal(paid))throw new IllegalArgumentException("Unknown paid reservation");if(units>CAPACITY-paid)throw new IllegalStateException("Cannot restore into a changed tank");units+=paid;}
}
