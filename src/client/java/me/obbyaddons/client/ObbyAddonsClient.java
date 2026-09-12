package me.obbyaddons.client;

import me.obbyaddons.client.command.ObbyAddonsCommands;
import me.obbyaddons.client.config.ObbyConfig;

import me.obbyaddons.client.features.chat.ChatCleanerFeature;
import me.obbyaddons.client.features.chat.ChatRules;

import me.obbyaddons.client.features.dungeon.explosivearrow.ExplosiveArrowDamageTracker;
import me.obbyaddons.client.features.dungeon.explosivearrow.ExplosiveArrowFeature;
import me.obbyaddons.client.features.dungeon.explosivearrow.ExplosiveArrowSettings;

import me.obbyaddons.client.features.dungeon.storm.StormFeatures;
import me.obbyaddons.client.features.dungeon.storm.StormLastBreathTimer;
import me.obbyaddons.client.features.dungeon.storm.StormSettings;
import me.obbyaddons.client.features.dungeon.storm.StormTickTimer;

import me.obbyaddons.client.features.dungeon.clickprot.TerminalClickTimer;
import me.obbyaddons.client.features.dungeon.clickprot.TerminalClickTimerFeature;
import me.obbyaddons.client.features.dungeon.clickprot.TerminalClickTimerSettings;

import me.obbyaddons.client.features.dungeon.terminator.TerminatorOverlayFeature;
import me.obbyaddons.client.features.dungeon.terminator.TerminatorOverlayRenderer;
import me.obbyaddons.client.features.dungeon.terminator.TerminatorOverlaySettings;

import me.obbyaddons.client.features.dungeon.tracker.AthenPriceProvider;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunHistory;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunTracker;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunTrackerFeature;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunTrackerHud;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunTrackerSettings;

import me.obbyaddons.client.features.dungeon.shared.DungeonBossTracker;
import me.obbyaddons.client.features.dungeon.shared.DungeonLocationTracker;

import me.obbyaddons.client.features.dungeon.itemhighlight.DungeonItemHighlight;
import me.obbyaddons.client.features.dungeon.itemhighlight.DungeonItemHighlightFeature;
import me.obbyaddons.client.features.dungeon.itemhighlight.DungeonItemHighlightRenderer;
import me.obbyaddons.client.features.dungeon.itemhighlight.DungeonItemHighlightSettings;

import me.obbyaddons.client.util.ServerTickTracker;

import me.obbyaddons.feature.FeatureManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class ObbyAddonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // =========================
        // LOAD CONFIG / HISTORY
        // =========================

        ObbyConfig.load();
        DungeonRunHistory.load();

        // =========================
        // STORM SETTINGS
        // =========================

        StormSettings.stormTimerX =
                ObbyConfig.get().stormTimerX;

        StormSettings.stormTimerY =
                ObbyConfig.get().stormTimerY;

        StormSettings.stormTimerScale =
                ObbyConfig.get().stormTimerScale;

        StormSettings.stormTimerColor =
                ObbyConfig.get().stormTimerColor;

        StormSettings.stormDeathTimerX =
                ObbyConfig.get().stormDeathTimerX;

        StormSettings.stormDeathTimerY =
                ObbyConfig.get().stormDeathTimerY;

        StormSettings.stormDeathTimerScale =
                ObbyConfig.get().stormDeathTimerScale;

        StormSettings.stormDeathTimerColor =
                ObbyConfig.get().stormDeathTimerColor;

        StormSettings.stormTickTimer =
                ObbyConfig.get().stormTickTimerEnabled;

        StormSettings.tickDownFrom5 =
                ObbyConfig.get().stormTickDownFrom5;

        StormSettings.firstDeathTime =
                ObbyConfig.get().stormFirstDeathTime;

        StormSettings.spiritMaskWarning =
                ObbyConfig.get().stormSpiritMaskWarning;

        StormSettings.stormLbEnabled =
                ObbyConfig.get().stormLbEnabled;

        StormSettings.stormLbTickOffset =
                ObbyConfig.get().stormLbTickOffset;

        StormSettings.stormLbColor =
                ObbyConfig.get().stormLbColor;

        StormSettings.stormLbX =
                ObbyConfig.get().stormLbX;

        StormSettings.stormLbY =
                ObbyConfig.get().stormLbY;

        StormSettings.stormLbScale =
                ObbyConfig.get().stormLbScale;

        // =========================
        // TERMINATOR OVERLAY
        // =========================

        TerminatorOverlaySettings.color =
                ObbyConfig.get().terminatorOverlayColor;

        // =========================
        // EXPLOSIVE ARROW SETTINGS
        // =========================

        ExplosiveArrowSettings.enabled =
                ObbyConfig.get().explosiveArrowEnabled;

        ExplosiveArrowSettings.damageTrackerHudEnabled =
                ObbyConfig.get().explosiveArrowDamageTrackerHudEnabled;

        ExplosiveArrowSettings.damageTrackerColor =
                ObbyConfig.get().explosiveArrowDamageTrackerColor;

        ExplosiveArrowSettings.damageTrackerX =
                ObbyConfig.get().explosiveArrowDamageTrackerX;

        ExplosiveArrowSettings.damageTrackerY =
                ObbyConfig.get().explosiveArrowDamageTrackerY;

        ExplosiveArrowSettings.damageTrackerScale =
                ObbyConfig.get().explosiveArrowDamageTrackerScale;

        // =========================
        // CLICK PROT DISPLAY
        // =========================

        TerminalClickTimerSettings.clickDelayMs =
                ObbyConfig.get().terminalClickDelayMs;

        TerminalClickTimerSettings.color =
                ObbyConfig.get().terminalClickTimerColor;

        TerminalClickTimerSettings.x =
                ObbyConfig.get().terminalClickTimerX;

        TerminalClickTimerSettings.y =
                ObbyConfig.get().terminalClickTimerY;

        TerminalClickTimerSettings.scale =
                ObbyConfig.get().terminalClickTimerScale;

        // =========================
        // DUNGEON RUN TRACKER
        // =========================

        DungeonRunTrackerSettings.enabled =
                ObbyConfig.get().dungeonRunTrackerEnabled;

        DungeonRunTrackerSettings.x =
                ObbyConfig.get().dungeonRunTrackerX;

        DungeonRunTrackerSettings.y =
                ObbyConfig.get().dungeonRunTrackerY;

        DungeonRunTrackerSettings.scale =
                ObbyConfig.get().dungeonRunTrackerScale;

        DungeonRunTrackerSettings.color =
                ObbyConfig.get().dungeonRunTrackerColor;

        // =========================
        // ITEM HIGHLIGHT
        // =========================

        DungeonItemHighlightSettings.enabled =
                ObbyConfig.get().dungeonItemHighlightEnabled;

        // =========================
        // CHAT CLEANER
        // =========================

        ChatRules.loadConfig();

        // =========================
        // COMMANDS
        // =========================

        ObbyAddonsCommands.register();

        // =========================
        // RUNTIME / HUD SYSTEMS
        // =========================

        ServerTickTracker.init();

        StormTickTimer.init();
        StormLastBreathTimer.init();

        ExplosiveArrowDamageTracker.init();

        DungeonLocationTracker.init();
        DungeonBossTracker.init();
        DungeonItemHighlight.init();
        DungeonItemHighlightRenderer.init();

        /*
         * Run tracking stays active even if the HUD
         * is toggled off.
         *
         * We still need this data for:
         * - Croesus
         * - Kismets
         * - chest history
         * - /oa loot
         */
        DungeonRunTracker.init();
        DungeonRunTrackerHud.init();

        /*
         * Start the live Athen price cache.
         *
         * It fetches immediately, then refreshes
         * automatically every 10 minutes.
         */
        AthenPriceProvider.start();

        // =========================
        // FEATURES
        // =========================

        ClientLifecycleEvents.CLIENT_STARTED.register(
                client -> {

                    ChatCleanerFeature chatCleaner =
                            new ChatCleanerFeature();

                    FeatureManager.register(
                            chatCleaner
                    );

                    FeatureManager.register(
                            new StormFeatures()
                    );

                    FeatureManager.register(
                            new ExplosiveArrowFeature()
                    );

                    FeatureManager.register(
                            new TerminatorOverlayFeature()
                    );

                    FeatureManager.register(
                            new TerminalClickTimerFeature()
                    );

                    FeatureManager.register(
                            new DungeonRunTrackerFeature()
                    );

                    FeatureManager.register(
                            new DungeonItemHighlightFeature()
                    );

                    // Apply saved Chat Cleaner state
                    chatCleaner.setEnabled(
                            ObbyConfig.get().chatCleanerEnabled
                    );

                    System.out.println(
                            "[ObbyAddons] Client initialized."
                    );
                }
        );
    }
}