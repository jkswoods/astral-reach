package dev.astralreach.core;
import java.util.*;
/** New-chunk-only lost survey sites, aligned to real connected lava channels. */
public final class LunarExpedition {
 public record Point(int x,int y,int z){}
 public record Site(int seed,int channel,int start){
  public String id(){return seed+":"+channel+":"+start;}
  public int axis(int z){return (int)Math.round(LunarTubes.axis(seed,channel,z));}
  public boolean valid(){for(int z=start;z<=start+512;z+=16)for(int dx:new int[]{-12,0,12})if(LunarTubes.protectedSite(axis(z)+dx,z))return false;var r=relay();return !LunarTubes.protectedSite(r.x(),r.z());}
  public Point relay(){int x=axis(start)+24;return new Point(x,LunarSurface.height(seed,x,start),start);}
  public Point shelter(){int z=start+275,x=axis(z);var t=LunarTubes.column(seed,x,z,LunarSurface.height(seed,x,z));return new Point(x,t.present()?t.floor():42,z);}
  public Point entrance(boolean south){int z=start+(south?512:0),x=axis(z);var t=LunarTubes.column(seed,x,z,LunarSurface.height(seed,x,z));return new Point(x,t.floor()+1,z);}
  public Point archive(int n){var p=shelter();return n==0?new Point(p.x()+3,p.y()+1,p.z()-5):new Point(p.x()-3,p.y()+1,p.z()+5);}
 }
 public static Site candidate(int seed,int x,int z){int channel=4*(int)Math.round((x-85)/1536d);int start=145+channel*37+1536*(int)Math.round((z-145-channel*37-256)/1536d);return new Site(seed,channel,start);}
 public static Site nearby(int seed,int x,int z){var site=candidate(seed,x,z);for(int i=0;!site.valid()&&i<32;i++)site=new Site(seed,site.channel(),site.start()-1536);return site;}
 public static boolean affectedColumn(Site s,int x,int z){var h=s.shelter();var r=s.relay();return Math.abs(x-h.x())<=5&&Math.abs(z-h.z())<=9||Math.abs(x-r.x())<=2&&Math.abs(z-r.z())<=2||z>=s.start()+225&&z<=s.start()+240&&Math.abs(x-s.axis(z))==14||z>s.start()+35&&z<s.start()+480&&Math.floorMod(z-s.start(),56)==0&&x==s.axis(z)+10;}
 /** Null preserves existing terrain. Air is intentional only inside the generated shelter. */
 public static String block(Site s,int x,int y,int z){
  if(LunarTubes.protectedSite(x,z))return null;
  var h=s.shelter();int dx=x-h.x(),dz=z-h.z(),dy=y-h.y();
  if(Math.abs(dx)<=5&&Math.abs(dz)<=9&&dy>=0&&dy<=7){
   if(dy==0||dy==7)return "AR_LuneBricks";
   if(Math.abs(dx)==5||Math.abs(dz)==9){if(Math.abs(dz)==9&&Math.abs(dx)<=1&&dy<=4)return "Empty";return dy>=3&&dy<=4&&Math.abs(dz)<7&&Math.abs(dx)==5?"AR_ExpeditionWindow":"AR_LuneBricks";}
   for(int i=0;i<2;i++)if(new Point(x,y,z).equals(s.archive(i)))return "AR_ResearchArchive";
   if(dy==1&&Math.abs(dx)==4&&Math.abs(dz)<=6)return "AR_LuneBricks";
   return "Empty";
  }
  var r=s.relay();int rx=x-r.x(),rz=z-r.z(),ry=y-r.y();
  if(Math.abs(rx)<=2&&Math.abs(rz)<=2){if(y>=Math.min(LunarSurface.height(s.seed(),x,z)-1,r.y()-1)&&ry<=0)return "AR_LuneBricks";if(rx==0&&rz==0&&ry==1)return "AR_DamagedRelay";if(ry>=1&&ry<=3)return "Empty";}
  // Recoverable ore exposed on natural cave walls; mined blocks do not regenerate.
  if(z>=s.start()+225&&z<=s.start()+240&&Math.abs(x-s.axis(z))==14){var t=LunarTubes.column(s.seed(),x,z,LunarSurface.height(s.seed(),x,z));if(t.present()&&y==t.floor()+1&&z%3==0)return x<s.axis(z)?"AR_TitaniumOre":"AR_AluminiumOre";}
  if(z>s.start()+35&&z<s.start()+480&&Math.floorMod(z-s.start(),56)==0&&x==s.axis(z)+10){var t=LunarTubes.column(s.seed(),x,z,LunarSurface.height(s.seed(),x,z));if(t.present()&&y==t.floor()+1)return "AR_TrailMarker";}
  return null;
 }
 public static Map<String,Integer>reward(int archive){return archive==0?Map.of("AR_TitaniumIngot",2,"AR_FramePlate",2):Map.of("AR_FramePlate",4,"AR_FlightComputer",1);}
 private LunarExpedition(){}
}
