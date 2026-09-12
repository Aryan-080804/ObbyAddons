package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;

public final class TerminalClickTimerSettings {

    private TerminalClickTimerSettings() {
    }

    public static int clickDelayMs = 400;

    public static int color = 0xFFFFFFFF;

    public static int x = 100;
    public static int y = 30;

    public static float scale = 1.0f;

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

        ObbyConfig.get().terminalClickTimerColor = color;
        ObbyConfig.save();
    }

    public static void setClickDelayMs(int value) {

        clickDelayMs =
                Math.max(
                        300,
                        Math.min(600, value)
                );

        ObbyConfig.get().terminalClickDelayMs =
                clickDelayMs;

        ObbyConfig.save();
    }

    public static void setPosition(
            int newX,
            int newY
    ) {

        x = newX;
        y = newY;

        ObbyConfig.get().terminalClickTimerX = x;
        ObbyConfig.get().terminalClickTimerY = y;

        ObbyConfig.save();
    }

    public static void setScale(float newScale) {

        scale =
                Math.max(
                        0.5f,
                        Math.min(2.0f, newScale)
                );

        ObbyConfig.get().terminalClickTimerScale = scale;

        ObbyConfig.save();
    }
}