package dev.astralreach.hytale;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** Presentation helpers only: inventories, journeys and work remain native server authority. */
final class NativeUi {
 private static final Map<UUID,Long> cues=new java.util.concurrent.ConcurrentHashMap<>();
 private NativeUi(){}
 static String number(int n){return String.format(Locale.ROOT,"%,d",n);}
 static String materials(Map<String,Integer> costs){var parts=new ArrayList<String>();for(var e:new TreeMap<>(costs).entrySet()){String name=switch(e.getKey()){case "AR_PressureHullPart"->"hull sections";case "AR_OrbitalEnginePart"->"engine parts";case "AR_FlightComputer"->"flight computers";case "AR_FramePlate"->"frame plates";case "AR_ThermalShield"->"Thermal Shields";default->e.getKey();};parts.add(e.getValue()+" "+name);}return String.join(" · ",parts);}
 static String duration(double seconds){int n=(int)Math.max(0,Math.min(Integer.MAX_VALUE,seconds));return (n/60)+":"+String.format(Locale.ROOT,"%02d",n%60);}
 static void height(UICommandBuilder c,String id,int height){var a=new Anchor();a.setHeight(Value.of(height));c.setObject(id+".Anchor",a);}
 static void bounds(UICommandBuilder c,String id,int width,int height){var a=new Anchor();a.setWidth(Value.of(width));a.setHeight(Value.of(height));c.setObject(id+".Anchor",a);}
 static void cue(Store<EntityStore>s,PlayerRef p){long now=System.nanoTime();if(now-cues.getOrDefault(p.getUuid(),0L)<180_000_000L)return;cues.put(p.getUuid(),now);LunarEnvironment.interior(s,p.getReference(),p,"AR_Console_Select");}
}
