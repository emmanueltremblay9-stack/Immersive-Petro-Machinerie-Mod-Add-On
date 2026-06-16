package com.emman.immersivepetromachinery.menu;

import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import com.emman.immersivepetromachinery.registry.IPMMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class DockingMaintenanceMenu extends AbstractContainerMenu {
    public static final int BORE_SLOT_X = 29;
    public static final int CORE_SLOT_X = 71;
    public static final int FUEL_SYSTEM_SLOT_X = 113;
    public static final int LUBRICANT_SYSTEM_SLOT_X = 155;
    public static final int UTILITY_SLOT_X = 197;
    public static final int GARAGE_UPGRADE_SLOT_Y = 184;
    public static final int PLAYER_INV_X = 47;
    public static final int PLAYER_INV_Y = 265;
    public static final int HOTBAR_Y = 323;

    private static final int GARAGE_UPGRADE_START = 0;
    private static final int GARAGE_UPGRADE_SLOT_COUNT = DockingControllerBlockEntity.GARAGE_UPGRADE_SLOTS;
    private static final int GARAGE_UPGRADE_END = GARAGE_UPGRADE_START + GARAGE_UPGRADE_SLOT_COUNT;
    private static final int PLAYER_MAIN_SLOT_COUNT = 27;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_MAIN_START = GARAGE_UPGRADE_END;
    private static final int HOTBAR_START = PLAYER_MAIN_START + PLAYER_MAIN_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = HOTBAR_START + HOTBAR_SLOT_COUNT;

    private final DockingMaintenanceSnapshot snapshot;

    public DockingMaintenanceMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, createClientUpgradeInventory(), DockingMaintenanceSnapshot.read(data));
    }

    public DockingMaintenanceMenu(
            int containerId,
            Inventory playerInventory,
            ItemStackHandler garageUpgradeInventory,
            DockingMaintenanceSnapshot snapshot
    ) {
        super(IPMMenus.DOCKING_MAINTENANCE.get(), containerId);
        this.snapshot = snapshot;
        addGarageUpgradeSlots(garageUpgradeInventory);
        addPlayerInventory(playerInventory);
    }

    public DockingMaintenanceSnapshot snapshot() {
        return snapshot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < GARAGE_UPGRADE_START || index >= PLAYER_SLOT_END || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index < GARAGE_UPGRADE_END) {
            moved = moveItemStackTo(stack, PLAYER_MAIN_START, PLAYER_SLOT_END, true);
        } else if (isGarageUpgrade(stack)) {
            moved = moveItemStackTo(stack, GARAGE_UPGRADE_START, GARAGE_UPGRADE_END, false);
            if (!moved) {
                moved = moveWithinPlayerInventory(index, stack);
            }
        } else {
            moved = moveWithinPlayerInventory(index, stack);
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.canInteractWithBlock(snapshot.controllerPos(), 4.0D)
                && player.level().getBlockState(snapshot.controllerPos()).is(IPMBlocks.DOCKING_CONTROLLER.get());
    }

    private boolean moveWithinPlayerInventory(int index, ItemStack stack) {
        return index < HOTBAR_START
                ? moveItemStackTo(stack, HOTBAR_START, PLAYER_SLOT_END, false)
                : moveItemStackTo(stack, PLAYER_MAIN_START, HOTBAR_START, false);
    }

    private boolean isGarageUpgrade(ItemStack stack) {
        return DockingControllerBlockEntity.isValidGarageUpgrade(DockingControllerBlockEntity.CORE_SAMPLING_SLOT, stack);
    }

    private void addGarageUpgradeSlots(ItemStackHandler garageUpgradeInventory) {
        addSlot(new SlotItemHandler(
                garageUpgradeInventory,
                DockingControllerBlockEntity.BORE_HEAD_SLOT,
                BORE_SLOT_X,
                GARAGE_UPGRADE_SLOT_Y
        ));
        addSlot(new SlotItemHandler(
                garageUpgradeInventory,
                DockingControllerBlockEntity.CORE_SAMPLING_SLOT,
                CORE_SLOT_X,
                GARAGE_UPGRADE_SLOT_Y
        ));
        addSlot(new SlotItemHandler(
                garageUpgradeInventory,
                DockingControllerBlockEntity.FUEL_SYSTEM_SLOT,
                FUEL_SYSTEM_SLOT_X,
                GARAGE_UPGRADE_SLOT_Y
        ));
        addSlot(new SlotItemHandler(
                garageUpgradeInventory,
                DockingControllerBlockEntity.LUBRICANT_SYSTEM_SLOT,
                LUBRICANT_SYSTEM_SLOT_X,
                GARAGE_UPGRADE_SLOT_Y
        ));
        addSlot(new SlotItemHandler(
                garageUpgradeInventory,
                DockingControllerBlockEntity.UTILITY_SLOT,
                UTILITY_SLOT_X,
                GARAGE_UPGRADE_SLOT_Y
        ));
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        PLAYER_INV_X + column * 18,
                        PLAYER_INV_Y + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, PLAYER_INV_X + column * 18, HOTBAR_Y));
        }
    }

    private static ItemStackHandler createClientUpgradeInventory() {
        return new ItemStackHandler(DockingControllerBlockEntity.GARAGE_UPGRADE_SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return DockingControllerBlockEntity.isValidGarageUpgrade(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

}
