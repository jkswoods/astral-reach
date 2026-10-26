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
import java.util.*;

/** Native top-down destination controls. The separate 3D option is retired. */
final class OrbitalMapPage extends InteractiveCustomUIPage<OrbitalMapPage.Action>{
 static final class Action{String action="";static final BuilderCodec<Action> CODEC=BuilderCodec.builder(Action.class,Action::new).append(new KeyedCodec<>("Action",Codec.STRING),(a,v)->a.action=v,a->a.action).add().build();}
 private record Body(String subtitle,String resource0,String label0,String resource1,String label1,String equipment,String detail){}
 private static final List<String> ORDER=List.of("Orbis","Lune","Rustreach","Nacre","Vesper","Umbra");
 private static final Map<String,Body> BODIES=Map.of(
  "Orbis",new Body("Home","AR_NickelOre","Nickel","AR_OilShale","Oil shale","Survey suit","Returned assemblies are credited."),
  "Lune",new Body("First expedition","AR_TitaniumOre","Titanium","AR_AluminiumOre","Aluminium","Survey suit","Your upper stage lands with you."),
  "Rustreach",new Body("Second expedition","","Vanadium","","Ferrite","Thermal suit","Thermal Shield · future route"),
  "Nacre",new Body("Frozen ocean","","Lithium","","Cryolite","Deep Space suit","Pressure Hull · future route"),
  "Vesper",new Body("Cloudlands","","Iridium","","Sulfur","Deep Space suit","Cryogenic Drive · future route"),
  "Umbra",new Body("Outer moon","","Aetherium","","Void quartz","Deep Space suit","Radiation Core · future route"));
 private static final List<String> VIEW_ACTIONS=List.of("PanLeft","PanUp","PanDown","PanRight","ZoomOut","ZoomIn","Fit");
 private static final Map<String,int[]> POINTS=Map.of("Orbis",new int[]{200,255,126},"Lune",new int[]{350,295,62},"Rustreach",new int[]{385,150,86},"Nacre",new int[]{360,400,90},"Vesper",new int[]{200,445,94},"Umbra",new int[]{490,460,84});
 private static final class Session{
  final Store<EntityStore> store;final PlayerRef player;final dev.astralreach.core.ChartViewport viewport=new dev.astralreach.core.ChartViewport();String selected="Lune";
  Session(Store<EntityStore>s,PlayerRef p){store=s;player=p;}
 }
 private static final Map<UUID,Session> viewing=new java.util.concurrent.ConcurrentHashMap<>();
 private final Session session;private String selected;private long refreshAt;
 OrbitalMapPage(Session session,String selected){super(session.player,CustomPageLifetime.CanDismiss,Action.CODEC);this.session=session;this.selected=selected;}
 static boolean viewing(UUID id){return viewing.containsKey(id);}
 static boolean threeD(UUID id){return false;}
 static void open(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  if(!SpaceFlight.inOrbit(s,r)){AstralReachPlugin.message(p,"Navigation becomes available after reaching space.");return;}
  if(s.getComponent(r,Player.getComponentType())==null)return;
  var session=viewing.computeIfAbsent(p.getUuid(),id->new Session(s,p));showDestinations(session,session.selected);
 }
 private static void showDestinations(Session session,String body){
  var r=session.player.getReference();if(r==null||!r.isValid()||r.getStore()!=session.store||!SpaceFlight.inOrbit(session.store,r))return;
  session.selected=body;var player=session.store.getComponent(r,Player.getComponentType());player.getPageManager().openCustomPage(r,session.store,new OrbitalMapPage(session,body));
 }
 static void route(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,String name){
  String body=ORDER.stream().filter(v->v.equalsIgnoreCase(name)).findFirst().orElse(null);
  if(body==null){AstralReachPlugin.message(p,"Choose Orbis, Lune, Rustreach, Nacre, Vesper or Umbra.");return;}
  if(!SpaceFlight.inOrbit(s,r)){AstralReachPlugin.message(p,"Navigation becomes available after reaching space.");return;}
  var session=viewing.computeIfAbsent(p.getUuid(),id->new Session(s,p));showDestinations(session,body);
 }
 private static void stop(Session session){viewing.remove(session.player.getUuid(),session);}
 static void close(PlayerRef p){var session=viewing.get(p.getUuid());if(session!=null)stop(session);}
 static void cleanup(Store<EntityStore>s){for(var session:new ArrayList<>(viewing.values()))if(session.store==s){var r=session.player.getReference();if(r==null||!r.isValid()||r.getStore()!=s||!SpaceFlight.inOrbit(s,r))stop(session);}}
 private static void place(UICommandBuilder c,String id,int x,int y,int width,int height){var a=new com.hypixel.hytale.server.core.ui.Anchor();a.setLeft(com.hypixel.hytale.server.core.ui.Value.of(x));a.setTop(com.hypixel.hytale.server.core.ui.Value.of(y));a.setWidth(com.hypixel.hytale.server.core.ui.Value.of(width));a.setHeight(com.hypixel.hytale.server.core.ui.Value.of(height));c.setObject(id+".Anchor",a);}
 private void chart(UICommandBuilder c){
  var v=session.viewport;place(c,"#ChartCanvas",v.left(),v.top(),v.scaled(640),v.scaled(614));
  for(String name:ORDER){int[] p=POINTS.get(name);int size=v.scaled(p[2]);place(c,"#Planet"+name,v.scaled(p[0]),v.scaled(p[1]),size+18,size+32);place(c,"#Ring"+name,0,0,size+6,size+6);place(c,"#Glyph"+name,3,3,size,size);place(c,"#PlanetLabel"+name,2,size+7,size+30,25);}
  c.set("#ZoomLevel.Text",v.percent()+"%");c.set("#ZoomIn.Disabled",!v.canZoomIn());c.set("#ZoomOut.Disabled",!v.canZoomOut());c.set("#Fit.Disabled",!v.moved());for(String id:List.of("PanLeft","PanUp","PanDown","PanRight"))c.set("#"+id+".Disabled",!v.canZoomOut());
 }
 private void labels(Ref<EntityStore>r,UICommandBuilder c,Store<EntityStore>s){
  var body=BODIES.get(selected);var f=s.getComponent(r,AstralReachPlugin.flightType);var p=s.getComponent(r,Player.getComponentType());int tier=ExpeditionSuit.tier(s,r);double oxygen=OxygenCanisters.total(p.getInventory().getCombinedStorageHotbarBackpack());
  c.set("#Selected.Text",selected.toUpperCase(Locale.ROOT));c.set("#Route.Text",body.subtitle);c.set("#Detail.Text",body.detail);
  c.set("#Resource0.Visible",!body.resource0.isEmpty());if(!body.resource0.isEmpty())c.set("#Resource0.ItemId",body.resource0);c.set("#ResourceLabel0.Text",body.label0);
  c.set("#Resource1.Visible",!body.resource1.isEmpty());if(!body.resource1.isEmpty())c.set("#Resource1.ItemId",body.resource1);c.set("#ResourceLabel1.Text",body.label1);
  int required=selected.equals("Rustreach")?2:selected.equals("Orbis")||selected.equals("Lune")?1:3;
  c.set("#SuitIcon.ItemId",required==1?"AR_Survey_Chest":required==2?"AR_Thermal_Chest":"AR_DeepSpace_Chest");c.set("#Suit.Text",(tier>=required?"":"Requires ")+body.equipment);c.set("#SuitCheck.Visible",tier>=required);c.set("#OxygenCheck.Visible",oxygen>0);c.set("#Oxygen.Text",oxygen>0?"Oxygen · "+NativeUi.duration(oxygen):"Carry filled oxygen canisters");
  int reserve=f!=null&&f.trip!=null?f.trip.returnUnits():0;c.set("#Reserve.Text",NativeUi.number(reserve)+" units protected");c.set("#Flight.Text","In orbit · "+(f==null?"Vehicle":f.plan.title));
  boolean available=dev.astralreach.core.DestinationAccess.canTravel(selected,f!=null&&f.trip!=null&&reserve>0,tier);c.set("#Travel.Text",selected.equals("Orbis")?"Return to Orbis":selected.equals("Lune")?"Land on Lune":"Route unavailable");c.set("#Travel.Disabled",!available);
  for(String name:ORDER){boolean active=name.equals(selected);c.set("#Chosen"+name+".Visible",active);c.set("#Ring"+name+".Visible",active);c.set("#Preview"+name+".Visible",active);c.set("#Mini"+name+".Visible",active);}
  chart(c);
 }
 @Override public void build(Ref<EntityStore>r,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  c.append("Pages/AstralReach/OrbitalMap.ui");labels(r,c,s);NativeUi.cue(s,playerRef);
  for(String body:ORDER)for(String prefix:List.of("Route","Planet"))e.addEventBinding(CustomUIEventBindingType.Activating,"#"+prefix+body,EventData.of("Action","Select:"+body),false);
  for(String id:List.of("Travel","Close","PanLeft","PanUp","PanDown","PanRight","ZoomOut","ZoomIn","Fit"))e.addEventBinding(CustomUIEventBindingType.Activating,"#"+id,EventData.of("Action",id),false);
 }
 @Override public void handleDataEvent(Ref<EntityStore>r,Store<EntityStore>s,Action action){s.getExternalData().getWorld().execute(()->{
  if(!r.isValid()||r.getStore()!=s||viewing.get(playerRef.getUuid())!=session||!SpaceFlight.inOrbit(s,r))return;var player=s.getComponent(r,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this)return;
  String a=action.action;if(a.equals("Close")){stop(session);close();return;}
  if(VIEW_ACTIONS.contains(a)&&session.viewport.action(a)){NativeUi.cue(s,playerRef);var c=new UICommandBuilder();chart(c);sendUpdate(c,false);return;}
  if(a.startsWith("Select:")){String body=a.substring(7);if(!BODIES.containsKey(body)||body.equals(selected))return;selected=body;session.selected=body;NativeUi.cue(s,playerRef);var c=new UICommandBuilder();labels(r,c,s);sendUpdate(c,false);return;}
  if(!a.equals("Travel")||!selected.equals("Lune")&&!selected.equals("Orbis"))return;
  try{if(selected.equals("Lune"))ExpeditionSuit.require(s,r,1);}catch(IllegalArgumentException ex){AstralReachPlugin.message(playerRef,ex.getMessage());return;}
  NativeUi.cue(s,playerRef);stop(session);close();if(selected.equals("Lune"))RocketService.chooseLune(s,r,playerRef);else RocketService.returnHome(s,r,playerRef);
 });}
 void refresh(Ref<EntityStore>r,Store<EntityStore>s){long now=System.nanoTime();if(now-refreshAt<1_000_000_000L)return;refreshAt=now;var c=new UICommandBuilder();labels(r,c,s);sendUpdate(c,false);}
 @Override public void onDismiss(Ref<EntityStore>r,Store<EntityStore>s){if(viewing.get(playerRef.getUuid())==session)s.getExternalData().getWorld().execute(()->{
  if(viewing.get(playerRef.getUuid())!=session||!r.isValid()||r.getStore()!=s)return;
  var p=s.getComponent(r,Player.getComponentType());if(p!=null&&p.getPageManager().getCustomPage() instanceof OrbitalMapPage next&&next.session==session)return;
  stop(session);
 });}
}
