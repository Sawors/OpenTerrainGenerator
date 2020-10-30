package com.pg85.otg.generator.biome;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.layers.legacy.Layer;
import com.pg85.otg.generator.biome.layers.legacy.OldBiomeLayers;

/**
 * This is the normal biome mode, which has all of Open Terrain Generator's features.
 */
public class NormalBiomeGenerator extends LayeredBiomeGenerator
{

	public NormalBiomeGenerator(LocalWorld world)
    {
        super(world);
    }

    @Override
    protected Layer[] initLayers()
    {
    	return OldBiomeLayers.createNormal(world);
    }
}
