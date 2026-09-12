package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class TerminatorOverlayFeature extends Feature {

    public TerminatorOverlayFeature() {
        super(
                "Term Ult Enchant",
                ObbyConfig.get().terminatorOverlayEnabled
        );
    }

    @Override
    protected void onEnable() {
        ObbyConfig.get().terminatorOverlayEnabled = true;
        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {
        ObbyConfig.get().terminatorOverlayEnabled = false;
        ObbyConfig.save();
    }
}