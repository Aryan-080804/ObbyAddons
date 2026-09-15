package me.obbyaddons.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import me.obbyaddons.client.features.dungeon.tracker.PriceProvider;
import me.obbyaddons.client.gui.DungeonLootScreen;
import me.obbyaddons.client.gui.HudEditorScreen;
import me.obbyaddons.client.gui.ObbyAddonsScreen;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ObbyAddonsCommands {

    private ObbyAddonsCommands() {
    }

    public static void register() {

        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext) -> {

                    dispatcher.register(
                            buildRootCommand("obbyaddons")
                    );

                    dispatcher.register(
                            buildRootCommand("oa")
                    );

                    dispatcher.register(
                            buildRootCommand("ob")
                    );
                }
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRootCommand(
        String name
    ) {

        return ClientCommands.literal(name)

                .executes(
                        context ->
                                openMainScreen()
                )

                .then(
                        ClientCommands.literal("hud")
                                .executes(
                                        context ->
                                                openHudEditor()
                                )
                )

                .then(
                        ClientCommands.literal("loot")
                                .executes(
                                        context ->
                                                openLootScreen()
                                )
                )

                .then(
                        ClientCommands.literal("test")
                                .executes(
                                        context ->
                                                testCommand()
                                )
                )

                .then(
                        ClientCommands.literal("calc")
                                .then(
                                        ClientCommands.argument(
                                                        "expression",
                                                        StringArgumentType.greedyString()
                                                )
                                                .executes(
                                                        context ->
                                                                calculateCommand(
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "expression"
                                                                        )
                                                                )
                                                )
                                )
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

        PriceProvider.refreshNow();

        minecraft.execute(
                () ->
                        minecraft.setScreen(
                                new DungeonLootScreen()
                        )
        );

        return 1;
    }

    // =========================
    // TEST COMMAND
    // =========================

    private static int testCommand() {

        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.execute(
                () ->
                        minecraft.gui
                                .getChat()
                                .addClientSystemMessage(
                                        Component.literal(
                                                "[ObbyAddons] Commands are working!"
                                        )
                                )
        );

        return 1;
    }

    // =========================
    // CALCULATOR COMMAND
    // =========================

    private static int calculateCommand(
            String expression
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        try {

            double result =
                    MathExpressionParser.evaluate(
                            expression
                    );

            String formattedResult =
                    formatNumber(
                            result
                    );

            minecraft.execute(
                    () ->
                            minecraft.gui
                                    .getChat()
                                    .addClientSystemMessage(
                                            Component.literal(
                                                    "[ObbyAddons] "
                                                            + expression
                                                            + " = "
                                                            + formattedResult
                                            )
                                    )
            );

            return 1;

        } catch (IllegalArgumentException exception) {

            minecraft.execute(
                    () ->
                            minecraft.gui
                                    .getChat()
                                    .addClientSystemMessage(
                                            Component.literal(
                                                    "[ObbyAddons] Invalid calculation."
                                            )
                                    )
            );

            return 0;
        }
    }

    private static String formatNumber(
            double value
    ) {

        if (value == Math.rint(value)) {
            return Long.toString(
                    (long) value
            );
        }

        return Double.toString(
                value
        );
    }
}