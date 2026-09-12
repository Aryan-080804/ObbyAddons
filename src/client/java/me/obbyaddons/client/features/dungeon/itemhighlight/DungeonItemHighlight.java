package me.obbyaddons.client.features.dungeon.itemhighlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

import me.obbyaddons.client.features.dungeon.shared.DungeonBossTracker;
import me.obbyaddons.client.features.dungeon.shared.DungeonLocationTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class DungeonItemHighlight {

    private static final HashSet<String> ITEMS =
            new HashSet<>(
                    List.of(
                            "Revive Stone",
                            "Trap",
                            "Decoy",
                            "Inflatable Jerry",
                            "Defuse Kit",
                            "Dungeon Chest Key",
                            "Treasure Talisman",
                            "Architect's First Draft",
                            "Spirit Leap",
                            "Healing VIII Splash Potion",
                            "Training Weights",
                            "Candycomb"
                    )
            );

    private static final int GOLD =
            0xFFFFAA00;

    private static final int GREEN =
            0xFF55FF55;

    private static final int RED =
            0xFFFF5555;

    private static final ConcurrentHashMap<ItemEntity, Integer> trackedItems =
            new ConcurrentHashMap<>();

    private DungeonItemHighlight() {
    }

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraftClient -> {

                    ClientLevel world =
                            minecraftClient.level;

                    if (world == null) {
                        return;
                    }

                    world.entitiesForRendering()
                            .forEach(
                                    entity -> {

                                        if (!(entity instanceof ItemEntity item)) {
                                            return;
                                        }

                                        if (trackedItems.containsKey(item)) {
                                            return;
                                        }

                                        if (highlightItem(item)) {
                                            trackedItems.put(
                                                    item,
                                                    0
                                            );
                                        }
                                    }
                            );
                }
        );

        ClientEntityEvents.ENTITY_UNLOAD.register(
                (entity, world) -> {

                    if (entity instanceof ItemEntity item) {
                        trackedItems.remove(item);
                    }
                }
        );
    }

    public static void render(
            LevelRenderContext context,
            PoseStack poseStack,
            VertexConsumer consumer
    ) {

        if (!DungeonItemHighlightSettings.enabled) {
            return;
        }

        double tickProgress =
                Minecraft.getInstance()
                        .getDeltaTracker()
                        .getGameTimeDeltaPartialTick(false);

        trackedItems.forEach(
                (itemEntity, ignored) -> {

                    double x =
                            Mth.lerp(
                                    tickProgress,
                                    itemEntity.xOld,
                                    itemEntity.getX()
                            );

                    double y =
                            Mth.lerp(
                                    tickProgress,
                                    itemEntity.yOld,
                                    itemEntity.getY()
                            );

                    double z =
                            Mth.lerp(
                                    tickProgress,
                                    itemEntity.zOld,
                                    itemEntity.getZ()
                            );

                    EntityDimensions dimensions =
                            itemEntity.getDimensions(
                                    itemEntity.getPose()
                            );

                    AABB box =
                            dimensions.makeBoundingBox(
                                            x,
                                            y,
                                            z
                                    )
                                    .inflate(0.1)
                                    .move(0, 0.05, 0);

                    DungeonItemHighlightRenderer.renderFilledBox(
                            poseStack,
                            consumer,
                            box,
                            getColor(itemEntity)
                    );
                }
        );
    }

    public static boolean highlightItem(
            ItemEntity item
    ) {

        if (!DungeonItemHighlightSettings.enabled) {
            return false;
        }

        if (!DungeonLocationTracker.inDungeon()) {
            return false;
        }

        if (DungeonBossTracker.inBoss()) {
            return false;
        }

        Component itemText =
                item.getItem()
                        .getHoverName();

        if (itemText == null) {
            return false;
        }

        String itemName =
                itemText.getString();

        return ITEMS.contains(itemName);
    }

    public static int getColor(
            ItemEntity item
    ) {

        LocalPlayer player =
                Minecraft.getInstance()
                        .player;

        if (player == null) {
            return 0;
        }

        double distance =
                item.position()
                        .distanceTo(
                                player.position()
                        );

        if (distance > 20.0D) {
            return 0;
        }

        if (distance > 3.5D) {
            return RED;
        }

        if (item.tickCount > 11) {
            return GREEN;
        }

        return GOLD;
    }

    public static boolean hideItem(
            ItemEntity item
    ) {

        if (item == null) {
            return false;
        }

        return trackedItems.containsKey(item);
    }
}