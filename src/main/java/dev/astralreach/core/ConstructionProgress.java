package dev.astralreach.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Quantity/state contract only. Native integration owns the actual typed escrow and world commit. */
public final class ConstructionProgress {
 public static final long BUILD_MILLIS=10_000;
 public enum Phase {PREVIEW,ASSEMBLING,READY}
 private LaunchPlan plan;
 private Phase phase=Phase.PREVIEW;
 private final Map<String,Integer> supplied=new HashMap<>();
 private long workedMillis;
 private boolean celebrated;
 public ConstructionProgress(LaunchPlan plan){this.plan=Objects.requireNonNull(plan);}
 public LaunchPlan plan(){return plan;}
 public Phase phase(){return phase;}
 public long workedMillis(){return workedMillis;}
 public boolean celebrated(){return celebrated;}
 public Map<String,Integer> supplied(){return Map.copyOf(supplied);}
 public int remaining(String id){return plan.materials().getOrDefault(id,0)-supplied.getOrDefault(id,0);}
 public boolean funded(){return plan.materials().keySet().stream().allMatch(id->remaining(id)==0);}
 public void select(LaunchPlan next){
  if(phase!=Phase.PREVIEW||!supplied.isEmpty())throw new IllegalStateException("Refund supplied materials before changing the blueprint");
  plan=Objects.requireNonNull(next);
 }
 public void accept(String id,int quantity){
  if(phase!=Phase.PREVIEW||quantity<=0||quantity>remaining(id))throw new IllegalArgumentException("Material transfer exceeds the current blueprint");
  supplied.merge(id,quantity,Math::addExact);
 }
 public boolean start(boolean formed,boolean clear){
  if(phase!=Phase.PREVIEW||!funded()||!formed||!clear)return false;
  phase=Phase.ASSEMBLING;return true;
 }
 public void advance(long millis,boolean active){
  if(millis<0)throw new IllegalArgumentException("Negative construction time");
  if(phase==Phase.ASSEMBLING&&active)workedMillis+=Math.min(millis,BUILD_MILLIS-workedMillis);
 }
 public boolean due(){return phase==Phase.ASSEMBLING&&workedMillis==BUILD_MILLIS;}
 /** Call only after the authoritative native rocket placement/ownership commit succeeds. */
 public void complete(){if(!due())throw new IllegalStateException("Construction is not ready to commit");phase=Phase.READY;}
 public boolean claimCelebration(){if(phase!=Phase.READY||celebrated)return false;celebrated=true;return true;}
 /** Call only after every escrow item has been returned through native output preflight. */
 public void resetAfterRefund(){if(phase==Phase.READY)throw new IllegalStateException("Completed rocket materials were spent");supplied.clear();phase=Phase.PREVIEW;workedMillis=0;celebrated=false;}
 public static ConstructionProgress restore(LaunchPlan plan,Map<String,Integer> supplied,Phase phase,long millis,boolean celebrated){
  var p=new ConstructionProgress(plan);
  for(var e:supplied.entrySet())p.accept(e.getKey(),e.getValue());
  if(millis<0||millis>BUILD_MILLIS||phase==Phase.PREVIEW&&millis!=0||phase!=Phase.PREVIEW&&!p.funded()||phase==Phase.READY&&millis!=BUILD_MILLIS||phase!=Phase.READY&&celebrated)throw new IllegalArgumentException("Invalid saved construction state");
  p.phase=Objects.requireNonNull(phase);p.workedMillis=millis;p.celebrated=celebrated;return p;
 }
}
