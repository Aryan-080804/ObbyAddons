package me.obbyaddons.client.features.dungeon.shared;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.ClientLevel;
import java.util.regex.Pattern;

public final class DungeonBossTracker {

    private static boolean inBoss = false;
    private static ClientLevel trackedWorld = null;

    private static final Pattern DUNGEON_ENTRY =
            Pattern.compile(
                    "entered\\s+(?:MM\\s+)?The Catacombs,\\s*Floor\\s+[IVX]+!",
                    Pattern.CASE_INSENSITIVE
            );

    private DungeonBossTracker() {
    }

    public static void init() {

        ClientReceiveMessageEvents.GAME.register(
                (message, overlay) -> onMessage(message)
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (client.level != trackedWorld) {
                        trackedWorld = client.level;
                        inBoss = false;
                    }

                    if (!DungeonLocationTracker.inDungeon()) {
                        inBoss = false;
                    }
                }
        );
    }

    private static void onMessage(Component message) {

        if (message == null) {
            return;
        }

        if (!DungeonLocationTracker.inDungeon()) {
            return;
        }

        String text =
                message.getString()
                        .trim();

        if (DUNGEON_ENTRY.matcher(text).find()) {
            inBoss = false;
            return;
        }

        if (text.isEmpty()) {
            return;
        }

        /*
         * Blade does NOT enter boss phase when The Watcher talks.
         *
         * Its phase system enters boss once the actual floor boss
         * introduction split completes.
         *
         * All normal dungeon boss introductions are [BOSS] messages,
         * while Blood Room dialogue is from The Watcher.
         */
        if (
                text.startsWith("[BOSS]")
                        && !text.startsWith("[BOSS] The Watcher:")
        ) {
            inBoss = true;
        }
    }

    public static boolean inBoss() {
        return inBoss;
    }
}