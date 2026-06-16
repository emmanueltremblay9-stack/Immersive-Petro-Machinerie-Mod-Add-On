package com.emman.immersivepetromachinery.gametest;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.block.DockingControllerBlock;
import com.emman.immersivepetromachinery.block.SurveyConsoleBlock;
import com.emman.immersivepetromachinery.block.entity.DockingControllerBlockEntity;
import com.emman.immersivepetromachinery.manual.IPMManualMultiblocks;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

@GameTestHolder(ImmersivePetroMachinery.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IPMGarageSafetyGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMPLATE = "empty";
    private static final BlockPos CONTROLLER_POS = new BlockPos(1, 1, 1);
    private static final ResourceLocation DIESEL_ID = ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "diesel");
    private static final ResourceLocation LUBRICANT_ID =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "lubricant");

    private IPMGarageSafetyGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void normal_mode_formation_disabled_does_not_replace_controller(GameTestHelper helper) {
        DockingControllerBlockEntity controller = placeController(helper);
        controller.getGarageUpgradeInventory().setStackInSlot(0, new ItemStack(IPMBlocks.CORE_SAMPLING_UPGRADE.get()));

        helper.assertFalse(
                IPMManualMultiblocks.isFormedShellCreationEnabledForRuntime(),
                "formed shell creation must be disabled for default runtime"
        );
        helper.assertValueEqual(
                IPMManualMultiblocks.FormedShellCreationDecision.DISABLED,
                IPMManualMultiblocks.evaluateFormedShellCreation(false, controller),
                "normal mode should stop before formed shell creation"
        );
        assertControllerUnreplaced(helper);
        helper.assertTrue(controller.hasProtectedStateForFormation(), "test controller should contain protected state");
        helper.assertValueEqual(
                1,
                controller.getGarageUpgradeItemCountForDiagnostics(),
                "upgrade inventory must remain intact"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void protected_state_blocks_experimental_formation(GameTestHelper helper) {
        DockingControllerBlockEntity controller = placeController(helper);
        controller.getGarageUpgradeInventory().setStackInSlot(0, new ItemStack(IPMBlocks.CORE_SAMPLING_UPGRADE.get()));

        helper.assertValueEqual(
                IPMManualMultiblocks.FormedShellCreationDecision.BLOCKED_PROTECTED_STATE,
                IPMManualMultiblocks.evaluateFormedShellCreation(true, controller),
                "experimental formation must block protected controller state"
        );
        assertControllerUnreplaced(helper);
        helper.assertValueEqual(
                1,
                controller.getGarageUpgradeItemCountForDiagnostics(),
                "protected upgrade item must remain intact"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void survey_console_containment_no_outputs(GameTestHelper helper) {
        DockingControllerBlockEntity controller = placeController(helper);
        BlockPos surveyPos = CONTROLLER_POS.above();
        helper.setBlock(surveyPos, IPMBlocks.SURVEY_CONSOLE.get());

        boolean contained = SurveyConsoleBlock.containSurveyOutputForDiagnostics(
                helper.getLevel(),
                helper.absolutePos(surveyPos),
                helper.absolutePos(CONTROLLER_POS)
        );
        List<ItemEntity> itemEntities = helper.getLevel().getEntitiesOfClass(
                ItemEntity.class,
                new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(8.0D)
        );

        helper.assertTrue(contained, "survey containment method should report contained output");
        helper.assertTrue(itemEntities.isEmpty(), "contained survey console path must not create item entities");
        helper.assertTrue(controller.getGarageUpgradeInventory().getStackInSlot(0).isEmpty(), "survey must not mutate upgrades");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void garage_upgrade_inventory_persists_basic_nbt(GameTestHelper helper) {
        DockingControllerBlockEntity controller = placeController(helper);
        controller.getGarageUpgradeInventory().setStackInSlot(0, new ItemStack(IPMBlocks.CORE_SAMPLING_UPGRADE.get()));

        DockingControllerBlockEntity loaded = saveAndReload(helper, controller);

        helper.assertValueEqual(
                1,
                loaded.getGarageUpgradeItemCountForDiagnostics(),
                "garage upgrade inventory should persist through NBT"
        );
        helper.assertTrue(
                loaded.getGarageUpgradeInventory().getStackInSlot(0).is(IPMBlocks.CORE_SAMPLING_UPGRADE.get()),
                "core sampling upgrade should survive NBT reload"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void fluid_tank_persists_basic_nbt_if_safe(GameTestHelper helper) {
        DockingControllerBlockEntity controller = placeController(helper);
        Fluid diesel = BuiltInRegistries.FLUID.get(DIESEL_ID);
        Fluid lubricant = BuiltInRegistries.FLUID.get(LUBRICANT_ID);
        if (diesel == Fluids.EMPTY || lubricant == Fluids.EMPTY) {
            LOGGER.warn(
                    "Skipping IPM fluid persistence diagnostic: required fluids unavailable, diesel={}, lubricant={}",
                    diesel,
                    lubricant
            );
            helper.succeed();
            return;
        }

        int fuelFilled = controller.getFuelPortHandler().fill(
                new FluidStack(diesel, 500),
                IFluidHandler.FluidAction.EXECUTE
        );
        int lubricantFilled = controller.getLubricantPortHandler().fill(
                new FluidStack(lubricant, 250),
                IFluidHandler.FluidAction.EXECUTE
        );
        helper.assertValueEqual(500, fuelFilled, "diesel fill should be accepted");
        helper.assertValueEqual(250, lubricantFilled, "lubricant fill should be accepted");

        DockingControllerBlockEntity loaded = saveAndReload(helper, controller);

        helper.assertValueEqual(500, loaded.getFuelAmountForDiagnostics(), "fuel tank should persist through NBT");
        helper.assertValueEqual(
                250,
                loaded.getLubricantAmountForDiagnostics(),
                "lubricant tank should persist through NBT"
        );
        helper.succeed();
    }

    private static DockingControllerBlockEntity placeController(GameTestHelper helper) {
        BlockState state = IPMBlocks.DOCKING_CONTROLLER.get()
                .defaultBlockState()
                .setValue(DockingControllerBlock.FACING, Direction.NORTH);
        helper.setBlock(CONTROLLER_POS, state);
        BlockEntity blockEntity = helper.getBlockEntity(CONTROLLER_POS);
        helper.assertTrue(
                blockEntity instanceof DockingControllerBlockEntity,
                "Docking Controller block entity should be present"
        );
        return (DockingControllerBlockEntity) blockEntity;
    }

    private static void assertControllerUnreplaced(GameTestHelper helper) {
        helper.assertBlockPresent(IPMBlocks.DOCKING_CONTROLLER.get(), CONTROLLER_POS);
        helper.assertBlockNotPresent(Blocks.AIR, CONTROLLER_POS);
        helper.assertTrue(
                helper.getBlockEntity(CONTROLLER_POS) instanceof DockingControllerBlockEntity,
                "Docking Controller block entity should remain present"
        );
    }

    private static DockingControllerBlockEntity saveAndReload(
            GameTestHelper helper,
            DockingControllerBlockEntity controller
    ) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag saved = controller.saveWithFullMetadata(registries);
        DockingControllerBlockEntity loaded = new DockingControllerBlockEntity(
                helper.absolutePos(CONTROLLER_POS),
                IPMBlocks.DOCKING_CONTROLLER.get()
                        .defaultBlockState()
                        .setValue(DockingControllerBlock.FACING, Direction.NORTH)
        );
        loaded.loadWithComponents(saved, registries);
        return loaded;
    }
}
