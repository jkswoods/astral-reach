package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.codec.ExtraInfo;
import org.bson.*;

/** Exact single-cell exchange on the world thread, preserving all unrelated stacks/metadata. */
final class FuelInventory {
 static boolean swap(ItemContainer inv,String input,String output){
  for(short source=0;source<inv.getCapacity();source++){
   var before=inv.getItemStack(source);if(ItemStack.isEmpty(before)||!input.equals(before.getItemId()))continue;
   var encoded=ItemStack.CODEC.encode(before,new ExtraInfo());encoded.put("Id",new BsonString(output));encoded.put("Quantity",new BsonInt32(1));
   var casing=ItemStack.CODEC.decode(encoded,new ExtraInfo());
   if(!output.equals(casing.getItemId())||casing.getQuantity()!=1)throw new IllegalStateException("Native fuel casing identity codec differs");
   if(before.getQuantity()==1){return inv.replaceItemStackInSlot(source,before,casing).succeeded();}
   for(short dest=0;dest<inv.getCapacity();dest++){
    if(dest==source)continue;var old=inv.getItemStack(dest);
    // 0.7.3's all-or-nothing canAdd predicate rejects occupied compatible slots.
    // A one-item probe with partial=false still means the entire one item fits.
    if(!inv.canAddItemStackToSlot(dest,casing,false,true))continue;
    var after=before.withQuantity(before.getQuantity()-1);
    if(!inv.replaceItemStackInSlot(source,before,after).succeeded())return false;
    var add=inv.addItemStackToSlot(dest,casing,true,true);
    if(add.succeeded()&&ItemStack.isEmpty(add.getRemainder()))return true;
    // An admitted world-thread slot add must be complete. Restore the exact preflight images on failure.
    inv.setItemStackForSlot(dest,old);inv.setItemStackForSlot(source,before);return false;
   }
   // A later single source cell can free its own slot even when this stack cannot.
  }
  return false;
 }
}
