package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** A saved recovery marker ejects a reloaded rider safely; native entity refs remain transient. */
public final class RoverSeat implements Component<EntityStore>{
 long moveAt,inputTraceAt;int inputTraces;org.joml.Vector3d sent;Ref<EntityStore>vehicle;boolean passenger,nativeAttached;String world="";
 public static final BuilderCodec<RoverSeat>CODEC=BuilderCodec.builder(RoverSeat.class,RoverSeat::new)
  .append(new KeyedCodec<>("World",Codec.STRING),(c,v)->c.world=v,c->c.world).add().build();
 public RoverSeat(){}
 RoverSeat(Ref<EntityStore>v,boolean passenger,String world){vehicle=v;this.passenger=passenger;this.world=world;}
 // Runtime ECS/teleport copies retain the seat; only CODEC decoding clears live refs.
 @Override public RoverSeat clone(){var c=new RoverSeat(vehicle,passenger,world);c.inputTraceAt=inputTraceAt;c.inputTraces=inputTraces;c.nativeAttached=nativeAttached;c.moveAt=moveAt;c.sent=sent==null?null:new org.joml.Vector3d(sent);return c;}
}
