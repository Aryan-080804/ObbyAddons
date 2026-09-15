package me.obbyaddons.client.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class SkyblockItemUtils {

    private SkyblockItemUtils() {
    }

    public static String getSkyblockId(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return "";
        }

        CustomData customData =
                stack.getOrDefault(
                        DataComponents.CUSTOM_DATA,
                        CustomData.EMPTY
                );

        CompoundTag tag =
                customData.copyTag();

        return tag.getStringOr(
                "id",
                ""
        );
    }

    public static boolean isTerminator(ItemStack stack) {

        return "TERMINATOR".equals(
                getSkyblockId(stack)
        );
    }

    public static String getTerminatorEnchantOverlay(
            ItemStack stack
    ) {

        if (!isTerminator(stack)) {
            return "";
        }

        CustomData customData =
                stack.getOrDefault(
                        DataComponents.CUSTOM_DATA,
                        CustomData.EMPTY
                );

        CompoundTag tag =
                customData.copyTag();

        CompoundTag enchantments =
                tag.getCompoundOrEmpty(
                        "enchantments"
                );

        if (enchantments.contains(
                "ultimate_reiterate"
        )) {
            return "D";
        }

        if (enchantments.contains(
                "ultimate_soul_eater"
        )) {
            return "SE";
        }

        if (enchantments.contains(
                "ultimate_fatal_tempo"  
        )) {
            return "FT";
        }

        if (enchantments.contains(
                "ultimate_rend"
        )) {
        return "R";
        }

        return "";
    }
}