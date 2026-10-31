package dev.astralreach.hytale;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;
public final class StationInteraction extends SimpleBlockInteraction{
 public static final BuilderCodec<StationInteraction>CODEC=BuilderCodec.builder(StationInteraction.class,StationInteraction::new,SimpleBlockInteraction.CODEC).build();
 @Override protected void interactWithBlock(World w,CommandBuffer<EntityStore>b,InteractionType t,InteractionContext c,ItemStack held,Vector3i at,CooldownHandler cd){var r=c.getEntity();var p=b.getComponent(r,PlayerRef.getComponentType());if(p!=null)w.execute(()->{if(r.isValid())StationService.use(r.getStore(),r,p,new RocketRegistry.Cell(at.x,at.y,at.z));});}
 @Override protected void simulateInteractWithBlock(InteractionType t,InteractionContext c,ItemStack h,World w,Vector3i at){}
}
