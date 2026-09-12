package me.obbyaddons.client.features.dungeon.tracker;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class DungeonRunTrackerFeature extends Feature {

    public DungeonRunTrackerFeature() {
        super(
                "Dungeon Run Tracker",
                ObbyConfig.get().dungeonRunTrackerEnabled
        );
    }

    @Override
    protected void onEnable() {

        DungeonRunTrackerSettings.enabled = true;

        ObbyConfig.get().dungeonRunTrackerEnabled = true;

        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {

        DungeonRunTrackerSettings.enabled = false;

        ObbyConfig.get().dungeonRunTrackerEnabled = false;

        ObbyConfig.save();
    }
}