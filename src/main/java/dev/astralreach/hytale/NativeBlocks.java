package dev.astralreach.hytale;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import static dev.astralreach.hytale.RocketRegistry.Cell;

final class NativeBlocks {
 static BlockType get(World w,Cell p){var chunks=w.getChunkStore();var ref=chunks.getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());if(ref==null||!ref.isValid())return null;var section=chunks.getStore().getComponent(ref,BlockSection.getComponentType());return section==null?null:BlockType.getAssetMap().getAsset(section.get(p.x()&31,p.y()&31,p.z()&31));}
 static String base(BlockType b){if(b==null)return "";var d=b.getDefaultStateKey();return d==null?b.getId():d;}
 static boolean rocket(World w,Cell p){var id=base(get(w,p));return id.equals("AR_OdysseyHeavy")||id.equals("AR_KestrelStack");}
 static int filler(World w,Cell p){var chunks=w.getChunkStore();var ref=chunks.getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());if(ref==null||!ref.isValid())return -1;var section=chunks.getStore().getComponent(ref,BlockSection.getComponentType());return section==null?-1:section.getFiller(p.x()&31,p.y()&31,p.z()&31);}
 static Cell root(World w,Cell p){if(get(w,p)==null)return null;var v=BlockOperations.resolveBaseBlockPosition(w.getChunkStore(),new com.hypixel.hytale.protocol.BlockPosition(p.x(),p.y(),p.z()));return new Cell(v.x,v.y,v.z);}
 static int rotation(World w,Cell p){var chunks=w.getChunkStore();var ref=chunks.getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());if(ref==null||!ref.isValid())return 0;var section=chunks.getStore().getComponent(ref,BlockSection.getComponentType());return section==null?0:section.getRotationIndex(p.x()&31,p.y()&31,p.z()&31);}
 static boolean ticking(World w,Cell p){var chunks=w.getChunkStore();var ref=chunks.getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());return ref!=null&&ref.isValid()&&!chunks.getStore().getArchetype(ref).contains(com.hypixel.hytale.server.core.universe.world.storage.ChunkStore.REGISTRY.getNonTickingComponentType());}
 static boolean empty(World w,Cell p){var chunks=w.getChunkStore();var ref=chunks.getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());if(ref==null||!ref.isValid())return false;var section=chunks.getStore().getComponent(ref,BlockSection.getComponentType());return section!=null&&section.get(p.x()&31,p.y()&31,p.z()&31)==BlockType.EMPTY_ID&&section.getFiller(p.x()&31,p.y()&31,p.z()&31)==0;}
 static void state(World w,Cell p,String state){if(!rocket(w,p))return;stateOf(w,p,base(get(w,p)),state);}
 static void stateOf(World w,Cell p,String id,String state){if(!base(get(w,p)).equals(id))return;var b=BlockType.getAssetMap().getAsset(id);set(w,p,state.equals("default")?b:b.getBlockForState(state));}
 static boolean set(World w,Cell p,BlockType target){
  if(target==null)return false;var chunks=w.getChunkStore();var ref=chunks.getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());if(ref==null||!ref.isValid())return false;
  if(get(w,p)!=null&&get(w,p).getId().equals(target.getId()))return true;
  var section=chunks.getStore().getComponent(ref,BlockSection.getComponentType());if(section==null)return false;int rotation=section.getRotationIndex(p.x()&31,p.y()&31,p.z()&31);
  BlockOperations.setBlock(chunks,ref,p.x(),p.y(),p.z(),BlockType.getAssetMap().getIndex(target.getId()),target,rotation,0,198);return get(w,p)!=null&&get(w,p).getId().equals(target.getId());
 }
}
