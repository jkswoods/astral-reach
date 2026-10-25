package dev.astralreach.hytale;

import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.LaunchPlan;
import org.bson.*;
import java.util.*;

/** Owned lunar parking sites. Models and passenger attachments remain transient native entities. */
public final class LandedShipRegistry implements Resource<EntityStore>{
 static final class Ship {
  final UUID owner;final LaunchPlan plan;final int x,y,z;boolean grounded,departed;
  Ship(UUID owner,LaunchPlan plan,int x,int y,int z){this.owner=owner;this.plan=plan;this.x=x;this.y=y;this.z=z;}
 }
 final Map<UUID,Ship> ships=new HashMap<>();
 final Map<UUID,Ref<EntityStore>> visuals=new HashMap<>();
 public static final BuilderCodec<LandedShipRegistry> CODEC=BuilderCodec.builder(LandedShipRegistry.class,LandedShipRegistry::new)
  .append(new KeyedCodec<>("ShipsV1",Codec.STRING),LandedShipRegistry::restore,LandedShipRegistry::snapshot).add().build();
 public LandedShipRegistry(){}
 String snapshot(){var d=new BsonDocument();for(var e:ships.entrySet()){var r=e.getValue();d.put(e.getKey().toString(),new BsonDocument("Plan",new BsonString(r.plan.name())).append("X",new BsonInt32(r.x)).append("Y",new BsonInt32(r.y)).append("Z",new BsonInt32(r.z)).append("Grounded",new BsonBoolean(r.grounded)).append("Departed",new BsonBoolean(r.departed)));}return d.toJson();}
 void restore(String encoded){ships.clear();if(encoded==null||encoded.isEmpty())return;for(var e:BsonDocument.parse(encoded).entrySet()){var id=UUID.fromString(e.getKey());var d=e.getValue().asDocument();var ship=new Ship(id,LaunchPlan.restore(d.getString("Plan").getValue()),d.getInt32("X").getValue(),d.getInt32("Y",new BsonInt32(64)).getValue(),d.getInt32("Z").getValue());ship.grounded=d.getBoolean("Grounded").getValue();ship.departed=d.getBoolean("Departed").getValue();ships.put(id,ship);}}
 @Override public Resource<EntityStore> clone(){var r=new LandedShipRegistry();r.restore(snapshot());return r;}
}
