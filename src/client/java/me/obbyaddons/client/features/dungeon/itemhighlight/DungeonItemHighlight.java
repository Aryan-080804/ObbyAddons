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
import java.util.Set;
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

    private static final Set<ItemEntity> trackedItems =
            ConcurrentHashMap.newKeySet();

    private static ClientLevel trackedLevel =
            null;

    private DungeonItemHighlight() {
    }

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(
                minecraftClient -> {

                    ClientLevel world =
                            minecraftClient.level;

                    // Completely reset when there is no active world.
                    if (world == null) {

                        clearTrackedItems();

                        trackedLevel =
                                null;

                        return;
                    }

                    // Never carry ItemEntity references between worlds.
                    if (trackedLevel != world) {

                        clearTrackedItems();

                        trackedLevel =
                                world;
                    }

                    // If the feature is disabled, do not retain stale
                    // highlighted entities.
                    if (!DungeonItemHighlightSettings.enabled) {

                        clearTrackedItems();

                        return;
                    }

                    // Remove entities that are no longer valid or should
                    // no longer be highlighted.
                    trackedItems.removeIf(
                            item ->
                                    item == null
                                            || item.isRemoved()
                                            || item.level() != world
                                            || !highlightItem(item)
                    );

                    // Discover currently loaded dungeon items.
                    world.entitiesForRendering()
                            .forEach(
                                    entity -> {

                                        if (!(entity instanceof ItemEntity item)) {
                                            return;
                                        }

                                        if (trackedItems.contains(item)) {
                                            return;
                                        }

                                        if (highlightItem(item)) {

                                            trackedItems.add(
                                                    item
                                            );
                                        }
                                    }
                            );
                }
        );

        ClientEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> {

                    if (!(entity instanceof ItemEntity item)) {
                        return;
                    }

                    if (!DungeonItemHighlightSettings.enabled) {
                        return;
                    }

                    if (highlightItem(item)) {

                        trackedItems.add(
                                item
                        );
                    }
                }
        );

        ClientEntityEvents.ENTITY_UNLOAD.register(
                (entity, world) -> {

                    if (entity instanceof ItemEntity item) {

                        trackedItems.remove(
                                item
                        );
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

        Minecraft minecraft =
                Minecraft.getInstance();

        ClientLevel world =
                minecraft.level;

        if (world == null) {
            return;
        }

        double tickProgress =
                minecraft
                        .getDeltaTracker()
                        .getGameTimeDeltaPartialTick(
                                false
                        );

        trackedItems.forEach(
                itemEntity -> {

                    if (
                            itemEntity == null
                                    || itemEntity.isRemoved()
                                    || itemEntity.level() != world
                    ) {
                        return;
                    }

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
                            dimensions
                                    .makeBoundingBox(
                                            x,
                                            y,
                                            z
                                    )
                                    .inflate(
                                            0.1
                                    )
                                    .move(
                                            0,
                                            0.05,
                                            0
                                    );

                    int color =
                            getColor(
                                    itemEntity
                            );

                    // >20 blocks intentionally stays invisible,
                    // matching the existing behavior.
                    if (color == 0) {
                        return;
                    }

                    DungeonItemHighlightRenderer.renderFilledBox(
                            poseStack,
                            consumer,
                            box,
                            color
                    );
                }
        );
    }

    public static boolean highlightItem(
            ItemEntity item
    ) {

        if (item == null) {
            return false;
        }

        if (item.isRemoved()) {
            return false;
        }

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

        if (itemName == null) {
            return false;
        }

        return ITEMS.contains(
                itemName.trim()
        );
    }

    public static int getColor(
            ItemEntity item
    ) {

        if (item == null) {
            return 0;
        }

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

        if (!DungeonItemHighlightSettings.enabled) {
            return false;
        }

        if (item.isRemoved()) {
            return false;
        }

        ClientLevel world =
                Minecraft.getInstance()
                        .level;

        if (
                world == null
                        || item.level() != world
        ) {
            return false;
        }

        return trackedItems.contains(
                item
        );
    }

    public static void clearTrackedItems() {

        trackedItems.clear();
    }
}