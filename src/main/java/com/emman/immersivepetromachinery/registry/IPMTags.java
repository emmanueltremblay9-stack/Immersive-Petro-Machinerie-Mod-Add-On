package com.emman.immersivepetromachinery.registry;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class IPMTags {
    public static final TagKey<Item> DIGGER_UPGRADES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ImmersivePetroMachinery.MOD_ID, "digger_upgrades")
    );

    private IPMTags() {
    }
}
