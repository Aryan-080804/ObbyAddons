package me.obbyaddons.client.features.dungeon.tracker;

public final class DungeonLootItem {

    private String itemId;
    private String displayName;
    private int quantity;

    /*
     * Optional historical snapshot.
     *
     * /oa loot will still use the CURRENT Athen price
     * when calculating current value.
     */
    private long priceAtOpenCoins;

    public DungeonLootItem() {
    }

    public DungeonLootItem(
            String itemId,
            String displayName,
            int quantity,
            long priceAtOpenCoins
    ) {
        this.itemId =
                itemId == null
                        ? ""
                        : itemId;

        this.displayName =
                displayName == null
                        ? ""
                        : displayName;

        this.quantity =
                Math.max(
                        1,
                        quantity
                );

        this.priceAtOpenCoins =
                Math.max(
                        0L,
                        priceAtOpenCoins
                );
    }

    public String getItemId() {
        return itemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPriceAtOpenCoins() {
        return priceAtOpenCoins;
    }

    public long getHistoricalValueCoins() {
        return priceAtOpenCoins
                * quantity;
    }
}