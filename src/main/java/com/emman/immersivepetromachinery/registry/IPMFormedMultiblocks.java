package com.emman.immersivepetromachinery.registry;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.manual.IPMManualMultiblocks;
import com.emman.immersivepetromachinery.multiblock.formed.DockingGantryFormedLogic;
import com.emman.immersivepetromachinery.multiblock.formed.DockingGantryFormedState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class IPMFormedMultiblocks {
    private static final DockingGantryFormedLogic DOCKING_GANTRY_LOGIC = new DockingGantryFormedLogic();

    public static final MultiblockRegistration<DockingGantryFormedState> DOCKING_GANTRY =
            MultiblockRegistration.builder(
                            DOCKING_GANTRY_LOGIC,
                            ResourceLocation.fromNamespaceAndPath(ImmersivePetroMachinery.MOD_ID, "docking_gantry")
                    )
                    .defaultBEs(IPMBlockEntities.BLOCK_ENTITIES)
                    .defaultBlock(IPMBlocks.BLOCKS, IPMBlocks.ITEMS, formedPartProperties())
                    .structure(IPMManualMultiblocks::dockingPlatform)
                    .build(callback -> {
                    });

    private IPMFormedMultiblocks() {
    }

    public static void bootstrap() {
        // Force static initialization before the shared deferred registers attach to the mod event bus.
    }

    private static BlockBehaviour.Properties formedPartProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }
}
