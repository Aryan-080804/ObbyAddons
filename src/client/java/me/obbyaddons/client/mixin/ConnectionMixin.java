package me.obbyaddons.client.mixin;

import me.obbyaddons.client.util.ServerTickTracker;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"
            )
    )
    private void obbyaddons$onPacket(
            ChannelHandlerContext context,
            Packet<?> packet,
            CallbackInfo ci
    ) {
        if (packet instanceof ClientboundPingPacket pingPacket) {

            // Blade Addons ignores ID 0 because Hypixel also uses
            // those ping packets for unrelated inventory/admin activity.
            if (pingPacket.getId() == 0) {
                return;
            }

            ServerTickTracker.fireServerTick();
        }
    }
}   