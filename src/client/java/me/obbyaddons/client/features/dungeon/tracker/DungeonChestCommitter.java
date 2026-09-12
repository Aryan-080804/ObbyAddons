package me.obbyaddons.client.features.dungeon.tracker;

import java.util.List;

public final class DungeonChestCommitter {

    /*
     * The reward chest currently being previewed.
     *
     * We DO NOT save this permanently until Hypixel
     * confirms the chest was actually opened.
     */
    private static DungeonChestTracker.ChestSnapshot pendingSnapshot =
            null;

    /*
     * Exact run the preview belongs to.
     *
     * Store the ID instead of trusting whatever reward
     * context happens to exist later.
     */
    private static String pendingRunId =
            "";

    private static String lastCommittedRunId =
            "";

    private static String lastCommittedChestTitle =
            "";

    private DungeonChestCommitter() {
    }

    // =========================
    // STAGE PREVIEW
    // =========================

    /*
     * RewardScreenTracker can safely call this every frame.
     *
     * This DOES NOT mark the chest opened.
     *
     * It only remembers the latest valid reward preview.
     */
    public static boolean commit(
            DungeonChestTracker.ChestSnapshot snapshot
    ) {

        if (snapshot == null) {
            return false;
        }

        if (
                snapshot.lootItems() == null
                        || snapshot.lootItems().isEmpty()
        ) {
            return false;
        }

        DungeonRunRecord run =
                DungeonRewardContext
                        .getCurrentRewardRun();

        if (run == null) {

            System.out.println(
                    "[ObbyAddons] Chest preview skipped: "
                            + "no reward run context."
            );

            return false;
        }

        /*
         * Never stage another chest for a run that is
         * already permanently opened.
         */
        if (run.isChestOpened()) {
            return false;
        }

        String runId =
                run.getId();

        if (
                runId == null
                        || runId.isBlank()
        ) {

            System.out.println(
                    "[ObbyAddons] Chest preview skipped: "
                            + "run has no ID."
            );

            return false;
        }

        String chestTitle =
                snapshot.chestTitle() == null
                        ? ""
                        : snapshot.chestTitle();

        /*
         * Copy the list so later GUI changes cannot mutate
         * the preview we intend to commit.
         */
        pendingSnapshot =
                new DungeonChestTracker.ChestSnapshot(
                        chestTitle,
                        snapshot.chestCostCoins(),
                        List.copyOf(
                                snapshot.lootItems()
                        )
                );

        pendingRunId =
                runId;

        System.out.println(
                "[ObbyAddons] Chest preview staged: "
                        + run.getFloor()
                        + " / "
                        + chestTitle
                        + " / loot "
                        + snapshot.lootItems().size()
                        + " items"
                        + " / cost "
                        + snapshot.chestCostCoins()
        );

        return true;
    }

    // =========================
    // ACTUAL OPEN CONFIRMATION
    // =========================

    /*
     * Call this only after Hypixel confirms that the chest
     * was actually opened.
     *
     * Best signal:
     *
     * BEDROCK CHEST REWARDS
     * OBSIDIAN CHEST REWARDS
     * etc.
     */
    public static boolean confirmOpened() {

        if (
                pendingSnapshot == null
                        || pendingRunId == null
                        || pendingRunId.isBlank()
        ) {

            System.out.println(
                    "[ObbyAddons] Chest open confirmation received, "
                            + "but no staged preview exists."
            );

            return false;
        }

        DungeonRunRecord run =
                DungeonRunHistory.getById(
                        pendingRunId
                );

        if (run == null) {

            System.out.println(
                    "[ObbyAddons] Chest commit failed: "
                            + "staged run no longer exists: "
                            + pendingRunId
            );

            clearPending();

            return false;
        }

        String chestTitle =
                pendingSnapshot.chestTitle() == null
                        ? ""
                        : pendingSnapshot.chestTitle();

        /*
         * Prevent duplicate confirmation messages from
         * committing the same chest twice.
         */
        if (
                run.getId() != null
                        && run.getId().equals(
                        lastCommittedRunId
                )
                        && chestTitle.equalsIgnoreCase(
                        lastCommittedChestTitle
                )
        ) {

            clearPending();

            return false;
        }

        /*
         * Our current data model allows one opened reward
         * chest per dungeon run.
         */
        if (run.isChestOpened()) {

            clearPending();

            return false;
        }

        // =========================
        // WRITE LOOT
        // =========================

        run.clearLootItems();

        for (
                DungeonLootItem item
                : pendingSnapshot.lootItems()
        ) {

            if (item == null) {
                continue;
            }

            run.addLootItem(
                    item
            );
        }

        /*
         * addLootItem() updates the historical stored
         * value using the price snapshot from chest scan.
         */
        long historicalLootValue =
                run.getLootValueCoins();

        run.setOpenedChest(
                chestTitle,
                pendingSnapshot.chestCostCoins(),
                historicalLootValue
        );

        DungeonRunHistory.save();

        lastCommittedRunId =
                run.getId() == null
                        ? ""
                        : run.getId();

        lastCommittedChestTitle =
                chestTitle;

        System.out.println(
                "[ObbyAddons] Chest committed: "
                        + run.getFloor()
                        + " / "
                        + chestTitle
                        + " / loot "
                        + pendingSnapshot.lootItems().size()
                        + " items"
                        + " / cost "
                        + pendingSnapshot.chestCostCoins()
                        + " / value "
                        + historicalLootValue
        );

        clearPending();

        return true;
    }

    // =========================
    // CANCEL / RESET
    // =========================

    /*
     * Called when reward context changes to another run.
     *
     * Any unclaimed preview from the previous context
     * should no longer be eligible for confirmation.
     */
    public static void resetForNewRewardContext() {

        clearPending();

        lastCommittedRunId =
                "";

        lastCommittedChestTitle =
                "";
    }

    private static void clearPending() {

        pendingSnapshot =
                null;

        pendingRunId =
                "";
    }

    // =========================
    // DEBUG / STATE
    // =========================

    public static boolean hasPendingSnapshot() {

        return pendingSnapshot != null
                && pendingRunId != null
                && !pendingRunId.isBlank();
    }

    public static String getPendingRunId() {

        return pendingRunId;
    }
}