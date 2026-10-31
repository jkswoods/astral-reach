package dev.astralreach.hytale;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3d;

/** Loaded terrain only: one-block steps, supported wheels and a conservative cabin volume. */
final class RoverTerrain {
 static int solid(World w,int x,int y,int z){var block=NativeBlocks.get(w,new RocketRegistry.Cell(x,y,z));return block==null?-1:block.getMaterial()==BlockMaterial.Solid?1:0;}
 static Vector3d ground(World w,Vector3d at,double yaw){
  double hi=-Double.MAX_VALUE,lo=Double.MAX_VALUE,c=Math.cos(yaw),s=Math.sin(yaw);
  for(double x:new double[]{-1,1})for(double z:new double[]{-1.08,1.08}){
   int px=(int)Math.floor(at.x+x*c+z*s),pz=(int)Math.floor(at.z-x*s+z*c),top=(int)Math.floor(at.y+1.01);double found=Double.NaN;
   for(int y=top;y>=Math.floor(at.y-2);y--){int block=solid(w,px,y,pz);if(block<0)return null;if(block==1){found=y+1;break;}}
   if(!Double.isFinite(found))return null;hi=Math.max(hi,found);lo=Math.min(lo,found);
  }
  if(hi-lo>1.01||hi-at.y>1.01||at.y-hi>1.01)return null;
  return new Vector3d(at.x,hi,at.z);
 }
 static boolean clear(World w,Vector3d at,double yaw){
  double c=Math.cos(yaw),s=Math.sin(yaw);
  for(double x:new double[]{-1.36,-.9,-.45,0,.45,.9,1.36})for(double z:new double[]{-1.64,-1.2,-.6,0,.6,1.2,1.64})for(double y:new double[]{.16,.70,1.3,1.95,2.06}){
   if(solid(w,(int)Math.floor(at.x+x*c+z*s),(int)Math.floor(at.y+y),(int)Math.floor(at.z-x*s+z*c))!=0)return false;
  }
  return solid(w,(int)Math.floor(at.x-.71*c-.55*s),(int)Math.floor(at.y+2.45),(int)Math.floor(at.z+.71*s-.55*c))==0;
 }
 static Vector3d exit(World w,Vector3d at,double yaw){
  for(double side:new double[]{-1.9,1.9,-2.5,2.5}){
   var next=new Vector3d(at).add(Math.cos(yaw)*side,0,-Math.sin(yaw)*side);int y=(int)Math.floor(at.y+1);
   while(y>=Math.floor(at.y-2)&&solid(w,(int)Math.floor(next.x),y,(int)Math.floor(next.z))==0)y--;
   if(solid(w,(int)Math.floor(next.x),y,(int)Math.floor(next.z))!=1)continue;next.y=y+1;boolean clear=true;
   for(double dx:new double[]{-.3,.3})for(double dz:new double[]{-.3,.3})for(double dy:new double[]{.1,.9,1.85})if(solid(w,(int)Math.floor(next.x+dx),(int)Math.floor(next.y+dy),(int)Math.floor(next.z+dz))!=0)clear=false;
   if(clear)return next;
  }
  return null;
 }
 private RoverTerrain(){}
}
