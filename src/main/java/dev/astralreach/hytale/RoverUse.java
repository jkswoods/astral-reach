package dev.astralreach.hytale;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
public final class RoverUse extends SimpleInstantInteraction{
 public static final BuilderCodec<RoverUse>CODEC=BuilderCodec.builder(RoverUse.class,RoverUse::new,SimpleInstantInteraction.CODEC).build();
 @Override protected void firstRun(InteractionType t,InteractionContext c,CooldownHandler cooldown){var r=c.getEntity();var target=c.getTargetEntity();c.getCommandBuffer().getExternalData().getWorld().execute(()->{if(r.isValid()&&target!=null&&target.isValid()&&r.getStore()==target.getStore())RoverService.use(r.getStore(),r,target);});}
}
