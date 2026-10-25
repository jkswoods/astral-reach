package dev.astralreach.hytale;

import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.worldgen.*;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.*;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import dev.astralreach.core.VeinPlan;
import dev.astralreach.core.LunarSurface;
import dev.astralreach.core.LunarTubes;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;

/** Decorates only first generation, never a loaded/saved player chunk. No existing world is enrolled. */
public final class OreWorldProvider implements IWorldGenProvider {
 public static final BuilderCodec<OreWorldProvider> CODEC=BuilderCodec.builder(OreWorldProvider.class,OreWorldProvider::new)
  .append(new KeyedCodec<>("Moon",Codec.BOOLEAN),(p,v)->p.moon=v,p->p.moon).add().build();
 private boolean moon;
 public OreWorldProvider(){}
 public OreWorldProvider(boolean moon){this.moon=moon;}
 @Override public IWorldGen getGenerator() throws WorldGenLoadException {
  IWorldGen base;
  if(moon){
   var layers=new FlatWorldGenProvider.Layer[]{new FlatWorldGenProvider.Layer(0,1,null,"Rock_Stone"),new FlatWorldGenProvider.Layer(1,61,null,"AR_LuneStone"),new FlatWorldGenProvider.Layer(61,64,null,"AR_LuneRegolith")};
   base=new FlatWorldGenProvider(new Color((byte)130,(byte)139,(byte)153),layers).getGenerator();
  }else base=new HytaleWorldGenProvider().getGenerator();
  return new Decorated(base,moon);
 }
 private record Decorated(IWorldGen base,boolean moon) implements IWorldGen {
  @Override public WorldGenTimingsCollector getTimings(){return base.getTimings();}
  @Override public Transform[] getSpawnPoints(int seed){return moon?new Transform[]{new Transform(16.5,65,16.5)}:base.getSpawnPoints(seed);}
  @Override public ISpawnProvider getDefaultSpawnProvider(int seed){return moon?IWorldGen.super.getDefaultSpawnProvider(seed):base.getDefaultSpawnProvider(seed);}
  @Override public void shutdown(){base.shutdown();}
  @Override public CompletableFuture<GeneratedChunk> generate(int seed,long index,int x,int z,LongPredicate stillNeeded,Long2FloatFunction distance){
   return base.generate(seed,index,x,z,stillNeeded,distance).thenApply(c->{decorate(c.getBlockChunk(),seed,x,z,moon);return c;});
  }
 }
 static void decorate(GeneratedBlockChunk chunk,int seed,int x,int z,boolean moon){
  var map=BlockType.getAssetMap();
  if(moon){
   int stone=map.getIndex("AR_LuneStone"),dust=map.getIndex("AR_LuneRegolith");
   for(int lx=0;lx<32;lx++)for(int lz=0;lz<32;lz++){
    int height=LunarSurface.height(seed,x*32+lx,z*32+lz);
    for(int y=1;y<Math.max(64,height);y++)chunk.setBlock(lx,y,lz,y>=height?BlockType.EMPTY_ID:y>=height-3?dust:stone,0,0);
    var tube=LunarTubes.column(seed,x*32+lx,z*32+lz,height);
    if(tube.present())for(int y=tube.floor()+1;y<=tube.ceiling();y++)chunk.setBlock(lx,y,lz,BlockType.EMPTY_ID,0,0);
   }
  }
  for(var v:VeinPlan.plan(seed,x,z,moon)){
   int id=map.getIndex(v.block());if(id==Integer.MIN_VALUE)throw new IllegalStateException("Missing ore "+v.block());
   int surface=moon?LunarSurface.height(seed,x*32+v.x(),z*32+v.z()):chunk.getHeight(v.x(),v.z());int center=Math.max(4,Math.min(300,surface-v.depth()));
   for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int dy=-1;dy<=1;dy++){
    if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>2)continue;
    var current=map.getAsset(chunk.getBlock(v.x()+dx,center+dy,v.z()+dz));
    if(current==null)continue;String name=current.getId();
    boolean host=moon?name.equals("AR_LuneStone"):Set.of("Rock_Stone","Rock_Shale","Rock_Slate","Rock_Granite","Rock_Basalt","Rock_Sandstone").contains(name);
    if(host)chunk.setBlock(v.x()+dx,center+dy,v.z()+dz,id,0,0);
   }
  }
 }
}
