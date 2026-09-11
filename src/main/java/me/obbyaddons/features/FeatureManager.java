package me.obbyaddons.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FeatureManager {

    private static final List<Feature> FEATURES = new ArrayList<>();

    private FeatureManager() {
    }

    public static void register(Feature feature) {
        FEATURES.add(feature);
        feature.initialize();
    }

    public static List<Feature> getFeatures() {
        return Collections.unmodifiableList(FEATURES);
    }

    public static Feature getFeature(String name) {
        for (Feature feature : FEATURES) {
            if (feature.getName().equalsIgnoreCase(name)) {
                return feature;
            }
        }

        return null;
    }
}