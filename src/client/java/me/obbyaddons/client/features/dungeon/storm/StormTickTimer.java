package me.obbyaddons.client.features.dungeon.storm;

import me.obbyaddons.ObbyAddons;
import me.obbyaddons.client.util.ServerTickTracker;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class StormTickTimer {

    private static final String STORM_START =
            "[BOSS] Storm: Pathetic Maxor, just like expected.";

    private static final String STORM_DEAD =
            "[BOSS] Storm: I should have known that I stood no chance.";

    private static final String SPIRIT_MASK_USED =
            "Second Wind Activated! Your Spirit Mask saved your life!";

    private static final String STORM_ENRAGED =
            "⚠ Storm is enraged! ⚠";

    private static int ticks = 0;
    private static boolean stormActive = false;

    private static double deathTime = 0.0;
    private static long deathDisplayStart = 0L;

    private static final long DEATH_DISPLAY_DURATION = 2000;

    private static final int CRUSH_TICKS = 31 * 20;
    private static final int COUNTDOWN_TICKS = 5 * 20;

    private StormTickTimer() {
    }

    public static void init() {

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();

            // Spirit Mask warning - only during Storm
            if (text.equals(SPIRIT_MASK_USED)) {
                if (StormSettings.spiritMaskWarning && stormActive) {
                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft.gui != null) {
                        minecraft.gui.getChat().addClientSystemMessage(
                                Component.literal(
                                        "§c⚠ Spirit Mask used during Storm! ⚠"
                                )
                        );
                    }

                    System.out.println(
                            "[ObbyAddons] Spirit Mask used during Storm."
                    );
                }
            }

            // Storm phase starts
            if (text.equals(STORM_START)) {
                ticks = 0;
                stormActive = true;

                deathTime = 0.0;
                deathDisplayStart = 0L;

                System.out.println(
                        "[ObbyAddons] Storm timer started."
                );
            }

            // First Storm death / enraged
            if (text.equals(STORM_ENRAGED)) {
                if (StormSettings.firstDeathTime && stormActive) {

                    deathTime = getSeconds();
                    deathDisplayStart = System.currentTimeMillis();

                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft.gui != null) {
                        minecraft.gui.getChat().addClientSystemMessage(
                                Component.literal(
                                        "§aStorm died at: §e"
                                                + String.format(
                                                        Locale.US,
                                                        "%.2f",
                                                        deathTime
                                                )
                                                + "s"
                                )
                        );
                    }

                    System.out.println(
                            "[ObbyAddons] Storm died at: "
                                    + String.format(
                                            Locale.US,
                                            "%.2f",
                                            deathTime
                                    )
                                    + "s"
                    );
                }
            }

            // Storm phase ends
            if (text.equals(STORM_DEAD)) {
                stormActive = false;

                System.out.println(
                        "[ObbyAddons] Storm timer stopped at "
                                + getSeconds()
                                + "s."
                );
            }
        });

        ServerTickTracker.register(() -> {

            if (!stormActive) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level == null || minecraft.player == null) {
                reset();
                return;
            }

            ticks++;
        });

        HudElementRegistry.addLast(
                ObbyAddons.id("storm_tick_timer"),
                (graphics, deltaTracker) -> {

                    Minecraft minecraft = Minecraft.getInstance();

                    // First Death timer
                    if (shouldRenderDeathTime()) {

                        int deathX =
                                StormSettings.stormDeathTimerX;

                        int deathY =
                                StormSettings.stormDeathTimerY;

                        float deathScale =
                                StormSettings.stormDeathTimerScale;

                        String deathText = String.format(
                                Locale.US,
                                "%.2f",
                                deathTime
                        );

                        graphics.pose().pushMatrix();

                        graphics.pose().translate(
                                deathX,
                                deathY
                        );

                        graphics.pose().scale(
                                deathScale,
                                deathScale
                        );

                        graphics.text(
                                minecraft.font,
                                Component.literal(deathText),
                                0,
                                0,
                                StormSettings.stormDeathTimerColor,
                                true
                        );

                        graphics.pose().popMatrix();

                        return;
                    }

                    // Normal Storm timer
                    if (!shouldRender()) {
                        return;
                    }

                    int x =
                            StormSettings.stormTimerX;

                    int y =
                            StormSettings.stormTimerY;

                    float scale =
                            StormSettings.stormTimerScale;

                    double displaySeconds;

                    if (StormSettings.tickDownFrom5) {
                        displaySeconds =
                                (CRUSH_TICKS - ticks) / 20.0;
                    } else {
                        displaySeconds = getSeconds();
                    }

                    String text = String.format(
                            Locale.US,
                            "%.2f",
                            displaySeconds
                    );

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
                            StormSettings.stormTimerColor,
                            true
                    );

                    graphics.pose().popMatrix();
                }
        );
    }

    private static boolean shouldRender() {

        if (!stormActive) {
            return false;
        }

        if (!StormSettings.stormTickTimer) {
            return false;
        }

        if (StormSettings.tickDownFrom5) {

            int remainingTicks =
                    CRUSH_TICKS - ticks;

            return remainingTicks >= 0
                    && remainingTicks <= COUNTDOWN_TICKS;
        }

        return true;
    }

    private static boolean shouldRenderDeathTime() {
        return StormSettings.firstDeathTime
                && deathTime > 0
                && System.currentTimeMillis()
                - deathDisplayStart
                <= DEATH_DISPLAY_DURATION;
    }

    public static double getSeconds() {
        return ticks / 20.0;
    }
    
    public static int getTicks() {
        return ticks;
    }

    public static void reset() {
        ticks = 0;
        stormActive = false;

        deathTime = 0.0;
        deathDisplayStart = 0L;
    }

    public static boolean isStormActive() {
        return stormActive;
    }
}