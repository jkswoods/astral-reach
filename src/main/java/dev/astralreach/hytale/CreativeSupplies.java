package dev.astralreach.hytale;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
/** Only the server-owned native game mode enables virtual supplies; these are never refundable escrow. */
final class CreativeSupplies {
 static boolean enabled(Store<EntityStore>s,Ref<EntityStore>r){var p=s.getComponent(r,Player.getComponentType());return p!=null&&p.getGameMode()==GameMode.Creative;}
 private CreativeSupplies(){}
}
