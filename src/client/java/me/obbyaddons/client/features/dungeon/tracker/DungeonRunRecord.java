package me.obbyaddons.client.features.dungeon.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DungeonRunRecord {

    private String id;
    private String floor;

    private long durationMs;
    private long completedAt;

    // =========================
    // REWARD / LOOT DATA
    // =========================

    private boolean chestOpened = false;

    private String chestTitle = "";

    private long chestCostCoins = 0L;

    /*
     * Historical snapshot of the loot value.
     */
    private long lootValueCoins = 0L;

    /*
     * Individual items from the opened chest.
     *
     * These IDs let us recalculate current value
     * later using the latest Athen prices.
     */
    private List<DungeonLootItem> lootItems =
            new ArrayList<>();

    // =========================
    // KISMET DATA
    // =========================

    private int kismetsUsed = 0;

    private long kismetCostCoins = 0L;

    /*
     * Empty constructor for Gson.
     */
    public DungeonRunRecord() {
    }

    public DungeonRunRecord(
            String floor,
            long durationMs,
            long completedAt
    ) {
        this.id =
                UUID.randomUUID().toString();

        this.floor =
                floor;

        this.durationMs =
                durationMs;

        this.completedAt =
                completedAt;
    }

    // =========================
    // BASIC GETTERS
    // =========================

    public String getId() {
        return id;
    }

    public String getFloor() {
        return floor;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    // =========================
    // CHEST / LOOT GETTERS
    // =========================

    public boolean isChestOpened() {
        return chestOpened;
    }

    public String getChestTitle() {
        return chestTitle;
    }

    public long getChestCostCoins() {
        return chestCostCoins;
    }

    /*
     * Historical loot value.
     */
    public long getLootValueCoins() {
        return lootValueCoins;
    }

    public List<DungeonLootItem> getLootItems() {

        if (lootItems == null) {
            lootItems =
                    new ArrayList<>();
        }

        return Collections.unmodifiableList(
                lootItems
        );
    }

    // =========================
    // KISMET GETTERS
    // =========================

    public int getKismetsUsed() {
        return kismetsUsed;
    }

    public long getKismetCostCoins() {
        return kismetCostCoins;
    }

    // =========================
    // KISMET
    // =========================

    /*
     * Kismet is recorded immediately when used.
     *
     * The cost stays attached to this run even if
     * its chest is never opened.
     */
    public void addKismet(
            long priceCoins
    ) {
        kismetsUsed++;

        kismetCostCoins +=
                Math.max(
                        0L,
                        priceCoins
                );
    }

    // =========================
    // LOOT ITEMS
    // =========================

    public void addLootItem(
            DungeonLootItem item
    ) {
        if (item == null) {
            return;
        }

        if (lootItems == null) {
            lootItems =
                    new ArrayList<>();
        }

        lootItems.add(
                item
        );

        recalculateHistoricalLootValue();
    }

    public void addLootItem(
            String itemId,
            String displayName,
            int quantity,
            long priceAtOpenCoins
    ) {
        addLootItem(
                new DungeonLootItem(
                        itemId,
                        displayName,
                        quantity,
                        priceAtOpenCoins
                )
        );
    }

    public void clearLootItems() {

        if (lootItems == null) {
            lootItems =
                    new ArrayList<>();
        } else {
            lootItems.clear();
        }

        lootValueCoins = 0L;
    }

    private void recalculateHistoricalLootValue() {

        if (lootItems == null) {
            lootValueCoins = 0L;
            return;
        }

        long total = 0L;

        for (DungeonLootItem item : lootItems) {

            if (item == null) {
                continue;
            }

            total +=
                    item.getHistoricalValueCoins();
        }

        lootValueCoins =
                Math.max(
                        0L,
                        total
                );
    }

    // =========================
    // CURRENT ATHEN VALUE
    // =========================

    /*
     * Recalculates this run's loot using current
     * Athen prices.
     *
     * If Athen does not currently have a price for
     * an item, fall back to the price saved when the
     * chest was opened.
     */
    public long getCurrentLootValueCoins() {

        if (
                lootItems == null
                        || lootItems.isEmpty()
        ) {
            return lootValueCoins;
        }

        long total = 0L;

        for (DungeonLootItem item : lootItems) {

            if (item == null) {
                continue;
            }

            long currentPrice =
                    AthenPriceProvider.getPriceCoins(
                            item.getItemId()
                    );

            if (currentPrice <= 0L) {

                currentPrice =
                        item.getPriceAtOpenCoins();
            }

            total +=
                    currentPrice
                            * item.getQuantity();
        }

        return Math.max(
                0L,
                total
        );
    }

    public long getCurrentProfitCoins() {

        return getCurrentLootValueCoins()
                - chestCostCoins
                - kismetCostCoins;
    }

    // =========================
    // CHEST OPENING
    // =========================

    /*
     * Called when a chest belonging to this run
     * is actually opened.
     *
     * lootValueCoins remains the historical snapshot.
     */
    public void setOpenedChest(
            String chestTitle,
            long chestCostCoins,
            long lootValueCoins
    ) {
        this.chestOpened = true;

        this.chestTitle =
                chestTitle == null
                        ? ""
                        : chestTitle;

        this.chestCostCoins =
                Math.max(
                        0L,
                        chestCostCoins
                );

        this.lootValueCoins =
                Math.max(
                        0L,
                        lootValueCoins
                );
    }

    // =========================
    // HISTORICAL PROFIT
    // =========================

    public long getProfitCoins() {

        return lootValueCoins
                - chestCostCoins
                - kismetCostCoins;
    }

    public boolean hasPendingLoot() {

        return !chestOpened;
    }
}