package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import static dev.astralreach.hytale.RocketRegistry.Cell;

/** Native control panel. Inventory exchanges and launch preflight are always server actions. */
final class RocketPage extends InteractiveCustomUIPage<RocketPage.Action> {
 public static final class Action {String action="";static final BuilderCodec<Action> CODEC=BuilderCodec.builder(Action.class,Action::new).append(new KeyedCodec<>("Action",Codec.STRING),(a,v)->a.action=v,a->a.action).add().build();}
 final Cell at;final boolean moon;boolean dismissed;
 // Private checkpoint trace for the reproduced repeated-open event stall.
 // Fixed lifecycle labels only; no player identity or raw event payload.
 void trace(String event){System.out.println("AR_PAGE_TRACE "+System.identityHashCode(this)+" "+event);}
 RocketPage(PlayerRef p,Cell c,boolean moon){super(p,CustomPageLifetime.CanDismiss,Action.CODEC);at=c;this.moon=moon;}
 static void open(Store<EntityStore>s,Ref<EntityStore>ref,PlayerRef p,Cell at){
  var w=s.getExternalData().getWorld();boolean moon=NativeBlocks.base(NativeBlocks.get(w,at)).equals("AR_ReturnBeacon");
  if(!RocketService.controlRange(s,ref,p,at))return;
  if(moon){if(!w.getName().equals("AstralLune")){AstralReachPlugin.message(p,"The return beacon works only on Lune.");return;}}
  else {var r=RocketService.data(s).rockets.get(at);if(r==null||!NativeBlocks.rocket(w,at)||!r.owner.equals(p.getUuid())){AstralReachPlugin.message(p,"Only the player who placed this rocket can operate it.");return;}}
  var player=s.getComponent(ref,Player.getComponentType());if(player!=null)player.getPageManager().openCustomPage(ref,s,new RocketPage(p,at,moon));
 }
 void labels(UICommandBuilder c,Store<EntityStore>s){
  var r=RocketService.data(s).rockets.get(at);var ref=playerRef.getReference();var f=s.getComponent(ref,AstralReachPlugin.flightType);var player=s.getComponent(ref,Player.getComponentType());
  boolean paid=f!=null&&f.trip!=null,returnable=moon&&paid&&(f.trip.phase()==dev.astralreach.core.Journey.Phase.LUNE||f.trip.phase()==dev.astralreach.core.Journey.Phase.ORBIT);
  boolean busy=!moon&&r!=null&&r.traveller!=null,rebuild=!moon&&r!=null&&r.needsRebuild,fill=!moon&&r!=null&&!rebuild&&!busy&&r.tank.units()<9000;
  int units=moon?(paid?f.trip.returnUnits():0):r==null?0:r.tank.units();boolean sealed=ExpeditionSuit.tier(s,ref)>0;double oxygen=player==null?0:OxygenCanisters.total(player.getInventory().getCombinedStorageHotbarBackpack());boolean oxygenReady=oxygen>=240;
  c.set("#RocketTitle.Text",moon?"RETURN TO ORBIS":"FLIGHT CONTROL · "+(r==null?"Rocket":r.plan.title));
  c.set("#Status.Text",moon?(returnable?"Your homeward fuel is already paid":"No paid journey"):r==null?"Rocket unavailable":busy?"Mission in progress · tank locked":rebuild?"Returned vehicle · service required":fill?"Prepare the round trip":"Round-trip fuel ready");
  c.set("#Fuel.Visible",!rebuild);c.set("#FuelProgress.Value",moon?(returnable?1:0):Math.clamp(units/9000d,0,1));c.set("#FuelCount.Text",moon?NativeUi.number(units)+" units protected":NativeUi.number(units)+" / 9,000 units");
  c.set("#Route.Text",moon?"Reserved at departure for this homeward trip.":"6,000 units outbound\n3,000 units protected for return");
  c.set("#Drain.Visible",!moon&&r!=null&&!rebuild&&!busy&&units>=1000);c.set("#Fill.Visible",fill);c.set("#Rebuild.Visible",rebuild);c.set("#Launch.Visible",moon||!fill&&!rebuild);
  c.set("#Launch.Text",moon?"Board and return to Orbis":"Launch to orbit");c.set("#Launch.Disabled",moon?!returnable:!(r!=null&&!busy&&!rebuild&&units>=9000&&sealed&&oxygenReady));
  c.set("#Preparation.Visible",!rebuild);c.set("#Suit.Text",sealed?"Ready · complete Survey suit or better":"Equip all four Survey suit pieces");c.set("#Oxygen.Text",oxygenReady?"Ready · "+NativeUi.duration(oxygen)+" oxygen reserve":"Carry at least 4:00 oxygen reserve");
  c.set("#Help.Text",rebuild?"Returned assemblies credited. Carry "+NativeUi.materials(dev.astralreach.core.RocketRecovery.missing(r.plan,r.boosterLanded))+".":moon?"Board the landed upper stage and return to your saved platform.":busy?"Finish the active mission before changing fuel.":"Board the rocket and ascend to orbit.");
  NativeUi.height(c,"#Help",rebuild?120:54);NativeUi.bounds(c,"#FlightPanel",900,rebuild?400:720);
 }
 @Override public void build(Ref<EntityStore>ref,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  trace("build");
  c.append("Pages/AstralReach/Rocket.ui");labels(c,s);NativeUi.cue(s,playerRef);
  for(String key:new String[]{"Fill","Drain","Rebuild","Launch","Close"})e.addEventBinding(CustomUIEventBindingType.Activating,"#"+key,EventData.of("Action",key),false);
 }
 @Override public void handleDataEvent(Ref<EntityStore>ref,Store<EntityStore>s,Action a){s.getExternalData().getWorld().execute(()->{
  trace("dispatch");
  if(dismissed||!ref.isValid()||ref.getStore()!=s){trace("rejected-lifecycle");return;}var player=s.getComponent(ref,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this){trace("rejected-current-page");return;}
  if(a.action.equals("Close")){close();return;}

  if(!RocketService.controlRange(s,ref,playerRef,at)){close();return;}
  if(a.action.equals("Launch")){if(moon){close();RocketService.returnHome(s,ref,playerRef);}else RocketService.launch(s,ref,playerRef,at);return;}
  if(!moon&&a.action.equals("Rebuild"))RocketService.rebuildLegacy(s,ref,playerRef,at);
  if(!moon&&(a.action.equals("Fill")||a.action.equals("Drain")))RocketService.fuel(s,ref,playerRef,at,a.action.equals("Fill"));
  var update=new UICommandBuilder();labels(update,s);sendUpdate(update,false);
 });}
 @Override public void onDismiss(Ref<EntityStore>r,Store<EntityStore>s){dismissed=true;trace("dismiss");}
}
