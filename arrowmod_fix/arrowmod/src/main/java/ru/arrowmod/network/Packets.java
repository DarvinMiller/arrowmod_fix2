package ru.arrowmod.network;

import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import ru.arrowmod.ArrowMod;
import ru.arrowmod.server.FriendServer;

public final class Packets {
    public static final Identifier REQUEST = id("request"), ACCEPT = id("accept"), REMOVE = id("remove"), TOGGLE = id("toggle"), PING = id("ping"), POSITION = id("position"), NOTICE = id("notice"), STATUS = id("status");
    private static Identifier id(String path) { return new Identifier(ArrowMod.MOD_ID, path); }
    private static void notice(ServerPlayerEntity p, String message) { PacketByteBuf b = PacketByteBufs.create(); b.writeString(message); ServerPlayNetworking.send(p, NOTICE, b); }
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST, (server, player, handler, buf, response) -> { String name=buf.readString(64); server.execute(() -> { String r=FriendServer.request(player, name); notice(player,r); }); });
        ServerPlayNetworking.registerGlobalReceiver(ACCEPT, (server, player, handler, buf, response) -> { String name=buf.readString(64); server.execute(() -> { String r=FriendServer.accept(player,name); notice(player,r); }); });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE, (server, player, handler, buf, response) -> { String name=buf.readString(64); server.execute(() -> { String r=FriendServer.remove(player,name); notice(player,r); }); });
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE, (server, player, handler, buf, response) -> { boolean on=buf.readBoolean(); server.execute(() -> FriendServer.setEnabled(player.getUuid(),on)); });
        ServerPlayNetworking.registerGlobalReceiver(PING, (server, player, handler, buf, response) -> { server.execute(() -> FriendServer.ping(player)); });
        ServerPlayNetworking.registerGlobalReceiver(STATUS, (server, player, handler, buf, response) -> { String status=buf.readString(32); server.execute(() -> FriendServer.setStatus(player.getUuid(),status)); });
        ServerTickEvents.END_SERVER_TICK.register(server -> { if (server.getTicks() % 5 == 0) FriendServer.broadcastPositions(server); });
    }
    public static void sendString(Identifier id, String value) { if (net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(id)) { PacketByteBuf b=PacketByteBufs.create(); b.writeString(value); net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(id,b); } }
    public static void sendBoolean(Identifier id, boolean value) { if (net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(id)) { PacketByteBuf b=PacketByteBufs.create(); b.writeBoolean(value); net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(id,b); } }
    public static void sendEmpty(Identifier id) { if (net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(id)) net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(id,PacketByteBufs.empty()); }
}
