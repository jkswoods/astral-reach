package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.ConstructionProgress;
import java.util.*;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** The separate native yard console: real material counts, inventory and server-backed actions. */
final class LaunchYardPage extends InteractiveCustomUIPage<RocketPage.Action>{
 final Cell at;boolean dismissed;
 LaunchYardPage(PlayerRef p,Cell at){super(p,CustomPageLifetime.CanDismiss,RocketPage.Action.CODEC);this.at=at;}
 static void open(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at){try{LaunchYardService.usable(s,ref,p,at);var player=s.getComponent(ref,Player.getComponentType());if(player!=null)player.getPageManager().openCustomPage(ref,s,new LaunchYardPage(p,at));}catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}}
 private static String title(String id){return switch(id){case "AR_PressureHullPart"->"Pressure hull sections";case "AR_OrbitalEnginePart"->"Orbital engine parts";case "AR_FlightComputer"->"Flight computers";case "AR_FramePlate"->"Nickel-steel frame plates";case "AR_ThermalShield"->"Thermal shield";default->id;};}
 private void labels(UICommandBuilder c,Ref<EntityStore>ref,Store<EntityStore>s){
  var y=LaunchYardService.data(s).yards.get(at);if(y==null)return;var p=y.progress;var world=s.getExternalData().getWorld();var formed=LaunchYardService.formed(s,at,y);
  String state=!formed?"Incomplete / unloaded pad · "+LaunchYardService.present(world,at,y)+" /25 modules":!y.selected?"Pad formed · select a rocket blueprint":p.phase()==ConstructionProgress.Phase.READY?"Rocket complete · use flight control":p.phase()==ConstructionProgress.Phase.ASSEMBLING?"Construction "+(p.workedMillis()/100)+"% · pauses if the pad or clearance is blocked":"Ghost ready · supply blueprint materials";
  boolean ready=p.phase()==ConstructionProgress.Phase.READY;c.set("#Status.Text",state);
  c.set("#Footprint.Text","25 × 25 launch platform · "+LaunchYardService.present(world,at,y)+" / 25 modules · "+y.facing);
  c.set("#Blueprint.Text",y.selected?p.plan().title+(!y.recovered.isEmpty()?" · recovered parts already included":ready?"":" · full-scale preview"):"Odyssey: first Lune expedition. Kestrel: reusable booster, requires a Thermal Shield.");
  var player=s.getComponent(ref,Player.getComponentType());var inv=player.getInventory().getCombinedHotbarFirst();
  var costs=new TreeMap<>(p.plan().materials());int row=0;for(var e:costs.entrySet()){
   if(p.remaining(e.getKey())==0)continue;
   int carried=0;for(short slot=0;slot<inv.getCapacity();slot++){var item=inv.getItemStack(slot);if(!com.hypixel.hytale.server.core.inventory.ItemStack.isEmpty(item)&&item.getItemId().equals(e.getKey()))carried+=item.getQuantity();}
   c.set("#Material"+row+".Visible",y.selected&&p.phase()!=ConstructionProgress.Phase.READY);c.set("#Material"+row+" #Icon.ItemId",e.getKey());c.set("#Material"+row+" #Count.Text",title(e.getKey())+" ×"+p.remaining(e.getKey())+" needed · "+carried+" carried"+(y.recovered.getOrDefault(e.getKey(),0)>0?" · "+y.recovered.get(e.getKey())+" recovered":""));row++;
  }int materialRows=y.selected&&!ready?row:0;for(;row<5;row++)c.set("#Material"+row+".Visible",false);
  var bounds=new com.hypixel.hytale.server.core.ui.Anchor();bounds.setWidth(com.hypixel.hytale.server.core.ui.Value.of(830));bounds.setHeight(com.hypixel.hytale.server.core.ui.Value.of(ready?330:!y.selected?430:430+materialRows*38));c.setObject("#YardPanel.Anchor",bounds);
  c.set("#Footprint.Visible",!ready);c.set("#Help.Visible",!ready);c.set("#Recovery.Visible",y.heldFuel>0);
  c.set("#Flight.Visible",p.phase()==ConstructionProgress.Phase.READY);
  c.set("#Deposit.Visible",y.selected&&p.phase()==ConstructionProgress.Phase.PREVIEW);c.set("#Build.Visible",y.selected&&p.funded()&&p.phase()==ConstructionProgress.Phase.PREVIEW);
  c.set("#Cancel.Visible",y.selected&&p.phase()!=ConstructionProgress.Phase.READY);c.set("#Rotate.Visible",!y.selected);
  c.set("#Plans.Visible",!y.selected);c.set("#Recovery.Text",y.heldFuel>0?"Saved propellant: "+y.heldFuel+" units · retained for the rebuilt rocket":"");
 }
 @Override public void build(Ref<EntityStore>ref,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  c.append("Pages/AstralReach/LaunchYard.ui");labels(c,ref,s);
  for(String key:new String[]{"Odyssey","Kestrel","Rotate","Deposit","Build","Cancel","Flight","Close"})e.addEventBinding(CustomUIEventBindingType.Activating,"#"+key,EventData.of("Action",key),false);
 }
 void refresh(Ref<EntityStore>ref,Store<EntityStore>s){if(dismissed)return;try{LaunchYardService.usable(s,ref,playerRef,at);}catch(IllegalArgumentException ex){close();return;}var c=new UICommandBuilder();labels(c,ref,s);sendUpdate(c,false);}
 @Override public void handleDataEvent(Ref<EntityStore>ref,Store<EntityStore>s,RocketPage.Action event){s.getExternalData().getWorld().execute(()->{
  if(dismissed||!ref.isValid()||ref.getStore()!=s)return;var player=s.getComponent(ref,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this)return;
  if(event.action.equals("Close")){close();return;}
  try{var yard=LaunchYardService.usable(s,ref,playerRef,at);if(event.action.equals("Flight")){if(yard.progress.phase()!=ConstructionProgress.Phase.READY)return;var anchor=LaunchYardService.anchor(at,yard);/* PageManager tears down this page during replacement. A separate close sends a late dismiss for the new page. */RocketPage.open(s,ref,playerRef,anchor);return;}}catch(IllegalArgumentException ex){close();return;}
  LaunchYardService.action(s,ref,playerRef,at,event.action);refresh(ref,s);
 });}
 @Override public void onDismiss(Ref<EntityStore>ref,Store<EntityStore>s){dismissed=true;}
}
