package dev.astralreach.core;
import java.util.*;
/** A physical transponder's finite wall-clock cell and private invitation policy. */
public final class RescueBeacon {
 public static final long CELL_MILLIS=30*60*1000L;
 public static final int INVITES=8;
 private final UUID owner;private final String ownerName;private final Map<UUID,String> guests=new LinkedHashMap<>();private long expires;private boolean enabled;
 public RescueBeacon(UUID owner,String name){this.owner=Objects.requireNonNull(owner);ownerName=Objects.requireNonNull(name);}
 public UUID owner(){return owner;}public String ownerName(){return ownerName;}public long expires(){return expires;}public boolean enabled(){return enabled;}public Map<UUID,String> guests(){return Map.copyOf(guests);}
 public boolean allows(UUID player){return owner.equals(player)||guests.containsKey(player);}
 public long remaining(long now){return Math.max(0,Math.min(CELL_MILLIS,expires-now));}
 public boolean active(long now){return enabled&&remaining(now)>0;}
 private void own(UUID actor){if(!owner.equals(actor))throw new IllegalArgumentException("Only the beacon owner can change its controls.");}
 public boolean canLoad(UUID actor,long now){own(actor);return remaining(now)==0;}
 public void load(UUID actor,long now){if(!canLoad(actor,now))throw new IllegalArgumentException("The current cell still has charge.");expires=Math.addExact(now,CELL_MILLIS);enabled=true;}
 public void toggle(UUID actor,long now){own(actor);if(remaining(now)==0)throw new IllegalArgumentException("Load a Beacon Cell first.");enabled=!enabled;}
 public void invite(UUID actor,UUID id,String name){own(actor);Objects.requireNonNull(id);if(id.equals(owner))throw new IllegalArgumentException("You already own this beacon.");if(!guests.containsKey(id)&&guests.size()>=INVITES)throw new IllegalArgumentException("This beacon already has eight invited rescuers.");guests.put(id,Objects.requireNonNull(name));}
 public void revoke(UUID actor,UUID id){own(actor);guests.remove(id);}
 public void restore(long deadline,boolean enabled,Map<UUID,String> invitees){if(deadline<0||invitees.size()>INVITES||invitees.containsKey(owner))throw new IllegalArgumentException("Invalid saved beacon.");expires=deadline;this.enabled=enabled;guests.clear();guests.putAll(invitees);}
}
