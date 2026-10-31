package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.universe.Universe;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.bson.BsonDocument;
/** Immutable owner identities only. No cross-world Store reads or orbital chunk loading. */
final class StationSkyCatalog {
 record Entry(UUID owner,String name,String ownerName,Set<UUID>crew){}
 private static volatile List<Entry>entries=List.of();
 static List<Entry>entries(){return entries;}
 private static List<Entry>snapshot(StationRegistry r){return r.stations.values().stream().sorted(Comparator.comparing(st->st.owner)).map(st->new Entry(st.owner,st.title(),st.name,Set.copyOf(st.crew.keySet()))).toList();}
 private static volatile List<UUID>owners=List.of();private static volatile long nextRead;private static boolean reading;private static long generation,lastPublish;
 static synchronized void publish(StationRegistry registry){long now=System.nanoTime();if(now-lastPublish<1_000_000_000L)return;lastPublish=now;generation++;{entries=snapshot(registry);owners=entries.stream().map(Entry::owner).toList();}}
 static List<UUID>owners(){return owners;}
 static synchronized void refresh(){long now=System.nanoTime();if(reading||now<nextRead||Universe.get().getWorld(SpaceFlight.ORBIT_WORLD)!=null)return;reading=true;long expected=generation;nextRead=now+30_000_000_000L;var file=Universe.get().getWorldsPath().resolve(SpaceFlight.ORBIT_WORLD).resolve("resources/AstralStationsV1.json");CompletableFuture.runAsync(()->{try{if(!Files.isRegularFile(file)){synchronized(StationSkyCatalog.class){if(generation==expected){owners=List.of();entries=List.of();}}return;}if(Files.size(file)>64*1024*1024)throw new IllegalArgumentException("Station registry exceeds bounded sky catalogue");var data=BsonDocument.parse(Files.readString(file));var registry=new StationRegistry();registry.restore(data.getString("StationsV1").getValue());synchronized(StationSkyCatalog.class){if(generation==expected){entries=snapshot(registry);owners=entries.stream().map(Entry::owner).toList();}}}catch(Exception ignored){/* A concurrent normal save may be incomplete; retain the last valid snapshot. */}finally{synchronized(StationSkyCatalog.class){reading=false;}}});}
 private StationSkyCatalog(){}
}
