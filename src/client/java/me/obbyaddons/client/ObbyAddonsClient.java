package me.obbyaddons.client;

import me.obbyaddons.client.command.ObbyAddonsCommands;
import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.client.features.chat.ChatCleanerFeature;
import me.obbyaddons.client.features.chat.ChatRules;
import me.obbyaddons.feature.FeatureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import me.obbyaddons.client.features.dungeon.StormFeatures;
import me.obbyaddons.client.features.dungeon.StormTickTimer;
import me.obbyaddons.client.features.dungeon.StormSettings;
import me.obbyaddons.client.util.ServerTickTracker;
import me.obbyaddons.client.features.dungeon.StormLastBreathTimer;

public class ObbyAddonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Load saved settings
        ObbyConfig.load();
        StormSettings.stormTimerX = ObbyConfig.get().stormTimerX;
        StormSettings.stormTimerY = ObbyConfig.get().stormTimerY;
        StormSettings.stormTimerScale = ObbyConfig.get().stormTimerScale;
		StormSettings.stormTimerColor = ObbyConfig.get().stormTimerColor;

		StormSettings.stormDeathTimerX = ObbyConfig.get().stormDeathTimerX;
		StormSettings.stormDeathTimerY = ObbyConfig.get().stormDeathTimerY;
		StormSettings.stormDeathTimerScale = ObbyConfig.get().stormDeathTimerScale;
		StormSettings.stormDeathTimerColor = ObbyConfig.get().stormDeathTimerColor;
		
		StormSettings.stormTickTimer = ObbyConfig.get().stormTickTimerEnabled;
		StormSettings.tickDownFrom5 = ObbyConfig.get().stormTickDownFrom5;
		StormSettings.firstDeathTime = ObbyConfig.get().stormFirstDeathTime;

		StormSettings.spiritMaskWarning = ObbyConfig.get().stormSpiritMaskWarning;

		StormSettings.stormLbEnabled = ObbyConfig.get().stormLbEnabled;
		StormSettings.stormLbTickOffset = ObbyConfig.get().stormLbTickOffset;
		StormSettings.stormLbColor = ObbyConfig.get().stormLbColor;
		StormSettings.stormLbX = ObbyConfig.get().stormLbX;
		StormSettings.stormLbY = ObbyConfig.get().stormLbY;

StormSettings.stormLbScale =
        ObbyConfig.get().stormLbScale;
        ChatRules.loadConfig();

        // Register client commands like /oa and /obbyaddons
        ObbyAddonsCommands.register();

		ServerTickTracker.init();
		StormTickTimer.init();
		StormLastBreathTimer.init();

        // Wait until Minecraft's GUI is fully initialized
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {

            ChatCleanerFeature chatCleaner = new ChatCleanerFeature();

            FeatureManager.register(chatCleaner);
			FeatureManager.register(new StormFeatures());

            // Apply saved Chat Cleaner state
            chatCleaner.setEnabled(
                    ObbyConfig.get().chatCleanerEnabled
            );

            System.out.println("[ObbyAddons] Client initialized.");
        });
    }
}