package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.astralreach.core.RoverPower;
final class RoverHud extends CustomUIHud{
 static final String KEY="AstralReach.Rover";private String last="";
 RoverHud(PlayerRef p){super(p,KEY,12);}
 @Override protected void build(UICommandBuilder c){c.append("Pages/AstralReach/RoverHud.ui");}
 void show(RoverBody rover,boolean passenger){String text=(passenger?"PASSENGER":"DRIVER")+" · "+NativeUi.duration(rover.power.seconds())+" drive power";String speed=String.format(java.util.Locale.ROOT,"%.1f m/s",Math.abs(rover.speed));String id=text+speed;if(id.equals(last))return;last=id;var c=new UICommandBuilder();c.set("#Power.Value",(double)rover.power.units()/RoverPower.CAPACITY);c.set("#RoverPower.Text",text);c.set("#Speed.Text",speed);c.set("#Controls.Text",passenger?"Dismount (F) to leave the passenger seat":"W/S drive · A/D steer · Space brake · F exit");update(false,c);}
}
