package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.*;
import java.util.*;

/** Four real canisters per placed diffuser; no offline generation or duplicate player-side tank. */
public final class OxygenRegistry implements Resource<EntityStore>{
 static final class Diffuser{final UUID owner;boolean enabled=true;final List<ItemStack> items=new ArrayList<>();int people;boolean powered;Ref<EntityStore> boundary;Diffuser(UUID id){owner=id;}double oxygen(){return items.stream().mapToDouble(OxygenCanisters::remaining).sum();}}
 final Map<RocketRegistry.Cell,Diffuser> diffusers=new HashMap<>();float elapsed;
 public static final BuilderCodec<OxygenRegistry> CODEC=BuilderCodec.builder(OxygenRegistry.class,OxygenRegistry::new).append(new KeyedCodec<>("DiffusersV1",Codec.STRING),OxygenRegistry::restore,OxygenRegistry::snapshot).add().build();
 public OxygenRegistry(){}
 String snapshot(){var all=new BsonDocument();for(var e:diffusers.entrySet()){var d=e.getValue();var list=new BsonArray();for(var i:d.items)list.add(ItemStack.CODEC.encode(i,new ExtraInfo()));all.put(e.getKey().key(),new BsonDocument("Owner",new BsonString(d.owner.toString())).append("Enabled",BsonBoolean.valueOf(d.enabled)).append("Items",list));}return all.toJson();}
 void restore(String encoded){diffusers.clear();if(encoded==null||encoded.isEmpty())return;for(var e:BsonDocument.parse(encoded).entrySet()){var v=e.getValue().asDocument();var d=new Diffuser(UUID.fromString(v.getString("Owner").getValue()));d.enabled=v.getBoolean("Enabled").getValue();for(var i:v.getArray("Items")){var item=ItemStack.CODEC.decode(i,new ExtraInfo());if(ItemStack.isEmpty(item)||item.getQuantity()!=1||!Set.of(OxygenCanisters.FULL,OxygenCanisters.EMPTY).contains(item.getItemId())||d.items.size()>=4)throw new IllegalArgumentException("Invalid saved diffuser canister");d.items.add(item);}diffusers.put(RocketRegistry.Cell.parse(e.getKey()),d);}}
 @Override public Resource<EntityStore>clone(){var d=new OxygenRegistry();d.restore(snapshot());return d;}
}
