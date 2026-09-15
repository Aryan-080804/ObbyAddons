package me.obbyaddons.client.features.dungeon.tracker;

public final class DungeonKismetTracker {

    private static final String KISMET_ITEM_ID =
            "KISMET_FEATHER";

    /*
     * Prevents the same GUI reroll event from being
     * recorded repeatedly across multiple render ticks.
     */
    private static boolean rerollRecordedThisRewardWindow =
            false;

    private DungeonKismetTracker() {
    }

    // =========================
    // KISMET RECORDING
    // =========================

    public static boolean recordKismetUsed() {

        DungeonRunRecord run =
                DungeonRewardContext
                        .getCurrentRewardRun();

        if (run == null) {

            System.out.println(
                    "[ObbyAddons] Kismet detected, "
                            + "but no reward run context exists."
            );

            return false;
        }

        if (rerollRecordedThisRewardWindow) {

            return false;
        }

        long currentKismetPrice =
                PriceProvider.getPriceCoins(
                        KISMET_ITEM_ID
                );

        run.addKismet(
                currentKismetPrice
        );

        /*
         * Save immediately.
         *
         * This means the Kismet stays attached to
         * this exact run even if the player backs
         * out without opening a chest.
         */
        DungeonRunHistory.save();

        rerollRecordedThisRewardWindow =
                true;

        System.out.println(
                "[ObbyAddons] Kismet recorded: "
                        + run.getFloor()
                        + " / run "
                        + run.getId()
                        + " / "
                        + formatCoins(currentKismetPrice)
        );

        return true;
    }

    // =========================
    // REWARD WINDOW
    // =========================

    public static void beginRewardWindow() {

        rerollRecordedThisRewardWindow =
                false;
    }

    public static void endRewardWindow() {

        rerollRecordedThisRewardWindow =
                false;
    }

    public static boolean wasRerollRecordedThisRewardWindow() {

        return rerollRecordedThisRewardWindow;
    }

    // =========================
    // HELPERS
    // =========================

    private static String formatCoins(
            long coins
    ) {

        if (coins >= 1_000_000_000L) {

            return String.format(
                    "%.2fb",
                    coins / 1_000_000_000.0
            );
        }

        if (coins >= 1_000_000L) {

            return String.format(
                    "%.2fm",
                    coins / 1_000_000.0
            );
        }

        if (coins >= 1_000L) {

            return String.format(
                    "%.1fk",
                    coins / 1_000.0
            );
        }

        return Long.toString(
                coins
        );
    }
}