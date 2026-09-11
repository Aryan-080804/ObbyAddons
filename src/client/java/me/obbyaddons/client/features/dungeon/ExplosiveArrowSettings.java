package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;

public final class ExplosiveArrowSettings {

    private ExplosiveArrowSettings() {
    }

    // Main Explosive Arrow feature
    public static boolean enabled = true;

    // Controls only whether the HUD is visible
    public static boolean damageTrackerHudEnabled = true;

    // HUD color
    public static int damageTrackerColor = 0xFF7C3AED;

    // Resizing
    public static int damageTrackerX = 10;
    public static int damageTrackerY = 10;
    public static float damageTrackerScale = 1.0f;

    public static String getDamageTrackerColorName() {
        return switch (damageTrackerColor) {
            case 0xFFFF5555 -> "Red";
            case 0xFF55FF55 -> "Green";
            case 0xFF55FFFF -> "Cyan";
            case 0xFFFFFF55 -> "Yellow";
            case 0xFFFFFFFF -> "White";
            default -> "Purple";
        };
    }

    public static void cycleDamageTrackerColor() {
        damageTrackerColor = switch (damageTrackerColor) {
            case 0xFF7C3AED -> 0xFFFF5555;
            case 0xFFFF5555 -> 0xFF55FF55;
            case 0xFF55FF55 -> 0xFF55FFFF;
            case 0xFF55FFFF -> 0xFFFFFF55;
            case 0xFFFFFF55 -> 0xFFFFFFFF;
            default -> 0xFF7C3AED;
        };

        ObbyConfig.get().explosiveArrowDamageTrackerColor =
                damageTrackerColor;

        ObbyConfig.save();
    }
}