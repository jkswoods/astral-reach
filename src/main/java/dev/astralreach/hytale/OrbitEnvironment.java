package dev.astralreach.hytale;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import java.util.*;
/** Replace only Astral Reach's original single-layer staging backdrop; retain builds and Y200 staging. */
final class OrbitEnvironment {
 private static final Map<World,Work>work=new WeakHashMap<>();
 private static final class Work{final ArrayDeque<Long>chunks=new ArrayDeque<>();boolean loading;long retry;}
 static void tick(Store<EntityStore>s){var w=s.getExternalData().getWorld();var data=StationService.data(s);if(data.skyVersion>=1)return;var job=work.computeIfAbsent(w,k->new Work());if(job.loading||System.nanoTime()<job.retry)return;
  try{
   var provider=w.getWorldConfig().getWorldGenProvider();if(provider instanceof FlatWorldGenProvider flat){var json=FlatWorldGenProvider.CODEC.encode(flat,new ExtraInfo());var layers=json.getArray("Layers");if(layers.size()!=1||layers.getFirst().asDocument().getInt32("From").getValue()!=0||layers.getFirst().asDocument().getInt32("To").getValue()!=1||!layers.getFirst().asDocument().getString("BlockType").getValue().equals("Rock_Stone")){data.skyVersion=1;return;}var empty=new VoidWorldGenProvider();w.getChunkStore().setGenerator(empty.getGenerator());w.getWorldConfig().setWorldGenProvider(empty);w.getWorldConfig().markChanged();
   }else if(!(provider instanceof VoidWorldGenProvider)){data.skyVersion=1;return;}
   if(job.chunks.isEmpty()){var indexes=w.getChunkStore().getSaver().getIndexes();for(long index:indexes)job.chunks.add(index);if(job.chunks.isEmpty()){data.skyVersion=1;work.remove(w);return;}}
   long index=job.chunks.peek();int x=ChunkUtil.xOfChunkIndex(index)*32,z=ChunkUtil.zOfChunkIndex(index)*32;job.loading=true;
   RocketService.load(w,x,z).whenComplete((v,error)->w.execute(()->{job.loading=false;if(error!=null){job.retry=System.nanoTime()+20_000_000_000L;return;}clearOriginalLayer(w,x,z);job.chunks.poll();if(job.chunks.isEmpty()){data.skyVersion=1;work.remove(w);}}));
  }catch(Exception error){job.retry=System.nanoTime()+20_000_000_000L;}
 }
 private static void clearOriginalLayer(World w,int x,int z){
  // A changed cell, a filler or anything built immediately above preserves the entire column.
  for(int dx=0;dx<32;dx++)for(int dz=0;dz<32;dz++){var p=new RocketRegistry.Cell(x+dx,0,z+dz);String id=NativeBlocks.base(NativeBlocks.get(w,p));if(!id.equals("Rock_Stone")&&!id.equals("Empty")||NativeBlocks.filler(w,p)!=0||!NativeBlocks.empty(w,new RocketRegistry.Cell(x+dx,1,z+dz)))return;}
  var empty=BlockType.getAssetMap().getAsset("Empty");for(int dx=0;dx<32;dx++)for(int dz=0;dz<32;dz++){var p=new RocketRegistry.Cell(x+dx,0,z+dz);if(NativeBlocks.base(NativeBlocks.get(w,p)).equals("Rock_Stone"))NativeBlocks.set(w,p,empty);}
 }
 private OrbitEnvironment(){}
}
