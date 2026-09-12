package me.obbyaddons.client.mixin;

import me.obbyaddons.client.features.dungeon.DungeonItemHighlight;
import me.obbyaddons.client.features.dungeon.DungeonItemHighlightSettings;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "shouldRender",
            at = @At("TAIL"),
            cancellable = true
    )
    private <E extends Entity> void obbyaddons$hideHighlightedItem(
            E entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (
                entity instanceof ItemEntity item
                        && DungeonItemHighlightSettings.enabled
                        && DungeonItemHighlight.hideItem(item)
        ) {
            cir.setReturnValue(false);
        }
    }
}