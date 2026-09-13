package me.obbyaddons.client.features.general.chat;

import me.obbyaddons.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessage;

public final class ChatCleanerFeature extends Feature {

    public ChatCleanerFeature() {
        super("Chat Cleaner", true);
    }

    @Override
    public void initialize() {
        Minecraft.getInstance()
                .gui
                .getChat()
                .setVisibleMessageFilter(this::shouldShowMessage);

        System.out.println("[ObbyAddons] Chat Cleaner initialized.");
    }

    private boolean shouldShowMessage(GuiMessage message) {
        if (!isEnabled()) {
            return true;
        }

        String text = message.content().getString();

        boolean shouldHide = ChatRules.shouldHide(text);

        if (shouldHide) {
            System.out.println("[ObbyAddons] Hiding chat message: " + text);
        }

        return !shouldHide;
    }
}