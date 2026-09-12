package me.obbyaddons.client.features.dungeon.terminator;

import me.obbyaddons.client.config.ObbyConfig;

public final class TerminatorOverlaySettings {

    private TerminatorOverlaySettings() {
    }

    public static int color = 0xFFFFFFFF;

    public static String getColorName() {
        return switch (color) {
            case 0xFF7C3AED -> "Purple";
            case 0xFFFF5555 -> "Red";
            case 0xFF55FF55 -> "Green";
            case 0xFF55FFFF -> "Cyan";
            case 0xFFFFFF55 -> "Yellow";
            case 0xFFFFFFFF -> "White";
            default -> "White";
        };
    }

    public static void cycleColor() {

        color = switch (color) {
            case 0xFFFFFFFF -> 0xFF7C3AED;
            case 0xFF7C3AED -> 0xFFFF5555;
            case 0xFFFF5555 -> 0xFF55FF55;
            case 0xFF55FF55 -> 0xFF55FFFF;
            case 0xFF55FFFF -> 0xFFFFFF55;
            case 0xFFFFFF55 -> 0xFFFFFFFF;
            default -> 0xFFFFFFFF;
        };

        ObbyConfig.get().terminatorOverlayColor = color;
        ObbyConfig.save();
    }
}