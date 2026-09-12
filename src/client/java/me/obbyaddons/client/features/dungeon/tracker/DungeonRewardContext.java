package me.obbyaddons.client.features.dungeon.tracker;

public final class DungeonRewardContext {

    private static DungeonRunRecord currentRewardRun = null;

    private DungeonRewardContext() {
    }

    // =========================
    // GETTERS
    // =========================

    public static DungeonRunRecord getCurrentRewardRun() {
        return currentRewardRun;
    }

    public static boolean hasCurrentRewardRun() {
        return currentRewardRun != null;
    }

    // =========================
    // CONTEXT MANAGEMENT
    // =========================

    public static void setCurrentRewardRun(
            DungeonRunRecord run
    ) {
        boolean changedRun =
                currentRewardRun == null
                        || run == null
                        || !currentRewardRun
                        .getId()
                        .equals(
                                run.getId()
                        );

        currentRewardRun =
                run;

        /*
         * A different run means a fresh reward context.
         *
         * Reset both:
         * - Kismet dedup state
         * - chest commit dedup state
         */
        if (changedRun) {
            DungeonKismetTracker.beginRewardWindow();
            DungeonChestCommitter.resetForNewRewardContext();
        }

        if (run != null) {
            System.out.println(
                    "[ObbyAddons] Reward context set: "
                            + run.getId()
                            + " / "
                            + run.getFloor()
            );
        }
    }

    public static void clear() {

        if (currentRewardRun != null) {
            System.out.println(
                    "[ObbyAddons] Reward context cleared: "
                            + currentRewardRun.getId()
            );
        }

        currentRewardRun =
                null;

        DungeonKismetTracker.endRewardWindow();
        DungeonChestCommitter.resetForNewRewardContext();
    }

    public static void setLatestRun() {

        setCurrentRewardRun(
                DungeonRunHistory.getLatestRun()
        );
    }

    // =========================
    // HELPERS
    // =========================

    public static boolean matchesRunId(
            String runId
    ) {
        if (
                currentRewardRun == null
                        || runId == null
        ) {
            return false;
        }

        return runId.equals(
                currentRewardRun.getId()
        );
    }
}