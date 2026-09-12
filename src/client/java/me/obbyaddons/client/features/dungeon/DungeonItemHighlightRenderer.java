package me.obbyaddons.client.features.dungeon;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

public final class DungeonItemHighlightRenderer {

    private static final RenderPipeline FILLED_NO_DEPTH_PIPELINE =
            net.minecraft.client.renderer.RenderPipelines.register(
                    RenderPipeline.builder(
                                    net.minecraft.client.renderer
                                            .RenderPipelines
                                            .DEBUG_FILLED_SNIPPET
                            )
                            .withLocation(
                                    Identifier.fromNamespaceAndPath(
                                            "obbyaddons",
                                            "item-highlight-filled-no-depth"
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

    private static final RenderType FILLED_NO_DEPTH =
            RenderType.create(
                    "obbyaddons-item-highlight-filled-no-depth",
                    RenderSetup.builder(
                                    FILLED_NO_DEPTH_PIPELINE
                            )
                            .createRenderSetup()
            );

    private DungeonItemHighlightRenderer() {
    }

    public static void init() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(
                DungeonItemHighlightRenderer::render
        );
    }

    private static void render(LevelRenderContext context) {

        if (!DungeonItemHighlightSettings.enabled) {
            return;
        }

        LevelRenderState levelState =
                context.levelState();

        if (levelState == null) {
            return;
        }

        PoseStack poseStack =
                context.poseStack();

        if (poseStack == null) {
            return;
        }

        MultiBufferSource bufferSource =
                context.bufferSource();

        if (bufferSource == null) {
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

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        FILLED_NO_DEPTH
                );

        DungeonItemHighlight.render(
                context,
                poseStack,
                consumer
        );

        poseStack.popPose();
    }

    public static void renderFilledBox(
            PoseStack poseStack,
            VertexConsumer consumer,
            AABB box,
            int argb
    ) {

        float[] color =
                toFloats(argb);

        if (color[3] == 0.0F) {
            return;
        }

        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];

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
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);

        consumer.addVertex(
                        matrix,
                        (float) x2,
                        (float) y2,
                        (float) z2
                )
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);

        consumer.addVertex(
                        matrix,
                        (float) x3,
                        (float) y3,
                        (float) z3
                )
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);

        consumer.addVertex(
                        matrix,
                        (float) x4,
                        (float) y4,
                        (float) z4
                )
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
    }

    private static float[] toFloats(int argb) {

        float r =
                ((argb >> 16) & 0xFF)
                        / 255.0F;

        float g =
                ((argb >> 8) & 0xFF)
                        / 255.0F;

        float b =
                (argb & 0xFF)
                        / 255.0F;

        float a =
                ((argb >> 24) & 0xFF)
                        / 255.0F;

        return new float[]{
                r,
                g,
                b,
                a
        };
    }
}