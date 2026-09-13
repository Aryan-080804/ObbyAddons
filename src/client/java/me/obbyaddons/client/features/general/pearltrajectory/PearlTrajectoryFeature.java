package me.obbyaddons.client.features.general.pearltrajectory;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.feature.Feature;

public final class PearlTrajectoryFeature extends Feature {

    public PearlTrajectoryFeature() {

        super(
                "Pearl Trajectory",
                ObbyConfig.get().pearlTrajectoryEnabled
        );

        PearlTrajectorySettings.enabled =
                ObbyConfig.get().pearlTrajectoryEnabled;

        PearlTrajectorySettings.color =
                ObbyConfig.get().pearlTrajectoryColor;
    }

    @Override
    protected void onEnable() {

        PearlTrajectorySettings.enabled =
                true;

        ObbyConfig.get().pearlTrajectoryEnabled =
                true;

        ObbyConfig.save();
    }

    @Override
    protected void onDisable() {

        PearlTrajectorySettings.enabled =
                false;

        ObbyConfig.get().pearlTrajectoryEnabled =
                false;

        ObbyConfig.save();
    }
}