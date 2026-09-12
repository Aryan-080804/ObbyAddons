package me.obbyaddons.client.features.dungeon.tracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DungeonCroesusTracker {

    private static final Pattern PAGE_PATTERN =
            Pattern.compile(
                    "^\\((\\d+)/(\\d+)\\)\\s*CROESUS$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern LONG_FLOOR_PATTERN =
            Pattern.compile(
                    "(?i)(MM\\s+|MASTER MODE\\s+)?THE CATACOMBS.*?FLOOR\\s+([IVX]+)"
            );

    private static final Pattern SHORT_FLOOR_PATTERN =
            Pattern.compile(
                    "(?:^|[^A-Z0-9])([FM])\\s*([1-7])(?:$|[^A-Z0-9])",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern OPENED_CHEST_PATTERN =
            Pattern.compile(
                    "^OPENED CHESTS?\\s*:\\s*(.*)$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Map<Integer, DungeonRunRecord> slotMappings =
            new HashMap<>();

    private static int observedPageSize =
            0;

    private DungeonCroesusTracker() {
    }

    // =========================
    // SCAN
    // =========================

    public static void scan(
            AbstractContainerScreen<?> screen
    ) {

        if (screen == null) {
            return;
        }

        String title =
                screen.getTitle()
                        .getString()
                        .trim();

        if (
                !title.toUpperCase(Locale.ROOT)
                        .contains("CROESUS")
        ) {
            slotMappings.clear();
            return;
        }

        AbstractContainerMenu menu =
                screen.getMenu();

        if (menu == null) {
            return;
        }

        List<CroesusRunEntry> entries =
                findRunEntries(
                        menu
                );

        if (entries.isEmpty()) {
            return;
        }

        observedPageSize =
                Math.max(
                        observedPageSize,
                        entries.size()
                );

        int page =
                getPageNumber(
                        title
                );

        buildMappings(
                entries,
                page
        );
    }

    // =========================
    // BUILD MAPPINGS
    // =========================

    private static void buildMappings(
            List<CroesusRunEntry> entries,
            int page
    ) {

        slotMappings.clear();

        /*
         * IMPORTANT:
         *
         * Use ALL historical runs here, not only pending runs.
         *
         * If Croesus contains:
         *
         * A = opened
         * B = pending
         * C = pending
         *
         * removing A from our local candidates would shift
         * B and C by one position.
         */
        List<DungeonRunRecord> runs =
                getRunsNewestFirst();

        if (runs.isEmpty()) {
            return;
        }

        int pageSize =
                Math.max(
                        observedPageSize,
                        entries.size()
                );

        int pageOffset =
                Math.max(
                        0,
                        (page - 1) * pageSize
                );

        Set<String> usedRunIds =
                new HashSet<>();

        for (
                int visibleIndex = 0;
                visibleIndex < entries.size();
                visibleIndex++
        ) {

            CroesusRunEntry entry =
                    entries.get(
                            visibleIndex
                    );

            int expectedRunIndex =
                    pageOffset
                            + visibleIndex;

            DungeonRunRecord run =
                    findBestRun(
                            entry,
                            runs,
                            expectedRunIndex,
                            usedRunIds
                    );

            if (run == null) {

                System.out.println(
                        "[ObbyAddons] Croesus entry unmapped: "
                                + "slot="
                                + entry.slotIndex
                                + " floor="
                                + (
                                entry.floor.isEmpty()
                                        ? "?"
                                        : entry.floor
                        )
                                + " state="
                                + entry.openState
                );

                continue;
            }

            slotMappings.put(
                    entry.slotIndex,
                    run
            );

            if (run.getId() != null) {
                usedRunIds.add(
                        run.getId()
                );
            }

            System.out.println(
                    "[ObbyAddons] Croesus mapped slot "
                            + entry.slotIndex
                            + " -> "
                            + run.getId()
                            + " / "
                            + run.getFloor()
                            + " / opened="
                            + run.isChestOpened()
            );
        }
    }

    // =========================
    // MATCH RUN
    // =========================

    private static DungeonRunRecord findBestRun(
            CroesusRunEntry entry,
            List<DungeonRunRecord> runs,
            int expectedIndex,
            Set<String> usedRunIds
    ) {

        /*
         * First try the exact chronological position.
         */
        if (
                expectedIndex >= 0
                        && expectedIndex < runs.size()
        ) {

            DungeonRunRecord expected =
                    runs.get(
                            expectedIndex
                    );

            if (
                    !isUsed(
                            expected,
                            usedRunIds
                    )
                            && floorsCompatible(
                            entry.floor,
                            expected.getFloor()
                    )
                            && stateCompatible(
                            entry,
                            expected
                    )
            ) {
                return expected;
            }
        }

        /*
         * Otherwise find the closest compatible run.
         */
        DungeonRunRecord best =
                null;

        int bestDistance =
                Integer.MAX_VALUE;

        for (
                int i = 0;
                i < runs.size();
                i++
        ) {

            DungeonRunRecord candidate =
                    runs.get(i);

            if (
                    isUsed(
                            candidate,
                            usedRunIds
                    )
            ) {
                continue;
            }

            if (
                    !floorsCompatible(
                            entry.floor,
                            candidate.getFloor()
                    )
            ) {
                continue;
            }

            if (
                    !stateCompatible(
                            entry,
                            candidate
                    )
            ) {
                continue;
            }

            int distance =
                    Math.abs(
                            i - expectedIndex
                    );

            if (distance < bestDistance) {

                best =
                        candidate;

                bestDistance =
                        distance;
            }
        }

        return best;
    }

    private static boolean stateCompatible(
            CroesusRunEntry entry,
            DungeonRunRecord run
    ) {

        if (
                entry == null
                        || run == null
        ) {
            return false;
        }

        /*
         * UNKNOWN means Croesus did not give us a reliable
         * opened/pending signal, so don't reject the run.
         */
        if (
                entry.openState
                        == CroesusOpenState.UNKNOWN
        ) {
            return true;
        }

        if (
                entry.openState
                        == CroesusOpenState.OPENED
        ) {
            return run.isChestOpened();
        }

        return !run.isChestOpened();
    }

    private static boolean floorsCompatible(
            String croesusFloor,
            String runFloor
    ) {

        if (
                croesusFloor == null
                        || croesusFloor.isBlank()
        ) {
            return true;
        }

        if (
                runFloor == null
                        || runFloor.isBlank()
                        || runFloor.equalsIgnoreCase(
                        "UNKNOWN"
                )
        ) {
            return false;
        }

        return croesusFloor.equalsIgnoreCase(
                runFloor
        );
    }

    private static boolean isUsed(
            DungeonRunRecord run,
            Set<String> usedRunIds
    ) {

        if (run == null) {
            return true;
        }

        String id =
                run.getId();

        return id != null
                && usedRunIds.contains(
                id
        );
    }

    // =========================
    // ENTRY DETECTION
    // =========================

    private static List<CroesusRunEntry> findRunEntries(
            AbstractContainerMenu menu
    ) {

        List<CroesusRunEntry> entries =
                new ArrayList<>();

        Minecraft minecraft =
                Minecraft.getInstance();

        for (Slot slot : menu.slots) {

            if (slot == null) {
                continue;
            }

            if (
                    minecraft.player != null
                            && slot.container
                            == minecraft.player.getInventory()
            ) {
                continue;
            }

            ItemStack stack =
                    slot.getItem();

            if (
                    stack == null
                            || stack.isEmpty()
            ) {
                continue;
            }

            CroesusRunEntry entry =
                    parseCroesusRunEntry(
                            slot,
                            stack
                    );

            if (entry != null) {
                entries.add(
                        entry
                );
            }
        }

        entries.sort(
                Comparator.comparingInt(
                        entry ->
                                entry.slotIndex
                )
        );

        return entries;
    }

    private static CroesusRunEntry parseCroesusRunEntry(
            Slot slot,
            ItemStack stack
    ) {

        ItemLore lore =
                stack.get(
                        DataComponents.LORE
                );

        if (lore == null) {
            return null;
        }

        List<String> lines =
                new ArrayList<>();

        lines.add(
                stack.getHoverName()
                        .getString()
                        .trim()
        );

        for (var component : lore.lines()) {

            lines.add(
                    component.getString()
                            .trim()
            );
        }

        boolean dungeonSignal =
                false;

        boolean runStateSignal =
                false;

        String floor =
                "";

        CroesusOpenState openState =
                CroesusOpenState.UNKNOWN;

        boolean kismetUsed =
                false;

        for (String line : lines) {

            if (
                    line == null
                            || line.isBlank()
            ) {
                continue;
            }

            String upper =
                    line.toUpperCase(
                            Locale.ROOT
                    );

            if (
                    upper.contains("CATACOMBS")
                            || upper.contains(
                            "MASTER MODE"
                    )
            ) {

                dungeonSignal =
                        true;
            }

            if (floor.isEmpty()) {

                floor =
                        detectFloor(
                                line
                        );
            }

            if (
                    upper.contains(
                            "CLICK TO OPEN"
                    )
                            || upper.contains(
                            "UNOPENED"
                    )
            ) {

                openState =
                        CroesusOpenState.PENDING;

                runStateSignal =
                        true;
            }

            if (
                    upper.contains(
                            "ALREADY OPENED"
                    )
            ) {

                openState =
                        CroesusOpenState.OPENED;

                runStateSignal =
                        true;
            }

            Matcher openedMatcher =
                    OPENED_CHEST_PATTERN.matcher(
                            line
                    );

            if (openedMatcher.find()) {

                runStateSignal =
                        true;

                String value =
                        openedMatcher.group(1)
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );

                /*
                 * Examples:
                 *
                 * Opened Chest: Bedrock
                 * Opened Chests: 1
                 * Opened Chests: 0
                 */
                if (
                        value.equals("0")
                                || value.equals("0/1")
                                || value.equals("NONE")
                                || value.equals("NO")
                ) {

                    openState =
                            CroesusOpenState.PENDING;

                } else if (!value.isBlank()) {

                    openState =
                            CroesusOpenState.OPENED;
                }
            }

            if (
                    upper.contains("KISMET")
                            && (
                            upper.contains("USED")
                                    || upper.contains(
                                    "REROLL"
                            )
                                    || upper.contains(
                                    "CONSUMED"
                            )
                    )
            ) {

                kismetUsed =
                        true;
            }

            if (
                    upper.contains(
                            "CLICK TO VIEW"
                    )
            ) {

                runStateSignal =
                        true;
            }
        }

        if (
                !dungeonSignal
                        || !runStateSignal
        ) {
            return null;
        }

        return new CroesusRunEntry(
                slot.index,
                floor,
                openState,
                kismetUsed
        );
    }

    // =========================
    // FLOOR
    // =========================

    private static String detectFloor(
            String text
    ) {

        if (
                text == null
                        || text.isBlank()
        ) {
            return "";
        }

        Matcher longMatcher =
                LONG_FLOOR_PATTERN.matcher(
                        text
                );

        if (longMatcher.find()) {

            boolean master =
                    longMatcher.group(1) != null;

            int floor =
                    romanToInt(
                            longMatcher.group(2)
                    );

            if (
                    floor >= 1
                            && floor <= 7
            ) {

                return (
                        master
                                ? "M"
                                : "F"
                )
                        + floor;
            }
        }

        Matcher shortMatcher =
                SHORT_FLOOR_PATTERN.matcher(
                        text
                );

        if (shortMatcher.find()) {

            return shortMatcher.group(1)
                    .toUpperCase(
                            Locale.ROOT
                    )
                    + shortMatcher.group(2);
        }

        return "";
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
    // PAGE
    // =========================

    private static int getPageNumber(
            String title
    ) {

        Matcher matcher =
                PAGE_PATTERN.matcher(
                        title
                );

        if (!matcher.find()) {
            return 1;
        }

        try {

            return Math.max(
                    1,
                    Integer.parseInt(
                            matcher.group(1)
                    )
            );

        } catch (NumberFormatException ignored) {

            return 1;
        }
    }

    // =========================
    // HISTORY
    // =========================

    private static List<DungeonRunRecord> getRunsNewestFirst() {

        List<DungeonRunRecord> runs =
                new ArrayList<>();

        for (
                DungeonRunRecord run
                : DungeonRunHistory.getRuns()
        ) {

            if (run != null) {
                runs.add(
                        run
                );
            }
        }

        runs.sort(
                Comparator.comparingLong(
                                DungeonRunRecord::getCompletedAt
                        )
                        .reversed()
        );

        return runs;
    }

    // =========================
    // CLICK SELECTION
    // =========================

    public static void selectRunForSlot(
            AbstractContainerScreen<?> screen,
            int slotIndex
    ) {

        if (screen == null) {
            return;
        }

        String title =
                screen.getTitle()
                        .getString()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!title.contains("CROESUS")) {
            return;
        }

        scan(
                screen
        );

        DungeonRunRecord run =
                slotMappings.get(
                        slotIndex
                );

        if (run == null) {

            System.out.println(
                    "[ObbyAddons] Croesus click unmapped: slot="
                            + slotIndex
            );

            return;
        }

        DungeonRewardContext
                .setCurrentRewardRun(
                        run
                );

        System.out.println(
                "[ObbyAddons] Croesus selected run: "
                        + run.getId()
                        + " / "
                        + run.getFloor()
                        + " / opened="
                        + run.isChestOpened()
        );
    }

    // =========================
    // MODEL
    // =========================

    private enum CroesusOpenState {
        OPENED,
        PENDING,
        UNKNOWN
    }

    private static final class CroesusRunEntry {

        private final int slotIndex;

        private final String floor;

        private final CroesusOpenState openState;

        private final boolean kismetUsed;

        private CroesusRunEntry(
                int slotIndex,
                String floor,
                CroesusOpenState openState,
                boolean kismetUsed
        ) {

            this.slotIndex =
                    slotIndex;

            this.floor =
                    floor == null
                            ? ""
                            : floor;

            this.openState =
                    openState;

            this.kismetUsed =
                    kismetUsed;
        }
    }
}