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
  var r=RocketService.data(s).rockets.get(at);var f=s.getComponent(playerRef.getReference(),AstralReachPlugin.flightType);
  c.set("#RocketTitle.Text",moon?"Lune Return Beacon":(r==null?"Rocket":r.plan.title)+" · Flight Control");
  c.set("#Status.Text",moon?(f!=null&&f.trip!=null?"Return reserve: "+f.trip.returnUnits()+" units paid at home":"No paid journey. This beacon does not grant travel."):(r==null?"Rocket unavailable":r.traveller!=null?"Mission in progress · tank locked":r.needsRebuild?"Returned vehicle · replacement parts required":"Fuel: "+r.tank.units()+" / 9,000 units needed"));
  c.set("#Route.Text",moon?"Return to your saved home departure point with your gathered ores.":"Home to Lune and return. Requires 9,000 units: 6,000 outward + 3,000 protected for return.");
  c.set("#Rebuild.Visible",!moon&&r!=null&&r.needsRebuild);c.set("#Fill.Visible",!moon&&r!=null&&!r.needsRebuild&&r.traveller==null&&r.tank.units()<9000);c.set("#Launch.Visible",moon||r!=null&&!r.needsRebuild&&r.traveller==null&&r.tank.units()>=9000);
  if(!moon&&r!=null&&r.needsRebuild)c.set("#Route.Text","Returned assemblies are credited. Missing parts: "+dev.astralreach.core.RocketRecovery.missing(r.plan,r.boosterLanded));
  boolean fill=!moon&&r!=null&&!r.needsRebuild&&r.traveller==null&&r.tank.units()<9000,drain=!moon&&r!=null&&!r.needsRebuild&&r.traveller==null&&r.tank.units()>=1000;
  c.set("#Fuel.Visible",fill||drain);c.set("#FillRow.Visible",fill);c.set("#Drain.Visible",drain);c.set("#Launch.Text",moon?"Board and return to Orbis":"Board and launch");
  var fuelBounds=new com.hypixel.hytale.server.core.ui.Anchor();fuelBounds.setHeight(com.hypixel.hytale.server.core.ui.Value.of((fill?88:0)+(drain?44:0)));c.setObject("#Fuel.Anchor",fuelBounds);
  var bounds=new com.hypixel.hytale.server.core.ui.Anchor();bounds.setWidth(com.hypixel.hytale.server.core.ui.Value.of(640));bounds.setHeight(com.hypixel.hytale.server.core.ui.Value.of(moon?360:390+(fill?88:0)+(drain?44:0)));c.setObject("#FlightPanel.Anchor",bounds);
 }
 @Override public void build(Ref<EntityStore>ref,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  trace("build");
  c.append("Pages/AstralReach/Rocket.ui");labels(c,s);
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
