package com.pg85.otg.configuration.settingType;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.biome.settings.ReplaceBlocks;
import com.pg85.otg.configuration.biome.settings.ReplacedBlocksMatrix;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.generator.surface.SurfaceGenerator;
import com.pg85.otg.util.MaterialSet;
import com.pg85.otg.util.Rotation;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import java.util.List;

/**
 * Acts as a factory for creating settings. Classes holding settings must
 * extends this class and call the appropriate methods to create settings.
 *
 * <p>We might eventually want the class to keep track of all settings
 * created. For now, it just creates instances of the appropriate settings
 * type.
 */
public abstract class Settings
{
	// OTG+

    protected static final Setting<List<ReplaceBlocks>> replaceBlocksListSetting(String name)
    {
        return new ReplaceBlocksListSetting(name);
    }

	//

    /**
     * Creates a setting that can be {@code true} or {@code false}.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<Boolean> booleanSetting(String name, boolean defaultValue)
    {
        return new BooleanSetting(name, defaultValue);
    }

    /**
     * Creates a setting that represents a RGB color.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<Integer> colorSetting(String name, String defaultValue)
    {
        return new ColorSetting(name, defaultValue);
    }

    /**
     * Creates a setting that represents double-precision floating point number.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @param min          Lowest allowed value.
     * @param max          Highest allowed value.
     * @return The newly created setting.
     */
    protected static final Setting<Double> doubleSetting(String name, double defaultValue, double min, double max)
    {
        return new DoubleSetting(name, defaultValue, min, max);
    }

    /**
     * Creates a setting that represents one of the options in the provided enum.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @return The newly created setting.
     */
    protected static final <T extends Enum<T>> Setting<T> enumSetting(String name, T defaultValue)
    {
        return new EnumSetting<T>(name, defaultValue);
    }

    /**
     * Creates a setting that represents single-precision floating point number.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @param min          Lowest allowed value.
     * @param max          Highest allowed value.
     * @return The newly created setting.
     */
    protected static final Setting<Float> floatSetting(String name, float defaultValue, float min, float max)
    {
        return new FloatSetting(name, defaultValue, min, max);
    }

    /**
     * Creates a setting that represents a whole number.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @param min          Lowest allowed value.
     * @param max          Highest allowed value.
     * @return The newly created setting.
     */
    protected static final Setting<Integer> intSetting(String name, int defaultValue, int min, int max)
    {
        return new IntSetting(name, defaultValue, min, max);
    }

    protected static final Setting<Rotation> rotationSetting(String name, Rotation defaultValue)
    {
        return new RotationSetting(name, defaultValue);
    }

    /**
     * Creates a setting that represents a whole number as a {@code long}.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @param min          Lowest allowed value.
     * @param max          Highest allowed value.
     * @return The newly created setting.
     */
    protected static final Setting<Long> longSetting(String name, long defaultValue, long min, long max)
    {
        return new LongSetting(name, defaultValue, min, max);
    }

    /**
     * Creates a setting that represents a set of block materials.
     * @param name          Name of the setting.
     * @param defaultValues Default values for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<MaterialSet> materialSetSetting(String name, DefaultMaterial... defaultValues)
    {
        return new MaterialSetSetting(name, defaultValues);
    }

    /**
     * Creates a setting that represents a set of block materials.
     * Warning: you will get an AssertionError later on (during config
     * reading) if you provide invalid materials.
     * {@link Settings#materialSetSetting(String, DefaultMaterial...)} is the
     * suggested alternative.
     * @param name          Name of the setting.
     * @param defaultValues Default values for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<MaterialSet> materialSetSetting(String name, String... defaultValues)
    {
        return new MaterialSetSetting(name, defaultValues);
    }

    /**
     * Creates a setting that represents one of the block materials in the game.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<LocalMaterialData> materialSetting(String name, DefaultMaterial defaultValue)
    {
        return new MaterialSetting(name, defaultValue);
    }

    /**
     * Creates a setting that represents a list of possible mob spawns.
     * @param name Name of the setting.
     * @return The newly created setting.
     */
    protected static final Setting<List<WeightedMobSpawnGroup>> mobGroupListSetting(String name)
    {
        return new MobGroupListSetting(name);
    }

    /**
     * Creates a setting that represents a {@link ReplacedBlocksMatrix}.
     * @param name Name of the setting.
     * @return The newly created setting.
     */
    protected static final Setting<ReplacedBlocksMatrix> replacedBlocksSetting(String name)
    {
        return new ReplacedBlocksSetting(name);
    }

    /**
     * Creates a setting that represents a string of text.
     * @param name         Name of the setting.
     * @param defaultValue Default value for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<String> stringSetting(String name, String defaultValue)
    {
        return new StringSetting(name, defaultValue);
    }

    /**
     * Creates a setting that represents a list of text strings.
     * @param name          Name of the setting.
     * @param defaultValues Default values for the setting.
     * @return The newly created setting.
     */
    protected static final Setting<List<String>> stringListSetting(String name, String... defaultValues)
    {
        return new StringListSetting(name, defaultValues);
    }

    /**
     * Creates a setting that represents a {@link SurfaceGenerator}.
     * @param name Name of the setting.
     * @return The newly created setting.
     */
    protected static final Setting<SurfaceGenerator> surfaceGeneratorSetting(String name)
    {
        return new SurfaceGeneratorSetting(name);
    }
}
