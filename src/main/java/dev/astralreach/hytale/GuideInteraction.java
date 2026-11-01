package dev.astralreach.hytale;
import com.hypixel.hytale.codec.builder.BuilderCodec;import com.hypixel.hytale.protocol.InteractionType;import com.hypixel.hytale.server.core.entity.InteractionContext;import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;import com.hypixel.hytale.server.core.universe.PlayerRef;
public final class GuideInteraction extends SimpleInstantInteraction{
 public static final BuilderCodec<GuideInteraction>CODEC=BuilderCodec.builder(GuideInteraction.class,GuideInteraction::new,SimpleInstantInteraction.CODEC).build();
 @Override protected void firstRun(InteractionType t,InteractionContext c,CooldownHandler h){var b=c.getCommandBuffer();var r=c.getEntity();var p=b.getComponent(r,PlayerRef.getComponentType());if(p!=null){var world=b.getExternalData().getWorld();world.execute(()->{if(r.isValid()&&r.getStore().getExternalData().getWorld()==world)GuidePage.open(r.getStore(),r,p);});}}
}
