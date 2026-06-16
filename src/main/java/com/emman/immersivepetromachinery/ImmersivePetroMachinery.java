package com.emman.immersivepetromachinery;

import com.mojang.logging.LogUtils;
import com.emman.immersivepetromachinery.registry.IPMBlockEntities;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import com.emman.immersivepetromachinery.registry.IPMCapabilities;
import com.emman.immersivepetromachinery.registry.IPMCreativeTabs;
import com.emman.immersivepetromachinery.registry.IPMFormedMultiblocks;
import com.emman.immersivepetromachinery.registry.IPMMenus;
import com.emman.immersivepetromachinery.manual.IPMManualMultiblocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

@Mod(ImmersivePetroMachinery.MOD_ID)
public final class ImmersivePetroMachinery {
    public static final String MOD_ID = "immersive_petro_machinery";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ImmersivePetroMachinery(IEventBus modEventBus) {
        IPMFormedMultiblocks.bootstrap();
        IPMBlocks.register(modEventBus);
        IPMBlockEntities.register(modEventBus);
        IPMMenus.register(modEventBus);
        IPMCreativeTabs.register(modEventBus);
        modEventBus.addListener(IPMCapabilities::register);
        IPMManualMultiblocks.register();

        LOGGER.info("Immersive Petro-Machinery loaded");
    }
}
