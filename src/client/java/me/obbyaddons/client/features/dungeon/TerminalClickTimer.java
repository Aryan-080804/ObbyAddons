package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.client.config.ObbyConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class TerminalClickTimer {

    private static long startTimeNanos = 0L;
    private static boolean active = false;
    private static Screen lastScreen = null;

    private TerminalClickTimer() {
    }

    public static void render(
            Screen screen,
            GuiGraphicsExtractor graphics
    ) {

        if (!ObbyConfig.get().terminalClickTimerEnabled) {
            reset();
            return;
        }

        boolean terminalScreen =
                isTerminalScreen(screen);

        if (!terminalScreen) {
            active = false;
            lastScreen = screen;
            return;
        }

        // Start again whenever a new terminal screen opens.
        if (screen != lastScreen) {
            start();
        }

        lastScreen = screen;

        if (!active) {
            return;
        }

        long elapsedNanos =
                System.nanoTime() - startTimeNanos;

        long elapsedMs =
                elapsedNanos / 1_000_000L;

        long remainingMs =
                TerminalClickTimerSettings.clickDelayMs
                        - elapsedMs;

        if (remainingMs <= 0) {
            active = false;
            return;
        }

        String text =
                remainingMs + "ms";

        Minecraft minecraft =
                Minecraft.getInstance();

        int x =
                TerminalClickTimerSettings.x;

        int y =
                TerminalClickTimerSettings.y;

        float scale =
                TerminalClickTimerSettings.scale;

        graphics.pose().pushMatrix();

        graphics.pose().translate(
                x,
                y
        );

        graphics.pose().scale(
                scale,
                scale
        );

        graphics.text(
                minecraft.font,
                Component.literal(text),
                0,
                0,
                TerminalClickTimerSettings.color,
                true
        );

        graphics.pose().popMatrix();
    }

    private static boolean isTerminalScreen(
            Screen screen
    ) {

        if (screen == null) {
            return false;
        }

        String title =
                screen.getTitle()
                        .getString()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        // Real Hypixel F7 / M7 terminals.

        if (title.equals("correct all the panes!")) {
            return true;
        }

        if (title.equals("click in order!")) {
            return true;
        }

        if (title.equals("change all to same color!")) {
            return true;
        }

        if (title.equals("click the button on time!")) {
            return true;
        }

        if (title.startsWith("what starts with:")) {
            return true;
        }

        if (title.startsWith("select all the ")) {
            return true;
        }

        // Odin terminal simulator support.
        String className =
                screen.getClass()
                        .getName()
                        .toLowerCase(Locale.ROOT);

        return className.contains("com.odtheking.odin")
                && className.contains("termsimgui");
    }

    private static void start() {
        startTimeNanos =
                System.nanoTime();

        active = true;
    }

    public static void reset() {
        startTimeNanos = 0L;
        active = false;
        lastScreen = null;
    }
}