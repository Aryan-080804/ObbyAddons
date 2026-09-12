package me.obbyaddons.client.features.dungeon.tracker;

import java.util.List;

public final class DungeonRunStats {

    private DungeonRunStats() {
    }

    public static int getTotalRuns() {
        return DungeonRunTracker
                .getSessionRunRecords()
                .size();
    }

    public static long getLastTimeMs() {

        List<DungeonRunRecord> runs =
                DungeonRunTracker.getSessionRunRecords();

        if (runs.isEmpty()) {
            return 0L;
        }

        return runs.get(
                runs.size() - 1
        ).getDurationMs();
    }

    public static long getBestTimeMs() {

        List<DungeonRunRecord> runs =
                DungeonRunTracker.getSessionRunRecords();

        long best = Long.MAX_VALUE;

        for (DungeonRunRecord run : runs) {

            long time =
                    run.getDurationMs();

            if (time > 0L && time < best) {
                best = time;
            }
        }

        return best == Long.MAX_VALUE
                ? 0L
                : best;
    }

    public static long getAverageTimeMs() {

        List<DungeonRunRecord> runs =
                DungeonRunTracker.getSessionRunRecords();

        if (runs.isEmpty()) {
            return 0L;
        }

        long total = 0L;
        int count = 0;

        for (DungeonRunRecord run : runs) {

            if (run.getDurationMs() <= 0L) {
                continue;
            }

            total += run.getDurationMs();
            count++;
        }

        if (count == 0) {
            return 0L;
        }

        return total / count;
    }

    public static String getLatestFloor() {

        if (DungeonRunTracker.isActive()) {

            String current =
                    DungeonRunTracker.getCurrentFloor();

            if (
                    current != null
                            && !current.isBlank()
            ) {
                return current;
            }
        }

        List<DungeonRunRecord> runs =
                DungeonRunTracker.getSessionRunRecords();

        if (runs.isEmpty()) {
            return "Dungeon";
        }

        String floor =
                runs.get(
                        runs.size() - 1
                ).getFloor();

        if (
                floor == null
                        || floor.isBlank()
                        || floor.equals("UNKNOWN")
        ) {
            return "Dungeon";
        }

        return floor;
    }

    public static String formatTime(
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
}