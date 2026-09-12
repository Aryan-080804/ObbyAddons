package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class TerminalClickTimerFeature extends Feature {

    public TerminalClickTimerFeature() {
        super(
                "Click Prot Display",
                ObbyConfig.get().terminalClickTimerEnabled
        );
    }

    @Override
    protected void onEnable() {

        ObbyConfig.get().terminalClickTimerEnabled = true;
        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {

        ObbyConfig.get().terminalClickTimerEnabled = false;
        ObbyConfig.save();

        TerminalClickTimer.reset();
    }
}