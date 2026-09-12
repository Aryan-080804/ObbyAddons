package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.dungeon.tracker.DungeonRunTrackerSettings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class DungeonRunTrackerSettingsWindow {

    private int x;
    private int y;

    private static final int WIDTH = 190;
    private static final int HEADER_HEIGHT = 20;
    private static final int ROW_HEIGHT = 20;

    private static final int PURPLE = 0xFF7C3AED;
    private static final int PURPLE_DARK = 0xFF3B0764;

    private static final int BACKGROUND = 0xF0181818;
    private static final int HEADER = 0xF0222222;
    private static final int ROW_BACKGROUND = 0xFF1D1D1D;
    private static final int ROW_HOVER = 0xFF2A2A2A;
    private static final int CLOSE_HOVER = 0xFF3A3A3A;

    private static final int TOGGLE_OFF = 0xFF3A3A3A;
    private static final int TOGGLE_ON = PURPLE_DARK;
    private static final int TOGGLE_KNOB = 0xFFFFFFFF;

    private static final int TEXT = 0xFFFFFFFF;

    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public DungeonRunTrackerSettingsWindow(
            int x,
            int y
    ) {
        this.x = x;
        this.y = y;
    }

    public void render(
            Screen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        int rows = 1;

        int height =
                HEADER_HEIGHT
                        + rows * ROW_HEIGHT;

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + height,
                BACKGROUND
        );

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + 2,
                PURPLE
        );

        graphics.fill(
                x,
                y + 2,
                x + WIDTH,
                y + HEADER_HEIGHT,
                HEADER
        );

        if (
                isCloseButtonHovered(
                        mouseX,
                        mouseY
                )
        ) {
            graphics.fill(
                    x + WIDTH - 18,
                    y + 2,
                    x + WIDTH,
                    y + HEADER_HEIGHT,
                    CLOSE_HOVER
            );
        }

        graphics.text(
                screen.getFont(),
                "DUNGEON RUN TRACKER",
                x + 6,
                y + 7,
                TEXT,
                true
        );

        graphics.text(
                screen.getFont(),
                "X",
                x + WIDTH - 12,
                y + 7,
                TEXT,
                true
        );

        int rowY =
                y + HEADER_HEIGHT;

        drawToggleRow(
                screen,
                graphics,
                "HUD Toggle",
                DungeonRunTrackerSettings.hudEnabled,
                rowY,
                mouseX,
                mouseY
        );
    }

    private void drawToggleRow(
            Screen screen,
            GuiGraphicsExtractor graphics,
            String label,
            boolean enabled,
            int rowY,
            int mouseX,
            int mouseY
    ) {
        boolean hovered =
                mouseX >= x
                        && mouseX < x + WIDTH
                        && mouseY >= rowY
                        && mouseY < rowY + ROW_HEIGHT;

        graphics.fill(
                x,
                rowY,
                x + WIDTH,
                rowY + ROW_HEIGHT,
                hovered
                        ? ROW_HOVER
                        : ROW_BACKGROUND
        );

        graphics.text(
                screen.getFont(),
                label,
                x + 6,
                rowY + 6,
                TEXT,
                true
        );

        int toggleWidth = 24;
        int toggleHeight = 10;

        int toggleX =
                x + WIDTH
                        - toggleWidth
                        - 7;

        int toggleY =
                rowY
                        + (
                        ROW_HEIGHT
                                - toggleHeight
                ) / 2;

        graphics.fill(
                toggleX,
                toggleY,
                toggleX + toggleWidth,
                toggleY + toggleHeight,
                enabled
                        ? TOGGLE_ON
                        : TOGGLE_OFF
        );

        int knobSize = 8;

        int knobX =
                enabled
                        ? toggleX
                        + toggleWidth
                        - knobSize
                        - 1
                        : toggleX + 1;

        int knobY =
                toggleY + 1;

        graphics.fill(
                knobX,
                knobY,
                knobX + knobSize,
                knobY + knobSize,
                TOGGLE_KNOB
        );
    }

    public boolean handleClick(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return false;
        }

        if (
                mouseX < x
                        || mouseX >= x + WIDTH
                        || mouseY < y + HEADER_HEIGHT
        ) {
            return false;
        }

        int index =
                (int) (
                        (
                                mouseY
                                        - (
                                        y
                                                + HEADER_HEIGHT
                                )
                        )
                                / ROW_HEIGHT
                );

        if (index == 0) {

            DungeonRunTrackerSettings.setHudEnabled(
                    !DungeonRunTrackerSettings.hudEnabled
            );

            return true;
        }

        return false;
    }

    public boolean isCloseButtonHovered(
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x + WIDTH - 18
                && mouseX < x + WIDTH
                && mouseY >= y + 2
                && mouseY < y + HEADER_HEIGHT;
    }

    public boolean isHeaderHovered(
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x
                && mouseX < x + WIDTH
                && mouseY >= y
                && mouseY < y + HEADER_HEIGHT;
    }

    public void startDragging(
            double mouseX,
            double mouseY
    ) {
        dragging = true;

        dragOffsetX =
                mouseX - x;

        dragOffsetY =
                mouseY - y;
    }

    public void dragTo(
            double mouseX,
            double mouseY
    ) {
        if (!dragging) {
            return;
        }

        x =
                (int) (
                        mouseX
                                - dragOffsetX
                );

        y =
                (int) (
                        mouseY
                                - dragOffsetY
                );
    }

    public void stopDragging() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }
}