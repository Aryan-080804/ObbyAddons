package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.ObbyAddons;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.text.NumberFormat;
import java.util.Locale;

public final class ExplosiveArrowDamageTracker {

    private static long damagePerEnemy = 0;

    private ExplosiveArrowDamageTracker() {
    }

    public static void init() {

        // =========================
        // CHAT DETECTION
        // =========================

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {

            // Main Explosive Arrow feature is OFF.
            // Do not calculate anything.
            if (!ExplosiveArrowSettings.enabled) {
                return;
            }

            String text = message.getString();

            if (!text.contains("Your Explosive Shot hit")) {
                return;
            }

            try {
                String prefix = "Your Explosive Shot hit ";
                String pluralMiddle = " enemies for ";
                String singularMiddle = " enemy for ";
                String suffix = " damage.";

                int prefixIndex = text.indexOf(prefix);

                if (prefixIndex == -1) {
                    return;
                }

                int enemiesStart =
                        prefixIndex + prefix.length();

                int middleIndex =
                        text.indexOf(
                                pluralMiddle,
                                enemiesStart
                        );

                String middle = pluralMiddle;

                // Also support:
                // "hit 1 enemy for ..."
                if (middleIndex == -1) {
                    middleIndex =
                            text.indexOf(
                                    singularMiddle,
                                    enemiesStart
                            );

                    middle = singularMiddle;
                }

                if (middleIndex == -1) {
                    return;
                }

                int damageStart =
                        middleIndex + middle.length();

                int damageEnd =
                        text.indexOf(
                                suffix,
                                damageStart
                        );

                if (damageEnd == -1) {
                    return;
                }

                String enemiesText =
                        text.substring(
                                enemiesStart,
                                middleIndex
                        );

                String damageText =
                        text.substring(
                                damageStart,
                                damageEnd
                        );

                int enemies =
                        Integer.parseInt(
                                enemiesText.trim()
                        );

                double totalDamage =
                        Double.parseDouble(
                                damageText
                                        .replace(",", "")
                                        .trim()
                        );

                if (enemies <= 0) {
                    return;
                }

                damagePerEnemy =
                        Math.round(
                                totalDamage / enemies
                        );

                Minecraft minecraft =
                        Minecraft.getInstance();

                if (minecraft.gui != null) {

                    String formattedDamage =
                            NumberFormat
                                    .getIntegerInstance(Locale.US)
                                    .format(damagePerEnemy);

                    minecraft.gui
                            .getChat()
                            .addClientSystemMessage(
                                    Component.literal(
                                            "§aExplosive Shot did §e"
                                                    + formattedDamage
                                                    + " §adamage per enemy."
                                    )
                            );
                }

            } catch (Exception exception) {

                System.out.println(
                        "[ObbyAddons] Failed to parse Explosive Shot damage."
                );

                exception.printStackTrace();
            }
        });

        // =========================
        // HUD
        // =========================

        HudElementRegistry.addLast(
                ObbyAddons.id(
                        "explosive_arrow_damage_tracker"
                ),
                (graphics, deltaTracker) -> {

                    // Whole Explosive Arrow feature OFF
                    if (!ExplosiveArrowSettings.enabled) {
                        return;
                    }

                    // Calculator ON, but HUD toggle OFF
                    if (!ExplosiveArrowSettings.damageTrackerHudEnabled) {
                        return;
                    }

                    Minecraft minecraft =
                            Minecraft.getInstance();

                    if (minecraft.player == null) {
                        return;
                    }

                    String formattedDamage =
                            NumberFormat
                                    .getIntegerInstance(Locale.US)
                                    .format(damagePerEnemy);

                    String text =
                            "Explosive Arrow: "
                                    + formattedDamage;

                    int x =
                            ExplosiveArrowSettings.damageTrackerX;

                    int y =
                            ExplosiveArrowSettings.damageTrackerY;

                    float scale =
                            ExplosiveArrowSettings.damageTrackerScale;

                    graphics.pose().pushMatrix();

                    graphics.pose().translate(
                            x,
                            y
                    );

                    graphics.pose().scale(
                            scale,
                            scale
                    );

                    graphics.text(
                            minecraft.font,
                            Component.literal(text),
                            0,
                            0,
                            ExplosiveArrowSettings.damageTrackerColor,
                            true
                    );

                    graphics.pose().popMatrix();
                }
        );
    }

    public static long getDamagePerEnemy() {
        return damagePerEnemy;
    }
}