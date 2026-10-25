package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** Mission checks inspect the actual native equipped armor, never a duplicate suit inventory. */
final class ExpeditionSuit {
 static int tier(Store<EntityStore>s,Ref<EntityStore>r){
  var p=s.getComponent(r,Player.getComponentType());if(p==null)return 0;var armor=p.getInventory().getArmor();var found=new HashMap<String,Integer>();
  for(short slot=0;slot<armor.getCapacity();slot++){var stack=armor.getItemStack(slot);if(stack==null||stack.isEmpty()||stack.isBroken())continue;String id=stack.getItemId();for(int t=1;t<=3;t++){String prefix="AR_"+new String[]{"","Survey","Thermal","DeepSpace"}[t]+"_";if(id.startsWith(prefix))found.put(id.substring(prefix.length()),t);}}
  int tier=3;for(String part:List.of("Head","Chest","Hands","Legs"))tier=Math.min(tier,found.getOrDefault(part,0));return tier;
 }
 static boolean helmet(Store<EntityStore>s,Ref<EntityStore>r){var p=s.getComponent(r,Player.getComponentType());if(p==null)return false;var a=p.getInventory().getArmor();for(short slot=0;slot<a.getCapacity();slot++){var i=a.getItemStack(slot);if(i!=null&&!i.isEmpty()&&!i.isBroken()&&i.getItemId().matches("AR_(Survey|Thermal|DeepSpace)_Head"))return true;}return false;}
 static void require(Store<EntityStore>s,Ref<EntityStore>r,int tier){if(tier(s,r)<tier)throw new IllegalArgumentException("Equip a complete "+(tier==1?"Survey":tier==2?"Thermal":"Deep Space")+" suit (helmet, jacket, gloves and boots) before this mission. Craft it at the Suit Workbench.");}
}
