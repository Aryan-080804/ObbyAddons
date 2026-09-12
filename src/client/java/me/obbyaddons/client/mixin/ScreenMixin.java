package me.obbyaddons.client.mixin;

import me.obbyaddons.client.features.dungeon.TerminalClickTimer;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRewardScreenTracker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(
            method = "extractRenderStateWithTooltipAndSubtitles",
            at = @At("HEAD")
    )
    private void obbyaddons$renderTerminalClickTimer(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        TerminalClickTimer.render(
                (Screen) (Object) this,
                graphics
        );

        DungeonRewardScreenTracker.onScreenRendered(
                (Screen) (Object) this
        );
    }
}