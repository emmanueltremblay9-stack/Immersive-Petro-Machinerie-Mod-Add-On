package com.emman.immersivepetromachinery.fluid;

import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import com.emman.immersivepetromachinery.multiblock.DockingPlatformValidator;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class DockFluidPortAccess {
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private DockFluidPortAccess() {
    }

    @Nullable
    public static IFluidHandler getFuelHandler(Level level, BlockPos fuelPortPos) {
        DockingControllerBlockEntity controller = findController(level, fuelPortPos, PortType.FUEL);
        return controller == null ? null : controller.getFuelPortHandler();
    }

    @Nullable
    public static IFluidHandler getLubricantHandler(Level level, BlockPos lubricantPortPos) {
        DockingControllerBlockEntity controller = findController(level, lubricantPortPos, PortType.LUBRICANT);
        return controller == null ? null : controller.getLubricantPortHandler();
    }

    public static void invalidatePortCapabilities(Level level, BlockPos controllerPos, Direction facing) {
        level.invalidateCapabilities(fuelPortPos(controllerPos, facing));
        level.invalidateCapabilities(lubricantPortPos(controllerPos, facing));
    }

    public static BlockPos fuelPortPos(BlockPos controllerPos, Direction facing) {
        return DockingPlatformValidator.toWorld(controllerPos, facing, PortType.FUEL.localX, 0, 0);
    }

    public static BlockPos lubricantPortPos(BlockPos controllerPos, Direction facing) {
        return DockingPlatformValidator.toWorld(controllerPos, facing, PortType.LUBRICANT.localX, 0, 0);
    }

    @Nullable
    private static DockingControllerBlockEntity findController(Level level, BlockPos portPos, PortType portType) {
        for (Direction facing : HORIZONTAL_DIRECTIONS) {
            Direction localRight = facing.getClockWise();
            BlockPos controllerPos = portPos.relative(localRight, -portType.localX);
            BlockState controllerState = level.getBlockState(controllerPos);

            if (!controllerState.is(IPMBlocks.DOCKING_CONTROLLER.get())
                    || controllerState.getValue(HorizontalDirectionalBlock.FACING) != facing) {
                continue;
            }

            if (!DockingPlatformValidator.validate(level, controllerPos, facing).valid()) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(controllerPos);
            if (blockEntity instanceof DockingControllerBlockEntity controller) {
                return controller;
            }
        }

        return null;
    }

    private enum PortType {
        FUEL(-4),
        LUBRICANT(4);

        private final int localX;

        PortType(int localX) {
            this.localX = localX;
        }
    }
}
