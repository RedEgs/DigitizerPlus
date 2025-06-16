package net.redegs.digitizerplus.api.graphics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.redegs.digitizerplus.DigitizerPlus;

import java.util.ArrayList;

public class ScrollMenuGraphic extends Graphic{

    protected int dx, dy;

    private ResourceLocation MAIN_TEX =  new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/robot_gui.png");
    private Font font = Minecraft.getInstance().font;
    protected ImageGraphic baseGraphic;

    private ArrayList<String> Options;
    private int Selector = 0;


    public ScrollMenuGraphic(Integer x, Integer y, ArrayList<String> options) {
        super(x, y);
        this.Options = options;
        this.baseGraphic = new ImageGraphic(x, y, MAIN_TEX, 122, 245, 23, 11);
        this.baseGraphic.setPosition(x-1, y-1);
    }

    @Override
    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY, Screen screen) {
        dx = x + guiPositionX; dy = y + guiPositionY;

        this.baseGraphic.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY, screen);
        guiGraphics.drawString(font, Options.get(Selector), dx, dy, 0xFFFFFFFF);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= dx && mouseX < dx+ baseGraphic.sx &&
                mouseY >= dy && mouseY < dy + baseGraphic.sy;
    };

    @Override
    public void OnMouseScroll(int mouseX, int mouseY, double delta) {

        if (isHovered(mouseX, mouseY)) {
            int size = Options.size();

            if (delta > 0) {
                Selector = (Selector - 1 + size) % size; // scroll up
            } else if (delta < 0) {
                Selector = (Selector + 1) % size; // scroll down
            }
        }
    }

    public void UpdateList(ArrayList<String> list) {
        this.Options = list;
    }

    public String GetSelected() {
        return this.Options.get(Selector);
    }
    public Integer GetSelectedIndex() {
        return this.Selector;
    }

}
