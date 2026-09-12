package me.obbyaddons.client.features.dungeon.tracker;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class DungeonRewardScreenTracker {

    private static final Pattern CROESUS_TITLE_PATTERN =
            Pattern.compile(
                    "^(?:\\(\\d+/\\d+\\)\\s*)?CROESUS$",
                    Pattern.CASE_INSENSITIVE
            );

    private static boolean rewardScreenOpen =
            false;

    private static String lastScreenTitle =
            "";

    private static final Set<String> seenKismetSignals =
            new HashSet<>();

    private DungeonRewardScreenTracker() {
    }

    // =========================
    // SCREEN RENDER
    // =========================

    public static void onScreenRendered(
            Screen screen
    ) {

        if (screen == null) {
            return;
        }

        String title =
                screen.getTitle()
                        .getString()
                        .trim();

        if (title.isEmpty()) {
            return;
        }

        boolean rewardChest =
                isDungeonRewardChestTitle(
                        title
                );

        boolean croesus =
                isCroesusTitle(
                        title
                );

        boolean trackedScreen =
                rewardChest
                        || croesus;

        // =========================
        // SCREEN CLOSED
        // =========================

        if (!trackedScreen) {

            if (rewardScreenOpen) {

                rewardScreenOpen =
                        false;

                lastScreenTitle =
                        "";

                seenKismetSignals.clear();

                System.out.println(
                        "[ObbyAddons] Reward-related screen closed."
                );
            }

            return;
        }

        // =========================
        // NEW REWARD SCREEN
        // =========================

        if (
                !rewardScreenOpen
                        || !title.equals(
                        lastScreenTitle
                )
        ) {

            rewardScreenOpen =
                    true;

            lastScreenTitle =
                    title;

            seenKismetSignals.clear();

        }

        if (
                !(screen instanceof AbstractContainerScreen<?> containerScreen)
        ) {
            return;
        }

        // =========================
        // CROESUS
        // =========================

        if (croesus) {

            /*
             * First:
             *
             * Map the Croesus slot/page to the correct
             * historical DungeonRunRecord.
             */
            DungeonCroesusTracker.scan(
                    containerScreen
            );

            /*
             * Second:
             *
             * Croesus can keep the title:
             *
             * (1/3) Croesus
             *
             * even while showing an individual run's
             * reward chest.
             *
             * DungeonChestTracker now checks the actual
             * container contents and returns null unless
             * it really looks like a reward chest.
             */
            scanForKismetSignals(
                    containerScreen
            );

            DungeonChestTracker.ChestSnapshot snapshot =
                    DungeonChestTracker.scan(
                            containerScreen
                    );

            if (
                    snapshot != null
                            && isCommitReady(
                            snapshot
                    )
            ) {

                DungeonChestCommitter.commit(
                        snapshot
                );
            }

            return;
        }

        // =========================
        // NORMAL REWARD CHEST
        // =========================

        if (rewardChest) {

            handleRewardChest(
                    containerScreen
            );
        }
    }

    // =========================
    // REWARD CHEST
    // =========================

    private static void handleRewardChest(
            AbstractContainerScreen<?> screen
    ) {

        scanForKismetSignals(
                screen
        );

        DungeonChestTracker.ChestSnapshot snapshot =
                DungeonChestTracker.scan(
                        screen
                );

        if (
                snapshot != null
                        && isCommitReady(
                        snapshot
                )
        ) {

            DungeonChestCommitter.commit(
                    snapshot
            );
        }
    }

    // =========================
    // KISMET DETECTION
    // =========================

    private static void scanForKismetSignals(
            AbstractContainerScreen<?> screen
    ) {

        AbstractContainerMenu menu =
                screen.getMenu();

        if (menu == null) {
            return;
        }

        for (Slot slot : menu.slots) {

            if (slot == null) {
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

            // =========================
            // ITEM NAME
            // =========================

            checkKismetText(
                    "name:"
                            + slot.index
                            + ":"
                            + stack.getHoverName()
                            .getString()
            );

            // =========================
            // ITEM LORE
            // =========================

            ItemLore lore =
                    stack.get(
                            DataComponents.LORE
                    );

            if (lore == null) {
                continue;
            }

            for (
                    int i = 0;
                    i < lore.lines().size();
                    i++
            ) {

                String line =
                        lore.lines()
                                .get(i)
                                .getString();

                checkKismetText(
                        "lore:"
                                + slot.index
                                + ":"
                                + i
                                + ":"
                                + line
                );
            }
        }
    }

    private static void checkKismetText(
            String rawSignal
    ) {

        if (
                rawSignal == null
                        || rawSignal.isBlank()
        ) {
            return;
        }

        String upper =
                rawSignal.toUpperCase(
                        Locale.ROOT
                );

        if (!upper.contains("KISMET")) {
            return;
        }

        boolean indicatesUse =
                upper.contains("USED")
                        || upper.contains("CONSUMED")
                        || upper.contains("REROLLED")
                        || upper.contains("RE-ROLLED")
                        || upper.contains("REROLL USED")
                        || upper.contains("ALREADY REROLLED");

        if (!indicatesUse) {
            return;
        }

        /*
         * Prevent the same visible lore line from
         * triggering every render frame.
         */
        if (
                !seenKismetSignals.add(
                        rawSignal
                )
        ) {
            return;
        }

        DungeonKismetTracker.recordKismetUsed();
    }

    // =========================
    // COMMIT CHECK
    // =========================

    private static boolean isCommitReady(
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

        return true;
    }

    // =========================
    // SCREEN TITLES
    // =========================

    private static boolean isDungeonRewardChestTitle(
            String title
    ) {

        if (
                title == null
                        || title.isBlank()
        ) {
            return false;
        }

        String upper =
                title.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return upper.equals("WOOD")
                || upper.equals("WOOD CHEST")
                || upper.equals("GOLD")
                || upper.equals("GOLD CHEST")
                || upper.equals("DIAMOND")
                || upper.equals("DIAMOND CHEST")
                || upper.equals("EMERALD")
                || upper.equals("EMERALD CHEST")
                || upper.equals("OBSIDIAN")
                || upper.equals("OBSIDIAN CHEST")
                || upper.equals("BEDROCK")
                || upper.equals("BEDROCK CHEST")
                || upper.equals("FREE")
                || upper.equals("FREE CHEST")
                || upper.equals("PAID")
                || upper.equals("PAID CHEST");
    }

    private static boolean isCroesusTitle(
            String title
    ) {

        if (
                title == null
                        || title.isBlank()
        ) {
            return false;
        }

        return CROESUS_TITLE_PATTERN
                .matcher(
                        title.trim()
                )
                .matches();
    }


    // =========================
    // GETTERS
    // =========================

    public static boolean isRewardScreenOpen() {
        return rewardScreenOpen;
    }

    public static String getLastScreenTitle() {
        return lastScreenTitle;
    }
}