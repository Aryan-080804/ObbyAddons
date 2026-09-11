package me.obbyaddons.client.features.chat;

import me.obbyaddons.client.config.ObbyConfig;

public final class ChatRules {

    private static boolean dungeonSpamEnabled;
    private static boolean m7BossSpamEnabled;

    private static final ChatMessageList DUNGEON_SPAM =
            ChatMessageList.load("/obbyaddons/chat/dungeon_spam.txt");

    private static final ChatMessageList M7_BOSS_SPAM =
            ChatMessageList.load("/obbyaddons/chat/m7_boss_spam.txt");

    private ChatRules() {
    }

    public static void loadConfig() {
        dungeonSpamEnabled = ObbyConfig.get().dungeonSpamEnabled;
        m7BossSpamEnabled = ObbyConfig.get().m7BossSpamEnabled;
    }

    public static boolean isDungeonSpamEnabled() {
        return dungeonSpamEnabled;
    }

    public static void setDungeonSpamEnabled(boolean enabled) {
        dungeonSpamEnabled = enabled;

        ObbyConfig.get().dungeonSpamEnabled = enabled;
        ObbyConfig.save();
    }

    public static boolean isM7BossSpamEnabled() {
        return m7BossSpamEnabled;
    }

    public static void setM7BossSpamEnabled(boolean enabled) {
        m7BossSpamEnabled = enabled;

        ObbyConfig.get().m7BossSpamEnabled = enabled;
        ObbyConfig.save();
    }

    public static boolean shouldHide(String message) {
        if (dungeonSpamEnabled && DUNGEON_SPAM.matches(message)) {
            return true;
        }

        if (m7BossSpamEnabled && M7_BOSS_SPAM.matches(message)) {
            return true;
        }

        return false;
    }
}