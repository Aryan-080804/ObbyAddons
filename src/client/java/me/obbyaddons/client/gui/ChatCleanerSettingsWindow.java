package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.chat.ChatRules;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class ChatCleanerSettingsWindow {

    private int x;
    private int y;

    private static final int WIDTH = 145;
    private static final int HEIGHT = 82;
    private static final int HEADER_HEIGHT = 20;
    private static final int ROW_HEIGHT = 22;

    private static final int PURPLE = 0xFF7C3AED;
    private static final int PURPLE_DARK = 0xFF3B0764;

    private static final int BACKGROUND = 0xF0181818;
    private static final int HEADER = 0xF0222222;
    private static final int ROW_BACKGROUND = 0xFF1D1D1D;
    private static final int ROW_HOVER = 0xFF2A2A2A;
    private static final int CLOSE_HOVER = 0xFF3A3A3A;

    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DISABLED = 0xFFAAAAAA;

    private static final int TOGGLE_OFF = 0xFF3A3A3A;
    private static final int TOGGLE_ON = 0xFF5B21B6;
    private static final int TOGGLE_KNOB = 0xFFFFFFFF;

    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public ChatCleanerSettingsWindow(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(
            Screen screen,
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                BACKGROUND
        );

        // Purple top line
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
                "CHAT CLEANER",
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

        drawToggleRow(
                screen,
                graphics,
                "Dungeon Spam",
                ChatRules.isDungeonSpamEnabled(),
                x,
                y + HEADER_HEIGHT,
                mouseX,
                mouseY
        );

        drawToggleRow(
                screen,
                graphics,
                "M7 Boss Spam",
                ChatRules.isM7BossSpamEnabled(),
                x,
                y + HEADER_HEIGHT + ROW_HEIGHT,
                mouseX,
                mouseY
        );
    }

    private void drawToggleRow(
        Screen screen,
        GuiGraphicsExtractor graphics,
        String label,
        boolean enabled,
        int rowX,
        int rowY,
        int mouseX,
        int mouseY
    ) {
        boolean hovered =
                mouseX >= rowX &&
                mouseX < rowX + WIDTH &&
                mouseY >= rowY &&
                mouseY < rowY + ROW_HEIGHT;

        int background = hovered ? ROW_HOVER : ROW_BACKGROUND;

        graphics.fill(
                rowX,
                rowY,
                rowX + WIDTH,
                rowY + ROW_HEIGHT,
                background
        );

        // Setting name
        graphics.text(
                screen.getFont(),
                label,
                rowX + 6,
                rowY + 7,
                TEXT,
                true
        );

        // Toggle dimensions
        int toggleWidth = 24;
        int toggleHeight = 10;

        int toggleX = rowX + WIDTH - toggleWidth - 7;
        int toggleY = rowY + (ROW_HEIGHT - toggleHeight) / 2;

        int trackColor = enabled ? TOGGLE_ON : TOGGLE_OFF;

        // Toggle track
        graphics.fill(
                toggleX,
                toggleY,
                toggleX + toggleWidth,
                toggleY + toggleHeight,
                trackColor
        );

        // Small purple accent when enabled
        if (enabled) {
            graphics.fill(
                    toggleX,
                    toggleY,
                    toggleX + toggleWidth,
                    toggleY + 2,
                    PURPLE
            );
        }

        // Sliding knob
        int knobSize = 8;

        int knobX;

        if (enabled) {
            knobX = toggleX + toggleWidth - knobSize - 1;
        } else {
            knobX = toggleX + 1;
        }

        int knobY = toggleY + 1;

        graphics.fill(
                knobX,
                knobY,
                knobX + knobSize,
                knobY + knobSize,
                TOGGLE_KNOB
        );
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (isDungeonSpamRowHovered(mouseX, mouseY)) {
            ChatRules.setDungeonSpamEnabled(
                    !ChatRules.isDungeonSpamEnabled()
            );
            return true;
        }

        if (isM7BossSpamRowHovered(mouseX, mouseY)) {
            ChatRules.setM7BossSpamEnabled(
                    !ChatRules.isM7BossSpamEnabled()
            );
            return true;
        }

        return false;
    }

    public boolean isCloseButtonHovered(double mouseX, double mouseY) {
        return mouseX >= x + WIDTH - 18 &&
                mouseX < x + WIDTH &&
                mouseY >= y + 2 &&
                mouseY < y + HEADER_HEIGHT;
    }

    public boolean isHeaderHovered(double mouseX, double mouseY) {
        return mouseX >= x &&
                mouseX < x + WIDTH &&
                mouseY >= y &&
                mouseY < y + HEADER_HEIGHT;
    }

    private boolean isDungeonSpamRowHovered(double mouseX, double mouseY) {
        int rowY = y + HEADER_HEIGHT;

        return mouseX >= x &&
                mouseX < x + WIDTH &&
                mouseY >= rowY &&
                mouseY < rowY + ROW_HEIGHT;
    }

    private boolean isM7BossSpamRowHovered(double mouseX, double mouseY) {
        int rowY = y + HEADER_HEIGHT + ROW_HEIGHT;

        return mouseX >= x &&
                mouseX < x + WIDTH &&
                mouseY >= rowY &&
                mouseY < rowY + ROW_HEIGHT;
    }

    public void startDragging(double mouseX, double mouseY) {
        dragging = true;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
    }

    public void dragTo(double mouseX, double mouseY) {
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
}