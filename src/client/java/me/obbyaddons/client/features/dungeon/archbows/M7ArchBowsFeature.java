
package me.obbyaddons.client.features.dungeon.archbows;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class M7ArchBowsFeature extends Feature {

    public M7ArchBowsFeature() {
        super(
                "M7 Arch Bows",
                ObbyConfig.get().m7ArchBowsEnabled
        );
    }

    @Override
    protected void onEnable() {

        ObbyConfig.get().m7ArchBowsEnabled = true;

        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {

        ObbyConfig.get().m7ArchBowsEnabled = false;

        ObbyConfig.save();
    }
}
