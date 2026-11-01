package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.*;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import org.joml.Vector3d;
/** Combustion cannot be reignited outside the actual powered oxygen volume. */
final class VacuumTorches {
 private static boolean torch(String id){return id.toLowerCase(java.util.Locale.ROOT).contains("torch");}
 static boolean air(Store<EntityStore>s,Vector3d at){return !OxygenService.vacuum(s)||OxygenService.shelter(s,at)!=null;}
 static ItemStack state(ItemStack item,boolean air){
  String id=item.getItemId();var metadata=item.getMetadata()==null?new org.bson.BsonDocument():item.getMetadata().clone();String saved=metadata.containsKey("AstralExtinguishedTorchV1")?metadata.getString("AstralExtinguishedTorchV1").getValue():null;
  String target=null;if(air&&saved!=null&&id.equals("AR_Unlit_"+saved)){target=saved;metadata.remove("AstralExtinguishedTorchV1");}
  else if(!air&&saved==null&&!id.startsWith("AR_Unlit_")&&torch(id)&&com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAsset("AR_Unlit_"+id)!=null){target="AR_Unlit_"+id;metadata.put("AstralExtinguishedTorchV1",new org.bson.BsonString(id));}
  if(target==null)return item;var result=new ItemStack(target,item.getQuantity(),item.getDurability(),item.getMaxDurability(),item.getQualityIndex(),metadata);result.setOverrideDroppedItemAnimation(item.getOverrideDroppedItemAnimation());return result;
 }
 static void tick(Store<EntityStore>s){
  var w=s.getExternalData().getWorld();
  for(var p:w.getPlayerRefs()){var r=p.getReference();if(r==null||!r.isValid()||r.getStore()!=s)continue;var transform=s.getComponent(r,TransformComponent.getComponentType());var player=s.getComponent(r,Player.getComponentType());if(transform==null||player==null)continue;var pos=transform.getPosition();
   boolean breathable=air(s,new Vector3d(pos).add(0,1.6,0));for(var inv:java.util.List.of(player.getInventory().getCombinedStorageHotbarBackpack(),player.getInventory().getUtility())){for(short slot=0;slot<inv.getCapacity();slot++){var item=inv.getItemStack(slot);if(ItemStack.isEmpty(item))continue;var changed=state(item,breathable);if(changed!=item)inv.replaceItemStackInSlot(slot,item,changed);}}
   if(!OxygenService.vacuum(s))continue;
   // Near-player active blocks include old saves; no global world scans or edits to unloaded chunks.
   int x=(int)Math.floor(pos.x),y=(int)Math.floor(pos.y),z=(int)Math.floor(pos.z);for(int dx=-5;dx<=5;dx++)for(int dy=-3;dy<=4;dy++)for(int dz=-5;dz<=5;dz++){var at=new RocketRegistry.Cell(x+dx,y+dy,z+dz);var block=NativeBlocks.get(w,at);if(block==null||!torch(block.getId())||air(s,new Vector3d(at.x()+.5,at.y()+.5,at.z()+.5)))continue;var off=block.getBlockForState("Off");if(off!=null&&!off.getId().equals(block.getId()))NativeBlocks.set(w,at,off);}
  }
 }
 static final class Placement extends EntityEventSystem<EntityStore,PlaceBlockEvent>{Placement(){super(PlaceBlockEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}@Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,PlaceBlockEvent e){if(e.isCancelled()||e.getItemInHand()==null||!torch(e.getItemInHand().getItemId()))return;var t=e.getTargetBlock();if(!air(s,new Vector3d(t.x+.5,t.y+.5,t.z+.5))){e.setCancelled(true);AstralReachPlugin.message(c.getComponent(i,PlayerRef.getComponentType()),"Torches need an oxygen atmosphere or a powered oxygen field. Use an electric light in vacuum.");}}}
 static final class Use extends EntityEventSystem<EntityStore,UseBlockEvent.Pre>{Use(){super(UseBlockEvent.Pre.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}@Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,UseBlockEvent.Pre e){if(!torch(e.getBlockType().getId()))return;var t=e.getTargetBlock();if(!air(s,new Vector3d(t.x+.5,t.y+.5,t.z+.5)))e.setCancelled(true);}}
 private VacuumTorches(){}
}
