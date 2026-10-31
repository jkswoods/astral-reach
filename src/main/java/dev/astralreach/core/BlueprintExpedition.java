package dev.astralreach.core;

import java.util.Set;

/** Distinct finite field missions; only new chunks receive their original outposts. */
public final class BlueprintExpedition {
 public enum Kind {
  LIFE_SUPPORT("life_support_survey","Life Support Survey","High-Capacity Oxygen Tank","AR_LifeSupportArchive","AR_EmptyLargeOxygenCanister"),
  MOBILITY("surface_mobility_survey","Surface Mobility Survey","Lunar Rover","AR_RoverArchive","AR_LunarRover");
  public final String id,title,reward,archive,item;
  Kind(String id,String title,String reward,String archive,String item){this.id=id;this.title=title;this.reward=reward;this.archive=archive;this.item=item;}
  public static Kind from(String id){for(var k:values())if(k.id.equals(id))return k;throw new IllegalArgumentException("Unknown expedition");}
 }
 public record Site(int seed,int channel,int start,Kind kind){
  public String id(){return kind.id+":"+seed+":"+channel+":"+start;}
  public LunarExpedition.Site survey(){return new LunarExpedition.Site(seed,channel,start);}
  public LunarExpedition.Point record(int n){
   if(n<0||n>1)throw new IllegalArgumentException("Unknown field record");
   int z=start+(kind==Kind.LIFE_SUPPORT?80+n*64:272+n*128);
   int x=survey().axis(z)+(kind==Kind.LIFE_SUPPORT?64:112);
   return new LunarExpedition.Point(x,LunarSurface.height(seed,x,z)+1,z);
  }
  public boolean valid(){
   for(int n=0;n<2;n++){var p=record(n);for(int dx:new int[]{-6,0,6})for(int dz:new int[]{-5,0,5})if(LunarTubes.protectedSite(p.x()+dx,p.z()+dz)||Math.abs(LunarSurface.height(seed,p.x()+dx,p.z()+dz)-(p.y()-1))>6)return false;}
   return true;
  }
 }
 public static Site at(LunarExpedition.Site s,Kind kind){return new Site(s.seed(),s.channel(),s.start(),kind);}
 public static Site nearby(int seed,int x,int z,Kind kind){var base=LunarExpedition.candidate(seed,x,z);var s=at(base,kind);for(int i=0;i<64&&!s.valid();i++)s=new Site(seed,base.channel(),s.start()-1536,kind);return s;}
 public static String claim(Site s,int record){if(record<0||record>1)throw new IllegalArgumentException("Unknown field record");return s.id()+"/"+record;}
 public static boolean completed(Set<String>claims,Kind kind){for(String key:claims)if(key.matches(kind.id+":-?[0-9]+:-?[0-9]+:-?[0-9]+/0")&&claims.contains(key.substring(0,key.length()-1)+"1"))return true;return false;}
 public static boolean reconcile(Set<String>claims,Set<String>blueprints){boolean changed=false;for(var k:Kind.values())if(completed(claims,k))changed|=blueprints.add(k.id);return changed;}
 public static boolean affectedColumn(Site s,int x,int z){for(int n=0;n<2;n++){var p=s.record(n);if(Math.abs(x-p.x())<=6&&Math.abs(z-p.z())<=5)return true;}return false;}
 /** No live terrain replacement or regeneration: this is a pure new-chunk block recipe. */
 public static String block(Site s,int x,int y,int z){
  if(LunarTubes.protectedSite(x,z))return null;
  for(int n=0;n<2;n++){
   var p=s.record(n);int dx=x-p.x(),dz=z-p.z(),dy=y-(p.y()-1);
   if(Math.abs(dx)>6||Math.abs(dz)>5||dy< -7||dy>6)continue;
   if(dy<=0)return y>=LunarSurface.height(s.seed(),x,z)-1||dy==0?"AR_LuneBricks":null;
   if(dx==0&&dz==0&&dy==1)return s.kind().archive;
   boolean lab=s.kind()==Kind.LIFE_SUPPORT&&n==0;
   if(lab){
    if(dy==6)return "AR_LuneBricks";
    if(Math.abs(dx)==6||Math.abs(dz)==5){if(dz==-5&&Math.abs(dx)<=1&&dy<=4)return "Empty";return Math.abs(dx)==6&&Math.abs(dz)<=2&&dy>=2&&dy<=3?"AR_ExpeditionWindow":"AR_LuneBricks";}
    return "Empty";
   }
   if(dy==1&&Math.abs(dx)==6&&dz==4)return "AR_TrailMarker";
   if((Math.abs(dx)==6||dz==5)&&dy<=2)return "AR_LuneBricks";
   if(dz==3&&Math.abs(dx)<=3&&dy==1)return "AR_LuneBricks";
   return "Empty";
  }
  return null;
 }
 private BlueprintExpedition(){}
}
