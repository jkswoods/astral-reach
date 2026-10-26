package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseMotionEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InventorySetActiveSlotEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** Native destination controls with an optional free-cursor 3D scene. */
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
 private static final String HUD="AstralReach.Map";
 private static final class Session{final Store<EntityStore> store;final PlayerRef player;final OrbitalScene scene;final byte originalSlot;boolean dragging,loggedMotion,dragMoved,threeD;String selected="Lune";double dragged;int pressTarget=-1,lastSlot;long watchAt;org.joml.Vector3i pressPlane;Session(Store<EntityStore>s,PlayerRef p){store=s;player=p;scene=new OrbitalScene(s,p);var bar=s.getComponent(p.getReference(),InventoryComponent.Hotbar.getComponentType());originalSlot=bar==null?s.getComponent(p.getReference(),Player.getComponentType()).getInventory().getActiveHotbarSlot():bar.getActiveSlot();lastSlot=originalSlot;}}
 private static final Map<UUID,Session> viewing=new java.util.concurrent.ConcurrentHashMap<>();
 private final Session session;private String selected;private boolean sceneHandoff;private long refreshAt;
 OrbitalMapPage(Session session,String selected){super(session.player,CustomPageLifetime.CanDismiss,Action.CODEC);this.session=session;this.selected=selected;}
 static boolean viewing(UUID id){return viewing.containsKey(id);}
 static void open(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){
  if(!SpaceFlight.inOrbit(s,r)){AstralReachPlugin.message(p,"Navigation becomes available after reaching space.");return;}
  var player=s.getComponent(r,Player.getComponentType());if(player==null)return;var session=viewing.get(p.getUuid());
  if(session==null){session=new Session(s,p);viewing.put(p.getUuid(),session);}showDestinations(session,session.selected);
 }
 private static void showDestinations(Session session,String body){
  var r=session.player.getReference();if(r==null||!r.isValid()||r.getStore()!=session.store||!SpaceFlight.inOrbit(session.store,r))return;
  session.threeD=false;session.selected=body;var player=session.store.getComponent(r,Player.getComponentType());player.getHudManager().removeCustomHud(session.player,HUD);
  session.scene.select(body);session.scene.view();player.getPageManager().openCustomPage(r,session.store,new OrbitalMapPage(session,body));
 }
 private static void showScene(Session session){
  var r=session.player.getReference();if(r==null||!r.isValid()||r.getStore()!=session.store)return;session.threeD=true;session.dragging=false;
  session.store.getComponent(r,Player.getComponentType()).getHudManager().addCustomHud(session.player,new CustomUIHud(session.player,HUD,7){@Override protected void build(UICommandBuilder c){c.append("Pages/AstralReach/OrbitalMapHud.ui");}});session.scene.view();
 }
 static void route(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,String name){String body=ORDER.stream().filter(v->v.equalsIgnoreCase(name)).findFirst().orElse(null);if(body==null){AstralReachPlugin.message(p,"Choose Orbis, Lune, Rustreach, Nacre, Vesper or Umbra.");return;}if(!SpaceFlight.inOrbit(s,r)){AstralReachPlugin.message(p,"Navigation becomes available after reaching space.");return;}var session=viewing.get(p.getUuid());if(session==null){session=new Session(s,p);viewing.put(p.getUuid(),session);}showDestinations(session,body);}
 private static void stop(Session session){if(!viewing.remove(session.player.getUuid(),session))return;var world=session.store.getExternalData().getWorld();if(world.isInThread())session.scene.close();else world.execute(session.scene::close);var r=session.player.getReference();if(r!=null&&r.isValid()){var current=r.getStore();Runnable remove=()->{if(!r.isValid()||r.getStore()!=current)return;var player=current.getComponent(r,Player.getComponentType());if(player!=null){player.getHudManager().removeCustomHud(session.player,HUD);var bar=current.getComponent(r,InventoryComponent.Hotbar.getComponentType());if(bar!=null&&session.originalSlot>=0&&session.originalSlot<bar.getInventory().getCapacity()&&bar.getActiveSlot()!=session.originalSlot){bar.setActiveSlot(session.originalSlot,r,current);session.player.getPacketHandler().writeNoCache(new com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot(bar.getSectionId(),session.originalSlot));}}};if(current.getExternalData().getWorld().isInThread())remove.run();else current.getExternalData().getWorld().execute(remove);}}
 static void close(PlayerRef p){var session=viewing.get(p.getUuid());if(session!=null)stop(session);}
 static void cleanup(Store<EntityStore>s){for(var session:new ArrayList<>(viewing.values()))if(session.store==s){var r=session.player.getReference();if(r==null||!r.isValid()||r.getStore()!=s||!SpaceFlight.inOrbit(s,r)){stop(session);continue;}long now=System.nanoTime();if(now-session.watchAt<75_000_000L||active(r)==null)continue;session.watchAt=now;var player=s.getComponent(r,Player.getComponentType());if(player!=null){var inv=player.getInventory();observeSlot(session,inv.getActiveHotbarSlot(),inv.getHotbar().getCapacity());}}}
 private static void observeSlot(Session session,int next,int count){int previous=session.lastSlot;session.lastSlot=next;if(next==previous||next<0||next>=count||previous<0||previous>=count)return;int delta=Math.floorMod(next-previous+count/2,count)-count/2;if(Math.abs(delta)==1){session.scene.zoom=Math.clamp(session.scene.zoom+delta*7,32,130);session.scene.view();System.out.println("AR_MAP_SCROLL step="+delta);}}

 private static Session active(Ref<EntityStore>r){if(r==null||!r.isValid())return null;var s=r.getStore();var p=s.getComponent(r,PlayerRef.getComponentType());if(p==null)return null;var session=viewing.get(p.getUuid());if(session==null||session.store!=s||!SpaceFlight.inOrbit(s,r))return null;var player=s.getComponent(r,Player.getComponentType());return session.threeD&&player!=null&&player.getPageManager().getCustomPage()==null?session:null;}
 static void button(PlayerMouseButtonEvent e){var r=e.getPlayerRef();var session=active(r);if(session==null)return;e.setCancelled(true);var button=e.getMouseButton();if(button==null)return;System.out.println("AR_MAP_BUTTON "+button.mouseButtonType+" "+button.state+" screen="+e.getScreenPoint()+" plane="+e.getTargetBlock());boolean pressed=button.state==MouseButtonState.Pressed;
  if(pressed&&button.mouseButtonType==MouseButtonType.Right){showDestinations(session,session.selected);return;}if(pressed&&button.mouseButtonType==MouseButtonType.Middle){session.scene.reset();return;}if(button.mouseButtonType!=MouseButtonType.Left)return;
  int target=-1;var hit=e.getTargetEntityRef();if(hit!=null&&hit.isValid()){var id=session.store.getComponent(hit,NetworkId.getComponentType());if(id!=null)target=id.getId();}
  if(pressed){session.dragging=true;session.dragMoved=false;session.dragged=0;session.pressTarget=target;session.pressPlane=e.getTargetBlock()==null?null:new org.joml.Vector3i(e.getTargetBlock());return;}
  // Some client input paths report only button endpoints. Preserve actual drag semantics there.
  if(session.dragging&&!session.dragMoved&&session.pressPlane!=null&&e.getTargetBlock()!=null){double dx=e.getTargetBlock().x-session.pressPlane.x,dz=e.getTargetBlock().z-session.pressPlane.z;session.dragged=Math.hypot(dx,dz)*12;if(session.dragged>=5)session.scene.dragPlane(dx,dz);}
  if(session.dragging&&session.dragged<5){String body=session.scene.hit(target>=0?target:session.pressTarget);if(body==null)body=session.scene.hitPlane(e.getTargetBlock());if(BODIES.containsKey(body==null?"":body)){showDestinations(session,body);}}
  session.dragging=false;
 }
 static void motion(PlayerMouseMotionEvent e){var session=active(e.getPlayerRef());if(session==null)return;e.setCancelled(true);var m=e.getMouseMotion();if(!session.loggedMotion){session.loggedMotion=true;System.out.println("AR_MAP_MOTION native plane="+e.getTargetBlock()+" screen="+e.getScreenPoint());}if(!session.dragging)return;
  if(session.pressPlane!=null&&e.getTargetBlock()!=null){double dx=e.getTargetBlock().x-session.pressPlane.x,dz=e.getTargetBlock().z-session.pressPlane.z;if(dx!=0||dz!=0){session.dragMoved=true;session.dragged+=Math.hypot(dx,dz)*12;session.scene.dragPlane(dx,dz);}return;}
  if(m==null||m.relativeMotion==null)return;double dx=Math.clamp(m.relativeMotion.x,-300,300),dy=Math.clamp(m.relativeMotion.y,-300,300);if(dx!=0||dy!=0){session.dragMoved=true;session.dragged+=Math.hypot(dx,dy);session.scene.drag(dx,dy);}
 }

 /** Observe actual native slot changes; the hidden held slot is restored after map closure. */
 static final class Wheel extends EntityEventSystem<EntityStore,InventorySetActiveSlotEvent>{
  Wheel(){super(InventorySetActiveSlotEvent.class);}@Override public Query<EntityStore>getQuery(){return PlayerRef.getComponentType();}
  @Override public void handle(int i,ArchetypeChunk<EntityStore>c,Store<EntityStore>s,CommandBuffer<EntityStore>b,InventorySetActiveSlotEvent e){var p=c.getComponent(i,PlayerRef.getComponentType());if(p==null||!viewing.containsKey(p.getUuid()))return;var session=active(c.getReferenceTo(i));var hotbar=c.getComponent(i,InventoryComponent.Hotbar.getComponentType());System.out.println("AR_MAP_SLOT section="+e.getInventorySectionId()+" previous="+e.getPreviousSlot()+" next="+e.getNewSlot()+" active="+(session!=null)+" hotbar="+(hotbar!=null));if(session==null||hotbar==null||e.getInventorySectionId()!=hotbar.getSectionId())return;int count=hotbar.getInventory().getCapacity(),next=e.getNewSlot(),previous=e.getPreviousSlot();if(next<0||next>=count||previous<0||previous>=count)return;observeSlot(session,next,count);}

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
  for(String name:ORDER){boolean active=name.equals(selected);c.set("#Chosen"+name+".Visible",active);c.set("#Ring"+name+".Visible",active);c.set("#Preview"+name+".Visible",active);}
 }
 @Override public void build(Ref<EntityStore>r,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
  c.append("Pages/AstralReach/OrbitalMap.ui");labels(r,c,s);NativeUi.cue(s,playerRef);
  for(String body:ORDER)for(String prefix:List.of("Route","Planet"))e.addEventBinding(CustomUIEventBindingType.Activating,"#"+prefix+body,EventData.of("Action","Select:"+body),false);
  for(String id:List.of("Explore","Travel","Close"))e.addEventBinding(CustomUIEventBindingType.Activating,"#"+id,EventData.of("Action",id),false);
 }
 @Override public void handleDataEvent(Ref<EntityStore>r,Store<EntityStore>s,Action action){s.getExternalData().getWorld().execute(()->{
  if(!r.isValid()||r.getStore()!=s||viewing.get(playerRef.getUuid())!=session||!SpaceFlight.inOrbit(s,r))return;var player=s.getComponent(r,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this)return;
  String a=action.action;if(a.equals("Close")){stop(session);close();return;}
  if(a.startsWith("Select:")){String body=a.substring(7);if(!BODIES.containsKey(body)||body.equals(selected))return;selected=body;session.selected=body;session.scene.select(body);NativeUi.cue(s,playerRef);var c=new UICommandBuilder();labels(r,c,s);sendUpdate(c,false);return;}
  if(a.equals("Explore")){NativeUi.cue(s,playerRef);sceneHandoff=true;close();showScene(session);return;}
  if(!a.equals("Travel")||!selected.equals("Lune")&&!selected.equals("Orbis"))return;
  try{if(selected.equals("Lune"))ExpeditionSuit.require(s,r,1);}catch(IllegalArgumentException ex){AstralReachPlugin.message(playerRef,ex.getMessage());return;}
  NativeUi.cue(s,playerRef);stop(session);close();if(selected.equals("Lune"))RocketService.chooseLune(s,r,playerRef);else RocketService.returnHome(s,r,playerRef);
 });}
 void refresh(Ref<EntityStore>r,Store<EntityStore>s){long now=System.nanoTime();if(now-refreshAt<1_000_000_000L)return;refreshAt=now;var c=new UICommandBuilder();labels(r,c,s);sendUpdate(c,false);}
 @Override public void onDismiss(Ref<EntityStore>r,Store<EntityStore>s){if(!sceneHandoff&&viewing.get(playerRef.getUuid())==session)s.getExternalData().getWorld().execute(()->{
  if(viewing.get(playerRef.getUuid())!=session||session.threeD||!r.isValid()||r.getStore()!=s)return;
  var p=s.getComponent(r,Player.getComponentType());if(p!=null&&p.getPageManager().getCustomPage() instanceof OrbitalMapPage next&&next.session==session)return;
  stop(session);
 });}
}
