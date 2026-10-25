package dev.astralreach.hytale;

import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;
import org.bson.*;
import java.util.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Console-owned, normally saved native material escrow. Decorative references never persist. */
public final class LaunchYardRegistry implements Resource<EntityStore> {
 static final class Yard {
  final UUID owner;LaunchPadLayout.Facing facing;boolean selected;
  ConstructionProgress progress=new ConstructionProgress(LaunchPlan.ODYSSEY);
  final List<ItemStack> escrow=new ArrayList<>();
  final Map<String,Integer> recovered=new HashMap<>();int heldFuel;
  Yard(UUID owner,LaunchPadLayout.Facing facing){this.owner=Objects.requireNonNull(owner);this.facing=Objects.requireNonNull(facing);}
 }
 final Map<Cell,Yard> yards=new HashMap<>();
 long elapsedMillis;
 final Map<Cell,Ref<EntityStore>> decks=new HashMap<>(),ghosts=new HashMap<>();
 final Map<Cell,String> ghostPlans=new HashMap<>();
 static final class Celebration {final Cell center;long elapsed;int next=1;Celebration(Cell center){this.center=center;}}
 final List<Celebration> celebrations=new ArrayList<>();
 public static final BuilderCodec<LaunchYardRegistry> CODEC=BuilderCodec.builder(LaunchYardRegistry.class,LaunchYardRegistry::new)
  .append(new KeyedCodec<>("YardsV1",Codec.STRING),LaunchYardRegistry::restore,LaunchYardRegistry::snapshot).add().build();
 public LaunchYardRegistry(){}
 String snapshot(){var all=new BsonDocument();for(var e:yards.entrySet()){
  var y=e.getValue();var p=y.progress;var items=new BsonArray();for(var item:y.escrow)items.add(ItemStack.CODEC.encode(item,new ExtraInfo()));
  var d=new BsonDocument("Owner",new BsonString(y.owner.toString())).append("Facing",new BsonString(y.facing.name())).append("Selected",BsonBoolean.valueOf(y.selected)).append("Plan",new BsonString(p.plan().name())).append("Phase",new BsonString(p.phase().name())).append("WorkedMillis",new BsonInt64(p.workedMillis())).append("Celebrated",BsonBoolean.valueOf(p.celebrated())).append("Escrow",items);
  var credit=new BsonDocument();for(var c:y.recovered.entrySet())credit.put(c.getKey(),new BsonInt32(c.getValue()));d.append("Recovered",credit).append("HeldFuel",new BsonInt32(y.heldFuel));all.put(e.getKey().key(),d);
 }return all.toJson(org.bson.json.JsonWriterSettings.builder().outputMode(org.bson.json.JsonMode.EXTENDED).build());}
 void restore(String encoded){yards.clear();if(encoded==null||encoded.isEmpty())return;for(var e:BsonDocument.parse(encoded).entrySet()){
  var d=e.getValue().asDocument();var y=new Yard(UUID.fromString(d.getString("Owner").getValue()),LaunchPadLayout.Facing.valueOf(d.getString("Facing").getValue()));
  var plan=LaunchPlan.restore(d.getString("Plan").getValue());var phase=ConstructionProgress.Phase.valueOf(d.getString("Phase").getValue());var counts=new HashMap<String,Integer>();y.heldFuel=d.getInt32("HeldFuel",new BsonInt32(0)).getValue();if(y.heldFuel<0||y.heldFuel>16000||y.heldFuel%1000!=0)throw new IllegalArgumentException("Invalid held propellant");
  var credit=d.getDocument("Recovered",new BsonDocument());for(var c:credit.entrySet()){int n=c.getValue().asNumber().intValue();if(n<=0||n>plan.materials().getOrDefault(c.getKey(),0))throw new IllegalArgumentException("Invalid recovery credit");y.recovered.put(c.getKey(),n);counts.put(c.getKey(),n);}
  for(var v:d.getArray("Escrow")){var item=ItemStack.CODEC.decode(v.asDocument(),new ExtraInfo());if(ItemStack.isEmpty(item))throw new IllegalArgumentException("Empty construction escrow entry");y.escrow.add(item);counts.merge(item.getItemId(),item.getQuantity(),Math::addExact);}
  // Completed materials have been consumed by the rocket, while the progress record keeps its exact cost.
  if(phase==ConstructionProgress.Phase.READY){if(!y.recovered.isEmpty()||y.heldFuel!=0)throw new IllegalArgumentException("Completed rocket retained recovery credit");if(!counts.isEmpty())throw new IllegalArgumentException("Completed yard retained spent escrow");counts.putAll(plan.materials());}
  y.progress=ConstructionProgress.restore(plan,counts,phase,d.getNumber("WorkedMillis").longValue(),d.getBoolean("Celebrated").getValue());y.selected=d.getBoolean("Selected").getValue();
  if(!y.selected&&(!y.escrow.isEmpty()||phase!=ConstructionProgress.Phase.PREVIEW))throw new IllegalArgumentException("Unselected yard has paid work");
  yards.put(Cell.parse(e.getKey()),y);
 }}
 @Override public Resource<EntityStore> clone(){var result=new LaunchYardRegistry();result.restore(snapshot());return result;}
}
