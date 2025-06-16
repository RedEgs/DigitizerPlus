package net.redegs.digitizerplus.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen {
    public ImGuiScreen(Component pTitle) {
        super(pTitle);
    }

    public void open() {
        Minecraft.getInstance().setScreen(this);
    }

    @Override
    protected void init() { super.init(); }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        //renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }


}
