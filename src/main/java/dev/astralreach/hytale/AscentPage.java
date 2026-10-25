package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Transparent flight overlay. Escape/abort restores the exact paid pre-orbit reservation. */
final class AscentPage extends InteractiveCustomUIPage<AscentPage.Action>{
 static final class Action{String action="";static final BuilderCodec<Action> CODEC=BuilderCodec.builder(Action.class,Action::new).append(new KeyedCodec<>("Action",Codec.STRING),(a,v)->a.action=v,a->a.action).add().build();}
 boolean handoff,dismissed;String stage="";final boolean cancellable;
 AscentPage(PlayerRef p){this(p,true);}
 AscentPage(PlayerRef p,boolean cancellable){super(p,CustomPageLifetime.CanDismiss,Action.CODEC);this.cancellable=cancellable;}
 @Override public void build(Ref<EntityStore>r,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){c.append("Pages/AstralReach/Ascent.ui");c.set("#Abort.Visible",cancellable);if(cancellable)e.addEventBinding(CustomUIEventBindingType.Activating,"#Abort",EventData.of("Action","Abort"),false);}
 void status(String label,int percent){if(dismissed||label.equals(stage))return;stage=label;var c=new UICommandBuilder();c.set("#Stage.Text",label);sendUpdate(c,false);}
 void finish(){handoff=true;if(!dismissed)close();}
 @Override public void handleDataEvent(Ref<EntityStore>r,Store<EntityStore>s,Action a){if(!handoff&&a.action.equals("Abort"))close();}
 @Override public void onDismiss(Ref<EntityStore>r,Store<EntityStore>s){dismissed=true;if(!handoff&&cancellable)s.getExternalData().getWorld().execute(()->RocketService.abort(s,r,playerRef));}
}
