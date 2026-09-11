package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class ExplosiveArrowFeature extends Feature {

    public ExplosiveArrowFeature() {
        super(
                "Explosive Arrow",
                ObbyConfig.get().explosiveArrowEnabled
        );
    }

    @Override
    protected void onEnable() {
        ExplosiveArrowSettings.enabled = true;

        ObbyConfig.get().explosiveArrowEnabled = true;
        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {
        ExplosiveArrowSettings.enabled = false;

        ObbyConfig.get().explosiveArrowEnabled = false;
        ObbyConfig.save();
    }
}