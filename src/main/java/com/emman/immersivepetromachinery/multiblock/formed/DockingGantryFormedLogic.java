package com.emman.immersivepetromachinery.multiblock.formed;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.emman.immersivepetromachinery.multiblock.DockingPlatformValidator;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public final class DockingGantryFormedLogic implements IMultiblockLogic<DockingGantryFormedState> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final VoxelShape FULL_BLOCK = Shapes.block();
    private static final BlockPos CONTROLLER_POSITION_IN_MULTIBLOCK = new BlockPos(
            -DockingPlatformValidator.MIN_X,
            -DockingPlatformValidator.MIN_Y,
            -DockingPlatformValidator.MIN_Z
    );

    @Override
    public DockingGantryFormedState createInitialState(IInitialMultiblockContext<DockingGantryFormedState> context) {
        return new DockingGantryFormedState();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return pos -> FULL_BLOCK;
    }

    @Override
    public ItemInteractionResult click(
            IMultiblockContext<DockingGantryFormedState> context,
            BlockPos clickedPositionInMultiblock,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            boolean isClient
    ) {
        if (!player.getItemInHand(hand).isEmpty() || !clickedPositionInMultiblock.equals(CONTROLLER_POSITION_IN_MULTIBLOCK)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Level rawLevel = context.getLevel().getRawLevel();
        if (!rawLevel.isClientSide()) {
            BlockPos originPosition = context.getLevel().getAbsoluteOrigin();
            BlockPos masterPosition = context.getLevel().toAbsolute(CONTROLLER_POSITION_IN_MULTIBLOCK);
            BlockPos controllerRelativeClick = clickedPositionInMultiblock.subtract(CONTROLLER_POSITION_IN_MULTIBLOCK);
            Direction orientation = context.getLevel().getOrientation().front();
            boolean formedValid = context.isValid().getAsBoolean();
            context.getState().recordDiagnostics(
                    masterPosition,
                    orientation,
                    formedValid,
                    controllerRelativeClick
            );
            context.markDirtyAndSync();
            LOGGER.info(
                    "Industrial Driller Garage Gate 1 probe: origin={}, master={}, facing={}, valid={}, controllerRelativeClick={}",
                    originPosition,
                    masterPosition,
                    orientation.getName(),
                    formedValid,
                    controllerRelativeClick
            );
            player.displayClientMessage(
                    Component.translatable(
                            "message.immersive_petro_machinery.docking_platform.formed_status",
                            formatPos(originPosition),
                            formatPos(masterPosition),
                            orientation.getName(),
                            formedValid,
                            formatPos(controllerRelativeClick)
                    ),
                    false
            );
        }

        return ItemInteractionResult.sidedSuccess(rawLevel.isClientSide());
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
