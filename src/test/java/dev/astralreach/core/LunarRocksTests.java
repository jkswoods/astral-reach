package dev.astralreach.core;
final class LunarRocksTests {
 static void run(){int[]found=new int[3];int rocky=0,quiet=0,rockyArea=0,quietArea=0;int[]rotations=new int[4];
  for(int x=1792;x<2176;x++)for(int z=-2560;z<-2176;z++){
   boolean rough=LunarSurface.rugged(91023,x,z);if(rough)rockyArea++;else quietArea++;
   int v=LunarRocks.variant(91023,x,z);if(v<0)continue;found[v]++;if(rough)rocky++;else quiet++;rotations[LunarRocks.rotation(91023,x,z)]++;
   int h=LunarSurface.height(91023,x,z);CoreTests.check(!LunarTubes.column(91023,x,z,h).skylight(),"Debris cannot bridge a collapsed skylight");
   CoreTests.check(!LunarTubes.protectedSite(x,z),"Landing protection excludes all lunar rocks");
   CoreTests.check(v==LunarRocks.variant(91023,x,z)&&LunarRocks.id(v).startsWith("AR_Lune"),"Stable original surface scatter at positive/negative chunk boundaries");
  }
  for(int n:found)CoreTests.check(n>20,"All three natural rock forms occur without a player fixture");for(int n:rotations)CoreTests.check(n>20,"Natural debris varies across all four yaw orientations");
  int total=found[0]+found[1]+found[2];CoreTests.check(total>200&&total<5000,"Debris leaves broad open walking space");CoreTests.check(rocky/(double)rockyArea>quiet/(double)quietArea,"Rock fields carry more debris than quiet plains");
  for(int slot:new int[]{0,1,123,4095}){int x=LunarSurface.landingX(slot),z=LunarSurface.landingZ(slot);for(int dx=-18;dx<=18;dx++)for(int dz=-18;dz<=18;dz++)if(Math.hypot(dx,dz)<=18)CoreTests.check(LunarRocks.variant(91023,x+dx,z+dz)<0,"Saved ship clearings stay free of new debris");}
 }
}
