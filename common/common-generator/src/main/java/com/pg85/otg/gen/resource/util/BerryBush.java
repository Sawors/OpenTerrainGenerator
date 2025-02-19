package com.pg85.otg.gen.resource.util;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialProperties;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.PlantType;

import java.util.Random;

public class BerryBush {

    public enum SparseOption {
        Sparse,
        Decorated
    }

    public static void spawnBerryBushes(IWorldGenRegion world, Random random, int centerX, int centerZ, PlantType plant, int frequency, int minAltitude, int maxAltitude, MaterialSet sourceBlocks, SparseOption sparseOption) {
        int centerY = world.getHighestBlockAboveYAt(centerX, centerZ);

        if (centerY < world.getWorldMinY()) {
            return;
        }

        LocalMaterialData worldMaterial;

        // Fix y position
        while (
                (
                        //stay in y bounds
                        (centerY >= world.getWorldMinY() && centerY < world.getWorldMaxY()) &&
                                //null check
                                (worldMaterial = world.getMaterial(centerX, centerY, centerZ)) != null &&
                                //if air or leaves
                                (
                                        worldMaterial.isAir() ||
                                                worldMaterial.isLeaves()
                                ) &&
                                world.getMaterial(centerX, centerY - 1, centerZ) != null
                ) && (
                        centerY > 0
                )
        ) {
            //move down
            centerY--;
        }
        centerY++;

        // Try to place BERRY BUSH
        int x;
        int y;
        int z;

        int xzBounds = (sparseOption == SparseOption.Sparse) ? 7 : 5;
        int yBounds = (sparseOption == SparseOption.Sparse) ? 4 : 3;
        frequency += (sparseOption == SparseOption.Sparse) ? 0 : 10;

        for (int i = 0; i < frequency; i++) {
            x = centerX + random.nextInt(xzBounds) - random.nextInt(xzBounds);
            y = centerY + random.nextInt(yBounds) - random.nextInt(yBounds);
            z = centerZ + random.nextInt(xzBounds) - random.nextInt(xzBounds);
            //spawn if the block is in min/max altitude and block below is a source block
            if (
                    (worldMaterial = world.getMaterial(x, y, z)) != null &&
                            worldMaterial.isAir() &&
                            (
                                    (worldMaterial = world.getMaterial(x, y - 1, z)) != null &&
                                            sourceBlocks.contains(worldMaterial)
                            ) &&
                            (
                                    y >= minAltitude && y < maxAltitude
                            )
            ) {
                //set block directly so we can set the age of the berry bush
                if (plant == PlantType.BerryBush) {
                    //set block directly so we can set the age of the berry bush
                    world.setBlock(x, y, z, LocalMaterials.BERRY_BUSH.withProperty(MaterialProperties.AGE_0_3, random.nextInt(4)));
                } else {
                    plant.spawn(world, x, y, z);
                }            }
        }

    }
}