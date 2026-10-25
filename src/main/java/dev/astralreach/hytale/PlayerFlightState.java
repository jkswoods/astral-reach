package dev.astralreach.hytale;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.astralreach.core.Journey;
import dev.astralreach.core.LaunchPlan;

/** Native player component travels with the real holder; no duplicated inventory representation. */
public final class PlayerFlightState implements Component<EntityStore> {
 enum Leg { NONE,LUNE_DESCENT,LUNE_ASCENT,HOME_DESCENT }
 public static final BuilderCodec<PlayerFlightState> CODEC=BuilderCodec.builder(PlayerFlightState.class,PlayerFlightState::new)
  .append(new KeyedCodec<>("PaidJourney",Codec.STRING),(p,v)->p.trip=v==null||v.isEmpty()?null:Journey.restore(v),p->p.trip==null?"":p.trip.snapshot()).add()
  .append(new KeyedCodec<>("HomeWorldName",Codec.STRING),(p,v)->p.homeWorldName=v,p->p.homeWorldName).add()
  .append(new KeyedCodec<>("RocketX",Codec.INTEGER),(p,v)->p.x=v,p->p.x).add()
  .append(new KeyedCodec<>("RocketY",Codec.INTEGER),(p,v)->p.y=v,p->p.y).add()
  .append(new KeyedCodec<>("RocketZ",Codec.INTEGER),(p,v)->p.z=v,p->p.z).add()
  .append(new KeyedCodec<>("Plan",Codec.STRING),(p,v)->p.plan=LaunchPlan.restore(v),p->p.plan.name()).add()
  .append(new KeyedCodec<>("Leg",Codec.STRING),(p,v)->p.leg=Leg.valueOf(v),p->p.leg.name()).add()
  .append(new KeyedCodec<>("LegSeconds",Codec.DOUBLE),(p,v)->{if(!Double.isFinite(v)||v<0||v>120)throw new IllegalArgumentException("Invalid flight time");p.legSeconds=v;},p->p.legSeconds).add()
  .append(new KeyedCodec<>("Boarded",Codec.BOOLEAN),(p,v)->p.boarded=v,p->p.boarded).add()
  .append(new KeyedCodec<>("DepartureX",Codec.DOUBLE),(p,v)->p.departureX=v,p->p.departureX).add()
  .append(new KeyedCodec<>("DepartureY",Codec.DOUBLE),(p,v)->p.departureY=v,p->p.departureY).add()
  .append(new KeyedCodec<>("DepartureZ",Codec.DOUBLE),(p,v)->p.departureZ=v,p->p.departureZ).add().build();
 Journey trip;String homeWorldName="AstralOrbis";LaunchPlan plan=LaunchPlan.ODYSSEY;int x,y,z;
 Leg leg=Leg.NONE;double legSeconds;boolean boarded;
 double departureX,departureY,departureZ;
 public PlayerFlightState(){}
 @Override public PlayerFlightState clone(){var p=new PlayerFlightState();p.trip=trip;p.homeWorldName=homeWorldName;p.plan=plan;p.x=x;p.y=y;p.z=z;p.leg=leg;p.legSeconds=legSeconds;p.boarded=boarded;p.departureX=departureX;p.departureY=departureY;p.departureZ=departureZ;return p;}
}
