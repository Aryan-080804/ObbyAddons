package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.dungeon.tracker.PriceProvider;
import me.obbyaddons.client.features.dungeon.tracker.DungeonLootItem;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunRecord;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class DungeonRunDetailScreen extends Screen {

    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 330;

    private static final int BACKGROUND = 0xEE111111;
    private static final int BORDER = 0xFF3A3A3A;
    private static final int PURPLE = 0xFF7C3AED;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int CYAN = 0xFF55FFFF;

    private final Screen parent;
    private final DungeonRunRecord run;

    private int scrollOffset = 0;

    public DungeonRunDetailScreen(
            Screen parent,
            DungeonRunRecord run
    ) {
        super(
                Component.literal(
                        "Dungeon Run Details"
                )
        );

        this.parent = parent;
        this.run = run;
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
                Math.max(
                        10,
                        (this.width - PANEL_WIDTH) / 2
                );

        int panelY =
                Math.max(
                        10,
                        (this.height - PANEL_HEIGHT) / 2
                );

        drawBackground(
                graphics,
                panelX,
                panelY
        );

        drawRunInfo(
                graphics,
                panelX,
                panelY
        );

        drawLoot(
                graphics,
                panelX,
                panelY
        );
    }

    private void drawBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        graphics.fill(
                x,
                y,
                x + PANEL_WIDTH,
                y + PANEL_HEIGHT,
                BACKGROUND
        );

        graphics.fill(
                x,
                y,
                x + PANEL_WIDTH,
                y + 2,
                PURPLE
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + PANEL_HEIGHT,
                BORDER
        );

        graphics.fill(
                x + PANEL_WIDTH - 1,
                y,
                x + PANEL_WIDTH,
                y + PANEL_HEIGHT,
                BORDER
        );

        graphics.fill(
                x,
                y + PANEL_HEIGHT - 1,
                x + PANEL_WIDTH,
                y + PANEL_HEIGHT,
                BORDER
        );
    }

    private void drawRunInfo(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY
    ) {
        int x =
                panelX + 14;

        int y =
                panelY + 12;

        String floor =
                run.getFloor() == null
                        ? "?"
                        : run.getFloor();

        graphics.text(
                this.font,
                floor + " Run Details",
                x,
                y,
                WHITE,
                true
        );

        y += 20;

        graphics.text(
                this.font,
                "Time: "
                        + formatDuration(
                                run.getDurationMs()
                        ),
                x,
                y,
                GRAY,
                false
        );

        y += 14;

        graphics.text(
                this.font,
                "Chest: "
                        + getChestName(),
                x,
                y,
                run.isChestOpened()
                        ? WHITE
                        : YELLOW,
                false
        );

        y += 14;

        graphics.text(
                this.font,
                "Kismets Used: "
                        + run.getKismetsUsed(),
                x,
                y,
                run.getKismetsUsed() > 0
                        ? CYAN
                        : GRAY,
                false
        );

        y += 14;

        graphics.text(
                this.font,
                "Gross Loot: +"
                        + formatCoins(
                                run.getCurrentLootValueCoins()
                        ),
                x,
                y,
                GREEN,
                false
        );

        y += 14;

        graphics.text(
                this.font,
                "Chest Cost: -"
                        + formatCoins(
                                run.getChestCostCoins()
                        ),
                x,
                y,
                RED,
                false
        );

        y += 14;

        graphics.text(
                this.font,
                "Kismet Cost: -"
                        + formatCoins(
                                run.getKismetCostCoins()
                        ),
                x,
                y,
                YELLOW,
                false
        );

        y += 14;

        long profit =
                run.getCurrentProfitCoins();

        graphics.text(
                this.font,
                "Net Profit: "
                        + formatSignedCoins(
                                profit
                        ),
                x,
                y,
                profit >= 0L
                        ? GREEN
                        : RED,
                true
        );

        graphics.fill(
                panelX + 12,
                panelY + 132,
                panelX + PANEL_WIDTH - 12,
                panelY + 133,
                BORDER
        );

        graphics.text(
                this.font,
                "Loot",
                panelX + 14,
                panelY + 142,
                WHITE,
                true
        );
    }

    private void drawLoot(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY
    ) {
        List<DungeonLootItem> items =
                run.getLootItems();

        int listTop =
                panelY + 164;

        int listBottom =
                panelY + PANEL_HEIGHT - 20;

        int rowHeight =
                18;

        int visibleRows =
                Math.max(
                        1,
                        (listBottom - listTop)
                                / rowHeight
                );

        int maxScroll =
                Math.max(
                        0,
                        items.size()
                                - visibleRows
                );

        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                scrollOffset,
                                maxScroll
                        )
                );

        if (items.isEmpty()) {

            graphics.text(
                    this.font,
                    run.isChestOpened()
                            ? "No loot items recorded."
                            : "Chest not opened yet.",
                    panelX + 14,
                    listTop + 4,
                    GRAY,
                    false
            );

            return;
        }

        int end =
                Math.min(
                        items.size(),
                        scrollOffset
                                + visibleRows
                );

        for (
                int i = scrollOffset;
                i < end;
                i++
        ) {
            DungeonLootItem item =
                    items.get(i);

            if (item == null) {
                continue;
            }

            int y =
                    listTop
                            + (
                            i - scrollOffset
                    )
                            * rowHeight;

            String name =
                    getItemName(
                            item
                    );

            graphics.text(
                    this.font,
                    name,
                    panelX + 14,
                    y,
                    WHITE,
                    false
            );

            String quantity =
                    "x"
                            + item.getQuantity();

            graphics.text(
                    this.font,
                    quantity,
                    panelX + 300,
                    y,
                    GRAY,
                    false
            );

            long currentPrice =
                    PriceProvider
                            .getPriceCoins(
                                    item.getItemId()
                            );

            if (currentPrice <= 0L) {
                currentPrice =
                        item.getPriceAtOpenCoins();
            }

            long value =
                    currentPrice
                            * item.getQuantity();

            String valueText =
                    formatCoins(
                            value
                    );

            graphics.text(
                    this.font,
                    valueText,
                    panelX
                            + PANEL_WIDTH
                            - 14
                            - this.font.width(
                                    valueText
                            ),
                    y,
                    GREEN,
                    true
            );
        }
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (verticalAmount > 0.0D) {

            scrollOffset =
                    Math.max(
                            0,
                            scrollOffset - 1
                    );

            return true;
        }

        if (verticalAmount < 0.0D) {

            scrollOffset++;

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    @Override
    public void onClose() {

        Minecraft.getInstance()
                .setScreen(
                        parent
                );
    }

    private String getChestName() {

        if (!run.isChestOpened()) {
            return "Pending";
        }

        String title =
                run.getChestTitle();

        if (
                title == null
                        || title.isBlank()
        ) {
            return "Opened Chest";
        }

        return title;
    }

    private String getItemName(
            DungeonLootItem item
    ) {
        String name =
                item.getDisplayName();

        if (
                name != null
                        && !name.isBlank()
        ) {
            return name;
        }

        return item.getItemId();
    }

    private String formatDuration(
            long durationMs
    ) {
        if (durationMs <= 0L) {
            return "--:--";
        }

        long totalSeconds =
                durationMs / 1000L;

        long minutes =
                totalSeconds / 60L;

        long seconds =
                totalSeconds % 60L;

        return String.format(
                "%d:%02d",
                minutes,
                seconds
        );
    }

    private String formatSignedCoins(
            long coins
    ) {
        if (coins > 0L) {
            return "+"
                    + formatCoins(
                            coins
                    );
        }

        if (coins < 0L) {
            return "-"
                    + formatCoins(
                            Math.abs(
                                    coins
                            )
                    );
        }

        return "0";
    }

    private String formatCoins(
            long coins
    ) {
        long absolute =
                Math.abs(
                        coins
                );

        if (absolute >= 1_000_000_000L) {

            return String.format(
                    "%.2fb",
                    absolute
                            / 1_000_000_000.0D
            );
        }

        if (absolute >= 1_000_000L) {

            return String.format(
                    "%.2fm",
                    absolute
                            / 1_000_000.0D
            );
        }

        if (absolute >= 1_000L) {

            return String.format(
                    "%.1fk",
                    absolute
                            / 1_000.0D
            );
        }

        return Long.toString(
                absolute
        );
    }
}