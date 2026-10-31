package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.RoverPower;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.util.UUID;

/** Persistent vehicle ownership/energy; occupancy and input never survive a disconnected client. */
public final class RoverBody implements Component<EntityStore> {
 UUID owner=new UUID(0,0);RoverPower power=new RoverPower(0);
 ItemStack kit=new ItemStack("AR_LunarRover",1);
 Ref<EntityStore>driver,passenger;double speed,throttle,steer,brake,inputAge;boolean wish,moving;
 public static final BuilderCodec<RoverBody>CODEC=BuilderCodec.builder(RoverBody.class,RoverBody::new)
  .append(new KeyedCodec<>("Owner",Codec.STRING),(c,v)->c.owner=UUID.fromString(v),c->c.owner.toString()).add()
  .append(new KeyedCodec<>("Charge",Codec.LONG),(c,v)->c.power=new RoverPower(v),c->c.power.units()).add()
  .append(new KeyedCodec<>("Kit",ItemStack.CODEC),(c,v)->{if(ItemStack.isEmpty(v)||!v.getItemId().equals("AR_LunarRover")||v.getQuantity()!=1)throw new IllegalArgumentException("Invalid rover kit");c.kit=YardInventory.copy(v);},c->c.kit).add().build();
 @Override public RoverBody clone(){var c=new RoverBody();c.owner=owner;c.power=new RoverPower(power.units());c.kit=YardInventory.copy(kit);c.driver=driver;c.passenger=passenger;c.speed=speed;c.throttle=throttle;c.steer=steer;c.brake=brake;c.inputAge=inputAge;c.wish=wish;c.moving=moving;return c;}
}
