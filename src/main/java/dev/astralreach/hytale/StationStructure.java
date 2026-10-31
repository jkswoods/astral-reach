package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.*;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;
import java.util.*;
import java.util.concurrent.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;
/** Additive exterior installation; player structures inside the construction plot are never replaced. */
final class StationStructure {
 private static final Set<UUID>pending=ConcurrentHashMap.newKeySet();
 private static final Map<UUID,Long>retryAt=new HashMap<>();
 static CompletableFuture<Void>load(World w,List<StationLayout.Part>parts){var chunks=new LinkedHashSet<String>();var futures=new ArrayList<CompletableFuture<?>>();for(var p:parts)for(int dx:(p.item().equals("AR_StationAirlock")?new int[]{-32,0,32,64}:new int[]{-14,14}))for(int dz:(p.item().equals("AR_StationAirlock")?new int[]{-104,-72,-40,-8,8}:new int[]{-4,8})){int x=p.at().x()+dx,z=p.at().z()+dz;String key=Math.floorDiv(x,32)+","+Math.floorDiv(z,32);if(chunks.add(key))futures.add(RocketService.load(w,x,z));}return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));}
 static boolean put(World w,StationLayout.Part part){var at=StationService.cell(part.at());var block=BlockType.getAssetMap().getAsset(part.item());if(block==null)return false;var target=part.state().equals("default")?block:block.getBlockForState(part.state());if(target==null||!NativeBlocks.set(w,at,target))return false;if(part.quarterTurn()){var grid=w.getChunkStore();var ref=grid.getChunkSectionReferenceAtBlock(at.x(),at.y(),at.z());BlockOperations.setBlock(grid,ref,at.x(),at.y(),at.z(),BlockType.getAssetMap().getIndex(target.getId()),target,RotationTuple.index(Rotation.Ninety,Rotation.None,Rotation.None),0,198);}return true;}
 static boolean ready(World w,StationRegistry.Station st,int port){return st.shellVersion==2&&NativeBlocks.base(NativeBlocks.get(w,StationService.cell(st.layout.port(port)))).equals("AR_StationAirlock");}
 static void ensure(Store<EntityStore>s,StationRegistry.Station st){if(st.shellVersion==2||System.nanoTime()<retryAt.getOrDefault(st.owner,0L)||!pending.add(st.owner))return;var w=s.getExternalData().getWorld();var parts=st.layout.shell();load(w,parts).whenComplete((v,error)->w.execute(()->{pending.remove(st.owner);if(error!=null){retryAt.put(st.owner,System.nanoTime()+10_000_000_000L);return;}try{
   // Preflight the entire protected exterior before writing. Existing interior builds are retained.
   for(var p:parts){var at=StationService.cell(p.at());if(NativeBlocks.empty(w,at)||NativeBlocks.base(NativeBlocks.get(w,at)).equals(p.item())||st.layout.contains(at.x(),at.y(),at.z()))continue;throw new IllegalArgumentException("The protected concourse footprint is obstructed.");}
   for(var p:parts){var at=StationService.cell(p.at());String id=NativeBlocks.base(NativeBlocks.get(w,at));if(!NativeBlocks.empty(w,at)&&!id.equals(p.item()))continue;if(!put(w,p))throw new IllegalStateException("Concourse placement is incomplete");}
   st.shellVersion=2;
  }catch(RuntimeException ex){retryAt.put(st.owner,System.nanoTime()+10_000_000_000L);for(var p:w.getPlayerRefs())if(p.getUuid().equals(st.owner))AstralReachPlugin.message(p,ex.getMessage()+" Existing builds and return fuel are preserved.");}}));}
 static int portAt(StationRegistry.Station st,Cell at){for(int i=0;i<StationLayout.PORTS;i++)if(StationService.cell(st.layout.port(i)).equals(at))return i;return -1;}
 private StationStructure(){}
}
