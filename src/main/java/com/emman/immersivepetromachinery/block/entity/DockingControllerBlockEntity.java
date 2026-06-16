package com.emman.immersivepetromachinery.block.entity;

import com.emman.immersivepetromachinery.fluid.DockFluidFilters;
import com.emman.immersivepetromachinery.fluid.FillOnlyFluidHandler;
import com.emman.immersivepetromachinery.registry.IPMBlockEntities;
import com.emman.immersivepetromachinery.registry.IPMTags;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class DockingControllerBlockEntity extends BlockEntity {
    public static final int BORE_HEAD_SLOT = 0;
    public static final int CORE_SAMPLING_SLOT = 1;
    public static final int FUEL_SYSTEM_SLOT = 2;
    public static final int LUBRICANT_SYSTEM_SLOT = 3;
    public static final int UTILITY_SLOT = 4;
    public static final int GARAGE_UPGRADE_SLOTS = 5;

    private static final String LOCKED_TUNNEL_DIGGER_TAG = "LockedTunnelDigger";
    private static final String LOCKED_TUNNEL_DIGGER_NAME_TAG = "LockedTunnelDiggerName";
    private static final String FUEL_TANK_TAG = "FuelTank";
    private static final String LUBRICANT_TANK_TAG = "LubricantTank";
    private static final String GARAGE_UPGRADES_TAG = "GarageUpgrades";
    private static final int DOCK_TANK_CAPACITY = 16_000;

    @Nullable
    private UUID lockedTunnelDigger;
    private String lockedTunnelDiggerName = "";
    private final FluidTank fuelTank = new FluidTank(DOCK_TANK_CAPACITY, DockFluidFilters::isFuel) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final FluidTank lubricantTank = new FluidTank(DOCK_TANK_CAPACITY, DockFluidFilters::isLubricant) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final ItemStackHandler garageUpgradeInventory = new ItemStackHandler(GARAGE_UPGRADE_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isValidGarageUpgrade(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final IFluidHandler fuelPortHandler = new FillOnlyFluidHandler(fuelTank);
    private final IFluidHandler lubricantPortHandler = new FillOnlyFluidHandler(lubricantTank);

    public DockingControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(IPMBlockEntities.DOCKING_CONTROLLER.get(), pos, blockState);
    }

    public boolean isSoftLocked() {
        return lockedTunnelDigger != null;
    }

    public Optional<UUID> getLockedTunnelDigger() {
        return Optional.ofNullable(lockedTunnelDigger);
    }

    public String getLockedTunnelDiggerName() {
        return lockedTunnelDiggerName.isBlank() ? "Tunnel Digger" : lockedTunnelDiggerName;
    }

    public void softLock(UUID tunnelDiggerUuid, String tunnelDiggerName) {
        String displayName = tunnelDiggerName.isBlank() ? "Tunnel Digger" : tunnelDiggerName;
        if (!tunnelDiggerUuid.equals(lockedTunnelDigger)) {
            lockedTunnelDigger = tunnelDiggerUuid;
            lockedTunnelDiggerName = displayName;
            setChanged();
        } else if (!displayName.equals(lockedTunnelDiggerName)) {
            lockedTunnelDiggerName = displayName;
            setChanged();
        }
    }

    public boolean clearSoftLock() {
        if (lockedTunnelDigger == null) {
            return false;
        }

        lockedTunnelDigger = null;
        lockedTunnelDiggerName = "";
        setChanged();
        return true;
    }

    public IFluidHandler getFuelPortHandler() {
        return fuelPortHandler;
    }

    public IFluidHandler getLubricantPortHandler() {
        return lubricantPortHandler;
    }

    public int getFuelAmount() {
        return fuelTank.getFluidAmount();
    }

    public int getFuelCapacity() {
        return fuelTank.getCapacity();
    }

    public String getFuelFluidId() {
        return DockFluidFilters.fluidId(fuelTank.getFluid());
    }

    public int getLubricantAmount() {
        return lubricantTank.getFluidAmount();
    }

    public int getLubricantCapacity() {
        return lubricantTank.getCapacity();
    }

    public String getLubricantFluidId() {
        return DockFluidFilters.fluidId(lubricantTank.getFluid());
    }

    public ItemStackHandler getGarageUpgradeInventory() {
        return garageUpgradeInventory;
    }

    public boolean hasProtectedFormedState() {
        return lockedTunnelDigger != null
                || fuelTank.getFluidAmount() > 0
                || lubricantTank.getFluidAmount() > 0
                || hasGarageUpgradeItems();
    }

    public boolean hasProtectedStateForFormation() {
        return hasProtectedFormedState();
    }

    public String protectedFormedStateSummary() {
        StringBuilder summary = new StringBuilder();
        appendProtectedState(summary, lockedTunnelDigger != null, "locked Tunnel Digger");
        appendProtectedState(summary, fuelTank.getFluidAmount() > 0, "fuel tank");
        appendProtectedState(summary, lubricantTank.getFluidAmount() > 0, "lubricant tank");
        appendProtectedState(summary, hasGarageUpgradeItems(), "garage upgrades");
        return summary.isEmpty() ? "none" : summary.toString();
    }

    public int getGarageUpgradeItemCount() {
        int count = 0;
        for (int slot = 0; slot < garageUpgradeInventory.getSlots(); slot++) {
            if (!garageUpgradeInventory.getStackInSlot(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getGarageUpgradeItemCountForDiagnostics() {
        return getGarageUpgradeItemCount();
    }

    public int getFuelAmountForDiagnostics() {
        return fuelTank.getFluidAmount();
    }

    public int getLubricantAmountForDiagnostics() {
        return lubricantTank.getFluidAmount();
    }

    public boolean hasSoftLockedDiggerForDiagnostics() {
        return lockedTunnelDigger != null;
    }

    public static boolean isValidGarageUpgrade(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return stack.is(IPMTags.DIGGER_UPGRADES);
    }

    private boolean hasGarageUpgradeItems() {
        for (int slot = 0; slot < garageUpgradeInventory.getSlots(); slot++) {
            if (!garageUpgradeInventory.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void appendProtectedState(StringBuilder summary, boolean present, String label) {
        if (!present) {
            return;
        }

        if (!summary.isEmpty()) {
            summary.append(", ");
        }
        summary.append(label);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lockedTunnelDigger = tag.hasUUID(LOCKED_TUNNEL_DIGGER_TAG)
                ? tag.getUUID(LOCKED_TUNNEL_DIGGER_TAG)
                : null;
        lockedTunnelDiggerName = tag.getString(LOCKED_TUNNEL_DIGGER_NAME_TAG);
        fuelTank.readFromNBT(registries, tag.getCompound(FUEL_TANK_TAG));
        lubricantTank.readFromNBT(registries, tag.getCompound(LUBRICANT_TANK_TAG));
        garageUpgradeInventory.deserializeNBT(registries, tag.getCompound(GARAGE_UPGRADES_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (lockedTunnelDigger != null) {
            tag.putUUID(LOCKED_TUNNEL_DIGGER_TAG, lockedTunnelDigger);
            if (!lockedTunnelDiggerName.isBlank()) {
                tag.putString(LOCKED_TUNNEL_DIGGER_NAME_TAG, lockedTunnelDiggerName);
            }
        }

        CompoundTag fuelTag = new CompoundTag();
        fuelTank.writeToNBT(registries, fuelTag);
        if (!fuelTag.isEmpty()) {
            tag.put(FUEL_TANK_TAG, fuelTag);
        }

        CompoundTag lubricantTag = new CompoundTag();
        lubricantTank.writeToNBT(registries, lubricantTag);
        if (!lubricantTag.isEmpty()) {
            tag.put(LUBRICANT_TANK_TAG, lubricantTag);
        }

        CompoundTag upgradesTag = garageUpgradeInventory.serializeNBT(registries);
        if (!upgradesTag.isEmpty()) {
            tag.put(GARAGE_UPGRADES_TAG, upgradesTag);
        }
    }
}
