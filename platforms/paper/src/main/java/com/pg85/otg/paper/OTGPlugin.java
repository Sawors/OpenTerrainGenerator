package com.pg85.otg.paper;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.paper.biome.OTGBiomeProvider;
import com.pg85.otg.paper.commands.OTGCommandExecutor;
import com.pg85.otg.paper.events.OTGHandler;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.paper.gen.OTGPaperChunkGen;
import com.pg85.otg.paper.networking.NetworkingListener;
import com.pg85.otg.paper.util.ObfuscationHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.generator.CustomChunkGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;


public class OTGPlugin extends JavaPlugin implements Listener
{
	private static final ReentrantLock initLock = new ReentrantLock();
	private static final HashMap<String, String> worlds = new HashMap<>();
	private static final HashSet<String> processedWorlds = new HashSet<>();

	@SuppressWarnings("unused")
	private OTGHandler handler;

	public static OTGPlugin plugin;

	static
	{
		try
		{
			Field field = CustomChunkGenerator.class.getDeclaredField("delegate");
			field.setAccessible(true);
		} catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void onDisable ()
	{
		// Experimental test to stop crash on server stop for spigot
		// OTG.stopEngine();
	}

	@Override
	public void onEnable ()
	{
		plugin = this;
		Field frozen;
		try
		{
			frozen = ObfuscationHelper.getField(MappedRegistry.class, "frozen", "bL");
			// Make the frozen boolean accessible
			frozen.setAccessible(true);
			// Set the 'frozen' boolean to false for this registry
			frozen.set(Registry.BIOME_SOURCE, false);
			frozen.set(Registry.CHUNK_GENERATOR, false);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.BIOME_REGISTRY, "Failed to unfreeze registry");
			e.printStackTrace();
		}
		Registry.register(Registry.BIOME_SOURCE, new ResourceLocation(Constants.MOD_ID_SHORT, "default"), OTGBiomeProvider.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Constants.MOD_ID_SHORT, "default"), OTGNoiseChunkGenerator.CODEC);

		// Re-freeze the two registries
		try
		{
			frozen = ObfuscationHelper.getField(MappedRegistry.class, "frozen", "bL");
			frozen.setAccessible(true);
			frozen.set(Registry.BIOME_SOURCE, true);
			frozen.set(Registry.CHUNK_GENERATOR, true);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.BIOME_REGISTRY, "Failed to re-freeze registry");
			e.printStackTrace();
		}

		OTG.startEngine(new PaperEngine(this));
		
		OTGCommandExecutor.registerArguments();
		OTGCommandExecutor.register(((CraftServer)Bukkit.getServer()).getServer().vanillaCommandDispatcher.getDispatcher());
		// Does this go here?
		OTG.getEngine().getPresetLoader().registerBiomes();

		Registry<Biome> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
		int i = 0;

		if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
		{
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "-----------------");
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Registered biomes:");
			for (Biome biomeBase : biome_registry)
			{
				OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, (i++) + ": " + biomeBase.toString());
			}
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "-----------------");
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		this.handler = new OTGHandler(this);	
		Bukkit.getPluginManager().registerEvents(new NetworkingListener(this), this);
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator (String worldName, String id)
	{
		if (id == null || id.equals(""))
		{
			id = "Default";
		}
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(id);
		if (preset == null)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Could not find preset '" + id + "', did you install it correctly?");
			return null;
		}
		worlds.put(worldName, id);
		return new OTGPaperChunkGen(preset);
	}

	@EventHandler
	public void onWorldEnable (WorldInitEvent event)
	{
		if (worlds.containsKey(event.getWorld().getName()))
		{
			// Most likely no longer needed, but keeping it just in case. The lock keeps it from doing it double anyway.
			injectInternalGenerator(event.getWorld());
		}
	}

	public void injectInternalGenerator(World world)
	{
		initLock.lock();
		if (processedWorlds.contains(world.getName()))
		{
			// We have already processed this world, return
			return;
		}

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Taking over world " + world.getName());
		ServerLevel serverWorld = ((CraftWorld) world).getHandle();

		net.minecraft.world.level.chunk.ChunkGenerator generator = serverWorld.getChunkSource().getGenerator();
		if (!(generator instanceof CustomChunkGenerator))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Mission failed, we'll get them next time");
			return;
		}
		if (!(world.getGenerator() instanceof OTGPaperChunkGen OTGGen))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "World generator was not an OTG generator, cannot take over, something has gone wrong");
			return;
		}
		// We have a CustomChunkGenerator and a NoiseChunkGenerator
		OTGNoiseChunkGenerator OTGDelegate;
		// If generator is null, it has not been initialized yet. Initialize it.
		// The lock is used to avoid the accidental creation of two separate objects, in case
		// of a race condition.
		if (OTGGen.generator == null)
		{
			RegistryAccess registryAccess = ((CraftServer) Bukkit.getServer()).getServer().registryAccess();
			NoiseSettings noiseSettings = getNoiseSettings(OTGGen.getPreset().getWorldConfig());
			OTGDelegate = new OTGNoiseChunkGenerator(
				OTGGen.getPreset().getFolderName(),
				new OTGBiomeProvider(OTGGen.getPreset().getFolderName(), world.getSeed(), false, false, registryAccess.registryOrThrow(Registry.BIOME_REGISTRY)),
				registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
				registryAccess.registryOrThrow(Registry.NOISE_REGISTRY),
				world.getSeed(),
				Holder.direct(new NoiseGeneratorSettings(noiseSettings, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), getNoiseRouter(noiseSettings), SurfaceRuleData.overworld(), 63, false, true, true, false))
			);
			// add the weird Spigot config; it was complaining about this
			OTGDelegate.conf = serverWorld.spigotConfig;
		} else {
			OTGDelegate = OTGGen.generator;
		}

		try
		{
			
			//Field finalGenerator = ObfuscationHelper.getField(ChunkMap.class, "generator", "t");
			Field finalGenerator = ObfuscationHelper.getField(ChunkMap.class, "generator", "u");
			finalGenerator.setAccessible(true);

			finalGenerator.set(serverWorld.getChunkSource().chunkMap, OTGDelegate);

			/*Field pcmGen = ObfuscationHelper.getField(ChunkMap.class, "generator", "r");
			pcmGen.setAccessible(true);

			pcmGen.set(serverWorld.getChunkSource().chunkMap, OTGDelegate);*/
		} catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}

		if (OTGGen.generator == null)
		{
			OTGGen.generator = OTGDelegate;
		}

		// Spigot may have started generating - we gotta regen if so

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Success!");

		processedWorlds.add(world.getName());

		initLock.unlock();
	}

	// Taken from vanilla NoiseRouterData::overworldWithoutCaves
	private NoiseRouterWithOnlyNoises getNoiseRouter(NoiseSettings noiseSettings) {
		DensityFunction shiftX = getFunction("shift_x");
		DensityFunction shiftZ = getFunction("shift_z");
		return new NoiseRouterWithOnlyNoises(
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, BuiltinRegistries.NOISE.getHolderOrThrow(Noises.TEMPERATURE)),
				DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, BuiltinRegistries.NOISE.getHolderOrThrow(Noises.VEGETATION)),
				getFunction("overworld/continents"),
				getFunction("overworld/erosion"),
				getFunction("overworld/depth"),
				getFunction("overworld/ridges"),
				DensityFunctions.mul(DensityFunctions.constant(4.0D), DensityFunctions.mul(getFunction("overworld/depth"), DensityFunctions.cache2d(getFunction("overworld/factor"))).quarterNegative()),
				DensityFunctions.mul(DensityFunctions.interpolated(DensityFunctions.blendDensity(DensityFunctions.slide(noiseSettings, getFunction("overworld/sloped_cheese")))), DensityFunctions.constant(0.64D)).squeeze(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero()
		);
	}

	private DensityFunction getFunction(String string) {
		return BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, new ResourceLocation(string))).value();
	}

	private NoiseSettings getNoiseSettings(IWorldConfig worldConfig) {
		return NoiseSettings.create(
				worldConfig.getWorldMinY(),
				worldConfig.getWorldHeight(),
				new NoiseSamplingSettings(1.0D, 1.0D, 80.0D, 160.0D),
				new NoiseSlider(-0.078125D, 2, 8),
				new NoiseSlider(0.1171875D, 3, 0),
				1,
				2,
				TerrainProvider.overworld(false));
	}
}
