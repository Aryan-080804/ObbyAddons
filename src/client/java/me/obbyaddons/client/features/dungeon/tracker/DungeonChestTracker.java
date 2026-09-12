package me.obbyaddons.client.features.dungeon.tracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DungeonChestTracker {

    private static final Pattern COIN_PATTERN =
            Pattern.compile(
                    "([\\d,]+(?:\\.\\d+)?)\\s*([KMB])?\\s*COINS?",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern ESSENCE_PATTERN =
            Pattern.compile(
                    "(?i)(WITHER|UNDEAD|SPIDER|DRAGON|ICE|DIAMOND|GOLD|CRIMSON|FOREST)"
                            + "\\s+ESSENCE(?:\\s*[xX×]\\s*(\\d+))?"
            );

    private static final Pattern ENCHANT_LINE_PATTERN =
            Pattern.compile(
                    "^(.+?)\\s+([IVX]+)$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Set<String> ULTIMATE_ENCHANTS =
            Set.of(
                    "BANK",
                    "BOBBIN_TIME",
                    "CHIMERA",
                    "COMBO",
                    "DUPLEX",
                    "FATAL_TEMPO",
                    "FIRST_IMPRESSION",
                    "FLASH",
                    "FLOWSTATE",
                    "HABANERO_TACTICS",
                    "INFERNO",
                    "JERRY",
                    "ULTIMATE_JERRY",
                    "LAST_STAND",
                    "LEGION",
                    "MISSILE",
                    "NO_PAIN_NO_GAIN",
                    "ONE_FOR_ALL",
                    "REFRIGERATE",
                    "REND",
                    "SOUL_EATER",
                    "SWARM",
                    "THE_ONE",
                    "ULTIMATE_WISE",
                    "WISDOM"
            );

    private DungeonChestTracker() {
    }

    // =========================
    // SCAN
    // =========================

    public static ChestSnapshot scan(
            AbstractContainerScreen<?> screen
    ) {

        if (screen == null) {
            return null;
        }

        DungeonRunRecord run =
                DungeonRewardContext
                        .getCurrentRewardRun();

        if (run == null) {
            return null;
        }

        AbstractContainerMenu menu =
                screen.getMenu();

        if (menu == null) {
            return null;
        }

        if (!looksLikeRewardChest(menu)) {
            return null;
        }

        String chestTitle =
                detectChestTitle(
                        screen,
                        menu
                );

        List<DungeonLootItem> lootItems =
                new ArrayList<>();

        long chestCostCoins =
                0L;

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

            long parsedCost =
                    parseChestCost(
                            stack
                    );

            if (parsedCost > chestCostCoins) {

                chestCostCoins =
                        parsedCost;
            }

            if (isRewardControlItem(stack)) {
                continue;
            }

            String displayName =
                    getLootDisplayName(
                            stack
                    );

            String itemId =
                    getPricingItemId(
                            stack
                    );

            if (
                    itemId == null
                            || itemId.isBlank()
            ) {

                itemId =
                        getFallbackItemId(
                                displayName
                        );

                if (itemId.isBlank()) {
                    continue;
                }

                System.out.println(
                        "[ObbyAddons] Reward item using fallback ID: "
                                + displayName
                                + " -> "
                                + itemId
                );
            }

            if (isIgnoredMenuItem(itemId)) {
                continue;
            }

            int quantity =
                    getLootQuantity(
                            stack
                    );

            long price =
                    AthenPriceProvider
                            .getPriceCoins(
                                    itemId
                            );

            lootItems.add(
                    new DungeonLootItem(
                            itemId,
                            displayName,
                            quantity,
                            price
                    )
            );
        }

        if (lootItems.isEmpty()) {

            return null;
        }

        System.out.println(
                "[ObbyAddons] Reward chest scan: "
                        + chestTitle
                        + " / loot="
                        + lootItems.size()
                        + " / cost="
                        + chestCostCoins
        );

        return new ChestSnapshot(
                chestTitle,
                chestCostCoins,
                lootItems
        );
    }

    // =========================
    // ITEM ID
    // =========================

    private static String getPricingItemId(
            ItemStack stack
    ) {

        String essenceId =
                getEssenceId(
                        stack
                );

        if (!essenceId.isEmpty()) {
            return essenceId;
        }

        String skyblockId =
                getSkyblockItemId(
                        stack
                );

        if (
                "ENCHANTED_BOOK".equalsIgnoreCase(
                        skyblockId
                )
        ) {

            String enchantId =
                    getEnchantedBookId(
                            stack
                    );

            if (!enchantId.isEmpty()) {

                System.out.println(
                        "[ObbyAddons] Enchanted book resolved: "
                                + enchantId
                );

                return enchantId;
            }
        }

        return skyblockId;
    }

    private static String getSkyblockItemId(
            ItemStack stack
    ) {

        CustomData customData =
                stack.getOrDefault(
                        DataComponents.CUSTOM_DATA,
                        CustomData.EMPTY
                );

        CompoundTag tag =
                customData.copyTag();

        String id =
                tag.getStringOr(
                        "id",
                        ""
                );

        return id == null
                ? ""
                : id;
    }

    private static String getFallbackItemId(
            String displayName
    ) {

        if (
                displayName == null
                        || displayName.isBlank()
        ) {
            return "";
        }

        String normalized =
                displayName.trim()
                        .replaceFirst(
                                "(?i)\\s+[x×]\\d+$",
                                ""
                )
                        .toUpperCase(
                                Locale.ROOT
                        )
                        .replace(
                                "’S",
                                ""
                        )
                        .replace(
                                "'S",
                                ""
                        )
                        .replace(
                                "’",
                                ""
                        )
                        .replace(
                                "'",
                                ""
                        )
                        .replaceAll(
                                "[^A-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );

        if (normalized.isBlank()) {
            return "";
        }

        return switch (normalized) {
            case "APEX_DRAGON_SHARD" -> "SHARD_APEX";
            case "POWER_DRAGON_SHARD" -> "SHARD_POWER";
            case "BONZO_SHARD" -> "SHARD_BONZO";
            case "SCARF_SHARD" -> "SHARD_SCARF";
            case "THORN_SHARD" -> "SHARD_THORN";
            default -> normalized;
        };
    }

    // =========================
    // ENCHANTED BOOKS
    // =========================

    private static String getEnchantedBookId(
            ItemStack stack
    ) {

        ItemLore lore =
                stack.get(
                        DataComponents.LORE
                );

        if (lore == null) {
            return "";
        }

        for (var component : lore.lines()) {

            String line =
                    component.getString()
                            .trim();

            Matcher matcher =
                    ENCHANT_LINE_PATTERN.matcher(
                            line
                    );

            if (!matcher.matches()) {
                continue;
            }

            String enchantName =
                    normalizeEnchantName(
                            matcher.group(1)
                    );

            int level =
                    romanToInt(
                            matcher.group(2)
                    );

            if (
                    enchantName.isEmpty()
                            || level <= 0
            ) {
                continue;
            }

            String bazaarName =
                    getBazaarEnchantName(
                            enchantName
                    );

            if (
                    ULTIMATE_ENCHANTS.contains(
                            enchantName
                    )
                            || ULTIMATE_ENCHANTS.contains(
                            bazaarName
                    )
            ) {

                /*
                 * Some ultimate enchant names already
                 * include ULTIMATE in their display name.
                 */
                if (
                        bazaarName.startsWith(
                                "ULTIMATE_"
                        )
                ) {

                    return "ENCHANTMENT_"
                            + bazaarName
                            + "_"
                            + level;
                }

                return "ENCHANTMENT_ULTIMATE_"
                        + bazaarName
                        + "_"
                        + level;
            }

            return "ENCHANTMENT_"
                    + bazaarName
                    + "_"
                    + level;
        }

        return "";
    }

    private static String normalizeEnchantName(
            String name
    ) {

        if (name == null) {
            return "";
        }

        return name.trim()
                .toUpperCase(
                        Locale.ROOT
                )
                .replace(
                        "'",
                        ""
                )
                .replaceAll(
                        "[^A-Z0-9]+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );
    }

    private static String getBazaarEnchantName(
            String enchantName
    ) {

        /*
         * Hypixel Bazaar naming oddities.
         */
        return switch (enchantName) {

            case "DUPLEX" ->
                    "REITERATE";

            /*
             * "Ultimate Jerry III" should become
             * ENCHANTMENT_ULTIMATE_JERRY_3, not
             * ENCHANTMENT_ULTIMATE_ULTIMATE_JERRY_3.
             */
            case "ULTIMATE_JERRY" ->
                    "ULTIMATE_JERRY";

            case "ULTIMATE_WISE" ->
                    "ULTIMATE_WISE";

            default ->
                    enchantName;
        };
    }

    // =========================
    // ESSENCE
    // =========================

    private static String getEssenceId(
            ItemStack stack
    ) {

        EssenceInfo info =
                getEssenceInfo(
                        stack
                );

        if (info == null) {
            return "";
        }

        return "ESSENCE_"
                + info.type;
    }

    private static int getLootQuantity(
            ItemStack stack
    ) {

        EssenceInfo essence =
                getEssenceInfo(
                        stack
                );

        if (
                essence != null
                        && essence.quantity > 0
        ) {
            return essence.quantity;
        }

        return Math.max(
                1,
                stack.getCount()
        );
    }

    private static EssenceInfo getEssenceInfo(
            ItemStack stack
    ) {

        String name =
                stack.getHoverName()
                        .getString();

        EssenceInfo fromName =
                parseEssence(
                        name
                );

        if (fromName != null) {
            return fromName;
        }

        ItemLore lore =
                stack.get(
                        DataComponents.LORE
                );

        if (lore == null) {
            return null;
        }

        for (var component : lore.lines()) {

            EssenceInfo info =
                    parseEssence(
                            component.getString()
                    );

            if (info != null) {
                return info;
            }
        }

        return null;
    }

    private static EssenceInfo parseEssence(
            String text
    ) {

        if (
                text == null
                        || text.isBlank()
        ) {
            return null;
        }

        Matcher matcher =
                ESSENCE_PATTERN.matcher(
                        text
                );

        if (!matcher.find()) {
            return null;
        }

        String type =
                matcher.group(1)
                        .toUpperCase(
                                Locale.ROOT
                        );

        int quantity =
                1;

        if (matcher.group(2) != null) {

            try {

                quantity =
                        Integer.parseInt(
                                matcher.group(2)
                        );

            } catch (NumberFormatException ignored) {

                quantity =
                        1;
            }
        }

        return new EssenceInfo(
                type,
                Math.max(
                        1,
                        quantity
                )
        );
    }

    // =========================
    // DISPLAY NAME
    // =========================

    private static String getLootDisplayName(
            ItemStack stack
    ) {

        String skyblockId =
                getSkyblockItemId(
                        stack
                );

        if (
                "ENCHANTED_BOOK".equalsIgnoreCase(
                        skyblockId
                )
        ) {

            ItemLore lore =
                    stack.get(
                            DataComponents.LORE
                    );

            if (lore != null) {

                for (var component : lore.lines()) {

                    String line =
                            component.getString()
                                    .trim();

                    Matcher matcher =
                            ENCHANT_LINE_PATTERN.matcher(
                                    line
                            );

                    if (matcher.matches()) {

                        return "Enchanted Book ("
                                + matcher.group(1)
                                + " "
                                + matcher.group(2)
                                + ")";
                    }
                }
            }
        }

        return stack.getHoverName()
                .getString();
    }

    // =========================
    // REWARD CHEST DETECTION
    // =========================

    private static boolean looksLikeRewardChest(
            AbstractContainerMenu menu
    ) {

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

            if (containsRewardControlText(stack)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsRewardControlText(
            ItemStack stack
    ) {

        String name =
                stack.getHoverName()
                        .getString()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (isRewardControlText(name)) {
            return true;
        }

        ItemLore lore =
                stack.get(
                        DataComponents.LORE
                );

        if (lore == null) {
            return false;
        }

        for (var component : lore.lines()) {

            String line =
                    component.getString()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

            if (isRewardControlText(line)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isRewardControlText(
            String text
    ) {

        if (
                text == null
                        || text.isBlank()
        ) {
            return false;
        }

        return text.contains("OPEN REWARD CHEST")
                || text.contains("CLICK TO OPEN")
                || text.contains("OPEN CHEST")
                || text.contains("ALREADY OPENED")
                || text.contains("REROLL CHEST")
                || text.contains("REROLL REWARDS");
    }

    private static boolean isRewardControlItem(
            ItemStack stack
    ) {

        return containsRewardControlText(
                stack
        );
    }

    // =========================
    // CHEST TITLE
    // =========================

    private static String detectChestTitle(
            AbstractContainerScreen<?> screen,
            AbstractContainerMenu menu
    ) {

        String title =
                detectChestTitleFromText(
                        screen.getTitle()
                                .getString()
                );

        if (!title.isEmpty()) {
            return title;
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

            title =
                    detectChestTitleFromText(
                            stack.getHoverName()
                                    .getString()
                    );

            if (!title.isEmpty()) {
                return title;
            }

            ItemLore lore =
                    stack.get(
                            DataComponents.LORE
                    );

            if (lore == null) {
                continue;
            }

            for (var component : lore.lines()) {

                title =
                        detectChestTitleFromText(
                                component.getString()
                        );

                if (!title.isEmpty()) {
                    return title;
                }
            }
        }

        return screen.getTitle()
                .getString()
                .trim();
    }

    private static String detectChestTitleFromText(
            String text
    ) {

        if (text == null) {
            return "";
        }

        String upper =
                text.toUpperCase(
                        Locale.ROOT
                );

        if (upper.contains("BEDROCK CHEST")) {
            return "Bedrock Chest";
        }

        if (upper.contains("OBSIDIAN CHEST")) {
            return "Obsidian Chest";
        }

        if (upper.contains("EMERALD CHEST")) {
            return "Emerald Chest";
        }

        if (upper.contains("DIAMOND CHEST")) {
            return "Diamond Chest";
        }

        if (upper.contains("GOLD CHEST")) {
            return "Gold Chest";
        }

        if (upper.contains("WOOD CHEST")) {
            return "Wood Chest";
        }

        if (upper.contains("FREE CHEST")) {
            return "Free Chest";
        }

        if (upper.contains("PAID CHEST")) {
            return "Paid Chest";
        }

        return "";
    }

    // =========================
    // COST
    // =========================

    private static long parseChestCost(
            ItemStack stack
    ) {

        ItemLore lore =
                stack.get(
                        DataComponents.LORE
                );

        if (lore == null) {
            return 0L;
        }

        for (var component : lore.lines()) {

            long value =
                    parseCoinsFromLine(
                            component.getString()
                    );

            if (value > 0L) {
                return value;
            }
        }

        return 0L;
    }

    private static long parseCoinsFromLine(
            String line
    ) {

        if (
                line == null
                        || line.isBlank()
        ) {
            return 0L;
        }

        Matcher matcher =
                COIN_PATTERN.matcher(
                        line.toUpperCase(
                                Locale.ROOT
                        )
                );

        if (!matcher.find()) {
            return 0L;
        }

        double number;

        try {

            number =
                    Double.parseDouble(
                            matcher.group(1)
                                    .replace(
                                            ",",
                                            ""
                                    )
                    );

        } catch (NumberFormatException ignored) {

            return 0L;
        }

        String suffix =
                matcher.group(2);

        double multiplier =
                1.0D;

        if (suffix != null) {

            multiplier =
                    switch (
                            suffix.toUpperCase(
                                    Locale.ROOT
                            )
                    ) {
                        case "K" ->
                                1_000.0D;

                        case "M" ->
                                1_000_000.0D;

                        case "B" ->
                                1_000_000_000.0D;

                        default ->
                                1.0D;
                    };
        }

        return Math.max(
                0L,
                Math.round(
                        number * multiplier
                )
        );
    }

    // =========================
    // MENU FILTERS
    // =========================

    private static boolean isIgnoredMenuItem(
            String itemId
    ) {

        if (itemId == null) {
            return true;
        }

        String upper =
                itemId.toUpperCase(
                        Locale.ROOT
                );

        return upper.equals("KISMET_FEATHER")
                || upper.equals("DUNGEON_CHEST_KEY")
                || upper.equals("ARROW")
                || upper.equals("BARRIER")
                || upper.equals("CLOSE")
                || upper.equals("GO_BACK")
                || upper.equals("BACK");
    }

    // =========================
    // ROMAN NUMERALS
    // =========================

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
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            default -> 0;
        };
    }

    // =========================
    // MODELS
    // =========================

    private record EssenceInfo(
            String type,
            int quantity
    ) {
    }

    public record ChestSnapshot(
            String chestTitle,
            long chestCostCoins,
            List<DungeonLootItem> lootItems
    ) {
    }
}