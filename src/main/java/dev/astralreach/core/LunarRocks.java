package dev.astralreach.core;

/** Small deterministic impact fragments. Evaluated only for newly generated surface cells. */
public final class LunarRocks {
 private LunarRocks(){}
 private static long hash(int seed,int x,int z){long n=(long)seed+x*0x9e3779b97f4a7c15L+z*0xc2b2ae3d27d4eb4fL;n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
 /** -1 is open ground; other values identify the three original rubble models. */
 public static int variant(int seed,int x,int z){
  if(LunarTubes.protectedSite(x,z))return -1;
  long h=hash(seed+317,x,z);boolean patch=Math.floorMod(hash(seed+319,Math.floorDiv(x,24),Math.floorDiv(z,24)),100)<58;
  int chance=LunarSurface.rugged(seed,x,z)?(patch?34:9):(patch?9:2);
  if(Math.floorMod(h,1000)>=chance)return -1;
  int surface=LunarSurface.height(seed,x,z);
  // A model occupies one surface cell. Keep the ground beneath it, never bridge an opening or slope.
  if(LunarTubes.column(seed,x,z,surface).skylight())return -1;
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(Math.abs(LunarSurface.height(seed,x+dx,z+dz)-surface)>1)return -1;
  var site=LunarExpedition.candidate(seed,x,z);var relay=site.relay();
  if(Math.abs(x-relay.x())<=6&&Math.abs(z-relay.z())<=6)return -1;
  int v=(int)Math.floorMod(hash(seed+331,x,z),100);
  return v<55?0:v<87?1:2;
 }
 public static String id(int variant){return switch(variant){case 0->"AR_LunePebble";case 1->"AR_LuneRock";case 2->"AR_LuneRockCluster";default->throw new IllegalArgumentException("Unknown lunar rock");};}
 public static int rotation(int seed,int x,int z){return (int)Math.floorMod(hash(seed+337,x,z),4);}
}
