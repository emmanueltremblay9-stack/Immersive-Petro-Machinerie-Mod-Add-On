package com.emman.immersivepetromachinery.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public final class DockFluidFilters {
    private static final ResourceLocation DIESEL =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "diesel");
    private static final ResourceLocation GASOLINE =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "gasoline");
    private static final ResourceLocation LUBRICANT =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "lubricant");
    private static final TagKey<Fluid> GASOLINE_TAG = neoForgeFluidTag("gasoline");
    private static final TagKey<Fluid> LUBRICANT_TAG = neoForgeFluidTag("lubricant");

    private DockFluidFilters() {
    }

    public static boolean isFuel(FluidStack stack) {
        // Keep diesel exact-scoped: the verified neoforge:diesel tag also includes diesel_sulfur.
        return isExactFluid(stack, DIESEL) || isExactFluid(stack, GASOLINE) || stack.is(GASOLINE_TAG);
    }

    public static boolean isLubricant(FluidStack stack) {
        return isExactFluid(stack, LUBRICANT) || stack.is(LUBRICANT_TAG);
    }

    public static String fluidId(FluidStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }

        return BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString();
    }

    private static boolean isExactFluid(FluidStack stack, ResourceLocation fluidId) {
        return !stack.isEmpty() && fluidId.equals(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
    }

    private static TagKey<Fluid> neoForgeFluidTag(String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("neoforge", path));
    }
}
