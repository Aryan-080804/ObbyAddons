package me.obbyaddons.client.features.dungeon.tracker;

import me.obbyaddons.ObbyAddons;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class DungeonRunTrackerHud {

    private DungeonRunTrackerHud() {
    }

    public static void init() {

        HudElementRegistry.addLast(
                ObbyAddons.id(
                        "dungeon_run_tracker"
                ),
                (graphics, deltaTracker) -> {

                    if (
                            !DungeonRunTrackerSettings.enabled
                                    || !DungeonRunTrackerSettings.hudEnabled
                    ) {
                        return;
                    }

                    Minecraft minecraft =
                            Minecraft.getInstance();

                    if (minecraft.player == null) {
                        return;
                    }

                    String floor =
                            DungeonRunStats.getLatestFloor();

                    int runs =
                            DungeonRunStats.getTotalRuns();

                    String last =
                            DungeonRunStats.formatTime(
                                    DungeonRunStats.getLastTimeMs()
                            );

                    String average =
                            DungeonRunStats.formatTime(
                                    DungeonRunStats.getAverageTimeMs()
                            );

                    String best =
                            DungeonRunStats.formatTime(
                                    DungeonRunStats.getBestTimeMs()
                            );

                    String[] lines = {
                            floor,
                            "Runs: " + runs,
                            "Last: " + last,
                            "Avg: " + average,
                            "Best: " + best
                    };

                    int x =
                            DungeonRunTrackerSettings.x;

                    int y =
                            DungeonRunTrackerSettings.y;

                    float scale =
                            DungeonRunTrackerSettings.scale;

                    graphics.pose().pushMatrix();

                    graphics.pose().translate(
                            x,
                            y
                    );

                    graphics.pose().scale(
                            scale,
                            scale
                    );

                    int yOffset = 0;

                    for (String line : lines) {

                        graphics.text(
                                minecraft.font,
                                Component.literal(line),
                                0,
                                yOffset,
                                DungeonRunTrackerSettings.color,
                                true
                        );

                        yOffset +=
                                minecraft.font.lineHeight + 1;
                    }

                    graphics.pose().popMatrix();
                }
        );
    }
}