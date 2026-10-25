package dev.astralreach.core;

import java.util.*;

/** Deterministic new-chunk-only deposits; mining and reload never invoke this generator. */
public final class VeinPlan {
 private VeinPlan(){}
 public record Vein(int x,int depth,int z,String block){}
 public static List<Vein> plan(long seed,int chunkX,int chunkZ,boolean moon){
  var r=new SplittableRandom(seed^((long)chunkX*0x632be59bd9b4e019L)^((long)chunkZ*0x9e3779b97f4a7c15L));
  var out=new ArrayList<Vein>();
  for(int i=0;i<8;i++)out.add(new Vein(3+r.nextInt(26),5+r.nextInt(moon?10:18),3+r.nextInt(26),moon?(i%2==0?"AR_TitaniumOre":"AR_AluminiumOre"):(i%2==0?"AR_NickelOre":"AR_OilShale")));
  return List.copyOf(out);
 }
}
