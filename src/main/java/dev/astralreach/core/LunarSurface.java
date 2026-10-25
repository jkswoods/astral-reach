package dev.astralreach.core;

/** Continuous world-space regolith relief. Only used when a new lunar chunk is generated. */
public final class LunarSurface {
 private LunarSurface(){}
 private static double smooth(double a,double b,double v){double q=Math.clamp((v-a)/(b-a),0,1);return q*q*(3-2*q);}
 private static long hash(int seed,int x,int z){long n=(long)seed^x*0x9e3779b97f4a7c15L^z*0xc2b2ae3d27d4eb4fL;n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
 private static double unit(long n){return (n>>>11)*0x1.0p-53;}
 private static double relief(int seed,double x,double z){
  double h=82+5*Math.sin(x/97+Math.sin(z/173))+3*Math.sin(z/61+x/211)+1.8*Math.sin((x+z)/31);
  int gx=(int)Math.floor(x/176),gz=(int)Math.floor(z/176);
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
   int cx=gx+dx,cz=gz+dz;long n=hash(seed,cx,cz);
   double px=(cx+.2+.6*unit(n))*176,pz=(cz+.2+.6*unit(hash(seed+7,cx,cz)))*176;
   double radius=37+unit(hash(seed+19,cx,cz))*40,r=Math.hypot(x-px,z-pz)/radius;
   if(r>=1.55)continue;
   double depth=11+radius*.13;
   // Smooth shallow floor, a steepening wall and a broad, rounded ejecta rim.
   h-=depth*(1-smooth(.18,1,r));
   h+=(3+radius*.025)*Math.exp(-Math.pow((r-1.04)/.16,2));
  }
  return h;
 }
 // Compact two-dimensional sites avoid float render precision loss at hundreds of thousands of blocks.
 public static int landingX(int slot){return 1024+Math.floorMod(slot,64)*192;}
 public static int landingZ(int slot){return 1024+Math.floorMod(slot/64,64)*192;}
 public static int height(int seed,int x,int z){
  double h=relief(seed,x,z);
  // Keep the original expedition's saved landing/beacon clear; never regenerate its chunks.
  double old=Math.hypot(x-16,z-16);h=64+(h-64)*smooth(32,96,old);
  // Preserve the legacy landing strip when generating adjacent old-site chunks.
  int legacy=(int)Math.round((x-512)/192d),oldX=512+legacy*192;double d=Math.hypot(x-oldX,z-512);
  if(legacy>=0&&d<28){double flat=Math.rint(relief(seed,oldX,512));h=flat+(h-flat)*smooth(18,28,d);}
  // Fresh missions use a compact grid; each clearing is only eighteen blocks across its radius.
  int gx=(int)Math.round((x-1024)/192d),gz=(int)Math.round((z-1024)/192d);
  if(gx>=0&&gx<64&&gz>=0&&gz<64){int sx=1024+gx*192,sz=1024+gz*192;d=Math.hypot(x-sx,z-sz);if(d<28){double flat=Math.rint(relief(seed,sx,sz));h=flat+(h-flat)*smooth(18,28,d);}}
  return (int)Math.round(Math.clamp(h,35,110));
 }
}
