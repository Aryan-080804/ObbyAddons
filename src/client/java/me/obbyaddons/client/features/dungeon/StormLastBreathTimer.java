package me.obbyaddons.client.features.dungeon;

import me.obbyaddons.ObbyAddons;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class StormLastBreathTimer {

    // 34.00 seconds after Storm begins.
    private static final int BASE_TARGET_TICK = 34 * 20;

    // Show countdown for 3 seconds.
    private static final int COUNTDOWN_TICKS = 3 * 20;

    // Show "SHOOT BOW" for 0.5 seconds.
    private static final int SHOOT_BOW_DURATION_TICKS = 10;

    private StormLastBreathTimer() {
    }

    public static void init() {
        HudElementRegistry.addLast(
                ObbyAddons.id("storm_last_breath_timer"),
                (graphics, deltaTracker) -> {

                    if (!StormSettings.stormLbEnabled) {
                        return;
                    }

                    if (!StormTickTimer.isStormActive()) {
                        return;
                    }

                    int ticks = StormTickTimer.getTicks();

                    /*
                     * Offset means "ticks earlier".
                     *
                     * Offset 0:
                     * target = 680 ticks = 34.00s
                     *
                     * Offset 5:
                     * target = 675 ticks = 33.75s
                     */
                    int targetTick =
                            BASE_TARGET_TICK
                                    + StormSettings.stormLbTickOffset;

                    int countdownStartTick =
                            targetTick - COUNTDOWN_TICKS;

                    int shootBowEndTick =
                            targetTick + SHOOT_BOW_DURATION_TICKS;

                    // Not time to show anything yet.
                    if (ticks < countdownStartTick) {
                        return;
                    }

                    // Everything is finished.
                    if (ticks >= shootBowEndTick) {
                        return;
                    }

                    Minecraft minecraft =
                            Minecraft.getInstance();

                    String text;

                    /*
                     * Countdown:
                     *
                     * 3.00
                     * 2.95
                     * ...
                     * 0.05
                     *
                     * At exactly the target tick,
                     * switch immediately to SHOOT BOW.
                     */
                    if (ticks < targetTick) {

                        double remainingSeconds =
                                (targetTick - ticks) / 20.0;

                        text = String.format(
                                Locale.US,
                                "%.2f",
                                remainingSeconds
                        );

                    } else {

                        text = "SHOOT BOW";
                    }

                    int x = StormSettings.stormLbX;
                    int y = StormSettings.stormLbY;
                    float scale = StormSettings.stormLbScale;

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
                            StormSettings.stormLbColor,
                            true
                    );

                    graphics.pose().popMatrix();
                }
        );
    }
}