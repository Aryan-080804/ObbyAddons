package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.dungeon.terminator.TerminatorOverlaySettings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class TerminatorOverlaySettingsWindow {

    private int x;
    private int y;

    private static final int WIDTH = 190;
    private static final int HEADER_HEIGHT = 20;
    private static final int ROW_HEIGHT = 20;

    private static final int PURPLE = 0xFF7C3AED;

    private static final int BACKGROUND = 0xF0181818;
    private static final int HEADER = 0xF0222222;
    private static final int ROW_BACKGROUND = 0xFF1D1D1D;
    private static final int CLOSE_HOVER = 0xFF3A3A3A;

    private static final int TEXT = 0xFFFFFFFF;

    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public TerminatorOverlaySettingsWindow(
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

        int height =
                HEADER_HEIGHT + ROW_HEIGHT;

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
                "TERMINATOR OVERLAY",
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

        graphics.fill(
                x,
                rowY,
                x + WIDTH,
                rowY + ROW_HEIGHT,
                ROW_BACKGROUND
        );

        graphics.text(
                screen.getFont(),
                "Overlay color",
                x + 6,
                rowY + 6,
                TEXT,
                true
        );

        String colorName =
                TerminatorOverlaySettings
                        .getColorName();

        graphics.text(
                screen.getFont(),
                colorName,
                x + WIDTH
                        - screen.getFont().width(colorName)
                        - 6,
                rowY + 6,
                TerminatorOverlaySettings.color,
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

        if (mouseX >= x &&
                mouseX < x + WIDTH &&
                mouseY >= y + HEADER_HEIGHT &&
                mouseY < y + HEADER_HEIGHT + ROW_HEIGHT) {

            TerminatorOverlaySettings
                    .cycleColor();

            return true;
        }

        return false;
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
}