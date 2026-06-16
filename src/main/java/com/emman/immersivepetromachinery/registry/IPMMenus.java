package com.emman.immersivepetromachinery.registry;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.menu.DockingMaintenanceMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IPMMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, ImmersivePetroMachinery.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DockingMaintenanceMenu>> DOCKING_MAINTENANCE =
            MENUS.register("docking_maintenance", () -> IMenuTypeExtension.create(DockingMaintenanceMenu::new));

    private IPMMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
