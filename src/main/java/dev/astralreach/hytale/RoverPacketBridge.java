package dev.astralreach.hytale;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.server.core.io.handlers.GenericPacketHandler;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Two per-connection slots; install/close only on that connection's processing thread. */
final class RoverPacketBridge implements AutoCloseable {
 private static final Field HANDLERS;
 static {try{HANDLERS=GenericPacketHandler.class.getDeclaredField("handlers");HANDLERS.setAccessible(true);}catch(ReflectiveOperationException ex){throw new ExceptionInInitializerError(ex);}}
 private final GenericPacketHandler handler;private final int[] ids;
 private final Consumer<ToServerPacket>[] original,wrappers;private boolean closed;
 @SuppressWarnings("unchecked") private static Consumer<ToServerPacket>[] slots(GenericPacketHandler handler){try{return (Consumer<ToServerPacket>[])HANDLERS.get(handler);}catch(IllegalAccessException ex){throw new IllegalStateException("Cannot preserve native rover packet handlers",ex);}}
 @SuppressWarnings("unchecked") private RoverPacketBridge(GenericPacketHandler handler,Predicate<Packet> consume,int[] ids){
  this.handler=handler;this.ids=ids.clone();original=new Consumer[ids.length];wrappers=new Consumer[ids.length];var slots=slots(handler);
  // Preflight every original before changing any slot. Preserve the exact native/mod chain.
  for(int i=0;i<ids.length;i++){int id=ids[i];if(id<0||id>=slots.length||slots[id]==null)throw new IllegalStateException("Missing native rover packet handler "+id);original[i]=slots[id];}
  for(int i=0;i<ids.length;i++){var next=original[i];wrappers[i]=packet->{if(!consume.test(packet))next.accept(packet);};handler.registerHandler(ids[i],wrappers[i]);}
 }
 static RoverPacketBridge attach(GenericPacketHandler handler,Predicate<Packet> consume,int[] ids){return new RoverPacketBridge(handler,consume,ids);}
 @Override public void close(){if(closed)return;closed=true;var slots=slots(handler);for(int i=0;i<ids.length;i++)if(ids[i]<slots.length&&slots[ids[i]]==wrappers[i])handler.registerHandler(ids[i],original[i]);}
}
