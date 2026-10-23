package ru.arrowmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.arrowmod.config.ArrowConfig;
import ru.arrowmod.network.Packets;
import java.util.*;

public class ArrowModClient implements ClientModInitializer {
    public static ArrowConfig CONFIG;
    public static final Map<UUID, FriendMarker> FRIENDS = new LinkedHashMap<>();
    public static final Map<UUID, PingMarker> PINGS = new HashMap<>();
    private static KeyBinding toggleKey, menuKey, pingKey;
    private static int ticks=0;
    public record FriendMarker(String name,double x,double y,double z,String dimension,int color,String status,boolean ping) {}
    public record PingMarker(String name,double x,double y,double z,long expires) {}
    @Override public void onInitializeClient() {
        CONFIG=ArrowConfig.load();
        toggleKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arrowmod.toggle",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_K,"category.arrowmod"));
        menuKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arrowmod.menu",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H,"category.arrowmod"));
        pingKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arrowmod.ping",InputUtil.Type.MOUSE,2,"category.arrowmod"));
        ClientPlayNetworking.registerGlobalReceiver(Packets.POSITION,(client,handler,buf,response)->{ UUID id=buf.readUuid(); String name=buf.readString(64); double x=buf.readDouble(),y=buf.readDouble(),z=buf.readDouble(); String dim=buf.readString(128); int color=buf.readInt(); String status=buf.readString(32); boolean ping=buf.readBoolean(); client.execute(()->FRIENDS.put(id,new FriendMarker(name,x,y,z,dim,color,status,ping))); });
        ClientPlayNetworking.registerGlobalReceiver(Packets.PING,(client,handler,buf,response)->{ UUID id=buf.readUuid(); String name=buf.readString(64); double x=buf.readDouble(),y=buf.readDouble(),z=buf.readDouble(); client.execute(()->PINGS.put(id,new PingMarker(name,x,y,z,client.world==null?0:client.world.getTime()+100))); });
        ClientPlayNetworking.registerGlobalReceiver(Packets.NOTICE,(client,handler,buf,response)->{ String msg=buf.readString(256); client.execute(()->{if(client.player!=null)client.player.sendMessage(Text.literal("§b[Arrow Friends] §f"+msg),false);}); });
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(toggleKey.wasPressed()){CONFIG.enabled=!CONFIG.enabled;CONFIG.save();Packets.sendBoolean(Packets.TOGGLE,CONFIG.enabled);if(client.player!=null)client.player.sendMessage(Text.literal("Arrow Friends: "+(CONFIG.enabled?"§aвключено":"§cвыключено")),true);}
            while(menuKey.wasPressed()) if(client.currentScreen==null) client.setScreen(new ArrowSettingsScreen());
            while(pingKey.wasPressed()) Packets.sendEmpty(Packets.PING);
            if(client.options.attackKey.wasPressed() && client.currentScreen==null) { /* vanilla attack remains untouched */ }
            if(++ticks%20==0 && client.player!=null) { String s=""; if(client.currentScreen instanceof ChatScreen)s="CHAT"; else if(client.currentScreen!=null)s="INVENTORY"; Packets.sendString(Packets.STATUS,s); }
            if(client.world!=null) PINGS.values().removeIf(p->p.expires()<client.world.getTime());
        });
        HudRenderCallback.EVENT.register((draw,tickDelta)->ArrowHud.render(draw));
    }
}
