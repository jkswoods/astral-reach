package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.*;
import java.util.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Saved selection chooses the catalogue or construction page; every transfer stays server-owned. */
final class LaunchYardPage extends InteractiveCustomUIPage<RocketPage.Action>{
 final Cell at;boolean dismissed,construction;
 private static final List<String> PARTS=List.of("AR_PressureHullPart","AR_OrbitalEnginePart","AR_FlightComputer","AR_FramePlate","AR_ThermalShield");
 LaunchYardPage(PlayerRef p,Cell at){super(p,CustomPageLifetime.CanDismiss,RocketPage.Action.CODEC);this.at=at;}
 static void open(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at){try{LaunchYardService.usable(s,ref,p,at);var player=s.getComponent(ref,Player.getComponentType());if(player!=null)player.getPageManager().openCustomPage(ref,s,new LaunchYardPage(p,at));}catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}}
 private static String title(String id){return switch(id){case "AR_PressureHullPart"->"Pressure hull sections";case "AR_OrbitalEnginePart"->"Orbital engine parts";case "AR_FlightComputer"->"Flight computers";case "AR_FramePlate"->"Frame plates";case "AR_ThermalShield"->"Thermal shield";default->id;};}
 private void labels(UICommandBuilder c,Ref<EntityStore>ref,Store<EntityStore>s){
  var y=LaunchYardService.data(s).yards.get(at);if(y==null)return;var p=y.progress;boolean formed=LaunchYardService.formed(s,at,y),ready=p.phase()==ConstructionProgress.Phase.READY,working=p.phase()==ConstructionProgress.Phase.ASSEMBLING;
  String blocked=LaunchYardService.data(s).blocked.get(at);
  c.set("#Footprint.Text",LaunchYardService.present(s.getExternalData().getWorld(),at,y)+" / 25 modules · 25 × 25 pad"+(formed?" · Ready":" · Incomplete"));
  if(!construction){
   c.set("#Rotate.Text","Pad: "+y.facing);
   for(var plan:LaunchPlan.values()){
    String id="#Select"+plan.name().replace("_", "");c.set(id+" #Height.Text",String.format(Locale.ROOT,"%.0f\nblocks",plan.height));c.set(id+" #Dimensions.Text",String.format(Locale.ROOT,"%.1f × %.1f footprint",plan.width,plan.depth));
    int row=0;for(String part:PARTS){Integer quantity=plan.materials().get(part);if(quantity==null)continue;String cost=id+" #Cost"+row++;c.set(cost+".Visible",true);c.set(cost+" #Icon.ItemId",part);c.set(cost+" #Quantity.Text",Integer.toString(quantity));}
    for(;row<5;row++)c.set(id+" #Cost"+row+".Visible",false);
   }return;
  }
  c.set("#Blueprint.Text",p.plan().title);
  c.set("#Status.Text",ready?"Rocket complete":working?(blocked==null?"Construction in progress":"Construction paused"):!y.recovered.isEmpty()?"Returned assemblies credited":"Awaiting parts");
  c.set("#Dimensions.Text",String.format(Locale.ROOT,"%.1f × %.1f footprint",p.plan().width,p.plan().depth));c.set("#PreviewName.Text",p.plan().title);c.set("#PreviewHeight.Text",String.format(Locale.ROOT,"%.0f\nblocks",p.plan().height));c.set("#Footprint.Text",formed?"Pad formed · 25 / 25 modules":"Pad incomplete · restore modules");c.set("#BuildInfo.Text","Build: 10 seconds\nRound trip: "+NativeUi.number(RocketService.fuelCost(s,ref).total())+" fuel\nLoaded after assembly");
  for(var plan:LaunchPlan.values())c.set("#Preview"+plan.name().replace("_", "")+".Visible",plan==p.plan());
  var inv=s.getComponent(ref,Player.getComponentType()).getInventory().getCombinedHotbarFirst();
  int row=0,total=0,supplied=0;boolean anyCarried=CreativeSupplies.enabled(s,ref);
  for(String part:PARTS){Integer required=p.plan().materials().get(part);if(required==null)continue;int held=0;
   for(short slot=0;slot<inv.getCapacity();slot++){var item=inv.getItemStack(slot);if(!ItemStack.isEmpty(item)&&item.getItemId().equals(part))held+=item.getQuantity();}
   int paid=p.supplied().getOrDefault(part,0);total+=required;supplied+=paid;anyCarried|=held>0&&p.remaining(part)>0;
   String id="#Material"+row++;c.set(id+".Visible",true);c.set(id+" #Icon.ItemId",part);c.set(id+" #Count.Text",title(part));c.set(id+" #Required.Text",Integer.toString(required));c.set(id+" #Supplied.Text",Integer.toString(paid));c.set(id+" #Carried.Text",Integer.toString(held));
  }
  for(;row<5;row++)c.set("#Material"+row+".Visible",false);
  c.set("#ProgressText.Text",working?"Construction · "+(p.workedMillis()/100)+"%":supplied+" / "+total+" parts supplied");
  c.set("#Progress.Value",working?p.workedMillis()/(double)ConstructionProgress.BUILD_MILLIS:supplied/(double)total);
  c.set("#Site.Text",blocked!=null?blocked:ready?"Build complete. Open Flight Control.":working?"Launch column clear · construction active.":"Launch column checked when construction starts.");
  c.set("#Recovery.Visible",y.heldFuel>0);c.set("#Recovery.Text","Saved propellant: "+NativeUi.number(y.heldFuel)+" units");
  c.set("#Help.Text",!y.recovered.isEmpty()?"Returned parts stay with this blueprint. Supply only the missing materials.":ready?"Reopen this console any time to access Flight Control.":"Change rocket returns all supplied parts. Carry room for the full refund.");
  if(CreativeSupplies.enabled(s,ref)&&!ready)c.set("#Help.Text","Creative: Supply fills every missing part without carrying materials.");
  c.set("#Deposit.Text",CreativeSupplies.enabled(s,ref)?"Supply all parts · Creative":"Supply carried parts");
  c.set("#Flight.Visible",ready);c.set("#Deposit.Visible",!ready&&!working&&!p.funded());c.set("#Deposit.Disabled",!formed||!anyCarried);c.set("#Build.Visible",!ready&&!working&&p.funded());c.set("#Change.Visible",!ready&&y.recovered.isEmpty());c.set("#Cancel.Visible",!ready&&!y.recovered.isEmpty());c.set("#Cancel.Disabled",y.escrow.isEmpty());
 }
 @Override public void build(Ref<EntityStore>ref,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  var yard=LaunchYardService.usable(s,ref,playerRef,at);construction=yard.selected;
  c.append("Pages/AstralReach/"+(construction?"LaunchYard.ui":"RocketSelection.ui"));labels(c,ref,s);NativeUi.cue(s,playerRef);
  var actions=construction?new String[]{"Deposit","Build","Change","Cancel","Flight","Close"}:new String[]{"Rotate","Close"};
  for(String key:actions)e.addEventBinding(CustomUIEventBindingType.Activating,"#"+key,EventData.of("Action",key),false);
  if(!construction)for(var plan:LaunchPlan.values())e.addEventBinding(CustomUIEventBindingType.Activating,"#Select"+plan.name().replace("_", ""),EventData.of("Action","Select:"+plan.name()),false);
 }
 void refresh(Ref<EntityStore>ref,Store<EntityStore>s){if(dismissed)return;LaunchYardRegistry.Yard y;try{y=LaunchYardService.usable(s,ref,playerRef,at);}catch(IllegalArgumentException ex){close();return;}
  if(y.selected!=construction){open(s,ref,playerRef,at);return;}var c=new UICommandBuilder();labels(c,ref,s);sendUpdate(c,false);
 }
 @Override public void handleDataEvent(Ref<EntityStore>ref,Store<EntityStore>s,RocketPage.Action event){s.getExternalData().getWorld().execute(()->{
  if(dismissed||!ref.isValid()||ref.getStore()!=s)return;var player=s.getComponent(ref,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this)return;
  if(event.action.equals("Close")){close();return;}
  try{var yard=LaunchYardService.usable(s,ref,playerRef,at);if(event.action.equals("Flight")){if(yard.progress.phase()!=ConstructionProgress.Phase.READY)return;RocketPage.open(s,ref,playerRef,LaunchYardService.anchor(at,yard));return;}}catch(IllegalArgumentException ex){close();return;}
  LaunchYardService.action(s,ref,playerRef,at,event.action);refresh(ref,s);
 });}
 @Override public void onDismiss(Ref<EntityStore>ref,Store<EntityStore>s){dismissed=true;}
}
