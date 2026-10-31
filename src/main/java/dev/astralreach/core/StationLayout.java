package dev.astralreach.core;
import java.util.*;
/** Editable orbital plots and their protected linear nine-airlock concourse. */
public final class StationLayout {
 public static final int MAX_PLOTS=65536,SPACING=512,RADIUS=96,DECK_Y=200,MIN_Y=170,MAX_Y=319,MAX_CREW=8,PORTS=9,OWNER_PORT=4,CONCOURSE_Z=-104,PORT_Z=-114;
 public record Cell(int x,int y,int z){}
 public record Part(Cell at,String item,boolean quarterTurn,String state){public Part(Cell p,String id,boolean turn){this(p,id,turn,"default");}}
 private final int index,x,z,version;
 public StationLayout(int index){this(index,2);}
 public StationLayout(int index,int version){if(index<0||index>=MAX_PLOTS||version<1||version>2)throw new IllegalArgumentException("Invalid orbital station layout");this.index=index;this.version=version;x=(index%256-128)*SPACING+16;z=4096+(index/256)*SPACING+16;}
 public int index(){return index;}public int version(){return version;}public int x(){return x;}public int z(){return z;}
 public boolean contains(int px,int py,int pz){return py>=MIN_Y&&py<=MAX_Y&&Math.abs((long)px-x)<=RADIUS&&Math.abs((long)pz-z)<=RADIUS;}
 public boolean walkingContains(double px,double py,double pz){return py>=MIN_Y-10&&py<MAX_Y+8&&Math.abs(px-x)<128&&pz-z>-240&&pz-z<RADIUS+8;}
 public boolean insideConcourse(double px,double py,double pz){return Math.abs(px-x-.5)<100&&py>DECK_Y+1&&py<DECK_Y+5.5&&Math.abs(pz-z-CONCOURSE_Z-.5)<3.1;}
 public Cell core(){return new Cell(x,DECK_Y+1,z+(version==1?0:-90));}public Cell diffuser(){var c=core();return new Cell(c.x()-2,c.y(),c.z());}
 public Cell arrival(){var c=core();return new Cell(c.x(),DECK_Y+1,c.z()+2);}
 public boolean arrivalSpace(Cell at){var a=arrival();return at.x==a.x&&at.z==a.z&&at.y>=DECK_Y&&at.y<=DECK_Y+2;}
 public Cell port(int i){checkPort(i);return new Cell(x-96+i*24,DECK_Y,z+PORT_Z);}
 public Cell portArrival(int i){var p=port(i);return new Cell(p.x(),DECK_Y+1,p.z()+3);}
 public double contactX(int i){return port(i).x();}public double contactY(){return DECK_Y+3;}public double contactZ(){return z+PORT_Z-3;}
 public static void checkPort(int i){if(i<0||i>=PORTS)throw new IllegalArgumentException("Invalid docking port");}
 /** Original paid prototypes retain their exact Core, arrival and built blocks. */
 public List<Part> legacySeed(){var p=new LinkedHashMap<Cell,Part>();platform(p,0,0,4,false);
  for(int dx=5;dx<=8;dx++)for(int dz=-1;dz<=1;dz++)add(p,dx,0,dz,"AR_StationDeck",false);
  for(int dx=9;dx<=13;dx++)for(int dz=-2;dz<=2;dz++)add(p,dx,0,dz,"AR_StationDeck",false);
  for(int i=5;i<=8;i++){add(p,i,1,-1,"AR_StationRail",false);add(p,i,1,1,"AR_StationRail",false);}
  for(int i=9;i<=13;i++){add(p,i,1,-2,"AR_StationRail",false);add(p,i,1,2,"AR_StationRail",false);}for(int i=-1;i<=1;i++)add(p,13,1,i,"AR_StationRail",true);
  return List.copyOf(p.values());
 }
 /** The permanent exterior is outside the editable area; only small rear platforms enter it. */
 public List<Part> shell(){var p=new LinkedHashMap<Cell,Part>();
  for(int i=0;i<25;i++){String state=i==0?"West":i==24?"East":i==12?"Junction":i==4||i==20?"Entry":i%3==0?"Port":"default";add(p,-96+i*8,0,CONCOURSE_Z,"AR_StationConcourse",false,state);}
  for(int i=0;i<PORTS;i++)add(p,-96+i*24,0,PORT_Z,"AR_StationAirlock",false,String.format(java.util.Locale.ROOT,"D%02d",i+1));
  for(int dx:new int[]{-64,0,64}){platform(p,dx,-90,dx==0?4:3,true);int end=dx==0?-94:-93;for(int dz=-100;dz<end;dz++)for(int w=-1;w<=1;w++)add(p,dx+w,0,dz,"AR_StationDeck",false);for(int dz=-99;dz<end;dz++)for(int w:new int[]{-1,1})add(p,dx+w,1,dz,"AR_StationRail",true);}
  // An existing centre platform gets a narrow removable link; never replace a player's structures.
  if(version==1)for(int dz=-85;dz<=-4;dz++)for(int w=-1;w<=1;w++)add(p,w,0,dz,"AR_StationDeck",false);
  return List.copyOf(p.values());
 }
 public List<Part> seed(){var p=new LinkedHashMap<Cell,Part>();if(version==1)for(var a:legacySeed())p.put(a.at,a);for(var a:shell())p.putIfAbsent(a.at,a);var c=core();p.put(c,new Part(c,"AR_StationCore",false));var d=diffuser();p.put(d,new Part(d,"AR_OxygenDiffuser",false));return List.copyOf(p.values());}
 private void platform(Map<Cell,Part>p,int cx,int cz,int radius,boolean gate){for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)add(p,cx+dx,0,cz+dz,"AR_StationDeck",false);for(int i=-radius;i<=radius;i++){if(!gate||Math.abs(i)>1)add(p,cx+i,1,cz-radius,"AR_StationRail",false);add(p,cx+i,1,cz+radius,"AR_StationRail",false);if(Math.abs(i)<radius){add(p,cx-radius,1,cz+i,"AR_StationRail",true);if(Math.abs(i)>1||gate)add(p,cx+radius,1,cz+i,"AR_StationRail",true);}}}
 private void add(Map<Cell,Part>p,int dx,int dy,int dz,String id,boolean turn){add(p,dx,dy,dz,id,turn,"default");}
 private void add(Map<Cell,Part>p,int dx,int dy,int dz,String id,boolean turn,String state){var c=new Cell(x+dx,DECK_Y+dy,z+dz);if(p.putIfAbsent(c,new Part(c,id,turn,state))!=null)throw new IllegalStateException("Overlapping station seed "+c);}
 public static boolean allowed(UUID owner,Set<UUID>crew,UUID player){return owner.equals(player)||crew.contains(player);}
}
