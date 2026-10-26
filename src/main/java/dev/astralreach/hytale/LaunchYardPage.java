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
  var y=LaunchYardService.data(s).yards.get(at);if(y==null)return;var p=y.progress;boolean formed=LaunchYardService.formed(s,at,y),ready=p.phase()==ConstructionProgress.Phase.READY,working=p.phase()==ConstructionProgress.Phase.ASSEMBLING;
  String blocked=LaunchYardService.data(s).blocked.get(at);
  c.set("#Status.Text",!formed?"Pad incomplete":ready?"Rocket complete":working?(blocked==null?"Building · "+(p.workedMillis()/100)+"%":"Construction paused"):y.selected?"Blueprint ready":"Choose a rocket");
  c.set("#Footprint.Text",LaunchYardService.present(s.getExternalData().getWorld(),at,y)+" of 25 modules · 25 × 25 pad · "+y.facing);
  c.set("#Blueprint.Text",y.selected?p.plan().title+(!y.recovered.isEmpty()?"\nRecovered assemblies credited":""):"First expedition: Odyssey Heavy");
  c.set("#PreviewOdyssey.Visible",p.plan()==dev.astralreach.core.LaunchPlan.ODYSSEY);c.set("#PreviewKestrel.Visible",p.plan()==dev.astralreach.core.LaunchPlan.KESTREL);c.set("#Rotate.Text","Pad: "+y.facing);c.set("#Plans.Visible",!y.selected);
  var inv=s.getComponent(ref,Player.getComponentType()).getInventory().getCombinedHotbarFirst();
  int row=0;boolean anyCarried=false;for(var e:new TreeMap<>(p.plan().materials()).entrySet()){
   int need=p.remaining(e.getKey());if(need==0||!y.selected||ready)continue;int carried=0;
   for(short slot=0;slot<inv.getCapacity();slot++){var item=inv.getItemStack(slot);if(!com.hypixel.hytale.server.core.inventory.ItemStack.isEmpty(item)&&item.getItemId().equals(e.getKey()))carried+=item.getQuantity();}
   anyCarried|=carried>0;String id="#Material"+row;c.set(id+".Visible",true);c.set(id+" #Icon.ItemId",e.getKey());c.set(id+" #Count.Text",title(e.getKey()));c.set(id+" #Needed.Text",Integer.toString(need));c.set(id+" #Carried.Text",Integer.toString(carried));row++;
  }
  int rows=row;for(;row<5;row++)c.set("#Material"+row+".Visible",false);c.set("#Materials.Visible",rows>0);NativeUi.height(c,"#Materials",102+rows*52);
  c.set("#Work.Visible",working);c.set("#WorkText.Text",blocked==null?"Construction · "+(p.workedMillis()/100)+"%":blocked);c.set("#WorkProgress.Value",Math.clamp(p.workedMillis()/10000d,0,1));
  c.set("#Recovery.Visible",y.heldFuel>0);c.set("#Recovery.Text","Saved propellant: "+NativeUi.number(y.heldFuel)+" units");
  c.set("#Help.Text",ready?"Use flight control to prepare your next expedition.":!y.selected?"Select a full-scale ghost, then supply the blueprint's parts.":working?"Paid work resumes when the complete site is loaded and clear.":!y.recovered.isEmpty()?"Returned assemblies are included. Supply only the missing parts.":"Supply only missing quantities. Other items stay in your pack.");
  c.set("#Flight.Visible",ready);c.set("#Deposit.Visible",y.selected&&!ready&&!working&&!p.funded());c.set("#Deposit.Disabled",!formed||!anyCarried);c.set("#Build.Visible",y.selected&&!ready&&!working&&p.funded());c.set("#Cancel.Visible",y.selected&&!ready);
  int body=230+(!y.selected?72:0)+(rows>0?102+rows*52:0)+(working?78:0)+(y.heldFuel>0?36:0)+48;
  NativeUi.bounds(c,"#YardPanel",1000,80+body+96);
 }
 @Override public void build(Ref<EntityStore>ref,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  c.append("Pages/AstralReach/LaunchYard.ui");labels(c,ref,s);NativeUi.cue(s,playerRef);
  for(String key:new String[]{"Odyssey","Kestrel","Rotate","Deposit","Build","Cancel","Flight","Close"})e.addEventBinding(CustomUIEventBindingType.Activating,"#"+key,EventData.of("Action",key),false);
 }
 void refresh(Ref<EntityStore>ref,Store<EntityStore>s){if(dismissed)return;try{LaunchYardService.usable(s,ref,playerRef,at);}catch(IllegalArgumentException ex){close();return;}var c=new UICommandBuilder();labels(c,ref,s);sendUpdate(c,false);}
 @Override public void handleDataEvent(Ref<EntityStore>ref,Store<EntityStore>s,RocketPage.Action event){s.getExternalData().getWorld().execute(()->{
  if(dismissed||!ref.isValid()||ref.getStore()!=s)return;var player=s.getComponent(ref,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this)return;
  if(event.action.equals("Close")){close();return;}
  if(Set.of("Odyssey","Kestrel","Rotate").contains(event.action))NativeUi.cue(s,playerRef);
  try{var yard=LaunchYardService.usable(s,ref,playerRef,at);if(event.action.equals("Flight")){if(yard.progress.phase()!=ConstructionProgress.Phase.READY)return;var anchor=LaunchYardService.anchor(at,yard);/* PageManager tears down this page during replacement. A separate close sends a late dismiss for the new page. */RocketPage.open(s,ref,playerRef,anchor);return;}}catch(IllegalArgumentException ex){close();return;}
  LaunchYardService.action(s,ref,playerRef,at,event.action);refresh(ref,s);
 });}
 @Override public void onDismiss(Ref<EntityStore>ref,Store<EntityStore>s){dismissed=true;}
}
