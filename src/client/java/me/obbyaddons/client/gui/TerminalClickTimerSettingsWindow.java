package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.dungeon.TerminalClickTimerSettings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class TerminalClickTimerSettingsWindow {

    private int x;
    private int y;

    private static final int WIDTH = 190;
    private static final int HEADER_HEIGHT = 20;
    private static final int ROW_HEIGHT = 20;

    private static final int CLICK_DELAY_MIN = 300;
    private static final int CLICK_DELAY_MAX = 600;

    private static final int PURPLE = 0xFF7C3AED;

    private static final int BACKGROUND = 0xF0181818;
    private static final int HEADER = 0xF0222222;
    private static final int ROW_BACKGROUND = 0xFF1D1D1D;
    private static final int CLOSE_HOVER = 0xFF3A3A3A;
    private static final int SLIDER_BACKGROUND = 0xFF3A3A3A;

    private static final int TEXT = 0xFFFFFFFF;

    private boolean dragging;
    private boolean draggingDelaySlider;

    private double dragOffsetX;
    private double dragOffsetY;

    public TerminalClickTimerSettingsWindow(
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

        int rows = 2;

        int height =
                HEADER_HEIGHT
                        + (rows * ROW_HEIGHT);

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

        if (isCloseButtonHovered(
                mouseX,
                mouseY
        )) {

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
                "TERMINAL TIMER",
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

        drawDelaySlider(
                screen,
                graphics,
                rowY
        );

        rowY += ROW_HEIGHT;

        drawColorRow(
                screen,
                graphics,
                rowY
        );
    }

    private void drawDelaySlider(
            Screen screen,
            GuiGraphicsExtractor graphics,
            int rowY
    ) {

        graphics.fill(
                x,
                rowY,
                x + WIDTH,
                rowY + ROW_HEIGHT,
                ROW_BACKGROUND
        );

        graphics.text(
                screen.getFont(),
                "First click",
                x + 6,
                rowY + 6,
                TEXT,
                true
        );

        String valueText =
                TerminalClickTimerSettings.clickDelayMs
                        + "ms";

        graphics.text(
                screen.getFont(),
                valueText,
                x + 88
                        - screen.getFont().width(valueText),
                rowY + 6,
                PURPLE,
                true
        );

        int sliderX = x + 98;
        int sliderWidth = 82;
        int sliderY = rowY + 9;

        graphics.fill(
                sliderX,
                sliderY,
                sliderX + sliderWidth,
                sliderY + 3,
                SLIDER_BACKGROUND
        );

        float progress =
                (
                        TerminalClickTimerSettings.clickDelayMs
                                - CLICK_DELAY_MIN
                )
                        / (float)
                        (
                                CLICK_DELAY_MAX
                                        - CLICK_DELAY_MIN
                        );

        int knobX =
                sliderX
                        + Math.round(
                                progress * sliderWidth
                        );

        graphics.fill(
                knobX - 2,
                sliderY - 3,
                knobX + 3,
                sliderY + 6,
                PURPLE
        );
    }

    private void drawColorRow(
            Screen screen,
            GuiGraphicsExtractor graphics,
            int rowY
    ) {

        graphics.fill(
                x,
                rowY,
                x + WIDTH,
                rowY + ROW_HEIGHT,
                ROW_BACKGROUND
        );

        graphics.text(
                screen.getFont(),
                "Timer color",
                x + 6,
                rowY + 6,
                TEXT,
                true
        );

        String colorName =
                TerminalClickTimerSettings
                        .getColorName();

        graphics.text(
                screen.getFont(),
                colorName,
                x + WIDTH
                        - screen.getFont().width(colorName)
                        - 6,
                rowY + 6,
                TerminalClickTimerSettings.color,
                true
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

        int index =
                getRowIndex(
                        mouseX,
                        mouseY
                );

        switch (index) {

            case 0 -> {

                draggingDelaySlider = true;

                updateDelay(
                        mouseX
                );
            }

            case 1 -> {

                TerminalClickTimerSettings
                        .cycleColor();
            }

            default -> {
                return false;
            }
        }

        return true;
    }

    private void updateDelay(
            double mouseX
    ) {

        int sliderX = x + 98;
        int sliderWidth = 82;

        double progress =
                (mouseX - sliderX)
                        / sliderWidth;

        progress =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                progress
                        )
                );

        int range =
                CLICK_DELAY_MAX
                        - CLICK_DELAY_MIN;

        int rawValue =
                CLICK_DELAY_MIN
                        + (int) Math.round(
                                progress * range
                        );

        /*
         * Snap to 10ms increments.
         */
        int snappedValue =
                Math.round(
                        rawValue / 10.0f
                ) * 10;

        TerminalClickTimerSettings
                .setClickDelayMs(
                        snappedValue
                );
    }

    private int getRowIndex(
            double mouseX,
            double mouseY
    ) {

        if (mouseX < x ||
                mouseX >= x + WIDTH) {

            return -1;
        }

        if (mouseY < y + HEADER_HEIGHT) {
            return -1;
        }

        return (int) (
                (
                        mouseY
                                - (
                                y + HEADER_HEIGHT
                        )
                )
                        / ROW_HEIGHT
        );
    }

    public boolean isCloseButtonHovered(
            double mouseX,
            double mouseY
    ) {

        return mouseX >= x + WIDTH - 18 &&
                mouseX < x + WIDTH &&
                mouseY >= y + 2 &&
                mouseY < y + HEADER_HEIGHT;
    }

    public boolean isHeaderHovered(
            double mouseX,
            double mouseY
    ) {

        return mouseX >= x &&
                mouseX < x + WIDTH &&
                mouseY >= y &&
                mouseY < y + HEADER_HEIGHT;
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

        x = (int) (
                mouseX - dragOffsetX
        );

        y = (int) (
                mouseY - dragOffsetY
        );
    }

    public void stopDragging() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isSliderDragging() {
        return draggingDelaySlider;
    }

    public void dragSlider(
            double mouseX
    ) {

        if (!draggingDelaySlider) {
            return;
        }

        updateDelay(mouseX);
    }

    public void stopSliderDragging() {
        draggingDelaySlider = false;
    }
}