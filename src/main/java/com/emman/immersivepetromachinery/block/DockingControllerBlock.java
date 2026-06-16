package com.emman.immersivepetromachinery.block;

import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import com.emman.immersivepetromachinery.docking.TunnelDiggerDockingDetector;
import com.emman.immersivepetromachinery.fluid.DockFluidPortAccess;
import com.emman.immersivepetromachinery.menu.DockingMaintenanceMenu;
import com.emman.immersivepetromachinery.menu.DockingMaintenanceSnapshot;
import com.emman.immersivepetromachinery.multiblock.DockingPlatformValidator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

public final class DockingControllerBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<DockingControllerBlock> CODEC = simpleCodec(DockingControllerBlock::new);
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VALIDATION_DELAY_TICKS = 2;
    private static final int REVALIDATION_INTERVAL_TICKS = 40;

    public DockingControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VALID, Boolean.FALSE));
    }

    @Override
    public MapCodec<DockingControllerBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(VALID, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VALID);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DockingControllerBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!state.is(oldState.getBlock())) {
            scheduleValidation(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        scheduleValidation(level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        validateAndStore(level, pos, state, getControllerState(level, pos));
        scheduleValidation(level, pos, REVALIDATION_INTERVAL_TICKS);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            DockingControllerBlockEntity controllerState = getControllerState(level, pos);
            DockingPlatformValidator.ValidationResult result = validateAndStore(level, pos, state, controllerState);

            if (controllerState == null) {
                player.displayClientMessage(
                        Component.translatable("message.immersive_petro_machinery.soft_lock.storage_unavailable"),
                        true
                );
            } else if (player.isShiftKeyDown()) {
                handleSoftLockShortcut(player, controllerState, result, state, level, pos);
            } else if (player instanceof ServerPlayer serverPlayer) {
                openMaintenanceMenu(serverPlayer, controllerState, result, state, level, pos);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.immersive_petro_machinery.maintenance.client_only"),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private Component messageFor(TunnelDiggerDockingDetector.DetectionResult detection) {
        return switch (detection.status()) {
            case NONE -> Component.translatable("message.immersive_petro_machinery.tunnel_digger.none");
            case MISALIGNED -> Component.translatable("message.immersive_petro_machinery.tunnel_digger.misaligned", detection.summary());
            case ALIGNED -> Component.translatable("message.immersive_petro_machinery.tunnel_digger.aligned", detection.summary());
        };
    }

    private void handleSoftLockShortcut(
            Player player,
            DockingControllerBlockEntity controllerState,
            DockingPlatformValidator.ValidationResult validation,
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        if (!validation.valid()) {
            sendInvalidValidationMessage(player, validation);
            return;
        }

        if (controllerState.isSoftLocked()) {
            unlock(player, controllerState);
            return;
        }

        TunnelDiggerDockingDetector.DetectionResult detection =
                TunnelDiggerDockingDetector.detect(level, pos, state.getValue(FACING));
        lockIfAligned(player, controllerState, detection);
    }

    private void unlock(Player player, DockingControllerBlockEntity controllerState) {
        String lockedTunnelDiggerName = controllerState.getLockedTunnelDiggerName();
        controllerState.clearSoftLock();
        player.displayClientMessage(
                Component.translatable("message.immersive_petro_machinery.soft_lock.unlocked", lockedTunnelDiggerName),
                true
        );
    }

    private void lockIfAligned(
            Player player,
            DockingControllerBlockEntity controllerState,
            TunnelDiggerDockingDetector.DetectionResult detection
    ) {
        if (detection.status() != TunnelDiggerDockingDetector.DetectionStatus.ALIGNED || detection.candidate() == null) {
            player.displayClientMessage(messageFor(detection), true);
            return;
        }

        UUID tunnelDiggerUuid = detection.candidate().uuid();
        String tunnelDiggerName = detection.candidate().displayName();
        controllerState.softLock(tunnelDiggerUuid, tunnelDiggerName);
        player.displayClientMessage(
                Component.translatable("message.immersive_petro_machinery.soft_lock.locked", tunnelDiggerName, detection.summary()),
                true
        );
    }

    private void openMaintenanceMenu(
            ServerPlayer player,
            DockingControllerBlockEntity controllerState,
            DockingPlatformValidator.ValidationResult validation,
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        TunnelDiggerDockingDetector.DetectionResult detection = validation.valid()
                ? TunnelDiggerDockingDetector.detect(level, pos, state.getValue(FACING))
                : null;
        DockingMaintenanceSnapshot snapshot = DockingMaintenanceSnapshot.create(
                pos,
                validation.valid(),
                validation.summary(),
                detection,
                controllerState.getLockedTunnelDigger().orElse(null),
                controllerState.getLockedTunnelDiggerName(),
                controllerState.getFuelAmount(),
                controllerState.getFuelCapacity(),
                controllerState.getFuelFluidId(),
                controllerState.getLubricantAmount(),
                controllerState.getLubricantCapacity(),
                controllerState.getLubricantFluidId()
        );
        LOGGER.info(
                "Opening Industrial Driller Garage Maintenance GUI at {}: valid={}, fuel={}mB, lubricant={}mB, upgradeItems={}, softLocked={}",
                pos,
                validation.valid(),
                controllerState.getFuelAmount(),
                controllerState.getLubricantAmount(),
                controllerState.getGarageUpgradeItemCount(),
                controllerState.isSoftLocked()
        );

        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new DockingMaintenanceMenu(
                                containerId,
                                inventory,
                                controllerState.getGarageUpgradeInventory(),
                                snapshot
                        ),
                        Component.translatable("container.immersive_petro_machinery.docking_maintenance")
                ),
                snapshot::write
        );
    }

    private void sendInvalidValidationMessage(Player player, DockingPlatformValidator.ValidationResult validation) {
        player.displayClientMessage(
                Component.translatable("message.immersive_petro_machinery.docking_platform.invalid_short"),
                true
        );
        player.displayClientMessage(
                Component.translatable("message.immersive_petro_machinery.docking_platform.invalid", validation.summary()),
                false
        );
    }

    private void scheduleValidation(Level level, BlockPos pos) {
        scheduleValidation(level, pos, VALIDATION_DELAY_TICKS);
    }

    private void scheduleValidation(Level level, BlockPos pos, int delayTicks) {
        if (!level.isClientSide() && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, delayTicks);
        }
    }

    private DockingPlatformValidator.ValidationResult validateAndStore(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable DockingControllerBlockEntity controllerState
    ) {
        DockingPlatformValidator.ValidationResult result = DockingPlatformValidator.validate(level, pos, state.getValue(FACING));
        boolean wasValid = state.getValue(VALID);

        if (!result.valid() && controllerState != null && controllerState.clearSoftLock()) {
            LOGGER.info("Industrial Driller Garage at {} cleared its soft lock because validation failed: {}", pos, result.summary());
        }

        if (wasValid != result.valid()) {
            level.setBlock(pos, state.setValue(VALID, result.valid()), Block.UPDATE_CLIENTS);
            DockFluidPortAccess.invalidatePortCapabilities(level, pos, state.getValue(FACING));
            LOGGER.info("Industrial Driller Garage at {} is now {}: {}", pos, result.valid() ? "valid" : "invalid", result.summary());
        }

        return result;
    }

    @Nullable
    private DockingControllerBlockEntity getControllerState(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof DockingControllerBlockEntity controllerState ? controllerState : null;
    }
}
