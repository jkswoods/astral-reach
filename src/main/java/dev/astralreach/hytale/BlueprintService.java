package dev.astralreach.hytale;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;

/** Native recipe knowledge follows actual per-player field record completion. */
final class BlueprintService {
 static void sync(Store<EntityStore>s,Ref<EntityStore>r,FieldJournal j){
  BlueprintExpedition.reconcile(j.blueprintRecords,j.blueprints);
  var player=s.getComponent(r,Player.getComponentType());if(player==null)return;
  for(var k:BlueprintExpedition.Kind.values())if(j.blueprints.contains(k.id)&&!player.getPlayerConfigData().getKnownRecipes().contains(k.item))CraftingPlugin.learnRecipe(r,k.item,s);
 }
 static BlueprintExpedition.Site site(Store<EntityStore>s,Ref<EntityStore>r,BlueprintExpedition.Kind kind,LunarExpedition.Site selected){
  var at=BlueprintExpedition.at(selected,kind);if(at.valid())return at;
  var t=s.getComponent(r,TransformComponent.getComponentType()).getPosition();int seed=FieldService.moon(s)?(int)s.getExternalData().getWorld().getWorldConfig().getSeed():91023;
  return BlueprintExpedition.nearby(seed,(int)t.x,(int)t.z,kind);
 }
 static BlueprintExpedition.Site inspect(Store<EntityStore>s,Ref<EntityStore>r,RocketRegistry.Cell at){
  if(!FieldService.moon(s)||at==null||!RocketService.near(s,r,at))return null;
  String id=NativeBlocks.base(NativeBlocks.get(s.getExternalData().getWorld(),at));
  for(var kind:BlueprintExpedition.Kind.values())if(kind.archive.equals(id)){
   int seed=(int)s.getExternalData().getWorld().getWorldConfig().getSeed();var site=BlueprintExpedition.at(LunarExpedition.candidate(seed,at.x(),at.z()),kind);
   for(int i=0;i<2;i++)if(at.equals(FieldService.cell(site.record(i)))){var j=FieldService.journal(s,r);j.blueprintDiscoveries.add(site.id());j.selectedMission=kind.id;j.selectedSite=site.survey().id();return site;}
  }
  return null;
 }
 static int recoverable(Store<EntityStore>s,Ref<EntityStore>r,BlueprintExpedition.Site site){
  if(!FieldService.moon(s)||!site.valid())return -1;int fallback=-1;var j=FieldService.journal(s,r);
  for(int i=0;i<2;i++){var at=FieldService.cell(site.record(i));if(RocketService.near(s,r,at)&&site.kind().archive.equals(NativeBlocks.base(NativeBlocks.get(s.getExternalData().getWorld(),at)))){fallback=i;if(!j.blueprintRecords.contains(BlueprintExpedition.claim(site,i)))return i;}}
  return fallback;
 }
 static boolean recover(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,BlueprintExpedition.Site site){
  int i=recoverable(s,r,site);if(i<0)throw new IllegalArgumentException("Stand beside an original "+site.kind().title+" terminal.");var j=FieldService.journal(s,r);String key=BlueprintExpedition.claim(site,i);
  if(!j.blueprintRecords.add(key))throw new IllegalArgumentException("You already recovered this record. Track the other outpost.");
  boolean before=j.blueprints.contains(site.kind().id);j.blueprintDiscoveries.add(site.id());sync(s,r,j);boolean earned=!before&&j.blueprints.contains(site.kind().id);
  LunarEnvironment.interior(s,r,p,earned?"AR_Expedition_Perk":"AR_rescue_radio");
  AstralReachPlugin.message(p,earned?"Blueprint unlocked: "+site.kind().reward+". Your native crafting recipe is permanently learned; gather its materials.":"Field record recovered. Recover the other record at this site to unlock "+site.kind().reward+".");return earned;
 }
 private BlueprintService(){}
}
