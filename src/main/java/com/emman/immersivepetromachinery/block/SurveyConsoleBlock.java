package com.emman.immersivepetromachinery.block;

import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import com.emman.immersivepetromachinery.multiblock.DockingPlatformValidator;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

public final class SurveyConsoleBlock extends Block {
    private static final Logger LOGGER = LogUtils.getLogger();

    public SurveyConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ControllerLink controllerLink = findController(level, pos);
        if (controllerLink == null) {
            player.displayClientMessage(
                    Component.translatable("message.immersive_petro_machinery.survey.no_controller"),
                    true
            );
            return InteractionResult.SUCCESS;
        }

        DockingPlatformValidator.ValidationResult validation = DockingPlatformValidator.validate(
                level,
                controllerLink.controllerPos(),
                controllerLink.facing()
        );
        if (!validation.valid()) {
            player.displayClientMessage(
                    Component.translatable("message.immersive_petro_machinery.survey.invalid_platform", validation.summary()),
                    true
            );
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(
                Component.translatable("message.immersive_petro_machinery.survey.scope_contained"),
                true
        );
        containSurveyOutputForDiagnostics(level, pos, controllerLink.controllerPos());
        return InteractionResult.SUCCESS;
    }

    public static boolean containSurveyOutputForDiagnostics(Level level, BlockPos surveyConsolePos, BlockPos controllerPos) {
        if (!level.isClientSide()) {
            LOGGER.info(
                    "IPM Survey Console output contained pending Core Sampling flow review at {}, linked controller {}",
                    surveyConsolePos,
                    controllerPos
            );
        }
        return true;
    }

    @Nullable
    private static ControllerLink findController(Level level, BlockPos surveyConsolePos) {
        BlockPos controllerPos = surveyConsolePos.below();
        BlockState controllerState = level.getBlockState(controllerPos);
        if (!controllerState.is(IPMBlocks.DOCKING_CONTROLLER.get())) {
            return null;
        }

        Direction facing = controllerState.getValue(DockingControllerBlock.FACING);
        if (level.getBlockEntity(controllerPos) instanceof DockingControllerBlockEntity controllerBlockEntity) {
            return new ControllerLink(controllerPos, facing, controllerBlockEntity);
        }

        return null;
    }

    private record ControllerLink(
            BlockPos controllerPos,
            Direction facing,
            DockingControllerBlockEntity controllerState
    ) {
    }
}
