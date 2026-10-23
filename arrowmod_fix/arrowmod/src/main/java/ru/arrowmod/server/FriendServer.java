package ru.arrowmod.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import ru.arrowmod.ArrowMod;
import ru.arrowmod.network.Packets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FriendServer {
    private static final Map<UUID, Set<UUID>> FRIENDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<UUID>> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> ENABLED = new ConcurrentHashMap<>();
    private static final Map<UUID, String> STATUS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> PINGS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> COLORS = new ConcurrentHashMap<>();
    private static final int[] PALETTE = {0xFF5555,0x55AAFF,0x55FF55,0xFFAA00,0xD855FF,0x55FFFF};
    private static UUID find(MinecraftServer server, String name) { ServerPlayerEntity p=server.getPlayerManager().getPlayer(name); return p==null?null:p.getUuid(); }
    public static String request(ServerPlayerEntity from, String name) {
        ServerPlayerEntity to=from.getServer().getPlayerManager().getPlayer(name);
        if(to==null) return "Игрок не найден онлайн или у него нет Arrow Friends.";
        if(to.getUuid().equals(from.getUuid())) return "Нельзя добавить самого себя.";
        if(FRIENDS.getOrDefault(from.getUuid(),Set.of()).contains(to.getUuid())) return "Этот игрок уже в списке.";
        PENDING.computeIfAbsent(to.getUuid(),k->ConcurrentHashMap.newKeySet()).add(from.getUuid());
        sendNotice(to,"Заявка в друзья от "+from.getName().getString()+". Напиши /arrow accept "+from.getName().getString());
        return "Заявка отправлена игроку "+name+".";
    }
    public static String accept(ServerPlayerEntity to, String name) {
        UUID from=find(to.getServer(),name); if(from==null) return "Игрок не найден онлайн.";
        Set<UUID> pending=PENDING.get(to.getUuid()); if(pending==null || !pending.remove(from)) return "От этого игрока нет ожидающей заявки.";
        FRIENDS.computeIfAbsent(to.getUuid(),k->ConcurrentHashMap.newKeySet()).add(from);
        FRIENDS.computeIfAbsent(from,k->ConcurrentHashMap.newKeySet()).add(to.getUuid());
        sendNotice(to,"Теперь вы отслеживаете друг друга: "+name+".");
        ServerPlayerEntity other=to.getServer().getPlayerManager().getPlayer(from); if(other!=null) sendNotice(other,"Заявка принята игроком "+to.getName().getString()+".");
        return "Заявка принята. Вы добавлены друг у друга.";
    }
    public static String remove(ServerPlayerEntity who, String name) {
        UUID other=find(who.getServer(),name); if(other==null) return "Игрок не найден онлайн. Удаление возможно, когда он онлайн.";
        Set<UUID> a=FRIENDS.get(who.getUuid()), b=FRIENDS.get(other); if(a!=null)a.remove(other); if(b!=null)b.remove(who.getUuid());
        ServerPlayerEntity p=who.getServer().getPlayerManager().getPlayer(other); if(p!=null) sendNotice(p,who.getName().getString()+" удалил связь. Стрелка удалена у обоих.");
        return "Связь удалена у обоих игроков.";
    }
    public static void setEnabled(UUID id, boolean on) { ENABLED.put(id,on); }
    public static void setStatus(UUID id, String status) { STATUS.put(id,status); }
    public static void ping(ServerPlayerEntity from) {
        long until=from.getServer().getTicks()+100; PINGS.put(from.getUuid(),until);
        for(UUID id:FRIENDS.getOrDefault(from.getUuid(),Set.of())) { ServerPlayerEntity p=from.getServer().getPlayerManager().getPlayer(id); if(p!=null && p.getWorld()==from.getWorld()) { PacketByteBuf b=PacketByteBufs.create(); b.writeUuid(from.getUuid()); b.writeString(from.getName().getString()); b.writeDouble(from.getX()); b.writeDouble(from.getY()); b.writeDouble(from.getZ()); ServerPlayNetworking.send(p,Packets.PING,b); } }
    }
    private static void sendNotice(ServerPlayerEntity p,String text) { PacketByteBuf b=PacketByteBufs.create(); b.writeString(text); ServerPlayNetworking.send(p,Packets.NOTICE,b); }
    public static void disconnect(UUID id) { ENABLED.remove(id); STATUS.remove(id); }
    public static void broadcastPositions(MinecraftServer server) {
        for(ServerPlayerEntity viewer:server.getPlayerManager().getPlayerList()) {
            if(!ENABLED.getOrDefault(viewer.getUuid(),true)) continue;
            for(UUID friend:FRIENDS.getOrDefault(viewer.getUuid(),Set.of())) {
                ServerPlayerEntity target=server.getPlayerManager().getPlayer(friend);
                if(target==null || target.getWorld()!=viewer.getWorld() || !ENABLED.getOrDefault(friend,true)) continue;
                int color=COLORS.computeIfAbsent(friend,u->PALETTE[Math.floorMod(u.hashCode(),PALETTE.length)]);
                PacketByteBuf b=PacketByteBufs.create(); b.writeUuid(friend); b.writeString(target.getName().getString()); b.writeDouble(target.getX()); b.writeDouble(target.getY()); b.writeDouble(target.getZ()); b.writeString(target.getWorld().getRegistryKey().getValue().toString()); b.writeInt(color); b.writeString(STATUS.getOrDefault(friend,"")); b.writeBoolean(PINGS.getOrDefault(friend,0L)>server.getTicks());
                ServerPlayNetworking.send(viewer,Packets.POSITION,b);
            }
        }
    }
}
