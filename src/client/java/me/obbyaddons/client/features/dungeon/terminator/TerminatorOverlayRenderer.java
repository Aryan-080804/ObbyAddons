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

        if (!ObbyConfig.get().terminatorOverlayEnabled) {
            return;
        }

        String overlay =
                SkyblockItemUtils
                        .getTerminatorEnchantOverlay(stack);

        if (overlay.isEmpty()) {
            return;
        }

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