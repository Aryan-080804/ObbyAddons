package me.obbyaddons.client.features.dungeon.tracker;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DungeonRunTracker {

    /*
     * Exact Hypixel dungeon entry examples:
     *
     * [MVP++] ObbyTheGorilla entered MM The Catacombs, Floor VII!
     * [MVP+] Player entered The Catacombs, Floor VI!
     *
     * Group 1 = "MM " when Master Mode
     * Group 2 = Roman numeral floor
     */
    private static final Pattern DUNGEON_ENTRY_PATTERN =
            Pattern.compile(
                    "entered\\s+(MM\\s+)?The Catacombs,\\s*Floor\\s+([IVX]+)!",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Fallback for messages that explicitly contain M7 / F7.
     */
    private static final Pattern SHORT_FLOOR_PATTERN =
            Pattern.compile(
                    "(?:^|[^A-Z0-9])([FM])\\s*([1-7])(?:$|[^A-Z0-9])",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Example:
     *
     * Defeated Necron in 5m 14s
     */
    private static final Pattern BOSS_TIME_PATTERN =
            Pattern.compile(
                    "Defeated .+ in (\\d+)m\\s+(\\d+)s",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Examples:
     *
     * BEDROCK CHEST REWARDS
     * OBSIDIAN CHEST REWARDS
     * GOLD CHEST REWARDS
     *
     * This is our authoritative signal that the player
     * actually opened/claimed the chest.
     */
    private static final Pattern CHEST_REWARDS_PATTERN =
            Pattern.compile(
                    "^(WOOD|GOLD|DIAMOND|EMERALD|OBSIDIAN|BEDROCK|FREE|PAID)\\s+CHEST\\s+REWARDS$",
                    Pattern.CASE_INSENSITIVE
            );

    private static boolean active =
            false;

    private static String currentFloor =
            "";

    private static long lastCompletionTime =
            0L;

    private static final long COMPLETION_DEBOUNCE_MS =
            3000L;

    /*
     * Only runs completed during the current Minecraft session.
     * Resets whenever Minecraft restarts.
     */
    private static final List<DungeonRunRecord> sessionRuns =
            new ArrayList<>();

    private DungeonRunTracker() {
    }

    // =========================
    // INIT
    // =========================

    public static void init() {

        ClientReceiveMessageEvents.GAME.register(
                (message, overlay) ->
                        onMessage(message)
        );
    }

    // =========================
    // CHAT HANDLER
    // =========================

    private static void onMessage(
            Component message
    ) {

        if (message == null) {
            return;
        }

        String raw =
                message.getString()
                        .trim();

        if (raw.isEmpty()) {
            return;
        }

        String upper =
                raw.toUpperCase(
                        Locale.ROOT
                );

        // =========================
        // DUNGEON ENTRY / FLOOR
        // =========================

        Matcher entryMatcher =
                DUNGEON_ENTRY_PATTERN.matcher(
                        raw
                );

        if (entryMatcher.find()) {

            boolean masterMode =
                    entryMatcher.group(1) != null;

            int floorNumber =
                    romanToInt(
                            entryMatcher.group(2)
                    );

            if (
                    floorNumber >= 1
                            && floorNumber <= 7
            ) {

                currentFloor =
                        (masterMode ? "M" : "F")
                                + floorNumber;

                active =
                        true;

                System.out.println(
                        "[ObbyAddons] Dungeon started: "
                                + currentFloor
                );

                System.out.println(
                        "[ObbyAddons] Dungeon floor detected: "
                                + currentFloor
                );
            }
        }

        // =========================
        // FALLBACK FLOOR DETECTION
        // =========================

        if (
                currentFloor.isEmpty()
                        && upper.contains(
                        "CATACOMBS"
                )
        ) {

            String detectedFloor =
                    detectShortFloor(
                            upper
                    );

            if (!detectedFloor.isEmpty()) {

                currentFloor =
                        detectedFloor;

                System.out.println(
                        "[ObbyAddons] Dungeon floor detected from fallback: "
                                + currentFloor
                );
            }
        }

        // =========================
        // KISMET
        // =========================

        if (
                upper.contains(
                        "YOU USED A KISMET FEATHER"
                )
        ) {

            System.out.println(
                    "[ObbyAddons] Kismet chat message detected."
            );

            DungeonKismetTracker.recordKismetUsed();
        }

        // =========================
        // CHEST ACTUALLY OPENED
        // =========================

        Matcher chestRewardsMatcher =
                CHEST_REWARDS_PATTERN.matcher(
                        raw
                );

        if (chestRewardsMatcher.find()) {

            String chest =
                    chestRewardsMatcher.group(1)
                            .toUpperCase(
                                    Locale.ROOT
                            );

            System.out.println(
                    "[ObbyAddons] Chest rewards confirmation detected: "
                            + chest
            );

            boolean committed =
                    DungeonChestCommitter
                            .confirmOpened();

            if (!committed) {

                System.out.println(
                        "[ObbyAddons] Chest confirmation did not commit "
                                + "a staged reward preview."
                );
            }
        }

        // =========================
        // RUN COMPLETION
        // =========================

        Matcher bossMatcher =
                BOSS_TIME_PATTERN.matcher(
                        raw
                );

        if (bossMatcher.find()) {

            if (!active || currentFloor.isEmpty()) {

                System.out.println(
                        "[ObbyAddons] Ignored completion because no dungeon is active: "
                                + raw
                );

                return;
            }

            long now =
                    System.currentTimeMillis();

            if (
                    now - lastCompletionTime
                            < COMPLETION_DEBOUNCE_MS
            ) {

                System.out.println(
                        "[ObbyAddons] Ignored duplicate dungeon completion: "
                                + raw
                );

                return;
            }

            int minutes =
                    Integer.parseInt(
                            bossMatcher.group(1)
                    );

            int seconds =
                    Integer.parseInt(
                            bossMatcher.group(2)
                    );

            long durationMs =
                    (
                            minutes * 60L
                                    + seconds
                    ) * 1000L;

            lastCompletionTime =
                    now;

            completeRun(
                    durationMs
            );
        }
    }

    // =========================
    // FLOOR HELPERS
    // =========================

    private static String detectShortFloor(
            String text
    ) {

        if (text == null) {
            return "";
        }

        Matcher matcher =
                SHORT_FLOOR_PATTERN.matcher(
                        text.toUpperCase(
                                Locale.ROOT
                        )
                );

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1)
                .toUpperCase(
                        Locale.ROOT
                )
                + matcher.group(2);
    }

    private static int romanToInt(
            String roman
    ) {

        if (roman == null) {
            return 0;
        }

        return switch (
                roman.toUpperCase(
                        Locale.ROOT
                )
        ) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            default -> 0;
        };
    }

    // =========================
    // RUN COMPLETION
    // =========================

    private static void completeRun(
            long durationMs
    ) {

        String floor =
                currentFloor.isEmpty()
                        ? "UNKNOWN"
                        : currentFloor;

        DungeonRunRecord run =
                new DungeonRunRecord(
                        floor,
                        durationMs,
                        System.currentTimeMillis()
                );

        /*
         * Lifetime history.
         *
         * Used for:
         * - /oa loot
         * - Croesus
         * - Kismet tracking
         * - loot history
         */
        DungeonRunHistory.addRun(
                run
        );

        /*
         * The completed run now becomes the active
         * reward context.
         */
        DungeonRewardContext.setCurrentRewardRun(
                run
        );

        /*
         * Session-only HUD history.
         */
        sessionRuns.add(
                run
        );

        System.out.println(
                "[ObbyAddons] Dungeon completed: "
                        + floor
                        + " / "
                        + formatDuration(
                        durationMs
                )
        );

        active =
                false;

        currentFloor =
                "";
    }

    // =========================
    // FORMAT
    // =========================

    private static String formatDuration(
            long durationMs
    ) {

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

    // =========================
    // GETTERS
    // =========================

    public static boolean isActive() {
        return active;
    }

    public static String getCurrentFloor() {
        return currentFloor;
    }

    public static int getSessionRuns() {
        return sessionRuns.size();
    }

    public static List<DungeonRunRecord> getSessionRunRecords() {

        return Collections.unmodifiableList(
                sessionRuns
        );
    }
}
