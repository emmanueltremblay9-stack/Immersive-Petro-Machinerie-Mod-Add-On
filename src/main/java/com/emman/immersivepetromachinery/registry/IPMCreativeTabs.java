package com.emman.immersivepetromachinery.registry;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IPMCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersivePetroMachinery.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ImmersivePetroMachinery.MOD_ID))
                    .icon(() -> new ItemStack(IPMBlocks.DOCKING_CONTROLLER.get()))
                    .displayItems((parameters, output) -> {
                        IPMBlocks.PLATFORM_BLOCK_ITEMS.forEach(item -> output.accept(item.get()));
                        output.accept(IPMBlocks.CORE_SAMPLING_UPGRADE.get());
                    })
                    .build()
    );

    private IPMCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
