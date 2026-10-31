package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;import com.hypixel.hytale.codec.builder.BuilderCodec;import com.hypixel.hytale.component.Resource;import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;import org.bson.*;import java.util.*;import dev.astralreach.core.StationPower;
/** Normal native resource persistence; transient grants never survive a reload. */
public final class StationSystemsRegistry implements Resource<EntityStore>{
 static final class Device{final UUID owner;final String type;double charge;boolean granted,previous;Device(UUID o,String t){owner=o;type=t;}}
 final Map<RocketRegistry.Cell,Device>devices=new TreeMap<>(Comparator.comparing(RocketRegistry.Cell::key));float elapsed;
 public static final BuilderCodec<StationSystemsRegistry>CODEC=BuilderCodec.builder(StationSystemsRegistry.class,StationSystemsRegistry::new).append(new KeyedCodec<>("SystemsV1",Codec.STRING),StationSystemsRegistry::restore,StationSystemsRegistry::snapshot).add().build();
 public StationSystemsRegistry(){}
 String snapshot(){var all=new BsonDocument();for(var e:devices.entrySet()){var d=e.getValue();all.put(e.getKey().key(),new BsonDocument("Owner",new BsonString(d.owner.toString())).append("Type",new BsonString(d.type)).append("Charge",new BsonDouble(d.charge)));}return all.toJson();}
 void restore(String text){devices.clear();if(text==null||text.isEmpty())return;for(var e:BsonDocument.parse(text).entrySet()){var v=e.getValue().asDocument();var d=new Device(UUID.fromString(v.getString("Owner").getValue()),v.getString("Type").getValue());d.charge=v.getNumber("Charge").doubleValue();if(!StationSystems.TYPES.contains(d.type)||!Double.isFinite(d.charge)||d.charge<0||d.charge>(d.type.equals("AR_StationBattery")?StationPower.CAPACITY:0))throw new IllegalArgumentException("Invalid saved station power device");devices.put(RocketRegistry.Cell.parse(e.getKey()),d);}}
 @Override public Resource<EntityStore>clone(){var d=new StationSystemsRegistry();d.restore(snapshot());return d;}
}
