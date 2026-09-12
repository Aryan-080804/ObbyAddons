package me.obbyaddons.client.features.dungeon.tracker;

import me.obbyaddons.client.config.ObbyConfig;

public final class DungeonRunTrackerSettings {

    private DungeonRunTrackerSettings() {
    }

    public static boolean enabled = true;

    public static int x = 10;
    public static int y = 120;

    public static float scale = 1.0f;

    public static int color = 0xFFFFFFFF;

    public static void setPosition(
            int newX,
            int newY
    ) {
        x = newX;
        y = newY;

        ObbyConfig.get().dungeonRunTrackerX = x;
        ObbyConfig.get().dungeonRunTrackerY = y;

        ObbyConfig.save();
    }

    public static void setScale(
            float newScale
    ) {
        scale =
                Math.max(
                        0.5f,
                        Math.min(
                                3.0f,
                                newScale
                        )
                );

        ObbyConfig.get().dungeonRunTrackerScale =
                scale;

        ObbyConfig.save();
    }
}