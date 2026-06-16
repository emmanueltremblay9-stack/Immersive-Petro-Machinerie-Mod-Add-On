package com.emman.immersivepetromachinery.survey;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.mojang.datafixers.util.Pair;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.survey.ReservoirInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class DockSurveyService {
    private DockSurveyService() {
    }

    public static SurveyResult createSurveyResults(Level level, BlockPos controllerPos) {
        BlockPos samplePos = samplePositionForControllerChunk(controllerPos);
        ItemStack reservoirSurveyResult = new ItemStack(IPContent.Items.SURVEYRESULT.get());
        Reservoir reservoir = ReservoirHandler.getReservoir(level, samplePos);
        boolean hasReservoirInfo = reservoir != null;

        if (reservoir != null) {
            ReservoirInfo.create(level, samplePos, reservoir).writeToStack(reservoirSurveyResult);
        }

        return new SurveyResult(
                samplePos,
                createCoreSample(level, samplePos),
                reservoirSurveyResult,
                hasReservoirInfo
        );
    }

    private static BlockPos samplePositionForControllerChunk(BlockPos controllerPos) {
        ChunkPos chunkPos = new ChunkPos(controllerPos);
        return new BlockPos(chunkPos.getMinBlockX() + 8, controllerPos.getY(), chunkPos.getMinBlockZ() + 8);
    }

    private static ItemStack createCoreSample(Level level, BlockPos samplePos) {
        MineralWorldInfo mineralInfo = ExcavatorHandler.getMineralWorldInfo(level, samplePos);
        List<CoresampleItem.VeinSample> veinSamples = mineralInfo == null
                ? List.of()
                : mineralInfo.getAllVeins().stream()
                        .map(vein -> createVeinSample(mineralInfo, samplePos, vein))
                        .toList();

        ItemStack coreSample = new ItemStack(IEItems.Misc.CORESAMPLE.get());
        coreSample.set(
                IEDataComponents.CORESAMPLE,
                new CoresampleItem.ItemData(
                        new CoresampleItem.SamplePosition(level.dimension(), samplePos.getX(), samplePos.getZ()),
                        veinSamples,
                        level.getGameTime()
                )
        );
        return coreSample;
    }

    private static CoresampleItem.VeinSample createVeinSample(
            MineralWorldInfo mineralInfo,
            BlockPos samplePos,
            Pair<MineralVein, Integer> veinWeight
    ) {
        MineralVein vein = veinWeight.getFirst();
        double saturation = 1.0D - vein.getFailChance(samplePos);
        double percentageInTotalSample = mineralInfo.getTotalWeight() <= 0
                ? 0.0D
                : veinWeight.getSecond() / (double) mineralInfo.getTotalWeight();
        return new CoresampleItem.VeinSample(
                vein.getMineralName(),
                vein.getDepletion(),
                saturation,
                percentageInTotalSample
        );
    }

    public record SurveyResult(
            BlockPos samplePos,
            ItemStack coreSample,
            ItemStack reservoirSurveyResult,
            boolean hasReservoirInfo
    ) {
    }
}
