package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.PropellantTank;
import dev.astralreach.core.LaunchPlan;
import org.bson.*;
import java.util.*;

/** World-native fuel/ownership resource. Engine processing inventories remain in native BenchBlock. */
public final class RocketRegistry implements Resource<EntityStore> {
 public record Cell(int x,int y,int z){
  String key(){return x+","+y+","+z;}
  static Cell parse(String s){var a=s.split(",",-1);if(a.length!=3)throw new IllegalArgumentException("Bad rocket location");return new Cell(Integer.parseInt(a[0]),Integer.parseInt(a[1]),Integer.parseInt(a[2]));}
 }
 static final class Rocket {
  final UUID owner;final PropellantTank tank;final LaunchPlan plan;UUID traveller;boolean boosterLanded,needsRebuild;
  Rocket(UUID owner,int units){this(owner,units,LaunchPlan.ODYSSEY);}
  Rocket(UUID owner,int units,LaunchPlan plan){this.owner=Objects.requireNonNull(owner);tank=new PropellantTank(units);this.plan=Objects.requireNonNull(plan);}
 }
 final Map<Cell,Rocket> rockets=new HashMap<>();boolean resetStates;
 // Runtime-only references: no codec fields and deliberately absent from resource clones.
 final Map<Cell,com.hypixel.hytale.component.Ref<EntityStore>> exteriors=new HashMap<>();
 final Map<Cell,String> exteriorStates=new HashMap<>();
 final Map<Cell,com.hypixel.hytale.component.Ref<EntityStore>> boosters=new HashMap<>();
 final Map<Cell,String> boosterStates=new HashMap<>();
 final Map<Cell,Long> exhaustAt=new HashMap<>();
 public static final BuilderCodec<RocketRegistry> CODEC=BuilderCodec.builder(RocketRegistry.class,RocketRegistry::new)
  .append(new KeyedCodec<>("RocketsV1",Codec.STRING),RocketRegistry::restore,RocketRegistry::snapshot).add().build();
 public RocketRegistry(){}
 String snapshot(){var doc=new BsonDocument();for(var e:rockets.entrySet()){var r=e.getValue();var value=new BsonDocument("Owner",new BsonString(r.owner.toString())).append("Units",new BsonInt32(r.tank.units())).append("Plan",new BsonString(r.plan.name())).append("BoosterLanded",new BsonBoolean(r.boosterLanded)).append("NeedsRebuild",BsonBoolean.valueOf(r.needsRebuild));if(r.traveller!=null)value.put("Traveller",new BsonString(r.traveller.toString()));doc.put(e.getKey().key(),value);}return doc.toJson();}
 void restore(String encoded){rockets.clear();if(encoded==null||encoded.isEmpty())return;for(var e:BsonDocument.parse(encoded).entrySet()){var d=e.getValue().asDocument();var r=new Rocket(UUID.fromString(d.getString("Owner").getValue()),d.getInt32("Units").getValue(),d.containsKey("Plan")?LaunchPlan.restore(d.getString("Plan").getValue()):LaunchPlan.ODYSSEY);if(d.containsKey("Traveller"))r.traveller=UUID.fromString(d.getString("Traveller").getValue());r.boosterLanded=d.getBoolean("BoosterLanded",BsonBoolean.FALSE).getValue();r.needsRebuild=d.getBoolean("NeedsRebuild",BsonBoolean.FALSE).getValue();rockets.put(Cell.parse(e.getKey()),r);}}
 @Override public Resource<EntityStore> clone(){var r=new RocketRegistry();r.restore(snapshot());return r;}
}
