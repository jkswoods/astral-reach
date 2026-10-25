package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import dev.astralreach.core.OxygenSupply;
import org.bson.*;

/** Partial charge travels with the native item, including inventory moves, death and normal saves. */
final class OxygenCanisters {
 static final String FULL="AR_OxygenCanister",EMPTY="AR_EmptyOxygenCanister",KEY="AstralOxygenSecondsV1";
 static boolean full(ItemStack i){return !ItemStack.isEmpty(i)&&i.getQuantity()==1&&i.getItemId().equals(FULL);}
 static double remaining(ItemStack i){
  if(!full(i))return 0;var m=i.getMetadata();var v=m==null?null:m.get(KEY);double n=v==null?(i.getMaxDurability()>0?i.getDurability():OxygenSupply.CAPACITY):v.isNumber()?v.asNumber().doubleValue():0;
  return Double.isFinite(n)?Math.clamp(n,0,OxygenSupply.CAPACITY):0;
 }
 static ItemStack charge(ItemStack before,double amount){
  if(!full(before)||!Double.isFinite(amount)||amount<0||amount>OxygenSupply.CAPACITY)throw new IllegalArgumentException("Invalid canister charge");
  var m=before.getMetadata()==null?new BsonDocument():before.getMetadata().clone();m.put(KEY,new BsonDouble(amount));
  return amount<=0?new ItemStack(EMPTY,1,0,0,before.getQualityIndex(),m):before.withMetadata(m).withMaxDurability(OxygenSupply.CAPACITY).withDurability(amount);
 }
 static short active(ItemContainer inv){short found=-1;double least=Double.MAX_VALUE;for(short slot=0;slot<inv.getCapacity();slot++){double n=remaining(inv.getItemStack(slot));if(n>0&&n<least){found=slot;least=n;}}return found;}
 static double total(ItemContainer inv){double n=0;for(short slot=0;slot<inv.getCapacity();slot++)n+=remaining(inv.getItemStack(slot));return n;}
 static boolean consume(ItemContainer inv,short slot,double cost){var before=inv.getItemStack(slot);double n=remaining(before);if(n<=0||cost<0||!Double.isFinite(cost))return false;return inv.replaceItemStackInSlot(slot,before,charge(before,Math.max(0,n-cost))).succeeded();}
}
