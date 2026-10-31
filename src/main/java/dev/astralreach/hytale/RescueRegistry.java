package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;import com.hypixel.hytale.codec.builder.BuilderCodec;import com.hypixel.hytale.component.Resource;import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;import dev.astralreach.core.RescueBeacon;import org.bson.*;import java.util.*;
/** Native world persistence: invitations and absolute expiry, never a fresh battery on reload. */
public final class RescueRegistry implements Resource<EntityStore>{
 final Map<RocketRegistry.Cell,RescueBeacon>beacons=new HashMap<>();float elapsed;
 public static final BuilderCodec<RescueRegistry>CODEC=BuilderCodec.builder(RescueRegistry.class,RescueRegistry::new).append(new KeyedCodec<>("BeaconsV1",Codec.STRING),RescueRegistry::restore,RescueRegistry::snapshot).add().build();
 public RescueRegistry(){}
 String snapshot(){var all=new BsonDocument();for(var e:beacons.entrySet()){var b=e.getValue();var guests=new BsonDocument();b.guests().forEach((id,name)->guests.put(id.toString(),new BsonString(name)));all.put(e.getKey().key(),new BsonDocument("Owner",new BsonString(b.owner().toString())).append("Name",new BsonString(b.ownerName())).append("Expires",new BsonInt64(b.expires())).append("Enabled",BsonBoolean.valueOf(b.enabled())).append("Guests",guests));}return all.toJson();}
 void restore(String text){beacons.clear();if(text==null||text.isEmpty())return;for(var e:BsonDocument.parse(text).entrySet()){var v=e.getValue().asDocument();var b=new RescueBeacon(UUID.fromString(v.getString("Owner").getValue()),v.getString("Name").getValue());var guests=new HashMap<UUID,String>();v.getDocument("Guests").forEach((id,name)->guests.put(UUID.fromString(id),name.asString().getValue()));b.restore(v.getNumber("Expires").longValue(),v.getBoolean("Enabled").getValue(),guests);beacons.put(RocketRegistry.Cell.parse(e.getKey()),b);}}
 @Override public Resource<EntityStore>clone(){var r=new RescueRegistry();r.restore(snapshot());return r;}
}
