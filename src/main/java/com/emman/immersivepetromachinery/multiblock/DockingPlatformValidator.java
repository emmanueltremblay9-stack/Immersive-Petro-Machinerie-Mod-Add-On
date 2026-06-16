package com.emman.immersivepetromachinery.multiblock;

import com.emman.immersivepetromachinery.ImmersivePetroMachinery;
import com.emman.immersivepetromachinery.registry.IPMBlocks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DockingPlatformValidator {
    public static final int MIN_X = -5;
    public static final int MAX_X = 5;
    public static final int MIN_Y = -2;
    public static final int MAX_Y = 7;
    public static final int MIN_Z = -12;
    public static final int MAX_Z = 0;

    private static final List<PatternEntry> PATTERN = buildPattern();
    private static final List<ClearanceEntry> CLEARANCE = buildClearance(PATTERN);

    public static final int ENTRY_COUNT = PATTERN.size();
    public static final int CLEARANCE_COUNT = CLEARANCE.size();

    private DockingPlatformValidator() {
    }

    public static ValidationResult validate(BlockGetter level, BlockPos controllerPos, Direction facing) {
        if (facing.getAxis().isVertical()) {
            return ValidationResult.invalid("controller facing is not horizontal", controllerPos, "horizontal facing", null);
        }

        for (PatternEntry entry : PATTERN) {
            BlockPos worldPos = toWorld(controllerPos, facing, entry.localX(), entry.localY(), entry.localZ());
            BlockState actualState = level.getBlockState(worldPos);
            Block expectedBlock = entry.expectedBlock().resolveBlock();

            if (expectedBlock == null) {
                return ValidationResult.invalid(
                        "required block id " + entry.expectedBlock().id() + " for " + entry.symbol()
                                + " at local " + entry.localCoordinates() + " is not registered",
                        worldPos,
                        entry.symbol(),
                        actualState
                );
            }

            if (!actualState.is(expectedBlock)) {
                return ValidationResult.invalid(
                        "expected " + describeExpected(entry, expectedBlock)
                                + " at local " + entry.localCoordinates()
                                + " / world " + worldPos
                                + ", found " + describeBlock(actualState),
                        worldPos,
                        entry.symbol(),
                        actualState
                );
            }
        }

        for (ClearanceEntry entry : CLEARANCE) {
            BlockPos worldPos = toWorld(controllerPos, facing, entry.localX(), entry.localY(), entry.localZ());
            BlockState actualState = level.getBlockState(worldPos);

            if (!actualState.isAir()) {
                return ValidationResult.invalid(
                        "expected clear garage bay air at local " + entry.localCoordinates()
                                + " / world " + worldPos
                                + ", found " + describeBlock(actualState),
                        worldPos,
                        "AIR",
                        actualState
                );
            }
        }

        return ValidationResult.valid("all " + PATTERN.size() + " required blocks and "
                + CLEARANCE.size() + " hollow bay spaces match");
    }

    public static BlockPos toWorld(BlockPos controllerPos, Direction facing, int localX, int localY, int localZ) {
        Direction localRight = facing.getClockWise();
        return controllerPos
                .relative(localRight, localX)
                .relative(Direction.UP, localY)
                .relative(facing, -localZ);
    }

    public static List<StructureEntry> requiredStructureEntries() {
        return PATTERN.stream()
                .map(entry -> new StructureEntry(
                        entry.localX(),
                        entry.localY(),
                        entry.localZ(),
                        entry.symbol(),
                        entry.expectedBlock().defaultState()
                ))
                .toList();
    }

    private static List<PatternEntry> buildPattern() {
        Map<LocalPos, PatternEntry> entries = new LinkedHashMap<>();
        addSideSupports(entries);
        addRampAndServiceLayer(entries);
        addRearServiceWallAndTanks(entries);
        addRearServiceWallDetails(entries);
        addTopArmature(entries);
        addSideProfileDetails(entries);
        addServiceArms(entries);
        addHempcreteFoundationFill(entries);
        return List.copyOf(entries.values());
    }

    private static List<ClearanceEntry> buildClearance(List<PatternEntry> requiredEntries) {
        List<ClearanceEntry> clearance = new ArrayList<>();
        List<LocalPos> requiredPositions = requiredEntries.stream()
                .map(PatternEntry::pos)
                .toList();

        for (int x = -3; x <= 3; x++) {
            for (int y = 1; y <= 5; y++) {
                for (int z = -9; z <= -3; z++) {
                    LocalPos pos = new LocalPos(x, y, z);
                    if (!requiredPositions.contains(pos)) {
                        clearance.add(new ClearanceEntry(pos));
                    }
                }
            }
        }

        for (int x = -2; x <= 2; x++) {
            for (int y = 1; y <= 4; y++) {
                for (int z = -12; z <= -10; z++) {
                    LocalPos pos = new LocalPos(x, y, z);
                    if (!requiredPositions.contains(pos)) {
                        clearance.add(new ClearanceEntry(pos));
                    }
                }
            }
        }

        return List.copyOf(clearance);
    }

    private static void addSideSupports(Map<LocalPos, PatternEntry> entries) {
        for (int x : new int[]{-5, 5}) {
            for (int z : new int[]{-12, -8, -4, 0}) {
                add(entries, x, -1, z, "SS");

                for (int y = 0; y <= 7; y++) {
                    if (isFunctionalServiceBlock(x, y, z) || isRearTankBody(x, y, z)) {
                        continue;
                    }

                    add(entries, x, y, z, "SS");
                }
            }
        }
    }

    private static void addRampAndServiceLayer(Map<LocalPos, PatternEntry> entries) {
        for (int z = -12; z <= -11; z++) {
            for (int x = -2; x <= 2; x++) {
                add(entries, x, -1, z, "SS");
            }
        }

        for (int z = -12; z <= -9; z++) {
            add(entries, -3, 0, z, "SF");
            add(entries, 3, 0, z, "SF");
        }

        for (int z = -10; z <= -1; z++) {
            for (int x = -2; x <= 2; x++) {
                add(entries, x, 0, z, Math.abs(x) <= 1 ? "LR" : "SS");
            }
        }

        for (int z = MIN_Z; z <= MAX_Z; z++) {
            for (int x : new int[]{-5, -4, 4, 5}) {
                if (!isFunctionalServiceBlock(x, 0, z)) {
                    add(entries, x, 0, z, "SS");
                }
            }
        }
    }

    private static void addRearServiceWallAndTanks(Map<LocalPos, PatternEntry> entries) {
        add(entries, -4, 0, 0, "FP");
        add(entries, -2, 0, 0, "RB");
        add(entries, 0, 0, 0, "DC");
        add(entries, 2, 0, 0, "UB");
        add(entries, 4, 0, 0, "LP");
        add(entries, 5, 0, -1, "OP");
        add(entries, 0, 1, 0, "SC");

        add(entries, -1, 0, 0, "LE");
        add(entries, 1, 0, 0, "RE");

        for (int x = -4; x <= -2; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = -2; z <= 0; z++) {
                    add(entries, x, y, z, "SM");
                }
            }
        }

        for (int x = 2; x <= 4; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = -2; z <= 0; z++) {
                    add(entries, x, y, z, "SM");
                }
            }
        }
    }

    private static void addRearServiceWallDetails(Map<LocalPos, PatternEntry> entries) {
        for (int x : new int[]{-3, 3}) {
            set(entries, x, 0, 0, "HE");
        }

        for (int x = -4; x <= 4; x++) {
            set(entries, x, 4, 0, x == -4 || x == 4 ? "PU" : "PI");
        }
    }

    private static void addTopArmature(Map<LocalPos, PatternEntry> entries) {
        for (int z = MIN_Z; z <= MAX_Z; z++) {
            add(entries, -5, 7, z, "SS");
            add(entries, 5, 7, z, "SS");
        }

        for (int x = MIN_X; x <= MAX_X; x++) {
            add(entries, x, 7, 0, "SS");
            add(entries, x, 7, MIN_Z, "SS");
            add(entries, x, 7, -8, "SS");
            add(entries, x, 7, -4, "SS");
        }

        for (int z = -10; z <= -2; z++) {
            add(entries, 0, 6, z, "SF");
        }

        for (int x = -2; x <= 2; x++) {
            add(entries, x, 6, -6, "SF");
        }
    }

    private static void addSideProfileDetails(Map<LocalPos, PatternEntry> entries) {
        for (int x : new int[]{-4, 4}) {
            for (int z = -10; z <= -2; z++) {
                set(entries, x, 6, z, "SF");
            }
        }
    }


    private static void addServiceArms(Map<LocalPos, PatternEntry> entries) {
        add(entries, -4, 4, -1, "PU");
        add(entries, 4, 4, -1, "PU");

        for (int z = -2; z >= -9; z--) {
            add(entries, -4, 4, z, "PI");
            add(entries, 4, 4, z, "PI");
        }

        add(entries, -3, 4, -7, "PI");
        add(entries, -2, 4, -7, "PI");
        add(entries, 3, 4, -7, "PI");
        add(entries, 2, 4, -7, "PI");
    }

    private static void addHempcreteFoundationFill(Map<LocalPos, PatternEntry> entries) {
        for (int y = MIN_Y; y <= 0; y++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                for (int x = MIN_X; x <= MAX_X; x++) {
                    if (isRampLowerApproachClearance(x, y, z)) {
                        continue;
                    }

                    addIfEmpty(entries, x, y, z, "HC");
                }
            }
        }
    }

    private static boolean isFunctionalServiceBlock(int x, int y, int z) {
        return (y == 0 && z == 0 && (x == -4 || x == -2 || x == 0 || x == 2 || x == 4))
                || (x == 5 && y == 0 && z == -1)
                || (x == 0 && y == 1 && z == 0);
    }

    private static boolean isRearTankBody(int x, int y, int z) {
        return z >= -2
                && z <= 0
                && y >= 1
                && y <= 3
                && ((x >= -4 && x <= -2) || (x >= 2 && x <= 4));
    }

    private static boolean isRampLowerApproachClearance(int x, int y, int z) {
        return y == 0
                && x >= -2
                && x <= 2
                && z >= -12
                && z <= -11;
    }

    private static void add(Map<LocalPos, PatternEntry> entries, int x, int y, int z, String symbol) {
        LocalPos pos = new LocalPos(x, y, z);
        PatternEntry entry = new PatternEntry(pos, symbol, blockFor(symbol));
        PatternEntry existing = entries.putIfAbsent(pos, entry);

        if (existing != null && !existing.symbol().equals(symbol)) {
            throw new IllegalStateException("Conflicting Industrial Driller Garage symbols at "
                    + pos.localCoordinates() + ": " + existing.symbol() + " and " + symbol);
        }
    }

    private static void addIfEmpty(Map<LocalPos, PatternEntry> entries, int x, int y, int z, String symbol) {
        LocalPos pos = new LocalPos(x, y, z);
        entries.putIfAbsent(pos, new PatternEntry(pos, symbol, blockFor(symbol)));
    }

    private static void set(Map<LocalPos, PatternEntry> entries, int x, int y, int z, String symbol) {
        LocalPos pos = new LocalPos(x, y, z);
        entries.put(pos, new PatternEntry(pos, symbol, blockFor(symbol)));
    }

    private static String describeExpected(PatternEntry entry, Block expectedBlock) {
        return entry.symbol() + " / " + entry.expectedBlock().id()
                + " / " + expectedBlock.getName().getString();
    }

    private static String describeBlock(BlockState state) {
        Block block = state.getBlock();
        return BuiltInRegistries.BLOCK.getKey(block) + " / " + block.getName().getString();
    }

    private static ExpectedBlock blockFor(String symbol) {
        return switch (symbol) {
            case "DC" -> ipm(symbol, "docking_controller", IPMBlocks.DOCKING_CONTROLLER);
            case "FP" -> ipm(symbol, "fuel_port", IPMBlocks.FUEL_PORT);
            case "LP" -> ipm(symbol, "lubricant_port", IPMBlocks.LUBRICANT_PORT);
            case "OP" -> ipm(symbol, "output_port", IPMBlocks.OUTPUT_PORT);
            case "RB" -> ipm(symbol, "repair_bay", IPMBlocks.REPAIR_BAY);
            case "UB" -> ipm(symbol, "upgrade_bay", IPMBlocks.UPGRADE_BAY);
            case "SC" -> ipm(symbol, "survey_console", IPMBlocks.SURVEY_CONSOLE);
            case "LR" -> ipm(symbol, "locking_rail", IPMBlocks.LOCKING_RAIL);
            case "SS" -> registry(symbol, "immersiveengineering", "steel_scaffolding_standard");
            case "HE" -> registry(symbol, "immersiveengineering", "heavy_engineering");
            case "LE" -> registry(symbol, "immersiveengineering", "light_engineering");
            case "RE" -> registry(symbol, "immersiveengineering", "rs_engineering");
            case "SM" -> registry(symbol, "immersiveengineering", "sheetmetal_steel");
            case "PI" -> registry(symbol, "immersiveengineering", "fluid_pipe");
            case "PU" -> registry(symbol, "immersiveengineering", "fluid_pump");
            case "SF" -> registry(symbol, "immersiveengineering", "steel_fence");
            case "HC" -> registry(symbol, "immersiveengineering", "hempcrete");
            default -> throw new IllegalArgumentException("Unknown Industrial Driller Garage symbol: " + symbol);
        };
    }

    private static ExpectedBlock ipm(String symbol, String path, Supplier<? extends Block> supplier) {
        return new ExpectedBlock(
                symbol,
                ResourceLocation.fromNamespaceAndPath(ImmersivePetroMachinery.MOD_ID, path),
                supplier
        );
    }

    private static ExpectedBlock registry(String symbol, String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        return new ExpectedBlock(symbol, id, () -> BuiltInRegistries.BLOCK.get(id));
    }

    private record LocalPos(int x, int y, int z) {
        private String localCoordinates() {
            return "(" + x + ", " + y + ", " + z + ")";
        }
    }

    private record PatternEntry(LocalPos pos, String symbol, ExpectedBlock expectedBlock) {
        private int localX() {
            return pos.x();
        }

        private int localY() {
            return pos.y();
        }

        private int localZ() {
            return pos.z();
        }

        private String localCoordinates() {
            return pos.localCoordinates();
        }
    }

    private record ClearanceEntry(LocalPos pos) {
        private int localX() {
            return pos.x();
        }

        private int localY() {
            return pos.y();
        }

        private int localZ() {
            return pos.z();
        }

        private String localCoordinates() {
            return pos.localCoordinates();
        }
    }

    private record ExpectedBlock(String symbol, ResourceLocation id, Supplier<? extends Block> supplier) {
        @Nullable
        private Block resolveBlock() {
            Block block = supplier.get();
            return id.equals(BuiltInRegistries.BLOCK.getKey(block)) ? block : null;
        }

        private Block requireBlock() {
            Block block = resolveBlock();
            if (block == null) {
                throw new IllegalStateException("Required block id is not registered for "
                        + symbol + ": " + id);
            }
            return block;
        }

        private BlockState defaultState() {
            return requireBlock().defaultBlockState();
        }
    }

    public record StructureEntry(int localX, int localY, int localZ, String symbol, BlockState state) {
    }

    public record ValidationResult(
            boolean valid,
            String summary,
            @Nullable BlockPos failurePos,
            @Nullable String expectedSymbol,
            @Nullable BlockState actualState
    ) {
        private static ValidationResult valid(String summary) {
            return new ValidationResult(true, summary, null, null, null);
        }

        private static ValidationResult invalid(String summary, BlockPos failurePos, String expectedSymbol, @Nullable BlockState actualState) {
            return new ValidationResult(false, summary, failurePos, expectedSymbol, actualState);
        }
    }
}
