package me.obbyaddons.client.features.general.pearltrajectory;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import net.minecraft.resources.Identifier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class PearlTrajectoryRenderer {

    /*
     * Odin-style pearl physics.
     */
    private static final double THROW_SPEED =
            1.5D;

    private static final double DRAG =
            0.99D;

    private static final double GRAVITY =
            0.03D;

    /*
     * Odin defaults to 30 solver ticks.
     * We use 60 so longer pearl throws still show.
     */
    private static final int MAX_TICKS =
            60;

    /*
     * Visual size of the impact marker.
     */
    private static final double IMPACT_HALF_SIZE =
            0.125D;

    /*
     * Conservative collision radius used for trajectory prediction.
     * This is intentionally smaller than the pearl's full half-width
     * to avoid false positives through tight gaps.
     */
    private static final double PEARL_COLLISION_RADIUS =
            0.06D;

    /*
     * Filled impact marker pipeline.
     *
     * The actual trajectory uses Minecraft's built-in
     * RenderType.lines(), which avoids the perspective
     * distortion from our old camera-facing ribbon.
     */
    private static final RenderPipeline IMPACT_PIPELINE =
            net.minecraft.client.renderer.RenderPipelines.register(
                    RenderPipeline.builder(
                                    net.minecraft.client.renderer
                                            .RenderPipelines
                                            .DEBUG_FILLED_SNIPPET
                            )
                            .withLocation(
                                    Identifier.fromNamespaceAndPath(
                                            "obbyaddons",
                                            "pearl-trajectory-impact"
                                    )
                            )
                            .withVertexFormat(
                                    DefaultVertexFormat.POSITION_COLOR,
                                    VertexFormat.Mode.QUADS
                            )
                            .withDepthStencilState(
                                    new DepthStencilState(
                                            CompareOp.ALWAYS_PASS,
                                            false
                                    )
                            )
                            .build()
            );

    private static final RenderType IMPACT_RENDER_TYPE =
            RenderType.create(
                    "obbyaddons-pearl-trajectory-impact",
                    RenderSetup.builder(
                                    IMPACT_PIPELINE
                            )
                            .createRenderSetup()
            );

    private PearlTrajectoryRenderer() {
    }

    public static void init() {

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(
                PearlTrajectoryRenderer::render
        );
    }

    private static void render(
            LevelRenderContext context
    ) {

        if (!PearlTrajectorySettings.enabled) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        LocalPlayer player =
                minecraft.player;

        if (
                player == null
                        || minecraft.level == null
        ) {
            return;
        }

        if (!isHoldingPearl(player)) {
            return;
        }

        LevelRenderState levelState =
                context.levelState();

        PoseStack poseStack =
                context.poseStack();

        MultiBufferSource bufferSource =
                context.bufferSource();

        if (
                levelState == null
                        || poseStack == null
                        || bufferSource == null
        ) {
            return;
        }

        TrajectoryResult result =
                calculateTrajectory(
                        minecraft,
                        player
                );

        if (result.points.size() < 2) {
            return;
        }

        Vec3 camera =
                levelState.cameraRenderState.pos;

        poseStack.pushPose();

        poseStack.translate(
                -camera.x,
                -camera.y,
                -camera.z
        );

        VertexConsumer lineConsumer =
                bufferSource.getBuffer(
                        RenderTypes.lines()
                );

        drawTrajectoryLine(
                poseStack,
                lineConsumer,
                result.points,
                PearlTrajectorySettings.color
        );

        if (result.hit != null) {

            VertexConsumer impactConsumer =
                    bufferSource.getBuffer(
                            IMPACT_RENDER_TYPE
                    );

            Vec3 impactPosition =
                    result.impactPosition != null
                            ? result.impactPosition
                            : result.hit.getLocation();

            renderImpactBox(
                    poseStack,
                    impactConsumer,
                    impactPosition,
                    PearlTrajectorySettings.color
            );
        }

        poseStack.popPose();
    }

    private static boolean isHoldingPearl(
            LocalPlayer player
    ) {

        ItemStack mainHand =
                player.getMainHandItem();

        ItemStack offHand =
                player.getOffhandItem();

        return mainHand.is(Items.ENDER_PEARL)
                || offHand.is(Items.ENDER_PEARL);
    }

    /*
     * Mirrors Odin's pearl trajectory structure:
     *
     * 1. Spawn slightly offset from the player.
     * 2. Launch at speed 1.5.
     * 3. Raycast every simulated step.
     * 4. Apply gravity before drag to Y:
     *
     *    x = x * 0.99
     *    y = (y - 0.03) * 0.99
     *    z = z * 0.99
     *
     * 5. Stop on the first block collision.
     */
    private static TrajectoryResult calculateTrajectory(
            Minecraft minecraft,
            LocalPlayer player
    ) {

        List<Vec3> points =
                new ArrayList<>();

        double yawRadians =
                Math.toRadians(
                        player.getYRot()
                );

        double xOffset =
                -Math.cos(
                        yawRadians
                ) * 0.16D;

        double zOffset =
                -Math.sin(
                        yawRadians
                ) * 0.16D;

        Vec3 position =
                new Vec3(
                        player.getX()
                                + xOffset,
                        player.getY()
                                + player.getEyeHeight()
                                - 0.1D,
                        player.getZ()
                                + zOffset
                );

        Vec3 motion =
                getLook(
                        player.getYRot(),
                        player.getXRot()
                )
                        .normalize()
                        .scale(
                                THROW_SPEED
                        );

        BlockHitResult finalHit =
                null;

        Vec3 finalImpactPosition =
                null;

        for (
                int tick = 0;
                tick < MAX_TICKS;
                tick++
        ) {

            points.add(
                    position
            );

            Vec3 next =
                    position.add(
                            motion
                    );

            PearlCollision collision =
                    findPearlCollision(
                            minecraft,
                            player,
                            position,
                            next
                    );

            if (collision != null) {

                finalHit =
                        collision.hit;

                finalImpactPosition =
                        collision.centerPosition;

                points.add(
                        finalImpactPosition
                );

                break;
            }

            motion =
                    new Vec3(
                            motion.x
                                    * DRAG,
                            (
                                    motion.y
                                            - GRAVITY
                            ) * DRAG,
                            motion.z
                                    * DRAG
                    );

            position =
                    position.add(
                            motion
                    );
        }

        return new TrajectoryResult(
                points,
                finalHit,
                finalImpactPosition
        );
    }

    /*
     * A normal level.clip(...) checks only the pearl's center line.
     *
     * To catch near-edge collisions without making the predictor too
     * aggressive, trace the center plus six axis-offset rays using a
     * reduced effective radius.
     *
     * We keep the earliest hit among those rays.
     */
    private static PearlCollision findPearlCollision(
            Minecraft minecraft,
            LocalPlayer player,
            Vec3 start,
            Vec3 end
    ) {

        double r =
                PEARL_COLLISION_RADIUS;

        Vec3[] offsets =
                new Vec3[] {
                        Vec3.ZERO,

                        new Vec3( r, 0.0D, 0.0D),
                        new Vec3(-r, 0.0D, 0.0D),

                        new Vec3(0.0D,  r, 0.0D),
                        new Vec3(0.0D, -r, 0.0D),

                        new Vec3(0.0D, 0.0D,  r),
                        new Vec3(0.0D, 0.0D, -r)
                };

        PearlCollision best =
                null;

        double bestFraction =
                Double.POSITIVE_INFINITY;

        Vec3 travel =
                end.subtract(
                        start
                );

        double segmentLength =
                travel.length();

        if (segmentLength <= 0.0000001D) {
            return null;
        }

        for (Vec3 offset : offsets) {

            Vec3 offsetStart =
                    start.add(
                            offset
                    );

            Vec3 offsetEnd =
                    end.add(
                            offset
                    );

            HitResult result =
                    minecraft.level.clip(
                            new ClipContext(
                                    offsetStart,
                                    offsetEnd,
                                    ClipContext.Block.COLLIDER,
                                    ClipContext.Fluid.NONE,
                                    player
                            )
                    );

            if (
                    result.getType()
                            != HitResult.Type.BLOCK
            ) {
                continue;
            }

            BlockHitResult blockHit =
                    (BlockHitResult) result;

            double hitDistance =
                    offsetStart.distanceTo(
                            blockHit.getLocation()
                    );

            double fraction =
                    Math.max(
                            0.0D,
                            Math.min(
                                    1.0D,
                                    hitDistance
                                            / segmentLength
                            )
                    );

            if (fraction >= bestFraction) {
                continue;
            }

            bestFraction =
                    fraction;

            Vec3 centerAtImpact =
                    start.add(
                            travel.scale(
                                    fraction
                            )
                    );

            best =
                    new PearlCollision(
                            blockHit,
                            centerAtImpact
                    );
        }

        return best;
    }

    /*
     * Odin-style yaw/pitch -> direction conversion.
     */
    private static Vec3 getLook(
            float yaw,
            float pitch
    ) {

        double pitchRadians =
                -pitch
                        * 0.017453292D;

        double yawRadians =
                -yaw
                        * 0.017453292D
                        - Math.PI;

        double horizontal =
                -Math.cos(
                        pitchRadians
                );

        return new Vec3(
                Math.sin(
                        yawRadians
                ) * horizontal,
                Math.sin(
                        pitchRadians
                ),
                Math.cos(
                        yawRadians
                ) * horizontal
        );
    }

    private static void drawTrajectoryLine(
            PoseStack poseStack,
            VertexConsumer consumer,
            List<Vec3> points,
            int argb
    ) {

        float a =
                ((argb >> 24) & 0xFF)
                        / 255.0F;

        float r =
                ((argb >> 16) & 0xFF)
                        / 255.0F;

        float g =
                ((argb >> 8) & 0xFF)
                        / 255.0F;

        float b =
                (argb & 0xFF)
                        / 255.0F;

        Matrix4f matrix =
                poseStack.last().pose();

        for (
                int i = 0;
                i < points.size() - 1;
                i++
        ) {

            Vec3 start =
                    points.get(
                            i
                    );

            Vec3 end =
                    points.get(
                            i + 1
                    );

            Vec3 delta =
                    end.subtract(
                            start
                    );

            if (
                    delta.lengthSqr()
                            <= 0.0000001D
            ) {
                continue;
            }

            Vec3 normal =
                    delta.normalize();

            consumer.addVertex(
                            matrix,
                            (float) start.x,
                            (float) start.y,
                            (float) start.z
                    )
                    .setColor(
                            r,
                            g,
                            b,
                            a
                    )
                    .setNormal(
                            (float) normal.x,
                            (float) normal.y,
                            (float) normal.z
                    )
                    .setLineWidth(
                            1.5F
                    );

            consumer.addVertex(
                            matrix,
                            (float) end.x,
                            (float) end.y,
                            (float) end.z
                    )
                    .setColor(
                            r,
                            g,
                            b,
                            a
                    )
                    .setNormal(
                            (float) normal.x,
                            (float) normal.y,
                            (float) normal.z
                    )
                    .setLineWidth(
                            1.5F
                    );
        }
    }

    private static void renderImpactBox(
            PoseStack poseStack,
            VertexConsumer consumer,
            Vec3 position,
            int argb
    ) {

        AABB box =
                new AABB(
                        position.x
                                - IMPACT_HALF_SIZE,
                        position.y
                                - IMPACT_HALF_SIZE,
                        position.z
                                - IMPACT_HALF_SIZE,
                        position.x
                                + IMPACT_HALF_SIZE,
                        position.y
                                + IMPACT_HALF_SIZE,
                        position.z
                                + IMPACT_HALF_SIZE
                );

        renderFilledBox(
                poseStack,
                consumer,
                box,
                argb
        );
    }

    private static void renderFilledBox(
            PoseStack poseStack,
            VertexConsumer consumer,
            AABB box,
            int argb
    ) {

        float a =
                ((argb >> 24) & 0xFF)
                        / 255.0F;

        float r =
                ((argb >> 16) & 0xFF)
                        / 255.0F;

        float g =
                ((argb >> 8) & 0xFF)
                        / 255.0F;

        float b =
                (argb & 0xFF)
                        / 255.0F;

        Matrix4f matrix =
                poseStack.last().pose();

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;

        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        quad(
                matrix,
                consumer,
                minX, minY, minZ,
                minX, maxY, minZ,
                maxX, maxY, minZ,
                maxX, minY, minZ,
                r, g, b, a
        );

        quad(
                matrix,
                consumer,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                maxX, minY, maxZ,
                r, g, b, a
        );

        quad(
                matrix,
                consumer,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ,
                minX, minY, maxZ,
                r, g, b, a
        );

        quad(
                matrix,
                consumer,
                minX, minY, maxZ,
                minX, maxY, maxZ,
                minX, maxY, minZ,
                minX, minY, minZ,
                r, g, b, a
        );

        quad(
                matrix,
                consumer,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ,
                r, g, b, a
        );

        quad(
                matrix,
                consumer,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                minX, minY, maxZ,
                minX, minY, minZ,
                r, g, b, a
        );
    }

    private static void quad(
            Matrix4f matrix,
            VertexConsumer consumer,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            double x3,
            double y3,
            double z3,
            double x4,
            double y4,
            double z4,
            float r,
            float g,
            float b,
            float a
    ) {

        consumer.addVertex(
                        matrix,
                        (float) x1,
                        (float) y1,
                        (float) z1
                )
                .setColor(
                        r,
                        g,
                        b,
                        a
                )
                .setNormal(
                        0,
                        1,
                        0
                );

        consumer.addVertex(
                        matrix,
                        (float) x2,
                        (float) y2,
                        (float) z2
                )
                .setColor(
                        r,
                        g,
                        b,
                        a
                )
                .setNormal(
                        0,
                        1,
                        0
                );

        consumer.addVertex(
                        matrix,
                        (float) x3,
                        (float) y3,
                        (float) z3
                )
                .setColor(
                        r,
                        g,
                        b,
                        a
                )
                .setNormal(
                        0,
                        1,
                        0
                );

        consumer.addVertex(
                        matrix,
                        (float) x4,
                        (float) y4,
                        (float) z4
                )
                .setColor(
                        r,
                        g,
                        b,
                        a
                )
                .setNormal(
                        0,
                        1,
                        0
                );
    }

    private static final class PearlCollision {

        private final BlockHitResult hit;
        private final Vec3 centerPosition;

        private PearlCollision(
                BlockHitResult hit,
                Vec3 centerPosition
        ) {

            this.hit =
                    hit;

            this.centerPosition =
                    centerPosition;
        }
    }

    private static final class TrajectoryResult {

        private final List<Vec3> points;
        private final BlockHitResult hit;
        private final Vec3 impactPosition;

        private TrajectoryResult(
                List<Vec3> points,
                BlockHitResult hit,
                Vec3 impactPosition
        ) {

            this.points =
                    points;

            this.hit =
                    hit;

            this.impactPosition =
                    impactPosition;
        }
    }
}
