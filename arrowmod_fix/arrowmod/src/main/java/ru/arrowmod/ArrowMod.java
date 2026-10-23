package ru.arrowmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import ru.arrowmod.network.Packets;
import ru.arrowmod.server.FriendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrowMod implements ModInitializer {
    public static final String MOD_ID = "arrowmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override public void onInitialize() {
        Packets.registerServerReceivers();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FriendServer.disconnect(handler.player.getUuid()));
        LOGGER.info("Arrow Friends loaded. The server must also have this mod installed.");
    }
}
