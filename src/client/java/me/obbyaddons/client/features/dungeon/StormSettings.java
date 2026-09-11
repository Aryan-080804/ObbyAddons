package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;

public final class StormSettings {

    private StormSettings() {
    }

    public static boolean stormTickTimer = true;

    public static int stormTimerColor = 0xFF7C3AED;
    public static int stormDeathTimerColor = 0xFFFFFF55;

    public static int stormTimerX = 100;
    public static int stormTimerY = 60;
    public static float stormTimerScale = 1.0f;

    public static int stormDeathTimerX = 100;
    public static int stormDeathTimerY = 80;
    public static float stormDeathTimerScale = 1.0f;

    public static boolean tickDownFrom5 = true;
    public static boolean firstDeathTime = true;
    public static boolean spiritMaskWarning = true;

    public static boolean stormLbEnabled = true;
    public static int stormLbTickOffset = 0;
    public static int stormLbColor = 0xFF7C3AED;

    public static int stormLbX = 100;
    public static int stormLbY = 100;
    public static float stormLbScale = 1.0f;

    public static String getStormTimerColorName() {
        return switch (stormTimerColor) {
            case 0xFFFF5555 -> "Red";
            case 0xFF55FF55 -> "Green";
            case 0xFF55FFFF -> "Cyan";
            case 0xFFFFFF55 -> "Yellow";
            case 0xFFFFFFFF -> "White";
            default -> "Purple";
        };
    }

    public static void cycleStormTimerColor() {
        stormTimerColor = switch (stormTimerColor) {
            case 0xFF7C3AED -> 0xFFFF5555;
            case 0xFFFF5555 -> 0xFF55FF55;
            case 0xFF55FF55 -> 0xFF55FFFF;
            case 0xFF55FFFF -> 0xFFFFFF55;
            case 0xFFFFFF55 -> 0xFFFFFFFF;
            default -> 0xFF7C3AED;
        };

        ObbyConfig.get().stormTimerColor = stormTimerColor;
        ObbyConfig.save();
    }

    public static String getStormDeathTimerColorName() {
        return switch (stormDeathTimerColor) {
            case 0xFFFF5555 -> "Red";
            case 0xFF55FF55 -> "Green";
            case 0xFF55FFFF -> "Cyan";
            case 0xFFFFFF55 -> "Yellow";
            case 0xFFFFFFFF -> "White";
            default -> "Purple";
        };
    }

    public static void cycleStormDeathTimerColor() {
        stormDeathTimerColor = switch (stormDeathTimerColor) {
            case 0xFF7C3AED -> 0xFFFF5555;
            case 0xFFFF5555 -> 0xFF55FF55;
            case 0xFF55FF55 -> 0xFF55FFFF;
            case 0xFF55FFFF -> 0xFFFFFF55;
            case 0xFFFFFF55 -> 0xFFFFFFFF;
            default -> 0xFF7C3AED;
        };

        ObbyConfig.get().stormDeathTimerColor = stormDeathTimerColor;
        ObbyConfig.save();
    }

    public static String getStormLbColorName() {
        return switch (stormLbColor) {
            case 0xFFFF5555 -> "Red";
            case 0xFF55FF55 -> "Green";
            case 0xFF55FFFF -> "Cyan";
            case 0xFFFFFF55 -> "Yellow";
            case 0xFFFFFFFF -> "White";
            default -> "Purple";
        };
    }

    public static void cycleStormLbColor() {
        stormLbColor = switch (stormLbColor) {
            case 0xFF7C3AED -> 0xFFFF5555;
            case 0xFFFF5555 -> 0xFF55FF55;
            case 0xFF55FF55 -> 0xFF55FFFF;
            case 0xFF55FFFF -> 0xFFFFFF55;
            case 0xFFFFFF55 -> 0xFFFFFFFF;
            default -> 0xFF7C3AED;
        };

        ObbyConfig.get().stormLbColor = stormLbColor;
        ObbyConfig.save();
    }
}