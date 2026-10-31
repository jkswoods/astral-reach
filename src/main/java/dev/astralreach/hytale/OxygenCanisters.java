package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import dev.astralreach.core.OxygenSupply;
import org.bson.*;
import java.util.Set;

/** Partial charge travels with the native item, including inventory moves, death and normal saves. */
final class OxygenCanisters {
 static final String FULL="AR_OxygenCanister",EMPTY="AR_EmptyOxygenCanister",KEY="AstralOxygenSecondsV1";
 static final String LARGE="AR_LargeOxygenCanister",LARGE_EMPTY="AR_EmptyLargeOxygenCanister";
 static boolean full(ItemStack i){return !ItemStack.isEmpty(i)&&i.getQuantity()==1&&(i.getItemId().equals(FULL)||i.getItemId().equals(LARGE));}
 static boolean extended(ItemStack i){return !ItemStack.isEmpty(i)&&(i.getItemId().equals(LARGE)||i.getItemId().equals(LARGE_EMPTY));}
 static double capacity(ItemStack i){return extended(i)?OxygenSupply.EXTENDED_CAPACITY:OxygenSupply.CAPACITY;}
 static double remaining(ItemStack i){
  if(!full(i))return 0;var m=i.getMetadata();var v=m==null?null:m.get(KEY);double n=v==null?(i.getMaxDurability()>0?i.getDurability():capacity(i)):v.isNumber()?v.asNumber().doubleValue():0;
  return Double.isFinite(n)?Math.clamp(n,0,capacity(i)):0;
 }
 static ItemStack charge(ItemStack before,double amount){
  if(!full(before)||!Double.isFinite(amount)||amount<0||amount>capacity(before))throw new IllegalArgumentException("Invalid canister charge");
  var m=before.getMetadata()==null?new BsonDocument():before.getMetadata().clone();
  if(amount<=0){m.remove(KEY);return new ItemStack(extended(before)?LARGE_EMPTY:EMPTY,1,0,0,before.getQualityIndex(),m.isEmpty()?null:m);}
  m.put(KEY,new BsonDouble(amount));return before.withMetadata(m).withMaxDurability(capacity(before)).withDurability(amount);
 }
 /** Native recipes compare item metadata. A spent server charge marker is no longer part of an empty casing. */
 static ItemStack normalizeEmpty(ItemStack before){
  if(ItemStack.isEmpty(before)||!Set.of(EMPTY,LARGE_EMPTY).contains(before.getItemId())||before.getMetadata()==null)return before;
  var metadata=before.getMetadata();var value=metadata.get(KEY);
  if(value!=null&&(!value.isNumber()||value.asNumber().doubleValue()!=0))return before;
  if(value==null&&!metadata.isEmpty())return before;
  var m=metadata.clone();m.remove(KEY);return before.withMetadata(m.isEmpty()?null:m);
 }
 static void normalizeEmpty(ItemContainer inv){for(short slot=0;slot<inv.getCapacity();slot++){var before=inv.getItemStack(slot);var after=normalizeEmpty(before);if(after!=before)inv.replaceItemStackInSlot(slot,before,after);}}
 static short active(ItemContainer inv){short found=-1;double least=Double.MAX_VALUE;for(short slot=0;slot<inv.getCapacity();slot++){double n=remaining(inv.getItemStack(slot));if(n>0&&n<least){found=slot;least=n;}}return found;}
 static double total(ItemContainer inv){double n=0;for(short slot=0;slot<inv.getCapacity();slot++)n+=remaining(inv.getItemStack(slot));return n;}
 static boolean consume(ItemContainer inv,short slot,double cost){var before=inv.getItemStack(slot);double n=remaining(before);if(n<=0||cost<0||!Double.isFinite(cost))return false;return inv.replaceItemStackInSlot(slot,before,charge(before,Math.max(0,n-cost))).succeeded();}
}
