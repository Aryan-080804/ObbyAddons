package me.obbyaddons.client.gui;

import me.obbyaddons.client.features.dungeon.tracker.AthenPriceProvider;
import me.obbyaddons.client.features.dungeon.tracker.DungeonLootItem;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunHistory;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunRecord;
import me.obbyaddons.client.features.dungeon.tracker.DungeonChestTracker;
import net.minecraft.world.item.ItemStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DungeonLootScreen extends Screen {

    // =========================
    // LAYOUT
    // =========================

    private static final int TARGET_PANEL_WIDTH = 900;
    private static final int TARGET_PANEL_HEIGHT = 520;

    private static final int PANEL_MARGIN = 6;

    private static final double LEFT_PANEL_RATIO =
            0.35D;

    private static final int TITLE_HEIGHT = 32;
    private static final int FILTER_HEIGHT = 50;
    private static final int SUMMARY_HEIGHT = 70;

    private static final int CONTENT_TOP =
            TITLE_HEIGHT + FILTER_HEIGHT;

    private static final int CONTENT_BOTTOM_PADDING =
            SUMMARY_HEIGHT + 8;

    private static final int ROW_HEIGHT = 22;

    private static final int CLEAR_BUTTON_WIDTH = 92;
    private static final int CLEAR_BUTTON_HEIGHT = 18;

    // =========================
    // COLORS
    // =========================

    private static final int BACKGROUND =
            0xEE111111;

    private static final int PANEL_BACKGROUND =
            0xCC181818;

    private static final int ROW_BACKGROUND =
            0x881F1F1F;

    private static final int HOVER_BACKGROUND =
            0x553B0764;

    private static final int BORDER =
            0xFF3A3A3A;

    private static final int PURPLE =
            0xFF7C3AED;

    private static final int PURPLE_DIM =
            0x553B0764;

    private static final int WHITE =
            0xFFFFFFFF;

    private static final int GRAY =
            0xFFAAAAAA;

    private static final int DARK_GRAY =
            0xFF777777;

    private static final int GREEN =
            0xFF55FF55;

    private static final int RED =
            0xFFFF5555;

    private static final int YELLOW =
            0xFFFFFF55;

    private static final int CYAN =
            0xFF55FFFF;

    // =========================
    // FILTERS
    // =========================

    private static final String[] FILTERS = {
            "ALL",
            "F1",
            "F2",
            "F3",
            "F4",
            "F5",
            "F6",
            "F7",
            "M1",
            "M2",
            "M3",
            "M4",
            "M5",
            "M6",
            "M7"
    };

    private String selectedFilter =
            "ALL";

    // =========================
    // SCROLL
    // =========================

    private int runScrollOffset =
            0;

    private int lootScrollOffset =
            0;

    // =========================
    // CLICKABLE RUN STATE
    // =========================

    private List<DungeonRunRecord> lastRenderedRuns =
            List.of();

    private int lastRunListTop =
            0;

    private int lastVisibleRunRows =
            0;

    private int lastPanelX =
            0;

    // =========================
    // SCREEN
    // =========================

    public DungeonLootScreen() {
        super(
                Component.literal(
                        "Dungeon Loot"
                )
        );
    }

    private int getPanelWidth() {

        return Math.min(
                TARGET_PANEL_WIDTH,
                Math.max(
                        1,
                        this.width - PANEL_MARGIN * 2
                )
        );
    }

    private int getPanelHeight() {

        return Math.min(
                TARGET_PANEL_HEIGHT,
                Math.max(
                        1,
                        this.height - PANEL_MARGIN * 2
                )
        );
    }

    private int getLeftPanelWidth() {

        return (int) Math.round(
                getPanelWidth()
                        * LEFT_PANEL_RATIO
        );
    }

    private int getPanelX() {

        return (this.width - getPanelWidth()) / 2;
    }

    private int getPanelY() {

        return (this.height - getPanelHeight()) / 2;
    }

    private int getDividerX(
            int panelX
    ) {

        return panelX + getLeftPanelWidth();
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
                getPanelX();

        int panelY =
                getPanelY();

        List<DungeonRunRecord> runs =
                getFilteredRuns();

        List<LootAggregate> loot =
                aggregateLoot(
                        runs
                );

        drawBackground(
                graphics,
                panelX,
                panelY
        );

        drawTitle(
                graphics,
                panelX,
                panelY,
                mouseX,
                mouseY
        );

        drawFilters(
                graphics,
                panelX,
                panelY,
                mouseX,
                mouseY
        );

        drawPanels(
                graphics,
                panelX,
                panelY
        );

        drawRunHistory(
                graphics,
                runs,
                panelX,
                panelY,
                mouseX,
                mouseY
        );

        drawLootSummary(
                graphics,
                loot,
                panelX,
                panelY
        );

        drawTotals(
                graphics,
                runs,
                panelX,
                panelY
        );
    }

    // =========================
    // BACKGROUND
    // =========================

    private void drawBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        graphics.fill(
                x,
                y,
                x + getPanelWidth(),
                y + getPanelHeight(),
                BACKGROUND
        );

        graphics.fill(
                x,
                y,
                x + getPanelWidth(),
                y + 2,
                PURPLE
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + getPanelHeight(),
                BORDER
        );

        graphics.fill(
                x + getPanelWidth() - 1,
                y,
                x + getPanelWidth(),
                y + getPanelHeight(),
                BORDER
        );

        graphics.fill(
                x,
                y + getPanelHeight() - 1,
                x + getPanelWidth(),
                y + getPanelHeight(),
                BORDER
        );
    }

    // =========================
    // TITLE
    // =========================

    private void drawTitle(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        graphics.text(
                this.font,
                "Dungeon Loot",
                panelX + 14,
                panelY + 10,
                WHITE,
                true
        );

        String pricing =
                AthenPriceProvider.getPriceCount() > 0
                        ? "Live Athen Prices"
                        : "Waiting for Athen...";

        graphics.text(
                this.font,
                pricing,
                panelX
                        + getPanelWidth()
                        - 14
                        - this.font.width(
                                pricing
                        ),
                panelY + 10,
                AthenPriceProvider.getPriceCount() > 0
                        ? CYAN
                        : YELLOW,
                false
        );

        int clearX =
                getClearButtonX(
                        panelX
                );

        int clearY =
                getClearButtonY(
                        panelY
                );

        boolean hovered =
                isInside(
                        mouseX,
                        mouseY,
                        clearX,
                        clearY,
                        CLEAR_BUTTON_WIDTH,
                        CLEAR_BUTTON_HEIGHT
                );

        graphics.fill(
                clearX,
                clearY,
                clearX + CLEAR_BUTTON_WIDTH,
                clearY + CLEAR_BUTTON_HEIGHT,
                hovered
                        ? 0xAA772222
                        : 0x88441111
        );

        String clearText =
                "Clear History";

        graphics.text(
                this.font,
                clearText,
                clearX
                        + (
                        CLEAR_BUTTON_WIDTH
                                - this.font.width(
                                clearText
                        )
                ) / 2,
                clearY + 4,
                RED,
                true
        );
    }

    private int getClearButtonX(
            int panelX
    ) {
        return panelX
                + getPanelWidth()
                - 14
                - CLEAR_BUTTON_WIDTH;
    }

    private int getClearButtonY(
            int panelY
    ) {
        return panelY + 30;
    }

    // =========================
    // FILTERS
    // =========================

    private void drawFilters(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int startX =
                panelX + 14;

        int startY =
                panelY + TITLE_HEIGHT + 4;

        int currentX =
                startX;

        int currentY =
                startY;

        for (String filter : FILTERS) {

            int width =
                    getFilterWidth(
                            filter
                    );

            if (
                    currentX + width
                            > panelX + getPanelWidth() - 110
            ) {
                currentX =
                        startX;

                currentY +=
                        20;
            }

            boolean selected =
                    filter.equals(
                            selectedFilter
                    );

            boolean hovered =
                    mouseX >= currentX
                            && mouseX <= currentX + width
                            && mouseY >= currentY
                            && mouseY <= currentY + 16;

            int background =
                    selected
                            ? PURPLE
                            : hovered
                            ? PURPLE_DIM
                            : PANEL_BACKGROUND;

            graphics.fill(
                    currentX,
                    currentY,
                    currentX + width,
                    currentY + 16,
                    background
            );

            graphics.text(
                    this.font,
                    filter,
                    currentX
                            + (
                            width
                                    - this.font.width(
                                    filter
                            )
                    ) / 2,
                    currentY + 4,
                    WHITE,
                    selected
            );

            currentX +=
                    width + 5;
        }
    }

    private int getFilterWidth(
            String filter
    ) {
        return Math.max(
                28,
                this.font.width(
                        filter
                ) + 12
        );
    }

    // =========================
    // PANELS
    // =========================

    private void drawPanels(
            GuiGraphicsExtractor graphics,
            int panelX,
            int panelY
    ) {
        int contentTop =
                panelY + CONTENT_TOP;

        int contentBottom =
                panelY
                        + getPanelHeight()
                        - CONTENT_BOTTOM_PADDING;

        int dividerX =
                getDividerX(
                        panelX
                );

        graphics.fill(
                panelX + 8,
                contentTop,
                dividerX - 4,
                contentBottom,
                PANEL_BACKGROUND
        );

        graphics.fill(
                dividerX + 4,
                contentTop,
                panelX + getPanelWidth() - 8,
                contentBottom,
                PANEL_BACKGROUND
        );

        graphics.fill(
                dividerX,
                contentTop,
                dividerX + 1,
                contentBottom,
                BORDER
        );

        graphics.text(
                this.font,
                "Run History",
                panelX + 16,
                contentTop + 8,
                WHITE,
                true
        );

        graphics.text(
                this.font,
                "Loot Summary",
                dividerX + 12,
                contentTop + 8,
                WHITE,
                true
        );
    }

    // =========================
    // RUN HISTORY
    // =========================

    private void drawRunHistory(
            GuiGraphicsExtractor graphics,
            List<DungeonRunRecord> runs,
            int panelX,
            int panelY,
            int mouseX,
            int mouseY
    ) {
        int listTop =
                panelY
                        + CONTENT_TOP
                        + 28;

        int listBottom =
                panelY
                        + getPanelHeight()
                        - CONTENT_BOTTOM_PADDING
                        - 6;

        int visibleRows =
                Math.max(
                        1,
                        (listBottom - listTop)
                                / ROW_HEIGHT
                );

        clampRunScroll(
                runs.size(),
                visibleRows
        );

        lastRenderedRuns =
                runs;

        lastRunListTop =
                listTop;

        lastVisibleRunRows =
                visibleRows;

        lastPanelX =
                panelX;

        if (runs.isEmpty()) {

            graphics.text(
                    this.font,
                    "No runs for "
                            + selectedFilter
                            + ".",
                    panelX + 16,
                    listTop + 6,
                    GRAY,
                    false
            );

            return;
        }

        int end =
                Math.min(
                        runs.size(),
                        runScrollOffset
                                + visibleRows
                );

        for (
                int i = runScrollOffset;
                i < end;
                i++
        ) {
            DungeonRunRecord run =
                    runs.get(i);

            int rowIndex =
                    i - runScrollOffset;

            int rowY =
                    listTop
                            + rowIndex
                            * ROW_HEIGHT;

            boolean hovered =
                    mouseX >= panelX + 12
                            && mouseX
                            <= panelX
                            + getLeftPanelWidth()
                            - 8
                            && mouseY >= rowY - 2
                            && mouseY
                            <= rowY
                            + ROW_HEIGHT
                            - 2;

            drawRunRow(
                    graphics,
                    run,
                    i,
                    panelX,
                    rowY,
                    hovered
            );
        }

        if (runs.size() > visibleRows) {

            String text =
                    (runScrollOffset + 1)
                            + "-"
                            + end
                            + " / "
                            + runs.size();

            graphics.text(
                    this.font,
                    text,
                    panelX
                            + getLeftPanelWidth()
                            - 14
                            - this.font.width(
                                    text
                            ),
                    listBottom + 2,
                    DARK_GRAY,
                    false
            );
        }
    }

    private void drawRunRow(
            GuiGraphicsExtractor graphics,
            DungeonRunRecord run,
            int index,
            int panelX,
            int y,
            boolean hovered
    ) {
        if (run == null) {
            return;
        }

        if (hovered) {

            graphics.fill(
                    panelX + 12,
                    y - 2,
                    panelX
                            + getLeftPanelWidth()
                            - 8,
                    y + ROW_HEIGHT - 2,
                    HOVER_BACKGROUND
            );

        } else if ((index & 1) == 0) {

            graphics.fill(
                    panelX + 12,
                    y - 2,
                    panelX
                            + getLeftPanelWidth()
                            - 8,
                    y + ROW_HEIGHT - 2,
                    ROW_BACKGROUND
            );
        }

        String floor =
                normalizeFloor(
                        run.getFloor()
                );

        String chest =
                getChestDisplayName(
                        run
                );

        String profit =
                formatSignedCoins(
                        run.getCurrentProfitCoins()
                );

        int x =
                panelX + 16;

        String number =
                "#"
                        + getHistoricalRunNumber(
                                run
                        );

        graphics.text(
                this.font,
                number,
                x,
                y,
                DARK_GRAY,
                false
        );

        x += 42;

        graphics.text(
                this.font,
                floor,
                x,
                y,
                WHITE,
                true
        );

        x += 30;

        int profitX =
                panelX
                        + getLeftPanelWidth()
                        - 12
                        - this.font.width(
                                profit
                        );

        String kismet =
                run.getKismetsUsed() > 0
                        ? " K" + run.getKismetsUsed()
                        : "";

        int kismetWidth =
                kismet.isEmpty()
                        ? 0
                        : this.font.width(
                                kismet
                        ) + 3;

        int chestMaxWidth =
                Math.max(
                        40,
                        profitX
                                - x
                                - kismetWidth
                                - 8
                );

        String shortenedChest =
                shorten(
                        chest,
                        chestMaxWidth
                );

        graphics.text(
                this.font,
                shortenedChest,
                x,
                y,
                run.isChestOpened()
                        ? WHITE
                        : YELLOW,
                false
        );

        if (!kismet.isEmpty()) {

            graphics.text(
                    this.font,
                    kismet,
                    x
                            + this.font.width(
                                    shortenedChest
                            )
                            + 3,
                    y,
                    CYAN,
                    true
            );
        }

        graphics.text(
                this.font,
                profit,
                profitX,
                y,
                run.getCurrentProfitCoins() >= 0L
                        ? GREEN
                        : RED,
                true
        );
    }

    // =========================
    // LOOT SUMMARY
    // =========================

    private void drawLootSummary(
            GuiGraphicsExtractor graphics,
            List<LootAggregate> loot,
            int panelX,
            int panelY
    ) {
        int dividerX =
                getDividerX(
                        panelX
                );

        int listTop =
                panelY
                        + CONTENT_TOP
                        + 28;

        int listBottom =
                panelY
                        + getPanelHeight()
                        - CONTENT_BOTTOM_PADDING
                        - 6;

        int visibleRows =
                Math.max(
                        1,
                        (listBottom - listTop)
                                / ROW_HEIGHT
                );

        clampLootScroll(
                loot.size(),
                visibleRows
        );

        if (loot.isEmpty()) {

            graphics.text(
                    this.font,
                    "No opened loot yet.",
                    dividerX + 12,
                    listTop + 6,
                    GRAY,
                    false
            );

            return;
        }

        int end =
                Math.min(
                        loot.size(),
                        lootScrollOffset
                                + visibleRows
                );

        for (
                int i = lootScrollOffset;
                i < end;
                i++
        ) {
            LootAggregate entry =
                    loot.get(i);

            int rowY =
                    listTop
                            + (
                            i - lootScrollOffset
                    )
                            * ROW_HEIGHT;

            drawLootRow(
                    graphics,
                    entry,
                    i,
                    dividerX,
                    panelX,
                    rowY
            );
        }

        if (loot.size() > visibleRows) {

            String text =
                    (lootScrollOffset + 1)
                            + "-"
                            + end
                            + " / "
                            + loot.size();

            graphics.text(
                    this.font,
                    text,
                    panelX
                            + getPanelWidth()
                            - 14
                            - this.font.width(
                                    text
                            ),
                    listBottom + 2,
                    DARK_GRAY,
                    false
            );
        }
    }

    private void drawLootRow(
            GuiGraphicsExtractor graphics,
            LootAggregate entry,
            int index,
            int dividerX,
            int panelX,
            int y
    ) {
        if ((index & 1) == 0) {

            graphics.fill(
                    dividerX + 8,
                    y - 2,
                    panelX
                            + getPanelWidth()
                            - 12,
                    y + ROW_HEIGHT - 2,
                    ROW_BACKGROUND
            );
        }

        ItemStack icon =
                getPersistentLootIcon(
                        entry
                );

        int iconX =
                dividerX + 12;

        int iconY =
                y - 4;

        if (!icon.isEmpty()) {

            graphics.item(
                    icon,
                    iconX,
                    iconY
            );
        }

        String quantity =
                "x"
                        + entry.quantity;

        int quantityX =
                panelX
                        + getPanelWidth()
                        - 100
                        - this.font.width(
                                quantity
                        );

        int nameX =
                dividerX + 34;

        int maxNameWidth =
                Math.max(
                        60,
                        quantityX
                                - nameX
                                - 12
                );

        String name =
                shorten(
                        entry.displayName,
                        maxNameWidth
                );

        graphics.text(
                this.font,
                name,
                nameX,
                y,
                WHITE,
                false
        );

        graphics.text(
                this.font,
                quantity,
                quantityX,
                y,
                GRAY,
                false
        );

        String value =
                formatCoins(
                        entry.currentValueCoins
                );

        int valueX =
                panelX
                        + getPanelWidth()
                        - 14
                        - this.font.width(
                                value
                        );

        graphics.text(
                this.font,
                value,
                valueX,
                y,
                GREEN,
                true
        );
    }

    // =========================
    // TOTALS
    // =========================

    private void drawTotals(
            GuiGraphicsExtractor graphics,
            List<DungeonRunRecord> runs,
            int panelX,
            int panelY
    ) {
        int totalRuns =
                runs.size();

        int opened =
                0;

        int pending =
                0;

        int kismets =
                0;

        long gross =
                0L;

        long chestCost =
                0L;

        long kismetCost =
                0L;

        long profit =
                0L;

        for (DungeonRunRecord run : runs) {

            if (run == null) {
                continue;
            }

            if (run.isChestOpened()) {
                opened++;
            } else {
                pending++;
            }

            kismets +=
                    run.getKismetsUsed();

            gross +=
                    run.getCurrentLootValueCoins();

            chestCost +=
                    run.getChestCostCoins();

            kismetCost +=
                    run.getKismetCostCoins();

            profit +=
                    run.getCurrentProfitCoins();
        }

        int separatorY =
                panelY
                        + getPanelHeight()
                        - SUMMARY_HEIGHT;

        graphics.fill(
                panelX + 10,
                separatorY,
                panelX + getPanelWidth() - 10,
                separatorY + 1,
                BORDER
        );

        int y =
                separatorY + 9;

        graphics.text(
                this.font,
                "Runs: "
                        + totalRuns
                        + "   Opened: "
                        + opened
                        + "   Pending: "
                        + pending
                        + "   Kismets: "
                        + kismets,
                panelX + 14,
                y,
                GRAY,
                false
        );

        y += 18;

        String grossText =
                "Gross: +"
                        + formatCoins(
                                gross
                        );

        String chestText =
                "Chest: -"
                        + formatCoins(
                                chestCost
                        );

        String kismetText =
                "Kismet: -"
                        + formatCoins(
                                kismetCost
                        );

        String profitText =
                "Net: "
                        + formatSignedCoins(
                                profit
                        );

        int x =
                panelX + 14;

        graphics.text(
                this.font,
                grossText,
                x,
                y,
                GREEN,
                true
        );

        x +=
                this.font.width(
                        grossText
                ) + 24;

        graphics.text(
                this.font,
                chestText,
                x,
                y,
                RED,
                true
        );

        x +=
                this.font.width(
                        chestText
                ) + 24;

        graphics.text(
                this.font,
                kismetText,
                x,
                y,
                YELLOW,
                true
        );

        int profitX =
                panelX
                        + getPanelWidth()
                        - 14
                        - this.font.width(
                                profitText
                        );

        graphics.text(
                this.font,
                profitText,
                profitX,
                y,
                profit >= 0L
                        ? GREEN
                        : RED,
                true
        );
    }

    // =========================
    // CLICK HANDLING
    // =========================

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() == 0) {

            int panelX =
                    getPanelX();

            int panelY =
                    getPanelY();

            // -------------------------
            // CLEAR HISTORY
            // -------------------------

            int clearX =
                    getClearButtonX(
                            panelX
                    );

            int clearY =
                    getClearButtonY(
                            panelY
                    );

            if (
                    isInside(
                            event.x(),
                            event.y(),
                            clearX,
                            clearY,
                            CLEAR_BUTTON_WIDTH,
                            CLEAR_BUTTON_HEIGHT
                    )
            ) {
                Minecraft.getInstance()
                        .setScreen(
                                new DungeonLootClearConfirmScreen(
                                        this
                                )
                        );

                return true;
            }

            // -------------------------
            // FILTER CLICK
            // -------------------------

            String clickedFilter =
                    getFilterAt(
                            panelX,
                            panelY,
                            event.x(),
                            event.y()
                    );

            if (clickedFilter != null) {

                selectedFilter =
                        clickedFilter;

                runScrollOffset =
                        0;

                lootScrollOffset =
                        0;

                return true;
            }

            // -------------------------
            // RUN CLICK
            // -------------------------

            DungeonRunRecord clickedRun =
                    getRunAt(
                            event.x(),
                            event.y()
                    );

            if (clickedRun != null) {

                Minecraft.getInstance()
                        .setScreen(
                                new DungeonRunDetailScreen(
                                        this,
                                        clickedRun
                                )
                        );

                return true;
            }
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    private DungeonRunRecord getRunAt(
            double mouseX,
            double mouseY
    ) {
        if (
                mouseX < lastPanelX + 12
                        || mouseX
                        > lastPanelX
                        + getLeftPanelWidth()
                        - 8
        ) {
            return null;
        }

        if (
                mouseY < lastRunListTop
                        || lastVisibleRunRows <= 0
        ) {
            return null;
        }

        int row =
                (int) (
                        (
                                mouseY
                                        - lastRunListTop
                        )
                                / ROW_HEIGHT
                );

        if (
                row < 0
                        || row >= lastVisibleRunRows
        ) {
            return null;
        }

        int index =
                runScrollOffset
                        + row;

        if (
                index < 0
                        || index >= lastRenderedRuns.size()
        ) {
            return null;
        }

        return lastRenderedRuns.get(
                index
        );
    }

    private String getFilterAt(
            int panelX,
            int panelY,
            double mouseX,
            double mouseY
    ) {
        int startX =
                panelX + 14;

        int startY =
                panelY + TITLE_HEIGHT + 4;

        int currentX =
                startX;

        int currentY =
                startY;

        for (String filter : FILTERS) {

            int width =
                    getFilterWidth(
                            filter
                    );

            if (
                    currentX + width
                            > panelX + getPanelWidth() - 110
            ) {
                currentX =
                        startX;

                currentY +=
                        20;
            }

            if (
                    mouseX >= currentX
                            && mouseX <= currentX + width
                            && mouseY >= currentY
                            && mouseY <= currentY + 16
            ) {
                return filter;
            }

            currentX +=
                    width + 5;
        }

        return null;
    }

    // =========================
    // SCROLLING
    // =========================

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        int panelX =
                getPanelX();

        int dividerX =
                getDividerX(
                        panelX
                );

        int change;

        if (verticalAmount > 0.0D) {
            change =
                    -1;
        } else if (verticalAmount < 0.0D) {
            change =
                    1;
        } else {
            return false;
        }

        if (mouseX < dividerX) {

            runScrollOffset =
                    Math.max(
                            0,
                            runScrollOffset + change
                    );

            return true;
        }

        lootScrollOffset =
                Math.max(
                        0,
                        lootScrollOffset + change
                );

        return true;
    }

    private void clampRunScroll(
            int totalRows,
            int visibleRows
    ) {
        int max =
                Math.max(
                        0,
                        totalRows - visibleRows
                );

        runScrollOffset =
                Math.max(
                        0,
                        Math.min(
                                runScrollOffset,
                                max
                        )
                );
    }

    private void clampLootScroll(
            int totalRows,
            int visibleRows
    ) {
        int max =
                Math.max(
                        0,
                        totalRows - visibleRows
                );

        lootScrollOffset =
                Math.max(
                        0,
                        Math.min(
                                lootScrollOffset,
                                max
                        )
                );
    }

    // =========================
    // RUN DATA
    // =========================

    private List<DungeonRunRecord> getFilteredRuns() {

        List<DungeonRunRecord> result =
                new ArrayList<>();

        List<DungeonRunRecord> all =
                DungeonRunHistory.getRuns();

        for (
                int i = all.size() - 1;
                i >= 0;
                i--
        ) {
            DungeonRunRecord run =
                    all.get(i);

            if (run == null) {
                continue;
            }

            if (
                    !selectedFilter.equals(
                            "ALL"
                    )
                            && !selectedFilter.equalsIgnoreCase(
                            normalizeFloor(
                                    run.getFloor()
                            )
                    )
            ) {
                continue;
            }

            result.add(
                    run
            );
        }

        return result;
    }

    private int getHistoricalRunNumber(
            DungeonRunRecord target
    ) {
        List<DungeonRunRecord> all =
                DungeonRunHistory.getRuns();

        for (
                int i = 0;
                i < all.size();
                i++
        ) {
            DungeonRunRecord run =
                    all.get(i);

            if (run == target) {
                return i + 1;
            }

            if (
                    run != null
                            && target.getId() != null
                            && target.getId()
                            .equals(
                                    run.getId()
                            )
            ) {
                return i + 1;
            }
        }

        return 0;
    }

    private String normalizeFloor(
            String floor
    ) {
        if (
                floor == null
                        || floor.isBlank()
                        || floor.equalsIgnoreCase(
                                "UNKNOWN"
                        )
        ) {
            return "?";
        }

        return floor.toUpperCase(
                Locale.ROOT
        );
    }

    private String getChestDisplayName(
            DungeonRunRecord run
    ) {
        if (!run.isChestOpened()) {

            if (run.getKismetsUsed() > 0) {
                return "Pending + Kismet";
            }

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

    // =========================
    // LOOT AGGREGATION
    // =========================

    private List<LootAggregate> aggregateLoot(
            List<DungeonRunRecord> runs
    ) {
        Map<String, LootAggregate> map =
                new LinkedHashMap<>();

        for (DungeonRunRecord run : runs) {

            if (
                    run == null
                            || !run.isChestOpened()
            ) {
                continue;
            }

            for (
                    DungeonLootItem item
                    : run.getLootItems()
            ) {
                if (item == null) {
                    continue;
                }

                String itemId =
                        normalizeLootItemId(
                                item.getItemId()
                        );

                if (itemId.isBlank()) {
                    continue;
                }

                LootAggregate aggregate =
                        map.computeIfAbsent(
                                itemId,
                                id ->
                                        new LootAggregate(
                                                id,
                                                getDisplayName(
                                                        item
                                                )
                                        )
                        );

                if (
                        (aggregate.iconData == null
                                || aggregate.iconData.isBlank())
                                && item.hasIconData()
                ) {
                    aggregate.iconData =
                            item.getIconData();
                }

                aggregate.quantity +=
                        item.getQuantity();

                long price =
                        AthenPriceProvider
                                .getPriceCoins(
                                        itemId
                                );

                if (price <= 0L) {
                    price =
                            item.getPriceAtOpenCoins();
                }

                aggregate.currentValueCoins +=
                        price
                                * item.getQuantity();
            }
        }

        List<LootAggregate> result =
                new ArrayList<>(
                        map.values()
                );

        result.sort(
                Comparator.comparingLong(
                                (LootAggregate entry) ->
                                        entry.currentValueCoins
                        )
                        .reversed()
        );

        return result;
    }

    private String getDisplayName(
            DungeonLootItem item
    ) {
        String name =
                item.getDisplayName();

        if (
                name != null
                        && !name.isBlank()
        ) {
            return name.replaceFirst(
                    "(?i)\\s+[x×]\\d+$",
                    ""
            );
        }

        String itemId =
                normalizeLootItemId(
                        item.getItemId()
                );

        if (
                itemId == null
                        || itemId.isBlank()
        ) {
            return "Unknown";
        }

        String[] words =
                itemId
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replace(
                                '_',
                                ' '
                        )
                        .split(
                                "\\s+"
                        );

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {
                result.append(
                        word.substring(1)
                );
            }
        }

        return result.toString();
    }

    // =========================
    // PERSISTENT LOOT ICONS
    // =========================

    private ItemStack getPersistentLootIcon(
            LootAggregate entry
    ) {
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        ItemStack cached =
                DungeonChestTracker.getLootIcon(
                        entry.itemId
                );

        if (!cached.isEmpty()) {
            return cached;
        }

        if (
                entry.iconData == null
                        || entry.iconData.isBlank()
        ) {
            return ItemStack.EMPTY;
        }

        try {
            CompoundTag tag =
                    TagParser.parseCompoundFully(
                            entry.iconData
                    );

            var decoded =
                    ItemStack.CODEC.decode(
                            NbtOps.INSTANCE,
                            tag
                    ).result();

            if (decoded.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack stack =
                    decoded.get()
                            .getFirst();

            stack.setCount(1);

            return stack;

        } catch (Exception exception) {

            System.err.println(
                    "[ObbyAddons] Failed to load saved dungeon loot icon for "
                            + entry.itemId
            );

            return ItemStack.EMPTY;
        }
    }

    // =========================
    // TEXT HELPERS
    // =========================

    private String shorten(
            String text,
            int maxWidth
    ) {
        if (text == null) {
            return "";
        }

        if (
                this.font.width(
                        text
                ) <= maxWidth
        ) {
            return text;
        }

        String suffix =
                "...";

        String result =
                text;

        while (
                !result.isEmpty()
                        && this.font.width(
                        result + suffix
                ) > maxWidth
        ) {
            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        return result
                + suffix;
    }

    // =========================
    // HITBOX HELPER
    // =========================

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

    // =========================
    // LOOT ID NORMALIZATION
    // =========================

    private String normalizeLootItemId(
            String itemId
    ) {
        if (
                itemId == null
                        || itemId.isBlank()
        ) {
            return "";
        }

        String normalized =
                itemId.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (normalized) {
            case "APEX_DRAGON_SHARD" ->
                    "SHARD_APEX_DRAGON";

            case "POWER_DRAGON_SHARD" ->
                    "SHARD_POWER_DRAGON";

            case "BONZO_SHARD" ->
                    "SHARD_BONZO";

            case "SCARF_SHARD" ->
                    "SHARD_SCARF";

            case "THORN_SHARD" ->
                    "SHARD_THORN";

            default ->
                    normalized;
        };
    }

    // =========================
    // COIN FORMATTING
    // =========================

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

    // =========================
    // AGGREGATE MODEL
    // =========================

    private static final class LootAggregate {

        private final String itemId;
        private final String displayName;

        private String iconData =
                "";

        private int quantity =
                0;

        private long currentValueCoins =
                0L;

        private LootAggregate(
                String itemId,
                String displayName
        ) {
            this.itemId =
                    itemId;

            this.displayName =
                    displayName;
        }
    }
}