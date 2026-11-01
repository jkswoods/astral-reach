package dev.astralreach.hytale;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.ProcessingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.*;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.math.vector.Transform;
import org.bson.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AstralReachPlugin extends JavaPlugin {
 static AstralReachPlugin instance;
 static ResourceType<EntityStore,RescueRegistry> rescueType;
 static ComponentType<EntityStore,FieldJournal> journalType;
 static ResourceType<EntityStore,StationRegistry> stationType;
 static ResourceType<EntityStore,StationSystemsRegistry> systemsType;
 static ResourceType<EntityStore,RocketRegistry> rocketType;
 static ResourceType<EntityStore,OxygenRegistry> oxygenType;
 static ResourceType<EntityStore,LaunchYardRegistry> yardType;
 static ResourceType<EntityStore,LandedShipRegistry> landedType;
 static ComponentType<EntityStore,PlayerFlightState> flightType;
 public AstralReachPlugin(JavaPluginInit init){super(init);}
 @Override protected void setup(){
  instance=this;
  RoverService.bodyType=getEntityStoreRegistry().registerComponent(RoverBody.class,"AstralLunarRoverV1",RoverBody.CODEC);
  RoverService.seatType=getEntityStoreRegistry().registerComponent(RoverSeat.class,"AstralRoverSeatV1",RoverSeat.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_PlaceRover",RoverPlace.class,RoverPlace.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_RoverUse",RoverUse.class,RoverUse.CODEC);
  getEntityStoreRegistry().registerSystem(new RoverService.Lifecycle());getEntityStoreRegistry().registerSystem(new RoverService.Inputs());getEntityStoreRegistry().registerSystem(new RoverService.Cars());RoverTransport.start();
  rescueType=getEntityStoreRegistry().registerResource(RescueRegistry.class,"AstralRescueV1",RescueRegistry.CODEC);
  journalType=getEntityStoreRegistry().registerComponent(FieldJournal.class,"AstralFieldJournalV1",FieldJournal.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_GuideTablet",GuideInteraction.class,GuideInteraction.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_FieldReceiver",FieldInteraction.class,FieldInteraction.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_FieldBlock",FieldBlockInteraction.class,FieldBlockInteraction.CODEC);
  getEntityStoreRegistry().registerSystem(new RescueService.Placement());getEntityStoreRegistry().registerSystem(new RescueService.Breaking());
  getCommandRegistry().registerCommand(new Field(false));getCommandRegistry().registerCommand(new Field(true));
  getCodecRegistry(IWorldGenProvider.CODEC).register("AstralOreWorld",OreWorldProvider.class,OreWorldProvider.CODEC);
  systemsType=getEntityStoreRegistry().registerResource(StationSystemsRegistry.class,"AstralStationSystemsV1",StationSystemsRegistry.CODEC);
  stationType=getEntityStoreRegistry().registerResource(StationRegistry.class,"AstralStationsV1",StationRegistry.CODEC);
  oxygenType=getEntityStoreRegistry().registerResource(OxygenRegistry.class,"AstralOxygenV1",OxygenRegistry.CODEC);
  rocketType=getEntityStoreRegistry().registerResource(RocketRegistry.class,"AstralRocketsV1",RocketRegistry.CODEC);
  yardType=getEntityStoreRegistry().registerResource(LaunchYardRegistry.class,"AstralLaunchYardsV1",LaunchYardRegistry.CODEC);
  landedType=getEntityStoreRegistry().registerResource(LandedShipRegistry.class,"AstralLandedShipsV1",LandedShipRegistry.CODEC);
  flightType=getEntityStoreRegistry().registerComponent(PlayerFlightState.class,"AstralPaidJourneyV1",PlayerFlightState.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_OpenOxygen",OxygenInteraction.class,OxygenInteraction.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_Observatory",ObservatoryInteraction.class,ObservatoryInteraction.CODEC);
  getEntityStoreRegistry().registerSystem(new StationSystems.Placement());getEntityStoreRegistry().registerSystem(new StationSystems.Breaking());
  getCodecRegistry(Interaction.CODEC).register("AR_OpenStation",StationInteraction.class,StationInteraction.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_OpenRocket",RocketInteraction.class,RocketInteraction.CODEC);
  getCodecRegistry(Interaction.CODEC).register("AR_OpenLaunchYard",LaunchYardInteraction.class,LaunchYardInteraction.CODEC);
  getEntityStoreRegistry().registerSystem(new StationService.Placement());getEntityStoreRegistry().registerSystem(new StationService.Breaking());getEntityStoreRegistry().registerSystem(new StationService.Damage());
  getEntityStoreRegistry().registerSystem(new VacuumTorches.Placement());getEntityStoreRegistry().registerSystem(new VacuumTorches.Use());
  getEntityStoreRegistry().registerSystem(new OxygenService.Breathing());getEntityStoreRegistry().registerSystem(new OxygenService.Placement());getEntityStoreRegistry().registerSystem(new OxygenService.Breaking());
  getEntityStoreRegistry().registerSystem(new LunarEnvironment.Mining());getEntityStoreRegistry().registerSystem(new RocketService.Tick());getEntityStoreRegistry().registerSystem(new RocketService.Placement());getEntityStoreRegistry().registerSystem(new RocketService.Breaking());
  getEntityStoreRegistry().registerSystem(new LaunchYardService.Placement());getEntityStoreRegistry().registerSystem(new LaunchYardService.Breaking());
  getCommandRegistry().registerCommand(new Frontier());getCommandRegistry().registerCommand(new Guide());getCommandRegistry().registerCommand(new Return());getCommandRegistry().registerCommand(new OrbitMap());getCommandRegistry().registerCommand(new Stations());
 }
 @Override protected void start(){
  verifyRecipes();RocketVisuals.model();
  getLogger().atInfo().log("Astral Reach development: native ores, processing, finite fuel and controlled paid Lune round trips registered. Native gameplay and human acceptance pending.");
 }
 static void message(PlayerRef p,String s){p.sendMessage(Message.raw(s));}
 private void verifyRecipes(){
  try(var in=getClass().getResourceAsStream("/astral-recipes.json")){
   if(in==null)throw new IllegalStateException("Missing recipe contract");var expected=BsonDocument.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8));
   for(var e:expected.entrySet()){
    String id=e.getKey();if(Item.getAssetMap().getAsset(id)==null)throw new IllegalStateException("Missing Astral item "+id);
    var data=e.getValue().asDocument();var req=data.getArray("BenchRequirement").getFirst().asDocument();var type=req.getString("Type").getValue().equals("Processing")?BenchType.Processing:BenchType.Crafting;String bench=req.getString("Id").getValue();
    Map<String,Integer> want=new TreeMap<>();for(var ingredient:data.getArray("Input")){var i=ingredient.asDocument();want.merge(i.getString("ItemId").getValue(),i.getNumber("Quantity").intValue(),Integer::sum);}
    var matches=CraftingPlugin.getBenchRecipes(type,bench).stream().filter(r->r.getPrimaryOutput()!=null&&id.equals(r.getPrimaryOutput().getItemId())).toList();
    if(matches.size()!=1)throw new IllegalStateException("Expected one native recipe for "+id+" at "+bench+", got "+matches.size());
    var r=matches.getFirst();Map<String,Integer> got=new TreeMap<>();for(var i:r.getInput())got.merge(i.getItemId(),i.getQuantity(),Integer::sum);
    if(!want.equals(got)||r.isKnowledgeRequired()!=data.getBoolean("KnowledgeRequired",BsonBoolean.FALSE).getValue()||r.getPrimaryOutput().getQuantity()!=data.getNumber("OutputQuantity").intValue())throw new IllegalStateException("Native recipe contract differs for "+id);
   }
   for(String id:List.of("AR_OrbitalSmelter","AR_ComponentPress","AR_FuelRefinery","AR_PropellantConditioner")){
    var b=BlockType.getAssetMap().getAsset(id);if(b==null||!(b.getBench() instanceof ProcessingBench p)||p.shouldAllowNoInputProcessing()||p.getFuel().length!=1)throw new IllegalStateException("Paid native processor missing: "+id);
   }
   getLogger().atInfo().log("AR_NATIVE_CONTRACT PASS: %s exact recipe contracts",expected.size());
  }catch(java.io.IOException e){throw new IllegalStateException("Cannot verify recipes",e);}
 }
 @Override protected void shutdown(){RoverTransport.stop();RescueService.shutdown();StationFlyovers.shutdown();}
 static CompletableFuture<World> world(boolean moon){
  var u=Universe.get();String name=moon?"AstralLune":"AstralOrbis";var loaded=u.getWorld(name);if(loaded!=null)return CompletableFuture.completedFuture(loaded);
  if(u.isWorldLoadable(name))return u.loadWorld(name);
  var cfg=new WorldConfig();cfg.setDisplayName(moon?"Astral Reach — Lune":"Astral Reach — Orbis Frontier");cfg.setWorldGenProvider(new OreWorldProvider(moon));cfg.setSeed(91023);cfg.setPvpEnabled(false);cfg.setSavingPlayers(true);cfg.setSaveNewChunks(true);
  if(moon){cfg.setForcedWeather("AR_LunarSurface");cfg.setGameplayConfig("AR_Lunar");cfg.setGameTimePaused(false);cfg.setSpawnProvider(new GlobalSpawnProvider(new Transform(16.5,65,16.5)));cfg.setSpawningNPC(false);cfg.setIsSpawnMarkersEnabled(false);}
  return u.makeWorld(name,u.getWorldsPath().resolve(name),cfg);
 }
 private static final class Field extends AbstractPlayerCommand{
  private final boolean expedition;
  Field(boolean expedition){super(expedition?"astral-expedition":"astral-rescue",expedition?"Open your Lune expedition receiver.":"Open private rescue signals and your beacon controls.");this.expedition=expedition;requireNoPermission();}
  @Override protected void execute(CommandContext c,Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,World w){FieldPage.open(s,r,p,null,expedition);}
 }
 private static final class Frontier extends AbstractPlayerCommand {
  Frontier(){super("astral-frontier","Enter the persistent Astral Orbis frontier. No materials or fuel are granted.");requireNoPermission();}
  @Override protected void execute(CommandContext context,Store<EntityStore>s,Ref<EntityStore> ref,PlayerRef p,World origin){
   var trip=s.getComponent(ref,flightType);if(trip!=null&&trip.trip!=null){message(p,"Complete your paid journey using the Lune beacon or /astral-return before entering Frontier.");return;}
   message(p,"Preparing the persistent Orbis Frontier. Gather iron/copper normally; nickel and oil shale occur in new natural rock.");
   world(false).thenCompose(w->w.getWorldConfig().getSpawnProvider().getSpawnPointAsync(w,p.getUuid()).thenApply(spawn->Map.entry(w,spawn))).thenAccept(dest->origin.execute(()->{
    if(!ref.isValid()||ref.getStore()!=s)return;var current=s.getComponent(ref,flightType);if(current!=null&&current.trip!=null){message(p,"Frontier entry cancelled: finish your paid journey first.");return;}s.putComponent(ref,Teleport.getComponentType(),Teleport.createForPlayer(dest.getKey(),dest.getValue()));
    message(p,"Orbis Frontier: craft an Astral Assembly Bench at the stock Workbench using 6 iron and 4 copper. /astral-guide lists the finite processing chain.");
   })).exceptionally(ex->{message(p,"Could not prepare Frontier: "+ex.getMessage());return null;});
  }
 }
 private static final class Guide extends AbstractPlayerCommand {
  Guide(){super("astral-guide","Show Astral Reach acquisition and fuel steps.");requireNoPermission();}
  @Override protected void execute(CommandContext c,Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,World w){
   GuidePage.open(s,r,p);
  }
 }
 private static final class Return extends AbstractPlayerCommand {
  Return(){super("astral-return","Recover your existing paid return from orbit or Lune. Does not grant travel or fuel.");requireNoPermission();}
  @Override protected void execute(CommandContext c,Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,World w){RocketService.returnHome(s,r,p);}
 }
 private static final class Stations extends AbstractPlayerCommand{
  Stations(){super("astral-station","Open station selection in orbit or your docked station controls.");requireNoPermission();}
  @Override protected void execute(CommandContext c,Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,World w){StationService.open(s,r,p);}
 }
 private static final class OrbitMap extends AbstractPlayerCommand {
  private final com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg<String> body=withOptionalArg("body","Optional keyboard route selection, for example Lune",com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes.STRING);
  OrbitMap(){super("astral-map","Open orbital navigation after reaching space.");requireNoPermission();}
  @Override protected void execute(CommandContext c,Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,World w){if(body.provided(c))OrbitalMapPage.route(s,r,p,body.get(c));else OrbitalMapPage.open(s,r,p);}
 }
}
