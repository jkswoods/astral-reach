package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.AudioUpdate;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
/** The native audio tracker only visits entities that still have an AudioComponent. */
final class RocketAudio {
 static void set(Store<EntityStore>s,Ref<EntityStore>r,boolean powered){
  if(r==null||!r.isValid()||r.getStore()!=s)return;
  var type=AudioComponent.getComponentType();var before=s.getComponent(r,type);
  if(powered){if(before==null||before.getSoundEventIds().length==0){var a=new AudioComponent();a.addSound(com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex("AR_rocket_thrust_loop"));s.putComponent(r,type,a);}}
  else if(before!=null&&before.getSoundEventIds().length>0){s.putComponent(r,type,new AudioComponent());var visible=s.getComponent(r,EntityTrackerSystems.Visible.getComponentType());if(visible!=null)for(var viewer:visible.visibleTo.values())viewer.queueUpdate(r,new AudioUpdate(new int[0]));}
 }
 private RocketAudio(){}
}
