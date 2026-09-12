package me.obbyaddons.client.command;

import me.obbyaddons.client.gui.DungeonLootScreen;
import me.obbyaddons.client.gui.HudEditorScreen;
import me.obbyaddons.client.gui.ObbyAddonsScreen;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;

import net.minecraft.client.Minecraft;

public final class ObbyAddonsCommands {

    private ObbyAddonsCommands() {
    }

    public static void register() {

        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext) -> {

                    dispatcher.register(
                            ClientCommands.literal(
                                            "obbyaddons"
                                    )
                                    .executes(
                                            context ->
                                                    openMainScreen()
                                    )
                                    .then(
                                            ClientCommands.literal(
                                                            "hud"
                                                    )
                                                    .executes(
                                                            context ->
                                                                    openHudEditor()
                                                    )
                                    )
                                    .then(
                                            ClientCommands.literal(
                                                            "loot"
                                                    )
                                                    .executes(
                                                            context ->
                                                                    openLootScreen()
                                                    )
                                    )
                    );

                    dispatcher.register(
                            ClientCommands.literal(
                                            "oa"
                                    )
                                    .executes(
                                            context ->
                                                    openMainScreen()
                                    )
                                    .then(
                                            ClientCommands.literal(
                                                            "hud"
                                                    )
                                                    .executes(
                                                            context ->
                                                                    openHudEditor()
                                                    )
                                    )
                                    .then(
                                            ClientCommands.literal(
                                                            "loot"
                                                    )
                                                    .executes(
                                                            context ->
                                                                    openLootScreen()
                                                    )
                                    )
                    );
                }
        );
    }

    // =========================
    // MAIN SCREEN
    // =========================

    private static int openMainScreen() {

        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.execute(
                () ->
                        minecraft.setScreen(
                                new ObbyAddonsScreen()
                        )
        );

        return 1;
    }

    // =========================
    // HUD EDITOR
    // =========================

    private static int openHudEditor() {

        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.execute(
                () ->
                        minecraft.setScreen(
                                new HudEditorScreen()
                        )
        );

        return 1;
    }

    // =========================
    // DUNGEON LOOT
    // =========================

    private static int openLootScreen() {

        Minecraft minecraft =
                Minecraft.getInstance();

        /*
         * Ask Athen for a fresh refresh when the
         * user opens the loot screen.
         *
         * The refresh happens asynchronously, so
         * opening the GUI is not blocked.
         */
        me.obbyaddons.client.features.dungeon.tracker
                .AthenPriceProvider
                .refreshNow();

        minecraft.execute(
                () ->
                        minecraft.setScreen(
                                new DungeonLootScreen()
                        )
        );

        return 1;
    }
}