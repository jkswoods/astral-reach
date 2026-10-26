package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
final class OxygenHud extends CustomUIHud{
 static final String KEY="AstralReach.Oxygen";private String last="";
 OxygenHud(PlayerRef p){super(p,KEY,8);}
 @Override protected void build(UICommandBuilder c){c.append("Pages/AstralReach/OxygenHud.ui");}
 void status(String text,double oxygen,String mode){String id=text+mode+(int)oxygen;if(id.equals(last))return;last=id;var c=new UICommandBuilder();c.set("#Oxygen.Text",text.replace("O₂  ",""));c.set("#Environment.Text",mode);c.set("#OxygenFill.Value",Math.clamp(oxygen/1200,0,1));update(false,c);}
}
