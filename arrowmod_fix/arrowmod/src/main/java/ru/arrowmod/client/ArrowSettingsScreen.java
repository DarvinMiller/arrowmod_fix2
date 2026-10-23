package ru.arrowmod.client;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import ru.arrowmod.network.Packets;

public class ArrowSettingsScreen extends Screen {
    private TextFieldWidget playerName;
    public ArrowSettingsScreen(){super(Text.literal("Arrow Friends · Настройки"));}
    @Override protected void init(){
        int cx=width/2; int y=42;
        addDrawableChild(ButtonWidget.builder(Text.literal("Стрелки: "+(ArrowModClient.CONFIG.enabled?"ВКЛ":"ВЫКЛ")),b->{ArrowModClient.CONFIG.enabled=!ArrowModClient.CONFIG.enabled;Packets.sendBoolean(Packets.TOGGLE,ArrowModClient.CONFIG.enabled);b.setMessage(Text.literal("Стрелки: "+(ArrowModClient.CONFIG.enabled?"ВКЛ":"ВЫКЛ")));ArrowModClient.CONFIG.save();}).dimensions(cx-150,y,300,22).build()); y+=27;
        addDrawableChild(ButtonWidget.builder(Text.literal("Расстояние: "+(ArrowModClient.CONFIG.showDistance?"ВКЛ":"ВЫКЛ")),b->{ArrowModClient.CONFIG.showDistance=!ArrowModClient.CONFIG.showDistance;b.setMessage(Text.literal("Расстояние: "+(ArrowModClient.CONFIG.showDistance?"ВКЛ":"ВЫКЛ")));ArrowModClient.CONFIG.save();}).dimensions(cx-150,y,145,22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Активность: "+(ArrowModClient.CONFIG.showActivity?"ВКЛ":"ВЫКЛ")),b->{ArrowModClient.CONFIG.showActivity=!ArrowModClient.CONFIG.showActivity;b.setMessage(Text.literal("Активность: "+(ArrowModClient.CONFIG.showActivity?"ВКЛ":"ВЫКЛ")));ArrowModClient.CONFIG.save();}).dimensions(cx+5,y,145,22).build()); y+=28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Позиция: "+ArrowModClient.CONFIG.position),b->{String[] p={"TOP","CENTER","CORNER"};int i=0;for(int n=0;n<p.length;n++)if(p[n].equals(ArrowModClient.CONFIG.position))i=n;ArrowModClient.CONFIG.position=p[(i+1)%p.length];b.setMessage(Text.literal("Позиция: "+ArrowModClient.CONFIG.position));ArrowModClient.CONFIG.save();}).dimensions(cx-150,y,300,22).build()); y+=29;
        playerName=new TextFieldWidget(textRenderer,cx-150,y,300,20,Text.literal("Имя игрока")); playerName.setMaxLength(64); playerName.setPlaceholder(Text.literal("Ник игрока на сервере")); addDrawableChild(playerName); y+=25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Отправить заявку"),b->send(Packets.REQUEST)).dimensions(cx-150,y,145,22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Принять заявку"),b->send(Packets.ACCEPT)).dimensions(cx+5,y,145,22).build()); y+=27;
        addDrawableChild(ButtonWidget.builder(Text.literal("Удалить связь у обоих"),b->send(Packets.REMOVE)).dimensions(cx-150,y,300,22).build()); y+=28;
        addDrawableChild(ButtonWidget.builder(Text.literal("Открыть управление биндами"),b->{if(client!=null)client.setScreen(new net.minecraft.client.gui.screen.option.ControlsOptionsScreen(this,client.options));}).dimensions(cx-150,y,300,22).build()); y+=27;
        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"),b->{ArrowModClient.CONFIG.save();close();}).dimensions(cx-70,y,140,22).build());
    }
    private void send(net.minecraft.util.Identifier packet){String name=playerName.getText().trim();if(!name.isEmpty())Packets.sendString(packet,name);}
    @Override public void render(MatrixStack matrices,int mouseX,int mouseY,float delta){
        renderBackground(matrices); fill(matrices,width/2-172,24,width/2+172,height-18,0xDD101827); fill(matrices,width/2-170,26,width/2+170,60,0xFF233D59);
        drawCenteredText(matrices,textRenderer,title,width/2,35,0xFFFFFF);
        drawCenteredText(matrices,textRenderer,Text.literal("Добавление требует подтверждения второго игрока"),width/2,height-31,0xFFB8D8F5);
        super.render(matrices,mouseX,mouseY,delta);
    }
    @Override public boolean shouldPause(){return false;}
}
