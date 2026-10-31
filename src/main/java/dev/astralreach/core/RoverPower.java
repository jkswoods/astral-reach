package dev.astralreach.core;

/** Integer microseconds preserve finite cell energy through ticks and ordinary saves. */
public final class RoverPower {
 public static final long CELL=900_000_000L,CAPACITY=CELL*2;
 private long units;
 public RoverPower(long units){if(units<0||units>CAPACITY)throw new IllegalArgumentException("Invalid rover charge");this.units=units;}
 public long units(){return units;}
 public boolean canLoad(){return units<=CAPACITY-CELL;}
 public void load(){if(!canLoad())throw new IllegalArgumentException("A full cell will not fit; use more charge first");units+=CELL;}
 public double consume(double seconds,boolean moving){if(!Double.isFinite(seconds)||seconds<0)throw new IllegalArgumentException("Invalid rover time");if(!moving||seconds==0)return 1;long asked=Math.max(1,Math.round(seconds*1_000_000));long used=Math.min(units,asked);units-=used;return (double)used/asked;}
 public double seconds(){return units/1_000_000d;}
}
