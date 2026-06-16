package com.emman.immersivepetromachinery.registry;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.block.DockingControllerBlock;
import com.emman.immersivepetromachinery.block.SurveyConsoleBlock;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IPMBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImmersivePetroMachinery.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersivePetroMachinery.MOD_ID);

    public static final DeferredBlock<DockingControllerBlock> DOCKING_CONTROLLER =
            BLOCKS.registerBlock("docking_controller", DockingControllerBlock::new, platformBlockProperties());
    public static final DeferredBlock<Block> DOCKING_FRAME = registerPlatformBlock("docking_frame");
    public static final DeferredBlock<Block> LOCKING_RAIL = registerPlatformBlock("locking_rail");
    public static final DeferredBlock<Block> FUEL_PORT = registerPlatformBlock("fuel_port");
    public static final DeferredBlock<Block> LUBRICANT_PORT = registerPlatformBlock("lubricant_port");
    public static final DeferredBlock<Block> OUTPUT_PORT = registerPlatformBlock("output_port");
    public static final DeferredBlock<Block> REPAIR_BAY = registerPlatformBlock("repair_bay");
    public static final DeferredBlock<Block> UPGRADE_BAY = registerPlatformBlock("upgrade_bay");
    public static final DeferredBlock<SurveyConsoleBlock> SURVEY_CONSOLE =
            BLOCKS.registerBlock("survey_console", SurveyConsoleBlock::new, platformBlockProperties());
    public static final DeferredBlock<Block> DOCKING_PIPE = registerPlatformBlock("docking_pipe");

    public static final DeferredItem<Item> CORE_SAMPLING_UPGRADE = ITEMS.register("core_sampling_upgrade",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final List<DeferredItem<BlockItem>> PLATFORM_BLOCK_ITEMS = List.of(
            ITEMS.registerSimpleBlockItem(DOCKING_CONTROLLER),
            ITEMS.registerSimpleBlockItem(DOCKING_FRAME),
            ITEMS.registerSimpleBlockItem(LOCKING_RAIL),
            ITEMS.registerSimpleBlockItem(FUEL_PORT),
            ITEMS.registerSimpleBlockItem(LUBRICANT_PORT),
            ITEMS.registerSimpleBlockItem(OUTPUT_PORT),
            ITEMS.registerSimpleBlockItem(REPAIR_BAY),
            ITEMS.registerSimpleBlockItem(UPGRADE_BAY),
            ITEMS.registerSimpleBlockItem(SURVEY_CONSOLE),
            ITEMS.registerSimpleBlockItem(DOCKING_PIPE)
    );

    private IPMBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    private static DeferredBlock<Block> registerPlatformBlock(String name) {
        return BLOCKS.registerSimpleBlock(name, platformBlockProperties());
    }

    private static BlockBehaviour.Properties platformBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.5F, 6.0F)
                .sound(SoundType.METAL);
    }
}
