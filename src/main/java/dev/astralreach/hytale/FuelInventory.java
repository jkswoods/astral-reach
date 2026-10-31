package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.codec.ExtraInfo;
import dev.astralreach.core.PropellantTank;
import org.bson.*;

/** Exact cell/tank transfers on the world thread, including saved sub-cell charges and native remainders. */
final class FuelInventory {
 static final String FULL="AR_PropellantCell",EMPTY="AR_EmptyFuelCell",KEY="AstralFuelUnitsV1";
 static int charge(ItemStack item){
  if(ItemStack.isEmpty(item)||!FULL.equals(item.getItemId()))return 0;
  var m=item.getMetadata();var v=m==null?null:m.get(KEY);if(v==null)return PropellantTank.CELL_UNITS;
  if(!v.isNumber())return 0;double n=v.asNumber().doubleValue();return Double.isFinite(n)&&n==Math.rint(n)&&n>0&&n<=PropellantTank.CELL_UNITS?(int)n:0;
 }
 static ItemStack cell(ItemStack before,String id,int units){
  if(!id.equals(FULL)&&!id.equals(EMPTY)||id.equals(FULL)&&(units<=0||units>1000)||id.equals(EMPTY)&&units!=0)throw new IllegalArgumentException("Invalid native fuel cell");
  var encoded=ItemStack.CODEC.encode(before,new ExtraInfo());encoded.put("Id",new BsonString(id));encoded.put("Quantity",new BsonInt32(1));var metadata=before.getMetadata()==null?new BsonDocument():before.getMetadata().clone();metadata.remove(KEY);if(units>0&&units<1000)metadata.put(KEY,new BsonInt32(units));
  encoded.put("MaxDurability",new BsonDouble(units>0&&units<1000?1000:0));encoded.put("Durability",new BsonDouble(units>0&&units<1000?units:0));return ItemStack.CODEC.decode(encoded,new ExtraInfo()).withMetadata(metadata);
 }
 static int deposit(ItemContainer inv,PropellantTank tank){
  for(short slot=0;slot<inv.getCapacity();slot++){var before=inv.getItemStack(slot);int n=charge(before);if(n<=0||!tank.canFill(n))continue;if(exchange(inv,slot,before,cell(before,EMPTY,0))){tank.fillCell(n);return n;}}
  return 0;
 }
 static int withdraw(ItemContainer inv,PropellantTank tank){
  int n=Math.min(1000,tank.units());if(n<=0)return 0;
  for(short slot=0;slot<inv.getCapacity();slot++){var before=inv.getItemStack(slot);if(ItemStack.isEmpty(before)||!EMPTY.equals(before.getItemId()))continue;if(exchange(inv,slot,before,cell(before,FULL,n))){tank.drainCell(n);return n;}}
  return 0;
 }
 private static boolean exchange(ItemContainer inv,short source,ItemStack before,ItemStack output){
  if(before.getQuantity()==1)return inv.replaceItemStackInSlot(source,before,output).succeeded();
  for(short dest=0;dest<inv.getCapacity();dest++){
   if(dest==source)continue;var old=inv.getItemStack(dest);
   if(!inv.canAddItemStackToSlot(dest,output,false,true))continue;
   if(!inv.replaceItemStackInSlot(source,before,before.withQuantity(before.getQuantity()-1)).succeeded())return false;
   var add=inv.addItemStackToSlot(dest,output,true,true);if(add.succeeded()&&ItemStack.isEmpty(add.getRemainder()))return true;
   inv.setItemStackForSlot(dest,old);inv.setItemStackForSlot(source,before);return false;
  }
  return false;
 }
}
