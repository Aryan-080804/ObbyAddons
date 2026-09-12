package me.obbyaddons.client.gui;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.client.features.dungeon.storm.StormSettings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class StormSettingsWindow {

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

    private static final int LB_OFFSET_MIN = 0;
    private static final int LB_OFFSET_MAX = 20;

    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    private boolean draggingStormLbSlider;

    public StormSettingsWindow(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(
            Screen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        int rows = 9;
        int height = HEADER_HEIGHT + (rows * ROW_HEIGHT);

        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + height,
                BACKGROUND
        );

        // Purple accent
        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + 2,
                PURPLE
        );

        // Header
        graphics.fill(
                x,
                y + 2,
                x + WIDTH,
                y + HEADER_HEIGHT,
                HEADER
        );

        if (isCloseButtonHovered(mouseX, mouseY)) {
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
                "STORM",
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

        int rowY = y + HEADER_HEIGHT;

        // Row 0
        drawToggleRow(
                screen,
                graphics,
                "Storm tick timer",
                StormSettings.stormTickTimer,
                rowY,
                mouseX,
                mouseY
        );

        rowY += ROW_HEIGHT;

        // Row 1
        drawButtonRow(
                screen,
                graphics,
                "Timer color",
                StormSettings.getStormTimerColorName(),
                rowY
        );

        rowY += ROW_HEIGHT;

        // Row 2
        drawToggleRow(
                screen,
                graphics,
                "Tick down from 5",
                StormSettings.tickDownFrom5,
                rowY,
                mouseX,
                mouseY
        );

        rowY += ROW_HEIGHT;

        // Row 3
        drawToggleRow(
                screen,
                graphics,
                "First Death time",
                StormSettings.firstDeathTime,
                rowY,
                mouseX,
                mouseY
        );

        rowY += ROW_HEIGHT;

        // Row 4
        drawButtonRow(
                screen,
                graphics,
                "Death timer color",
                StormSettings.getStormDeathTimerColorName(),
                rowY
        );

        rowY += ROW_HEIGHT;

        // Row 5
        drawToggleRow(
                screen,
                graphics,
                "Warn if spirit mask is used",
                StormSettings.spiritMaskWarning,
                rowY,
                mouseX,
                mouseY
        );

        rowY += ROW_HEIGHT;

        // Row 6
        drawToggleRow(
                screen,
                graphics,
                "Storm LB",
                StormSettings.stormLbEnabled,
                rowY,
                mouseX,
                mouseY
        );

        rowY += ROW_HEIGHT;

        // Row 7
        drawButtonRow(
                screen,
                graphics,
                "LB timer color",
                StormSettings.getStormLbColorName(),
                rowY
        );

        rowY += ROW_HEIGHT;

        // Row 8
        drawSliderRow(
                screen,
                graphics,
                "Tick offset",
                StormSettings.stormLbTickOffset,
                rowY
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
                mouseX >= x &&
                mouseX < x + WIDTH &&
                mouseY >= rowY &&
                mouseY < rowY + ROW_HEIGHT;

        graphics.fill(
                x,
                rowY,
                x + WIDTH,
                rowY + ROW_HEIGHT,
                hovered ? ROW_HOVER : ROW_BACKGROUND
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

        int toggleX = x + WIDTH - toggleWidth - 7;
        int toggleY = rowY + (ROW_HEIGHT - toggleHeight) / 2;

        graphics.fill(
                toggleX,
                toggleY,
                toggleX + toggleWidth,
                toggleY + toggleHeight,
                enabled ? TOGGLE_ON : TOGGLE_OFF
        );

        int knobSize = 8;

        int knobX = enabled
                ? toggleX + toggleWidth - knobSize - 1
                : toggleX + 1;

        int knobY = toggleY + 1;

        graphics.fill(
                knobX,
                knobY,
                knobX + knobSize,
                knobY + knobSize,
                TOGGLE_KNOB
        );
    }

    private void drawButtonRow(
            Screen screen,
            GuiGraphicsExtractor graphics,
            String label,
            String value,
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
                label,
                x + 6,
                rowY + 6,
                TEXT,
                true
        );

        if (!value.isEmpty()) {
            graphics.text(
                    screen.getFont(),
                    value,
                    x + WIDTH - screen.getFont().width(value) - 6,
                    rowY + 6,
                    PURPLE,
                    true
            );
        }
    }

    private void drawSliderRow(
            Screen screen,
            GuiGraphicsExtractor graphics,
            String label,
            int value,
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
                label,
                x + 6,
                rowY + 6,
                TEXT,
                true
        );

        String valueText = Integer.toString(value);

        graphics.text(
                screen.getFont(),
                valueText,
                x + 92 - screen.getFont().width(valueText),
                rowY + 6,
                PURPLE,
                true
        );

        int sliderX = x + 100;
        int sliderWidth = 80;
        int sliderY = rowY + 9;

        graphics.fill(
                sliderX,
                sliderY,
                sliderX + sliderWidth,
                sliderY + 3,
                TOGGLE_OFF
        );

        float progress =
                (value - LB_OFFSET_MIN)
                        / (float) (LB_OFFSET_MAX - LB_OFFSET_MIN);

        int knobX =
                sliderX + Math.round(progress * sliderWidth);

        graphics.fill(
                knobX - 2,
                sliderY - 3,
                knobX + 3,
                sliderY + 6,
                PURPLE
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

        int index = getRowIndex(mouseX, mouseY);

        switch (index) {

            case 0 -> {
                StormSettings.stormTickTimer =
                        !StormSettings.stormTickTimer;

                ObbyConfig.get().stormTickTimerEnabled =
                        StormSettings.stormTickTimer;

                ObbyConfig.save();
            }

            case 1 -> StormSettings.cycleStormTimerColor();

            case 2 -> {
                StormSettings.tickDownFrom5 =
                        !StormSettings.tickDownFrom5;

                ObbyConfig.get().stormTickDownFrom5 =
                        StormSettings.tickDownFrom5;

                ObbyConfig.save();
            }

            case 3 -> {
                StormSettings.firstDeathTime =
                        !StormSettings.firstDeathTime;

                ObbyConfig.get().stormFirstDeathTime =
                        StormSettings.firstDeathTime;

                ObbyConfig.save();
            }

            case 4 -> StormSettings.cycleStormDeathTimerColor();

            case 5 -> {
                StormSettings.spiritMaskWarning =
                        !StormSettings.spiritMaskWarning;

                ObbyConfig.get().stormSpiritMaskWarning =
                        StormSettings.spiritMaskWarning;

                ObbyConfig.save();
            }

            case 6 -> {
                StormSettings.stormLbEnabled =
                        !StormSettings.stormLbEnabled;

                ObbyConfig.get().stormLbEnabled =
                        StormSettings.stormLbEnabled;

                ObbyConfig.save();
            }

            case 7 -> StormSettings.cycleStormLbColor();

            case 8 -> {
                draggingStormLbSlider = true;
                updateStormLbOffset(mouseX);
            }

            default -> {
                return false;
            }
        }

        return true;
    }

    private void updateStormLbOffset(double mouseX) {
        int sliderX = x + 100;
        int sliderWidth = 80;

        double progress =
                (mouseX - sliderX) / sliderWidth;

        progress = Math.max(
                0.0,
                Math.min(1.0, progress)
        );

        int range = LB_OFFSET_MAX - LB_OFFSET_MIN;

        StormSettings.stormLbTickOffset =
                LB_OFFSET_MIN
                        + (int) Math.round(progress * range);

        ObbyConfig.get().stormLbTickOffset =
                StormSettings.stormLbTickOffset;

        ObbyConfig.save();
    }

    private int getRowIndex(
            double mouseX,
            double mouseY
    ) {
        if (mouseX < x || mouseX >= x + WIDTH) {
            return -1;
        }

        if (mouseY < y + HEADER_HEIGHT) {
            return -1;
        }

        return (int) (
                (mouseY - (y + HEADER_HEIGHT))
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

        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
    }

    public void dragTo(
            double mouseX,
            double mouseY
    ) {
        if (!dragging) {
            return;
        }

        x = (int) (mouseX - dragOffsetX);
        y = (int) (mouseY - dragOffsetY);
    }

    public void stopDragging() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isSliderDragging() {
        return draggingStormLbSlider;
    }

    public void dragSlider(double mouseX) {
        if (!draggingStormLbSlider) {
            return;
        }

        updateStormLbOffset(mouseX);
    }

    public void stopSliderDragging() {
        draggingStormLbSlider = false;
    }
}