package com.emman.immersivepetromachinery.menu;

import com.emman.immersivepetromachinery.docking.TunnelDiggerDockingDetector;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record DockingMaintenanceSnapshot(
        BlockPos controllerPos,
        boolean platformValid,
        String platformSummary,
        DetectionDisplayStatus detectionStatus,
        String detectionSummary,
        boolean softLocked,
        @Nullable UUID lockedTunnelDigger,
        String tunnelDiggerName,
        int fuelAmount,
        int fuelCapacity,
        String fuelFluidId,
        int lubricantAmount,
        int lubricantCapacity,
        String lubricantFluidId
) {
    private static final int MAX_SUMMARY_LENGTH = 256;
    private static final int MAX_FLUID_ID_LENGTH = 128;
    private static final int MAX_DIGGER_NAME_LENGTH = 128;

    public static DockingMaintenanceSnapshot notAvailable() {
        return new DockingMaintenanceSnapshot(
                BlockPos.ZERO,
                false,
                "controller state unavailable",
                DetectionDisplayStatus.NOT_CHECKED,
                "not checked",
                false,
                null,
                "none",
                0,
                0,
                "empty",
                0,
                0,
                "empty"
        );
    }

    public static DockingMaintenanceSnapshot create(
            BlockPos controllerPos,
            boolean platformValid,
            String platformSummary,
            @Nullable TunnelDiggerDockingDetector.DetectionResult detection,
            @Nullable UUID lockedTunnelDigger,
            String lockedTunnelDiggerName,
            int fuelAmount,
            int fuelCapacity,
            String fuelFluidId,
            int lubricantAmount,
            int lubricantCapacity,
            String lubricantFluidId
    ) {
        DetectionDisplayStatus status = DetectionDisplayStatus.from(detection);
        String tunnelDiggerName = detection != null && detection.candidate() != null
                ? detection.candidate().displayName()
                : lockedTunnelDigger == null ? "none" : lockedTunnelDiggerName;
        return new DockingMaintenanceSnapshot(
                controllerPos,
                platformValid,
                trim(platformSummary),
                status,
                trim(detection == null ? "not checked" : detection.summary()),
                lockedTunnelDigger != null,
                lockedTunnelDigger,
                trimName(tunnelDiggerName),
                fuelAmount,
                fuelCapacity,
                trimFluidId(fuelFluidId),
                lubricantAmount,
                lubricantCapacity,
                trimFluidId(lubricantFluidId)
        );
    }

    public static DockingMaintenanceSnapshot read(RegistryFriendlyByteBuf buffer) {
        if (buffer == null) {
            return notAvailable();
        }

        BlockPos controllerPos = buffer.readBlockPos();
        boolean platformValid = buffer.readBoolean();
        String platformSummary = buffer.readUtf(MAX_SUMMARY_LENGTH);
        DetectionDisplayStatus detectionStatus = buffer.readEnum(DetectionDisplayStatus.class);
        String detectionSummary = buffer.readUtf(MAX_SUMMARY_LENGTH);
        boolean softLocked = buffer.readBoolean();
        UUID lockedTunnelDigger = buffer.readBoolean() ? buffer.readUUID() : null;
        String tunnelDiggerName = buffer.readUtf(MAX_DIGGER_NAME_LENGTH);
        int fuelAmount = buffer.readVarInt();
        int fuelCapacity = buffer.readVarInt();
        String fuelFluidId = buffer.readUtf(MAX_FLUID_ID_LENGTH);
        int lubricantAmount = buffer.readVarInt();
        int lubricantCapacity = buffer.readVarInt();
        String lubricantFluidId = buffer.readUtf(MAX_FLUID_ID_LENGTH);
        return new DockingMaintenanceSnapshot(
                controllerPos,
                platformValid,
                platformSummary,
                detectionStatus,
                detectionSummary,
                softLocked,
                lockedTunnelDigger,
                tunnelDiggerName,
                fuelAmount,
                fuelCapacity,
                fuelFluidId,
                lubricantAmount,
                lubricantCapacity,
                lubricantFluidId
        );
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(controllerPos);
        buffer.writeBoolean(platformValid);
        buffer.writeUtf(trim(platformSummary), MAX_SUMMARY_LENGTH);
        buffer.writeEnum(detectionStatus);
        buffer.writeUtf(trim(detectionSummary), MAX_SUMMARY_LENGTH);
        buffer.writeBoolean(softLocked);
        buffer.writeBoolean(lockedTunnelDigger != null);

        if (lockedTunnelDigger != null) {
            buffer.writeUUID(lockedTunnelDigger);
        }

        buffer.writeUtf(trimName(tunnelDiggerName), MAX_DIGGER_NAME_LENGTH);
        buffer.writeVarInt(fuelAmount);
        buffer.writeVarInt(fuelCapacity);
        buffer.writeUtf(trimFluidId(fuelFluidId), MAX_FLUID_ID_LENGTH);
        buffer.writeVarInt(lubricantAmount);
        buffer.writeVarInt(lubricantCapacity);
        buffer.writeUtf(trimFluidId(lubricantFluidId), MAX_FLUID_ID_LENGTH);
    }

    public String lockedTunnelDiggerText() {
        return lockedTunnelDigger == null ? "none" : tunnelDiggerName;
    }

    private static String trim(String value) {
        return value.length() <= MAX_SUMMARY_LENGTH ? value : value.substring(0, MAX_SUMMARY_LENGTH);
    }

    private static String trimFluidId(String value) {
        return value.length() <= MAX_FLUID_ID_LENGTH ? value : value.substring(0, MAX_FLUID_ID_LENGTH);
    }

    private static String trimName(String value) {
        return value.length() <= MAX_DIGGER_NAME_LENGTH ? value : value.substring(0, MAX_DIGGER_NAME_LENGTH);
    }

    public enum DetectionDisplayStatus {
        NOT_CHECKED,
        NONE,
        MISALIGNED,
        ALIGNED;

        private static DetectionDisplayStatus from(@Nullable TunnelDiggerDockingDetector.DetectionResult detection) {
            if (detection == null) {
                return NOT_CHECKED;
            }

            return switch (detection.status()) {
                case NONE -> NONE;
                case MISALIGNED -> MISALIGNED;
                case ALIGNED -> ALIGNED;
            };
        }
    }
}
