package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.dungeon.tracker.DungeonRunHistory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class DungeonLootClearConfirmScreen extends Screen {

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 150;

    private static final int BACKGROUND = 0xEE111111;
    private static final int BORDER = 0xFF3A3A3A;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int RED = 0xFFFF5555;
    private static final int GREEN = 0xFF55FF55;

    private final Screen parent;

    public DungeonLootClearConfirmScreen(
            Screen parent
    ) {
        super(
                Component.literal(
                        "Clear Dungeon History"
                )
        );

        this.parent = parent;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int panelX =
                (this.width - PANEL_WIDTH) / 2;

        int panelY =
                (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(
                panelX,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                BACKGROUND
        );

        graphics.fill(
                panelX,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + 2,
                RED
        );

        graphics.fill(
                panelX,
                panelY,
                panelX + 1,
                panelY + PANEL_HEIGHT,
                BORDER
        );

        graphics.fill(
                panelX + PANEL_WIDTH - 1,
                panelY,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                BORDER
        );

        graphics.fill(
                panelX,
                panelY + PANEL_HEIGHT - 1,
                panelX + PANEL_WIDTH,
                panelY + PANEL_HEIGHT,
                BORDER
        );

        graphics.text(
                this.font,
                "Clear all dungeon run history?",
                panelX + 16,
                panelY + 18,
                WHITE,
                true
        );

        graphics.text(
                this.font,
                "This deletes saved runs, loot, chest costs,",
                panelX + 16,
                panelY + 42,
                GRAY,
                false
        );

        graphics.text(
                this.font,
                "Kismet history, and profit history.",
                panelX + 16,
                panelY + 56,
                GRAY,
                false
        );

        int clearX =
                panelX + 42;

        int clearY =
                panelY + 98;

        int cancelX =
                panelX + 198;

        int cancelY =
                clearY;

        graphics.fill(
                clearX,
                clearY,
                clearX + 120,
                clearY + 24,
                isInside(
                        mouseX,
                        mouseY,
                        clearX,
                        clearY,
                        120,
                        24
                )
                        ? 0xAA772222
                        : 0x88441111
        );

        graphics.fill(
                cancelX,
                cancelY,
                cancelX + 120,
                cancelY + 24,
                isInside(
                        mouseX,
                        mouseY,
                        cancelX,
                        cancelY,
                        120,
                        24
                )
                        ? 0xAA227722
                        : 0x88441111
        );

        drawCenteredText(
                graphics,
                "Clear",
                clearX,
                clearY,
                120,
                RED
        );

        drawCenteredText(
                graphics,
                "Cancel",
                cancelX,
                cancelY,
                120,
                GREEN
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(
                    event,
                    doubleClick
            );
        }

        int panelX =
                (this.width - PANEL_WIDTH) / 2;

        int panelY =
                (this.height - PANEL_HEIGHT) / 2;

        int clearX =
                panelX + 42;

        int clearY =
                panelY + 98;

        int cancelX =
                panelX + 198;

        int cancelY =
                clearY;

        if (
                isInside(
                        event.x(),
                        event.y(),
                        clearX,
                        clearY,
                        120,
                        24
                )
        ) {
            DungeonRunHistory.clear();

            Minecraft.getInstance()
                    .setScreen(
                            parent
                    );

            return true;
        }

        if (
                isInside(
                        event.x(),
                        event.y(),
                        cancelX,
                        cancelY,
                        120,
                        24
                )
        ) {
            Minecraft.getInstance()
                    .setScreen(
                            parent
                    );

            return true;
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    @Override
    public void onClose() {
        Minecraft.getInstance()
                .setScreen(
                        parent
                );
    }

    private void drawCenteredText(
            GuiGraphicsExtractor graphics,
            String text,
            int x,
            int y,
            int width,
            int color
    ) {
        graphics.text(
                this.font,
                text,
                x
                        + (
                        width
                                - this.font.width(
                                text
                        )
                ) / 2,
                y + 8,
                color,
                true
        );
    }

    private boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }
}