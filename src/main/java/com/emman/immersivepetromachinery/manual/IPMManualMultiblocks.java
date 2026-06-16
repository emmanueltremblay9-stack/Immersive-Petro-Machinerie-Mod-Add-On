package com.emman.immersivepetromachinery.manual;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.block.DockingControllerBlock;
import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import com.emman.immersivepetromachinery.fluid.DockFluidPortAccess;
import com.emman.immersivepetromachinery.multiblock.DockingPlatformValidator;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import com.emman.immersivepetromachinery.registry.IPMFormedMultiblocks;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public final class IPMManualMultiblocks {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation DOCKING_PLATFORM_ID =
            ResourceLocation.fromNamespaceAndPath(ImmersivePetroMachinery.MOD_ID, "multiblocks/docking_platform");
    private static final boolean ENABLE_FORMED_SHELL_CREATION =
            Boolean.getBoolean("immersive_petro_machinery.enableFormedShellCreation");

    private static final TemplateMultiblock DOCKING_PLATFORM = new DockingPlatformTemplateMultiblock();

    private IPMManualMultiblocks() {
    }

    public static void register() {
        MultiblockHandler.registerMultiblock(DOCKING_PLATFORM);
    }

    public static TemplateMultiblock dockingPlatform() {
        return DOCKING_PLATFORM;
    }

    public static boolean isFormedShellCreationEnabledForRuntime() {
        return ENABLE_FORMED_SHELL_CREATION;
    }

    public static FormedShellCreationDecision evaluateFormedShellCreation(
            boolean formedShellCreationEnabled,
            DockingControllerBlockEntity controllerState
    ) {
        if (!formedShellCreationEnabled) {
            return FormedShellCreationDecision.DISABLED;
        }

        if (controllerState == null) {
            return FormedShellCreationDecision.BLOCKED_STATE_UNAVAILABLE;
        }

        if (controllerState.hasProtectedStateForFormation()) {
            return FormedShellCreationDecision.BLOCKED_PROTECTED_STATE;
        }

        return FormedShellCreationDecision.ALLOWED_EMPTY_STATE;
    }

    public enum FormedShellCreationDecision {
        DISABLED,
        BLOCKED_STATE_UNAVAILABLE,
        BLOCKED_PROTECTED_STATE,
        ALLOWED_EMPTY_STATE
    }

    private static final class DockingPlatformTemplateMultiblock extends IETemplateMultiblock {
        private static final BlockPos SIZE = new BlockPos(
                DockingPlatformValidator.MAX_X - DockingPlatformValidator.MIN_X + 1,
                DockingPlatformValidator.MAX_Y - DockingPlatformValidator.MIN_Y + 1,
                DockingPlatformValidator.MAX_Z - DockingPlatformValidator.MIN_Z + 1
        );
        private static final BlockPos TRIGGER_OFFSET = new BlockPos(
                -DockingPlatformValidator.MIN_X,
                -DockingPlatformValidator.MIN_Y,
                -DockingPlatformValidator.MIN_Z
        );

        private DockingPlatformTemplateMultiblock() {
            super(
                    DOCKING_PLATFORM_ID,
                    TRIGGER_OFFSET,
                    TRIGGER_OFFSET,
                    SIZE,
                    IPMFormedMultiblocks.DOCKING_GANTRY,
                    List.of(DockingPlatformTemplateMultiblock::matchesValidatedBlockType)
            );
        }

        private static BlockMatcher.Result matchesValidatedBlockType(
                BlockState expected,
                BlockState found,
                Level level,
                BlockPos pos
        ) {
            return expected.is(found.getBlock()) ? BlockMatcher.Result.allow(2) : BlockMatcher.Result.DEFAULT;
        }

        @Override
        public List<StructureTemplate.StructureBlockInfo> getStructure(Level level) {
            return DockingPlatformValidator.requiredStructureEntries().stream()
                    .map(entry -> new StructureTemplate.StructureBlockInfo(
                            templatePos(entry),
                            entry.state(),
                            null
                    ))
                    .toList();
        }

        @Override
        public Vec3i getSize(Level level) {
            return SIZE;
        }

        private static BlockPos templatePos(DockingPlatformValidator.StructureEntry entry) {
            return new BlockPos(
                    entry.localX() + TRIGGER_OFFSET.getX(),
                    entry.localY() + TRIGGER_OFFSET.getY(),
                    entry.localZ() + TRIGGER_OFFSET.getZ()
            );
        }

        @Override
        public boolean isBlockTrigger(BlockState state, Direction side, Level level) {
            return state.is(IPMBlocks.DOCKING_CONTROLLER.get());
        }

        @Override
        public boolean createStructure(Level level, BlockPos pos, Direction side, Player player) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(IPMBlocks.DOCKING_CONTROLLER.get()) || !state.hasProperty(DockingControllerBlock.FACING)) {
                return false;
            }

            Direction facing = state.getValue(DockingControllerBlock.FACING);
            DockingPlatformValidator.ValidationResult result = DockingPlatformValidator.validate(level, pos, facing);
            if (level.isClientSide()) {
                return true;
            }

            if (!result.valid()) {
                storeValidation(level, pos, state, facing, result);
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.immersive_petro_machinery.docking_platform.invalid_short"),
                            true
                    );
                    player.displayClientMessage(
                            Component.translatable("message.immersive_petro_machinery.docking_platform.invalid", result.summary()),
                            false
                    );
                }
                // The IE hammer falls through to generic block rotation when this returns false.
                // IPM already handled the validation attempt, so consume it to preserve controller orientation.
                return true;
            }

            storeValidation(level, pos, state, facing, result);
            LOGGER.info(
                    "IPM formed shell creation enabled: {} for Industrial Driller Garage hammer validation at {}",
                    ENABLE_FORMED_SHELL_CREATION,
                    pos
            );
            BlockEntity blockEntity = level.getBlockEntity(pos);
            DockingControllerBlockEntity controllerState =
                    blockEntity instanceof DockingControllerBlockEntity controller ? controller : null;

            FormedShellCreationDecision decision = evaluateFormedShellCreation(
                    ENABLE_FORMED_SHELL_CREATION,
                    controllerState
            );

            if (decision == FormedShellCreationDecision.DISABLED) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.immersive_petro_machinery.docking_platform.hammer_validated",
                                    DockingPlatformValidator.ENTRY_COUNT
                            ),
                            true
                    );
                }
                return true;
            }

            if (decision == FormedShellCreationDecision.BLOCKED_STATE_UNAVAILABLE) {
                LOGGER.warn(
                        "Experimental formed shell creation blocked at {}: Docking Controller block entity is unavailable",
                        pos
                );
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.immersive_petro_machinery.docking_platform.formed_blocked_storage_unavailable"
                            ),
                            false
                    );
                }
                return true;
            }

            if (decision == FormedShellCreationDecision.BLOCKED_PROTECTED_STATE) {
                LOGGER.info(
                        "Experimental formed shell creation blocked at {} because protected state exists: {}",
                        pos,
                        controllerState.protectedFormedStateSummary()
                );
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.immersive_petro_machinery.docking_platform.formed_blocked_protected_state",
                                    controllerState.protectedFormedStateSummary()
                            ),
                            false
                    );
                }
                return true;
            }

            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.immersive_petro_machinery.docking_platform.formed_test_enabled"),
                        true
                );
            }
            LOGGER.info(
                    "Experimental formed shell test mode proceeding at {} with empty protected state",
                    pos
            );

            boolean formed = super.createStructure(level, pos, side, player);
            if (!formed) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.immersive_petro_machinery.docking_platform.formation_failed"),
                            true
                    );
                }
                return true;
            }

            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.immersive_petro_machinery.docking_platform.formed_created_test_mode"),
                        true
                );
            }
            return true;
        }

        private void storeValidation(
                Level level,
                BlockPos pos,
                BlockState state,
                Direction facing,
                DockingPlatformValidator.ValidationResult result
        ) {
            if (level.isClientSide()) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!result.valid() && blockEntity instanceof DockingControllerBlockEntity controllerState) {
                controllerState.clearSoftLock();
            }

            if (state.getValue(DockingControllerBlock.VALID) != result.valid()) {
                level.setBlock(pos, state.setValue(DockingControllerBlock.VALID, result.valid()), Block.UPDATE_CLIENTS);
                DockFluidPortAccess.invalidatePortCapabilities(level, pos, facing);
            }
        }

        @Override
        public float getManualScale() {
            return 8.0F;
        }

        @Override
        public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
            consumer.accept(new DockingPlatformManualData());
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("manual.immersive_petro_machinery.docking_platform");
        }

        @Override
        public Block getBlock() {
            return IPMBlocks.DOCKING_CONTROLLER.get();
        }
    }

    private static final class DockingPlatformManualData implements ClientMultiblocks.MultiblockManualData {
        @Override
        public NonNullList<ItemStack> getTotalMaterials() {
            NonNullList<ItemStack> materials = NonNullList.create();

            Map<Block, Integer> counts = new LinkedHashMap<>();
            for (DockingPlatformValidator.StructureEntry entry : DockingPlatformValidator.requiredStructureEntries()) {
                counts.merge(entry.state().getBlock(), 1, Integer::sum);
            }

            counts.forEach((block, count) -> materials.add(new ItemStack(block, count)));
            return materials;
        }

        @Override
        public boolean canRenderFormedStructure() {
            return false;
        }

        @Override
        public void renderFormedStructure(PoseStack poseStack, MultiBufferSource bufferSource) {
        }
    }
}
