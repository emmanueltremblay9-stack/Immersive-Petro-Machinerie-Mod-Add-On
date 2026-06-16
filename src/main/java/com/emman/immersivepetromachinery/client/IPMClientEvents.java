package com.emman.immersivepetromachinery.client;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.client.gui.DockingMaintenanceScreen;
import com.emman.immersivepetromachinery.registry.IPMMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ImmersivePetroMachinery.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IPMClientEvents {
    private IPMClientEvents() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(IPMMenus.DOCKING_MAINTENANCE.get(), DockingMaintenanceScreen::new);
    }
}
