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

/** Free-cursor native scene. A small route page opens only after an actual planet selection. */
final class OrbitalMapPage extends InteractiveCustomUIPage<OrbitalMapPage.Action>{
 static final class Action{String action="";static final BuilderCodec<Action> CODEC=BuilderCodec.builder(Action.class,Action::new).append(new KeyedCodec<>("Action",Codec.STRING),(a,v)->a.action=v,a->a.action).add().build();}
 private record Body(String title,String resources,String route){}
 private static final Map<String,Body> BODIES=Map.of(
  "Orbis",new Body("Orbis · Home","Nickel + oil shale","Return to your saved launch platform. Only recovered stages count toward rebuilding."),
  "Lune",new Body("Lune · First expedition","Titanium + aluminium · Survey suit","Land your upper stage, explore the lava tubes and gather lunar metals. Bring a full suit, oxygen and spare canisters."),
  "Rustreach",new Body("Rustreach · Second destination","Vanadium + ferrite","Requires Thermal suit + Thermal Shield. Landing route under development."),
  "Nacre",new Body("Nacre · Frozen ocean","Lithium + cryolite","Requires Deep Space suit + Pressure Hull. Landing route under development."),
  "Vesper",new Body("Vesper · Cloudlands","Iridium + sulfur","Cryogenic Drive progression. Landing route under development."),
  "Umbra",new Body("Umbra · Outer moon","Aetherium + void quartz","Radiation Core progression. Landing route under development."));
 private static final String HUD="AstralReach.Map";
 private static final class Session{final Store<EntityStore> store;final PlayerRef player;final OrbitalScene scene;final byte originalSlot;boolean dragging,loggedMotion,dragMoved;double dragged;int pressTarget=-1,lastSlot;long watchAt;org.joml.Vector3i pressPlane;Session(Store<EntityStore>s,PlayerRef p){store=s;player=p;scene=new OrbitalScene(s,p);var bar=s.getComponent(p.getReference(),InventoryComponent.Hotbar.getComponentType());originalSlot=bar==null?s.getComponent(p.getReference(),Player.getComponentType()).getInventory().getActiveHotbarSlot():bar.getActiveSlot();lastSlot=originalSlot;}}
 private static final Map<UUID,Session> viewing=new java.util.concurrent.ConcurrentHashMap<>();
 private final Session session;private final String selected;
 OrbitalMapPage(Session session,String selected){super(session.player,CustomPageLifetime.CanDismiss,Action.CODEC);this.session=session;this.selected=selected;}
 static boolean viewing(UUID id){return viewing.containsKey(id);}
 static void open(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p){if(!SpaceFlight.inOrbit(s,r)){AstralReachPlugin.message(p,"Navigation becomes available after reaching space.");return;}var player=s.getComponent(r,Player.getComponentType());if(player==null)return;var session=viewing.get(p.getUuid());if(session==null){session=new Session(s,p);viewing.put(p.getUuid(),session);player.getHudManager().addCustomHud(p,new CustomUIHud(p,HUD,7){@Override protected void build(UICommandBuilder c){c.append("Pages/AstralReach/OrbitalMapHud.ui");}});}session.scene.view();}
 static void route(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,String name){String body=BODIES.keySet().stream().filter(v->v.equalsIgnoreCase(name)).findFirst().orElse(null);if(body==null){AstralReachPlugin.message(p,"Choose Orbis, Lune, Rustreach, Nacre, Vesper or Umbra.");return;}open(s,r,p);var session=viewing.get(p.getUuid());if(session==null||session.store!=s||!SpaceFlight.inOrbit(s,r))return;session.scene.select(body);s.getComponent(r,Player.getComponentType()).getPageManager().openCustomPage(r,s,new OrbitalMapPage(session,body));}
 private static void stop(Session session){if(!viewing.remove(session.player.getUuid(),session))return;var world=session.store.getExternalData().getWorld();if(world.isInThread())session.scene.close();else world.execute(session.scene::close);var r=session.player.getReference();if(r!=null&&r.isValid()){var current=r.getStore();Runnable remove=()->{if(!r.isValid()||r.getStore()!=current)return;var player=current.getComponent(r,Player.getComponentType());if(player!=null){player.getHudManager().removeCustomHud(session.player,HUD);var bar=current.getComponent(r,InventoryComponent.Hotbar.getComponentType());if(bar!=null&&session.originalSlot>=0&&session.originalSlot<bar.getInventory().getCapacity()&&bar.getActiveSlot()!=session.originalSlot){bar.setActiveSlot(session.originalSlot,r,current);session.player.getPacketHandler().writeNoCache(new com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot(bar.getSectionId(),session.originalSlot));}}};if(current.getExternalData().getWorld().isInThread())remove.run();else current.getExternalData().getWorld().execute(remove);}}
 static void close(PlayerRef p){var session=viewing.get(p.getUuid());if(session!=null)stop(session);}
 static void cleanup(Store<EntityStore>s){for(var session:new ArrayList<>(viewing.values()))if(session.store==s){var r=session.player.getReference();if(r==null||!r.isValid()||r.getStore()!=s||!SpaceFlight.inOrbit(s,r)){stop(session);continue;}long now=System.nanoTime();if(now-session.watchAt<75_000_000L||active(r)==null)continue;session.watchAt=now;var player=s.getComponent(r,Player.getComponentType());if(player!=null){var inv=player.getInventory();observeSlot(session,inv.getActiveHotbarSlot(),inv.getHotbar().getCapacity());}}}
 private static void observeSlot(Session session,int next,int count){int previous=session.lastSlot;session.lastSlot=next;if(next==previous||next<0||next>=count||previous<0||previous>=count)return;int delta=Math.floorMod(next-previous+count/2,count)-count/2;if(Math.abs(delta)==1){session.scene.zoom=Math.clamp(session.scene.zoom+delta*7,32,130);session.scene.view();System.out.println("AR_MAP_SCROLL step="+delta);}}

 private static Session active(Ref<EntityStore>r){if(r==null||!r.isValid())return null;var s=r.getStore();var p=s.getComponent(r,PlayerRef.getComponentType());if(p==null)return null;var session=viewing.get(p.getUuid());if(session==null||session.store!=s||!SpaceFlight.inOrbit(s,r))return null;var player=s.getComponent(r,Player.getComponentType());return player!=null&&player.getPageManager().getCustomPage()==null?session:null;}
 static void button(PlayerMouseButtonEvent e){var r=e.getPlayerRef();var session=active(r);if(session==null)return;e.setCancelled(true);var button=e.getMouseButton();if(button==null)return;System.out.println("AR_MAP_BUTTON "+button.mouseButtonType+" "+button.state+" screen="+e.getScreenPoint()+" plane="+e.getTargetBlock());boolean pressed=button.state==MouseButtonState.Pressed;
  if(pressed&&button.mouseButtonType==MouseButtonType.Right){stop(session);return;}if(pressed&&button.mouseButtonType==MouseButtonType.Middle){session.scene.reset();return;}if(button.mouseButtonType!=MouseButtonType.Left)return;
  int target=-1;var hit=e.getTargetEntityRef();if(hit!=null&&hit.isValid()){var id=session.store.getComponent(hit,NetworkId.getComponentType());if(id!=null)target=id.getId();}
  if(pressed){session.dragging=true;session.dragMoved=false;session.dragged=0;session.pressTarget=target;session.pressPlane=e.getTargetBlock()==null?null:new org.joml.Vector3i(e.getTargetBlock());return;}
  // Some client input paths report only button endpoints. Preserve actual drag semantics there.
  if(session.dragging&&!session.dragMoved&&session.pressPlane!=null&&e.getTargetBlock()!=null){double dx=e.getTargetBlock().x-session.pressPlane.x,dz=e.getTargetBlock().z-session.pressPlane.z;session.dragged=Math.hypot(dx,dz)*12;if(session.dragged>=5)session.scene.dragPlane(dx,dz);}
  if(session.dragging&&session.dragged<5){String body=session.scene.hit(target>=0?target:session.pressTarget);if(body==null)body=session.scene.hitPlane(e.getTargetBlock());if(BODIES.containsKey(body==null?"":body)){session.scene.select(body);session.store.getComponent(r,Player.getComponentType()).getPageManager().openCustomPage(r,session.store,new OrbitalMapPage(session,body));}}
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
 @Override public void build(Ref<EntityStore>r,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){c.append("Pages/AstralReach/OrbitalMap.ui");var b=BODIES.get(selected);c.set("#Selected.Text",b.title);c.set("#Resources.Text",b.resources);c.set("#Route.Text",b.route);var f=s.getComponent(r,AstralReachPlugin.flightType);c.set("#Reserve.Text","Paid return: "+(f!=null&&f.trip!=null?f.trip.returnUnits():0)+" units");c.set("#Land.Visible",selected.equals("Lune"));c.set("#Home.Visible",selected.equals("Orbis"));for(String id:new String[]{"Land","Home","Close"})e.addEventBinding(CustomUIEventBindingType.Activating,"#"+id,EventData.of("Action",id),false);}
 @Override public void handleDataEvent(Ref<EntityStore>r,Store<EntityStore>s,Action a){s.getExternalData().getWorld().execute(()->{var player=s.getComponent(r,Player.getComponentType());if(player==null||player.getPageManager().getCustomPage()!=this||!SpaceFlight.inOrbit(s,r))return;if(a.action.equals("Close")){close();return;}if(a.action.equals("Land")&&selected.equals("Lune")){try{ExpeditionSuit.require(s,r,1);}catch(IllegalArgumentException ex){AstralReachPlugin.message(playerRef,ex.getMessage());return;}stop(session);close();RocketService.chooseLune(s,r,playerRef);}else if(a.action.equals("Home")&&selected.equals("Orbis")){stop(session);close();RocketService.returnHome(s,r,playerRef);}});}
 @Override public void onDismiss(Ref<EntityStore>r,Store<EntityStore>s){if(viewing.get(playerRef.getUuid())==session)s.getExternalData().getWorld().execute(session.scene::view);}
}
