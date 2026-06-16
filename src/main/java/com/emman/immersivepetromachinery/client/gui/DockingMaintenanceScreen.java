package com.emman.immersivepetromachinery.client.gui;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.menu.DockingMaintenanceMenu;
import com.emman.immersivepetromachinery.menu.DockingMaintenanceSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public final class DockingMaintenanceScreen extends AbstractContainerScreen<DockingMaintenanceMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ImmersivePetroMachinery.MOD_ID,
            "textures/gui/docking_maintenance.png"
    );
    private static final int SHADOW_COLOR = 0x78000000;
    private static final int TITLE_COLOR = 0xFF5B3918;
    private static final int SECTION_TITLE_COLOR = 0xFF30271D;
    private static final int TEXT_COLOR = 0xFF2F261A;
    private static final int MUTED_TEXT_COLOR = 0xFF5C5140;
    private static final int GOOD_COLOR = 0xFF345D1F;
    private static final int WARN_COLOR = 0xFF79521D;
    private static final int BAD_COLOR = 0xFF7B2D21;
    private static final int EMPTY_GAUGE_COLOR = 0xFF1A211E;
    private static final int FUEL_COLOR = 0xFFC77932;
    private static final int LUBRICANT_COLOR = 0xFF5E7F42;
    private static final int DISABLED_BUTTON_TEXT_COLOR = 0xFF5E513D;
    private static final int TEXTURE_HEIGHT = 238;
    private static final int LOWER_PANEL_COLOR = 0xFFD7C9A4;
    private static final int LOWER_PANEL_SHADOW = 0xFF6B5738;
    private static final int LOWER_PANEL_DARK = 0xFF3D2A18;
    private static final int SLOT_FILL_COLOR = 0xFF9A927C;
    private static final int SLOT_HIGHLIGHT_COLOR = 0xFFE8DEC0;
    private static final int UPGRADE_PANEL_COLOR = 0xFF72776D;
    private static final int UPGRADE_PANEL_DARK = 0xFF3B3024;

    private static final StatusWidget[] STATUS_WIDGETS = {
            new StatusWidget(18, 52, 54, "Garage"),
            new StatusWidget(75, 52, 54, "Digger"),
            new StatusWidget(132, 52, 54, "Align"),
            new StatusWidget(189, 52, 54, "Lock")
    };

    private static final ModuleWidget[] MODULE_WIDGETS = {
            new ModuleWidget(DockingMaintenanceMenu.BORE_SLOT_X),
            new ModuleWidget(DockingMaintenanceMenu.CORE_SLOT_X),
            new ModuleWidget(DockingMaintenanceMenu.FUEL_SYSTEM_SLOT_X),
            new ModuleWidget(DockingMaintenanceMenu.LUBRICANT_SYSTEM_SLOT_X),
            new ModuleWidget(DockingMaintenanceMenu.UTILITY_SLOT_X)
    };

    public DockingMaintenanceScreen(DockingMaintenanceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 256;
        imageHeight = 354;
        titleLabelX = 12;
        titleLabelY = 11;
        inventoryLabelX = DockingMaintenanceMenu.PLAYER_INV_X;
        inventoryLabelY = DockingMaintenanceMenu.PLAYER_INV_Y - 14;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderWidgetTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        DockingMaintenanceSnapshot snapshot = menu.snapshot();
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth + 4, topPos + imageHeight + 4, SHADOW_COLOR);
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, TEXTURE_HEIGHT);
        drawPlayerInventoryPanel(graphics);

        drawLamp(graphics, STATUS_WIDGETS[0], snapshot.platformValid() ? GOOD_COLOR : BAD_COLOR);
        drawLamp(graphics, STATUS_WIDGETS[1], detectionColor(snapshot.detectionStatus()));
        drawLamp(graphics, STATUS_WIDGETS[2], detectionColor(snapshot.detectionStatus()));
        drawLamp(graphics, STATUS_WIDGETS[3], snapshot.softLocked() ? GOOD_COLOR : MUTED_TEXT_COLOR);
        drawTankGauge(graphics, 22, 113, snapshot.fuelAmount(), snapshot.fuelCapacity(), FUEL_COLOR);
        drawTankGauge(graphics, 142, 113, snapshot.lubricantAmount(), snapshot.lubricantCapacity(), LUBRICANT_COLOR);

        drawGarageUpgradePanel(graphics);
        for (ModuleWidget module : MODULE_WIDGETS) {
            drawGarageUpgradeSlot(graphics, module);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        DockingMaintenanceSnapshot snapshot = menu.snapshot();
        graphics.drawString(font, fit(title, imageWidth - 28), titleLabelX, titleLabelY, TITLE_COLOR, false);
        drawSectionTitle(graphics, Component.translatable("screen.immersive_petro_machinery.section.status"), 14, 39);
        drawStatusWidgetLabel(graphics, STATUS_WIDGETS[0], Component.literal("Garage"), snapshot.platformValid()
                ? Component.translatable("screen.immersive_petro_machinery.status.valid")
                : Component.translatable("screen.immersive_petro_machinery.status.invalid"), snapshot.platformValid() ? GOOD_COLOR : BAD_COLOR);
        drawStatusWidgetLabel(graphics, STATUS_WIDGETS[1], Component.literal("Digger"),
                detectionLabel(snapshot.detectionStatus()), detectionColor(snapshot.detectionStatus()));
        drawStatusWidgetLabel(graphics, STATUS_WIDGETS[2], Component.literal("Align"),
                alignmentLabel(snapshot.detectionStatus()), detectionColor(snapshot.detectionStatus()));
        drawStatusWidgetLabel(graphics, STATUS_WIDGETS[3], Component.literal("Lock"), snapshot.softLocked()
                ? Component.translatable("screen.immersive_petro_machinery.status.locked")
                : Component.translatable("screen.immersive_petro_machinery.status.unlocked"), snapshot.softLocked() ? GOOD_COLOR : MUTED_TEXT_COLOR);
        graphics.drawString(font,
                fit(Component.literal("Digger: " + snapshot.lockedTunnelDiggerText()), 221),
                17,
                80,
                MUTED_TEXT_COLOR,
                false);

        drawTankReadout(graphics, 44, 108,
                Component.translatable("screen.immersive_petro_machinery.placeholder.fuel"),
                snapshot.fuelFluidId(),
                snapshot.fuelAmount(),
                snapshot.fuelCapacity());
        drawDisabledFuelButton(graphics, 48, 140, Component.literal("Fuel"));
        drawTankReadout(graphics, 164, 108,
                Component.translatable("screen.immersive_petro_machinery.placeholder.lubricant"),
                snapshot.lubricantFluidId(),
                snapshot.lubricantAmount(),
                snapshot.lubricantCapacity());
        drawDisabledFuelButton(graphics, 168, 140, Component.literal("Lube"));

        drawSectionTitle(graphics, Component.literal("IPM Upgrade Slots"), 14, 161);
        graphics.drawString(font,
                fit(Component.literal("Garage upgrades stored here."), 220),
                17,
                220,
                MUTED_TEXT_COLOR,
                false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_COLOR, false);
    }

    private void drawSectionTitle(GuiGraphics graphics, Component title, int x, int y) {
        graphics.drawString(font, fit(title, 220), x, y, SECTION_TITLE_COLOR, false);
    }

    private void drawStatusWidgetLabel(GuiGraphics graphics, StatusWidget widget, Component label, Component value, int valueColor) {
        graphics.drawString(font, fit(label, widget.textWidth()), widget.x() + 12, widget.y(), TEXT_COLOR, false);
        graphics.drawString(font, fit(value, widget.textWidth()), widget.x() + 12, widget.y() + 10, valueColor, false);
    }

    private void drawTankReadout(GuiGraphics graphics, int x, int y, Component label, String fluidId, int amount, int capacity) {
        graphics.drawString(font, fit(label, 72), x, y, TEXT_COLOR, false);
        graphics.drawString(font, fit(Component.literal(amount + " / " + capacity + " mB"), 72), x, y + 11, MUTED_TEXT_COLOR, false);
        graphics.drawString(font, fit(Component.literal(shortFluidName(fluidId)), 72), x, y + 22, MUTED_TEXT_COLOR, false);
    }

    private void drawDisabledFuelButton(GuiGraphics graphics, int x, int y, Component label) {
        graphics.drawString(font, fit(label, 52), x + 17, y, DISABLED_BUTTON_TEXT_COLOR, false);
    }

    private void drawLamp(GuiGraphics graphics, StatusWidget widget, int color) {
        int x = leftPos + widget.x() + 1;
        int y = topPos + widget.y() + 4;
        graphics.fill(x, y, x + 8, y + 8, 0xFF151915);
        graphics.fill(x + 1, y + 1, x + 7, y + 7, color);
        graphics.fill(x + 2, y + 2, x + 4, y + 4, 0x66FFFFFF);
    }

    private void drawTankGauge(GuiGraphics graphics, int x, int y, int amount, int capacity, int color) {
        graphics.fill(leftPos + x, topPos + y, leftPos + x + 16, topPos + y + 32, EMPTY_GAUGE_COLOR);
        int fillHeight = capacity <= 0 ? 0 : Math.min(30, Math.max(0, (int) Math.round((double) amount * 30.0 / capacity)));
        if (fillHeight > 0) {
            int top = topPos + y + 31 - fillHeight;
            graphics.fill(leftPos + x + 2, top, leftPos + x + 14, topPos + y + 31, color);
            graphics.fill(leftPos + x + 3, top, leftPos + x + 8, top + 1, 0x55FFFFFF);
        }
    }

    private void drawGarageUpgradePanel(GuiGraphics graphics) {
        int left = leftPos + 8;
        int top = topPos + 158;
        int right = leftPos + imageWidth - 8;
        int bottom = topPos + 234;
        graphics.fill(left, top, right, bottom, UPGRADE_PANEL_DARK);
        graphics.fill(left + 2, top + 2, right - 2, bottom - 2, UPGRADE_PANEL_COLOR);
        graphics.fill(left + 4, top + 18, right - 4, top + 19, LOWER_PANEL_SHADOW);
    }

    private void drawGarageUpgradeSlot(GuiGraphics graphics, ModuleWidget module) {
        drawSlotBackground(graphics, module.slotX(), DockingMaintenanceMenu.GARAGE_UPGRADE_SLOT_Y);
    }

    private void renderWidgetTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        DockingMaintenanceSnapshot snapshot = menu.snapshot();
        if (isOver(8, 35, 240, 56, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.translatable("screen.immersive_petro_machinery.section.status"),
                    Component.translatable("screen.immersive_petro_machinery.maintenance.platform").append(": ").append(snapshot.platformValid()
                            ? Component.translatable("screen.immersive_petro_machinery.status.valid")
                            : Component.translatable("screen.immersive_petro_machinery.status.invalid")),
                    Component.translatable("screen.immersive_petro_machinery.maintenance.detection").append(": ").append(detectionLabel(snapshot.detectionStatus())),
                    Component.translatable("screen.immersive_petro_machinery.maintenance.alignment").append(": ").append(alignmentLabel(snapshot.detectionStatus())),
                    Component.translatable("screen.immersive_petro_machinery.maintenance.lock").append(": ").append(snapshot.softLocked()
                            ? Component.translatable("screen.immersive_petro_machinery.status.locked")
                            : Component.translatable("screen.immersive_petro_machinery.status.unlocked")),
                    Component.translatable("screen.immersive_petro_machinery.maintenance.locked_digger").append(" ").append(snapshot.lockedTunnelDiggerText()),
                    Component.literal("Detection detail: " + snapshot.detectionSummary()),
                    Component.literal("Garage scan flow pending review")
            ), Optional.empty(), mouseX, mouseY);
            return;
        }

        if (isOver(48, 136, 62, 16, mouseX, mouseY)) {
            renderFuelButtonTooltip(graphics, mouseX, mouseY, Component.literal("Fuel Digger"));
            return;
        }

        if (isOver(17, 104, 102, 50, mouseX, mouseY)) {
            renderTankTooltip(graphics, mouseX, mouseY, Component.translatable("screen.immersive_petro_machinery.placeholder.fuel"),
                    snapshot.fuelFluidId(), snapshot.fuelAmount(), snapshot.fuelCapacity());
            return;
        }

        if (isOver(168, 136, 62, 16, mouseX, mouseY)) {
            renderFuelButtonTooltip(graphics, mouseX, mouseY, Component.literal("Lubricate Digger"));
            return;
        }

        if (isOver(137, 104, 102, 50, mouseX, mouseY)) {
            renderTankTooltip(graphics, mouseX, mouseY, Component.translatable("screen.immersive_petro_machinery.placeholder.lubricant"),
                    snapshot.lubricantFluidId(), snapshot.lubricantAmount(), snapshot.lubricantCapacity());
            return;
        }

        for (ModuleWidget module : MODULE_WIDGETS) {
            if (isOver(module.slotX() - 1, DockingMaintenanceMenu.GARAGE_UPGRADE_SLOT_Y - 1, 18, 18, mouseX, mouseY)) {
                if (hoveredSlot != null && hoveredSlot.hasItem()) {
                    return;
                }

                graphics.renderTooltip(font, List.of(
                        Component.literal("IPM Upgrade Slot"),
                        Component.literal("Accepts #immersive_petro_machinery:digger_upgrades."),
                        Component.literal("Stored only; upgrades have no effect yet.")
                ), Optional.empty(), mouseX, mouseY);
                return;
            }
        }
    }

    private void drawPlayerInventoryPanel(GuiGraphics graphics) {
        int panelLeft = leftPos + 8;
        int panelTop = topPos + 244;
        int panelRight = leftPos + imageWidth - 8;
        int panelBottom = topPos + imageHeight - 8;
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, LOWER_PANEL_DARK);
        graphics.fill(panelLeft + 2, panelTop + 2, panelRight - 2, panelBottom - 2, LOWER_PANEL_COLOR);
        graphics.fill(panelLeft + 4, panelTop + 18, panelRight - 4, panelTop + 19, LOWER_PANEL_SHADOW);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotBackground(
                        graphics,
                        DockingMaintenanceMenu.PLAYER_INV_X + column * 18,
                        DockingMaintenanceMenu.PLAYER_INV_Y + row * 18
                );
            }
        }

        for (int column = 0; column < 9; column++) {
            drawSlotBackground(graphics, DockingMaintenanceMenu.PLAYER_INV_X + column * 18, DockingMaintenanceMenu.HOTBAR_Y);
        }
    }

    private void drawSlotBackground(GuiGraphics graphics, int x, int y) {
        int left = leftPos + x - 1;
        int top = topPos + y - 1;
        graphics.fill(left, top, left + 18, top + 18, LOWER_PANEL_DARK);
        graphics.fill(left + 1, top + 1, left + 17, top + 17, SLOT_FILL_COLOR);
        graphics.fill(left + 1, top + 1, left + 17, top + 2, SLOT_HIGHLIGHT_COLOR);
        graphics.fill(left + 1, top + 1, left + 2, top + 17, SLOT_HIGHLIGHT_COLOR);
        graphics.fill(left + 16, top + 2, left + 17, top + 17, LOWER_PANEL_SHADOW);
        graphics.fill(left + 2, top + 16, left + 17, top + 17, LOWER_PANEL_SHADOW);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, Component label, String fluidId, int amount, int capacity) {
        graphics.renderTooltip(font, List.of(
                label,
                tankSummary(fluidId, amount, capacity),
                Component.literal(fluidId)
        ), Optional.empty(), mouseX, mouseY);
    }

    private void renderFuelButtonTooltip(GuiGraphics graphics, int mouseX, int mouseY, Component label) {
        graphics.renderTooltip(font, List.of(
                label,
                Component.literal("Button reserved for the next fuel mechanic review."),
                Component.literal("No transfer is performed in this build.")
        ), Optional.empty(), mouseX, mouseY);
    }

    private boolean isOver(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= leftPos + x
                && mouseX < leftPos + x + width
                && mouseY >= topPos + y
                && mouseY < topPos + y + height;
    }

    private Component tankSummary(String fluidId, int amount, int capacity) {
        String fluidName = shortFluidName(fluidId);
        return Component.translatable(
                "screen.immersive_petro_machinery.tank.summary",
                amount,
                capacity,
                fluidName
        );
    }

    private String shortFluidName(String fluidId) {
        if ("empty".equals(fluidId)) {
            return "empty";
        }

        int separator = fluidId.indexOf(':');
        return separator >= 0 && separator + 1 < fluidId.length()
                ? fluidId.substring(separator + 1)
                : fluidId;
    }

    private Component detectionLabel(DockingMaintenanceSnapshot.DetectionDisplayStatus status) {
        return switch (status) {
            case NOT_CHECKED -> Component.translatable("screen.immersive_petro_machinery.status.not_checked");
            case NONE -> Component.translatable("screen.immersive_petro_machinery.status.not_detected");
            case MISALIGNED -> Component.translatable("screen.immersive_petro_machinery.status.misaligned");
            case ALIGNED -> Component.translatable("screen.immersive_petro_machinery.status.detected");
        };
    }

    private Component alignmentLabel(DockingMaintenanceSnapshot.DetectionDisplayStatus status) {
        return switch (status) {
            case NOT_CHECKED -> Component.translatable("screen.immersive_petro_machinery.status.not_checked");
            case NONE -> Component.translatable("screen.immersive_petro_machinery.status.no_target");
            case MISALIGNED -> Component.translatable("screen.immersive_petro_machinery.status.misaligned");
            case ALIGNED -> Component.translatable("screen.immersive_petro_machinery.status.aligned");
        };
    }

    private int detectionColor(DockingMaintenanceSnapshot.DetectionDisplayStatus status) {
        return switch (status) {
            case NOT_CHECKED, NONE -> MUTED_TEXT_COLOR;
            case MISALIGNED -> WARN_COLOR;
            case ALIGNED -> GOOD_COLOR;
        };
    }

    private Component fit(Component component, int maxWidth) {
        String text = component.getString();
        if (font.width(text) <= maxWidth) {
            return component;
        }

        String suffix = "...";
        if (font.width(suffix) > maxWidth) {
            return Component.literal("");
        }

        int end = text.length();
        while (end > 0 && font.width(text.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return Component.literal(text.substring(0, end) + suffix);
    }

    private record StatusWidget(int x, int y, int textWidth, String name) {
    }

    private record ModuleWidget(int slotX) {
    }
}
