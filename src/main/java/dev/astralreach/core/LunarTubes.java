package dev.astralreach.core;

/** Ancient drained channels in world coordinates: vaulted roofs, broad floors and collapsed skylights. */
public final class LunarTubes {
 public record Column(int floor,int ceiling,boolean skylight){public boolean present(){return ceiling-floor>=4;}}
 private static final Column SOLID=new Column(0,0,false);
 private LunarTubes(){}
 public static boolean protectedSite(int x,int z){
  if(Math.hypot(x-16,z-16)<100)return true;
  int old=(int)Math.round((x-512)/192d);
  if(old>=0&&Math.hypot(x-(512+old*192),z-512)<34)return true;
  int gx=(int)Math.round((x-1024)/192d),gz=(int)Math.round((z-1024)/192d);
  return gx>=0&&gx<64&&gz>=0&&gz<64&&Math.hypot(x-(1024+gx*192),z-(1024+gz*192))<34;
 }
 public static double axis(int seed,int channel,double z){return channel*384+85+28*Math.sin(z/160+seed*.001)+12*Math.sin(z/71+channel);}
 public static Column column(int seed,int x,int z,int surface){
  if(protectedSite(x,z))return SOLID;
  int channel=(int)Math.round((x-85)/384d);
  double signed=x-axis(seed,channel,z),lateral=Math.abs(signed);
  double width=16+3*Math.sin(z/93+channel);
  if(lateral>=width)return SOLID;
  double q=lateral/width;
  int floor=42+(int)Math.round(2*Math.sin(z/141)+2*Math.sin(channel*2+z/321d));
  // A channel may run below a crater, but never remove the bottom eight blocks.
  floor=Math.min(floor,surface-10);floor=Math.max(8,floor);
  int vault=floor+3+(int)Math.round(17*Math.sqrt(1-q*q));
  int roof=Math.min(vault,surface-5);
  double along=Math.abs(Math.IEEEremainder(z-145-channel*37,512));
  boolean sky=along<90&&lateral<width-2;
  if(sky){
   // Slumped regolith forms a walkable ramp along the channel into the opening.
   double ramp=Math.max(0,(along-8)/82);
   // Keep the central channel connected beneath the collapsed roof. A side ramp reaches daylight.
   if(signed>5)floor+=(int)Math.round((surface-1-floor)*ramp);
   roof=surface+1;
  }
  return roof-floor<4?SOLID:new Column(floor,roof,sky);
 }
}
