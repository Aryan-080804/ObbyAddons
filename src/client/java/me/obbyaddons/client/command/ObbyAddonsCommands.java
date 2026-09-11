package me.obbyaddons.client.command;

import me.obbyaddons.client.gui.HudEditorScreen;
import me.obbyaddons.client.gui.ObbyAddonsScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;

public final class ObbyAddonsCommands {

    private ObbyAddonsCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {

            dispatcher.register(
                    ClientCommands.literal("obbyaddons")
                            .executes(context -> openScreen())
                            .then(
                                    ClientCommands.literal("hud")
                                            .executes(context -> openHudEditor())
                            )
            );

            dispatcher.register(
                    ClientCommands.literal("oa")
                            .executes(context -> openScreen())
                            .then(
                                    ClientCommands.literal("hud")
                                            .executes(context -> openHudEditor())
                            )
            );
        });
    }

    private static int openScreen() {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.execute(() -> {
            minecraft.setScreen(new ObbyAddonsScreen());
        });

        return 1;
    }

    private static int openHudEditor() {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.execute(() -> {
            minecraft.setScreen(new HudEditorScreen());
        });

        return 1;
    }
}