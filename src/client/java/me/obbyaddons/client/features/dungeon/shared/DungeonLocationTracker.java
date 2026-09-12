package me.obbyaddons.client.features.dungeon.shared;

import net.hypixel.data.type.ServerType;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;

public final class DungeonLocationTracker {

    private static boolean inSkyblock = false;
    private static boolean inDungeon = false;

    private DungeonLocationTracker() {
    }

    public static void init() {
        HypixelModAPI api = HypixelModAPI.getInstance();

        api.createHandler(
                ClientboundLocationPacket.class,
                packet -> packet.getMap().ifPresent(
                        locationName -> {
                            if (packet.getServerType().isPresent()) {
                                ServerType serverType =
                                        packet.getServerType().get();

                                inSkyblock =
                                        serverType.getName()
                                                .equals("SkyBlock");
                            }

                            inDungeon =
                                    inSkyblock
                                            && locationName.equalsIgnoreCase(
                                            "Dungeon"
                                    );
                        }
                )
        );

        api.subscribeToEventPacket(
                ClientboundLocationPacket.class
        );
    }

    public static boolean inDungeon() {
        return inSkyblock && inDungeon;
    }

    public static boolean inSkyblock() {
        return inSkyblock;
    }
}