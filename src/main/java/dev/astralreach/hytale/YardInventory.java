package dev.astralreach.hytale;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.*;
import java.util.*;

/** Plans exchanges on independent native stacks, then commits exact slot images on the world thread. */
final class YardInventory {
 static ItemStack copy(ItemStack item){return ItemStack.isEmpty(item)?null:ItemStack.CODEC.decode(ItemStack.CODEC.encode(item,new ExtraInfo()),new ExtraInfo());}
 private static boolean same(ItemStack a,ItemStack b){return ItemStack.isEmpty(a)?ItemStack.isEmpty(b):!ItemStack.isEmpty(b)&&ItemStack.CODEC.encode(a,new ExtraInfo()).equals(ItemStack.CODEC.encode(b,new ExtraInfo()));}
 private static SimpleItemContainer image(ItemContainer inv){var result=new SimpleItemContainer(inv.getCapacity());for(short i=0;i<inv.getCapacity();i++)result.setItemStackForSlot(i,copy(inv.getItemStack(i)));return result;}
 private static boolean commit(ItemContainer inv,ItemContainer before,ItemContainer after){
  for(short i=0;i<inv.getCapacity();i++)if(!same(inv.getItemStack(i),before.getItemStack(i)))return false;
  var applied=new ArrayList<Short>();
  for(short i=0;i<inv.getCapacity();i++){
   if(same(before.getItemStack(i),after.getItemStack(i)))continue;
   if(!inv.replaceItemStackInSlot(i,before.getItemStack(i),copy(after.getItemStack(i))).succeeded()){
    for(short slot:applied)if(!inv.replaceItemStackInSlot(slot,after.getItemStack(slot),copy(before.getItemStack(slot))).succeeded())throw new IllegalStateException("Concurrent inventory modification during construction rollback");
    return false;
   }applied.add(i);
  }return true;
 }
 /** Transfer only currently required materials; returned stacks retain all typed native metadata. */
 static List<ItemStack> take(ItemContainer inv,Map<String,Integer> needed){
  var before=image(inv);var after=image(before);var left=new HashMap<>(needed);var paid=new ArrayList<ItemStack>();
  for(short i=0;i<after.getCapacity();i++){
   var item=after.getItemStack(i);if(ItemStack.isEmpty(item))continue;int n=Math.min(item.getQuantity(),left.getOrDefault(item.getItemId(),0));if(n<=0)continue;
   paid.add(copy(item.withQuantity(n)));left.merge(item.getItemId(),-n,Integer::sum);after.setItemStackForSlot(i,n==item.getQuantity()?null:item.withQuantity(item.getQuantity()-n));
  }
  return paid.isEmpty()||!commit(inv,before,after)?List.of():List.copyOf(paid);
 }
 static boolean refund(ItemContainer inv,List<ItemStack> escrow){
  var before=image(inv);var after=image(before);
  for(var item:escrow){var result=after.addItemStack(copy(item),true,false,true);if(!result.succeeded()||!ItemStack.isEmpty(result.getRemainder()))return false;}
  return commit(inv,before,after);
 }
}
