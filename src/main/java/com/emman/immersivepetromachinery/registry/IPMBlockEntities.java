package com.emman.immersivepetromachinery.registry;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IPMBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ImmersivePetroMachinery.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DockingControllerBlockEntity>> DOCKING_CONTROLLER =
            BLOCK_ENTITIES.register("docking_controller",
                    () -> BlockEntityType.Builder.of(DockingControllerBlockEntity::new, IPMBlocks.DOCKING_CONTROLLER.get()).build(null));

    private IPMBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
