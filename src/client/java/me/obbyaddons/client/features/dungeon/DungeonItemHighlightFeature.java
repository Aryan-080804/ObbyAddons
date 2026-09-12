package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class DungeonItemHighlightFeature extends Feature {

    public DungeonItemHighlightFeature() {
        super(
                "Item Highlight",
                ObbyConfig.get().dungeonItemHighlightEnabled
        );
    }

    @Override
    protected void onEnable() {
        DungeonItemHighlightSettings.enabled = true;

        ObbyConfig.get().dungeonItemHighlightEnabled = true;
        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {
        DungeonItemHighlightSettings.enabled = false;

        ObbyConfig.get().dungeonItemHighlightEnabled = false;
        ObbyConfig.save();
    }
}