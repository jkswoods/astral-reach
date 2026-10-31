package dev.astralreach.hytale;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;
public final class RoverPlace extends SimpleBlockInteraction{
 public static final BuilderCodec<RoverPlace>CODEC=BuilderCodec.builder(RoverPlace.class,RoverPlace::new,SimpleBlockInteraction.CODEC).build();
 @Override protected void interactWithBlock(World w,CommandBuffer<EntityStore>b,InteractionType t,InteractionContext c,ItemStack held,Vector3i at,CooldownHandler cooldown){if(ItemStack.isEmpty(held)||!held.getItemId().equals("AR_LunarRover"))return;var r=c.getEntity();var inv=c.getHeldItemContainer();short slot=c.getHeldItemSlot();var position=new org.joml.Vector3d(at.x+.5,at.y+1,at.z+.5);w.execute(()->{if(r.isValid()&&r.getStore().getExternalData().getWorld()==w&&inv!=null&&held.equals(inv.getItemStack(slot)))RoverService.deploy(r.getStore(),r,position,inv,slot,held);});}
 @Override protected void simulateInteractWithBlock(InteractionType t,InteractionContext c,ItemStack held,World w,Vector3i at){}
}
