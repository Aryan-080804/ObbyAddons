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

    /*
     * Serialized item data used to rebuild the
     * original Minecraft ItemStack for rendering
     * the exact item icon in /oa loot.
     *
     * Old saved runs will simply have this as null.
     */
    private String iconData;

    public DungeonLootItem() {
    }

    public DungeonLootItem(
            String itemId,
            String displayName,
            int quantity,
            long priceAtOpenCoins
    ) {
        this(
                itemId,
                displayName,
                quantity,
                priceAtOpenCoins,
                ""
        );
    }

    public DungeonLootItem(
            String itemId,
            String displayName,
            int quantity,
            long priceAtOpenCoins,
            String iconData
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

        this.iconData =
                iconData == null
                        ? ""
                        : iconData;
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

    public String getIconData() {
        return iconData == null
                ? ""
                : iconData;
    }

    public boolean hasIconData() {
        return iconData != null
                && !iconData.isBlank();
    }

    public long getHistoricalValueCoins() {
        return priceAtOpenCoins
                * quantity;
    }
}