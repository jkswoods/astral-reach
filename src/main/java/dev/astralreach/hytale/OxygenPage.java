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
final class OxygenPage extends InteractiveCustomUIPage<OxygenPage.Action>{
 static final class Action{String action="";static final BuilderCodec<Action> CODEC=BuilderCodec.builder(Action.class,Action::new).append(new KeyedCodec<>("Action",Codec.STRING),(a,v)->a.action=v,a->a.action).add().build();}
 private final RocketRegistry.Cell at;
 OxygenPage(PlayerRef p,RocketRegistry.Cell at){super(p,CustomPageLifetime.CanDismiss,Action.CODEC);this.at=at;}
 static void open(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,RocketRegistry.Cell at){try{OxygenService.usable(s,r,p,at);s.getComponent(r,Player.getComponentType()).getPageManager().openCustomPage(r,s,new OxygenPage(p,at));}catch(IllegalArgumentException ex){AstralReachPlugin.message(p,ex.getMessage());}}
 @Override public void build(Ref<EntityStore>r,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){c.append("Pages/AstralReach/Oxygen.ui");labels(s,c);for(String id:new String[]{"Load","Collect","Toggle","Close"})e.addEventBinding(CustomUIEventBindingType.Activating,"#"+id,EventData.of("Action",id),false);}
 private void labels(Store<EntityStore>s,UICommandBuilder c){var d=OxygenService.data(s).diffusers.get(at);c.set("#Status.Text",d==null?"Diffuser unavailable":(d.powered?"BREATHABLE FIELD":d.enabled?"EMPTY / NOT LOADED":"SWITCHED OFF")+"\n"+d.items.size()+" / 4 canisters · "+Math.round(d.oxygen()/12)+"% total canister charge\n"+d.people+" people supplied");}
 void refresh(Store<EntityStore>s){var c=new UICommandBuilder();labels(s,c);sendUpdate(c,false);}
 @Override public void handleDataEvent(Ref<EntityStore>r,Store<EntityStore>s,Action a){s.getExternalData().getWorld().execute(()->{var p=s.getComponent(r,Player.getComponentType());if(p==null||p.getPageManager().getCustomPage()!=this)return;if(a.action.equals("Close")){close();return;}OxygenService.action(s,r,playerRef,at,a.action);refresh(s);});}
}
