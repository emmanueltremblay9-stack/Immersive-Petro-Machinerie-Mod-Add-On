package com.emman.immersivepetromachinery.docking;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TunnelDiggerDockingDetector {
    public static final ResourceLocation TUNNEL_DIGGER_ENTITY_ID =
            ResourceLocation.fromNamespaceAndPath("immersive_machinery", "tunnel_digger");

    public static final double SEARCH_MIN_X = -4.25D;
    public static final double SEARCH_MAX_X = 4.25D;
    public static final double SEARCH_MIN_Y = -1.25D;
    public static final double SEARCH_MAX_Y = 5.75D;
    public static final double SEARCH_MIN_Z = -12.75D;
    public static final double SEARCH_MAX_Z = 0.75D;

    private static final double LANE_HALF_WIDTH = 3.25D;
    private static final double READY_MIN_Z = -11.75D;
    private static final double READY_MAX_Z = -1.00D;
    private static final double MAX_AXIS_YAW_ERROR_DEGREES = 45.0D;

    private TunnelDiggerDockingDetector() {
    }

    public static DetectionResult detect(Level level, BlockPos controllerPos, Direction facing) {
        AABB searchBox = buildSearchBox(controllerPos, facing);
        List<Entity> tunnelDiggers = level.getEntities((Entity)null, searchBox, TunnelDiggerDockingDetector::isTunnelDigger);

        if (tunnelDiggers.isEmpty()) {
            return DetectionResult.none(searchBox);
        }

        List<Candidate> candidates = tunnelDiggers.stream()
                .map(entity -> inspect(entity, controllerPos, facing))
                .toList();

        Candidate bestAligned = candidates.stream()
                .filter(Candidate::aligned)
                .min(Comparator.comparingDouble(Candidate::alignmentScore))
                .orElse(null);

        if (bestAligned != null) {
            return DetectionResult.aligned(tunnelDiggers.size(), bestAligned, searchBox);
        }

        Candidate nearest = candidates.stream()
                .min(Comparator.comparingDouble(Candidate::alignmentScore))
                .orElseThrow();
        return DetectionResult.misaligned(tunnelDiggers.size(), nearest, searchBox);
    }

    public static AABB buildSearchBox(BlockPos controllerPos, Direction facing) {
        AABB box = null;

        for (double x : new double[]{SEARCH_MIN_X, SEARCH_MAX_X}) {
            for (double y : new double[]{SEARCH_MIN_Y, SEARCH_MAX_Y}) {
                for (double z : new double[]{SEARCH_MIN_Z, SEARCH_MAX_Z}) {
                    Vec3 corner = localToWorld(controllerPos, facing, x, y, z);
                    AABB pointBox = new AABB(corner, corner);
                    box = box == null ? pointBox : box.minmax(pointBox);
                }
            }
        }

        return box == null ? new AABB(controllerPos) : box.inflate(0.25D);
    }

    public static Vec3 localToWorld(BlockPos controllerPos, Direction facing, double localX, double localY, double localZ) {
        Direction localRight = facing.getClockWise();
        Vec3 origin = Vec3.atCenterOf(controllerPos);
        return origin.add(
                localRight.getStepX() * localX + facing.getStepX() * -localZ,
                localY,
                localRight.getStepZ() * localX + facing.getStepZ() * -localZ
        );
    }

    public static LocalPosition worldToLocal(BlockPos controllerPos, Direction facing, Vec3 worldPosition) {
        Direction localRight = facing.getClockWise();
        Vec3 origin = Vec3.atCenterOf(controllerPos);
        Vec3 delta = worldPosition.subtract(origin);

        double localX = delta.x * localRight.getStepX() + delta.z * localRight.getStepZ();
        double localY = delta.y;
        double localZ = -(delta.x * facing.getStepX() + delta.z * facing.getStepZ());
        return new LocalPosition(localX, localY, localZ);
    }

    private static Candidate inspect(Entity entity, BlockPos controllerPos, Direction facing) {
        Vec3 samplePosition = entity.getBoundingBox().getCenter();
        LocalPosition localPosition = worldToLocal(controllerPos, facing, samplePosition);
        double yawError = axisYawError(entity.getYRot(), facing);
        boolean inLane = Math.abs(localPosition.x()) <= LANE_HALF_WIDTH
                && localPosition.z() >= READY_MIN_Z
                && localPosition.z() <= READY_MAX_Z;
        boolean facingCompatible = yawError <= MAX_AXIS_YAW_ERROR_DEGREES;

        return new Candidate(
                entity.getUUID(),
                entity.getDisplayName().getString(),
                localPosition,
                Direction.fromYRot(entity.getYRot()),
                yawError,
                inLane,
                facingCompatible
        );
    }

    private static boolean isTunnelDigger(Entity entity) {
        return TUNNEL_DIGGER_ENTITY_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    private static double axisYawError(float entityYaw, Direction facing) {
        double frontError = angularDistanceDegrees(entityYaw, facing.toYRot());
        double backError = angularDistanceDegrees(entityYaw, facing.getOpposite().toYRot());
        return Math.min(frontError, backError);
    }

    private static double angularDistanceDegrees(float first, float second) {
        return Math.abs(Mth.wrapDegrees(first - second));
    }

    public enum DetectionStatus {
        NONE,
        MISALIGNED,
        ALIGNED
    }

    public record LocalPosition(double x, double y, double z) {
    }

    public record Candidate(
            UUID uuid,
            String displayName,
            LocalPosition localPosition,
            Direction direction,
            double yawErrorDegrees,
            boolean inLane,
            boolean facingCompatible
    ) {
        public boolean aligned() {
            return inLane && facingCompatible;
        }

        private double alignmentScore() {
            double lanePenalty = Math.abs(localPosition.x());
            double depthPenalty = localPosition.z() < READY_MIN_Z
                    ? READY_MIN_Z - localPosition.z()
                    : Math.max(0.0D, localPosition.z() - READY_MAX_Z);
            return lanePenalty + depthPenalty + yawErrorDegrees / 45.0D;
        }

        public String summary() {
            return String.format(
                    Locale.ROOT,
                    "local x=%.2f, z=%.2f, facing=%s, yaw error=%.1f deg, lane=%s",
                    localPosition.x(),
                    localPosition.z(),
                    direction.getSerializedName(),
                    yawErrorDegrees,
                    inLane ? "yes" : "no"
            );
        }
    }

    public record DetectionResult(
            DetectionStatus status,
            int candidates,
            @Nullable Candidate candidate,
            AABB searchBox
    ) {
        private static DetectionResult none(AABB searchBox) {
            return new DetectionResult(DetectionStatus.NONE, 0, null, searchBox);
        }

        private static DetectionResult misaligned(int candidates, Candidate candidate, AABB searchBox) {
            return new DetectionResult(DetectionStatus.MISALIGNED, candidates, candidate, searchBox);
        }

        private static DetectionResult aligned(int candidates, Candidate candidate, AABB searchBox) {
            return new DetectionResult(DetectionStatus.ALIGNED, candidates, candidate, searchBox);
        }

        public String summary() {
            return candidate == null ? "searched docking lane" : candidate.summary();
        }
    }
}
