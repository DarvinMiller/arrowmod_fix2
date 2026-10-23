package ru.arrowmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import ru.arrowmod.client.ArrowModClient.FriendMarker;
import java.util.*;

public final class ArrowHud {
    private static void drawCentered(MinecraftClient mc, MatrixStack matrices, Text text, int x, int y, int color) {
        mc.textRenderer.drawWithShadow(matrices, text, x - mc.textRenderer.getWidth(text) / 2.0f, y, color);
    }
    public static void render(MatrixStack ctx) {
        MinecraftClient mc=MinecraftClient.getInstance(); if(mc.player==null||mc.world==null||!ArrowModClient.CONFIG.enabled||mc.options.hudHidden)return;
        int w=mc.getWindow().getScaledWidth(), h=mc.getWindow().getScaledHeight();
        List<FriendMarker> list=new ArrayList<>(ArrowModClient.FRIENDS.values()); list.sort(Comparator.comparingDouble(f->mc.player.squaredDistanceTo(f.x(),f.y(),f.z())));
        int row=0;
        for(FriendMarker f:list) {
            if(!f.dimension().equals(mc.world.getRegistryKey().getValue().toString())) continue;
            double dx=f.x()-mc.player.getX(), dz=f.z()-mc.player.getZ(), dist=Math.sqrt(dx*dx+(f.y()-mc.player.getY())*(f.y()-mc.player.getY())+dz*dz);
            double bearing=Math.toDegrees(Math.atan2(dz,dx))-90.0-mc.player.getYaw();
            float angle=(float)Math.toRadians(bearing); int cx=w/2+(int)(Math.sin(angle)*Math.min(140,w/3)); int cy=ArrowModClient.CONFIG.position.equals("CENTER")?h/2-35:(ArrowModClient.CONFIG.position.equals("CORNER")?35:28);
            String arrow=angle>Math.PI/2||angle< -Math.PI/2?"◀":"▶"; if(ArrowModClient.CONFIG.position.equals("TOP")) cx=MathHelper.clamp(cx,50,w-50); else if(ArrowModClient.CONFIG.position.equals("CORNER")) {cx=30;cy=35+row*32;}
            int color=dist<ArrowModClient.CONFIG.closeDistance?0x55FF55:dist<ArrowModClient.CONFIG.mediumDistance?0xFFFF55:0xFF5555;
            if(ArrowModClient.CONFIG.position.equals("TOP")) {drawCentered(mc,ctx,Text.literal(arrow+" "+f.name()),cx,cy+row*30,f.color()); if(ArrowModClient.CONFIG.showDistance)drawCentered(mc,ctx,Text.literal(Math.round(dist)+"m"),cx,cy+11+row*30,color);}
            else {drawCentered(mc,ctx,Text.literal(arrow+" "+f.name()),cx,cy,f.color());if(ArrowModClient.CONFIG.showDistance)drawCentered(mc,ctx,Text.literal(Math.round(dist)+"m"),cx,cy+10,color);}
            if(ArrowModClient.CONFIG.showActivity&&!f.status().isEmpty())drawCentered(mc,ctx,Text.literal(f.status().equals("CHAT")?"💬":f.status().equals("INVENTORY")?"📦":""),cx,cy+21,0xFFFFFF);
            row++;
        }
        for(ArrowModClient.PingMarker p:ArrowModClient.PINGS.values()) { double d=mc.player.distanceTo(net.minecraft.util.math.Vec3d.ofCenter(new net.minecraft.util.math.BlockPos(p.x(),p.y(),p.z()))); drawCentered(mc,ctx,Text.literal("✦ "+p.name()+" — "+Math.round(d)+"m"),w/2,h/2+40,0x55FFFF); }
    }
}
