package me.obbyaddons.client.mixin;

import me.obbyaddons.client.features.dungeon.tracker.DungeonCroesusTracker;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(
            method = "slotClicked",
            at = @At("HEAD")
    )
    private void obbyaddons$trackCroesusClick(
            Slot slot,
            int slotId,
            int buttonNum,
            ContainerInput containerInput,
            CallbackInfo ci
    ) {
        if (slot == null) {
            return;
        }

        DungeonCroesusTracker.selectRunForSlot(
                (AbstractContainerScreen<?>) (Object) this,
                slotId
        );
    }
}