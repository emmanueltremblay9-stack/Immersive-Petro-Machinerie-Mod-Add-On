package com.emman.immersivepetromachinery.registry;

import com.emman.immersivepetromachinery.fluid.DockFluidPortAccess;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class IPMCapabilities {
    private IPMCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> DockFluidPortAccess.getFuelHandler(level, pos),
                IPMBlocks.FUEL_PORT.get()
        );
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> DockFluidPortAccess.getLubricantHandler(level, pos),
                IPMBlocks.LUBRICANT_PORT.get()
        );
    }
}
