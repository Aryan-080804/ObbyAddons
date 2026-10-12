
package me.obbyaddons.client.features.dungeon.terminator;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.client.util.SkyblockItemUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TerminatorOverlayRenderer {

    private TerminatorOverlayRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            ItemStack stack,
            int x,
            int y
    ) {

        String overlay = "";

        // =========================
        // TERMINATOR ULT ENCHANT
        // =========================

        if (ObbyConfig.get().terminatorOverlayEnabled) {

            overlay = SkyblockItemUtils
                    .getTerminatorEnchantOverlay(stack);
        }

        // =========================
        // M7 ARCH BOWS
        // =========================

        if (overlay.isEmpty()
                && ObbyConfig.get().m7ArchBowsEnabled) {

            overlay = SkyblockItemUtils
                    .getM7ArchBowOverlay(stack);
        }

        if (overlay.isEmpty()) {
            return;
        }

        // =========================
        // RENDER OVERLAY
        // =========================

        Minecraft minecraft =
                Minecraft.getInstance();

        graphics.pose().pushMatrix();

        graphics.pose().translate(
                x,
                y
        );

        graphics.pose().scale(
                0.9f,
                0.9f
        );

        graphics.text(
                minecraft.font,
                Component.literal(overlay),
                1,
                10,
                TerminatorOverlaySettings.color,
                true
        );

        graphics.pose().popMatrix();
    }
}
