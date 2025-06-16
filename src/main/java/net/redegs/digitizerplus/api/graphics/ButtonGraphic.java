package net.redegs.digitizerplus.api.graphics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.redegs.digitizerplus.DigitizerPlus;

import java.util.function.Consumer;

public class ButtonGraphic extends Graphic{
    public boolean down = false;
    public int button = -1;
    public boolean hovered = false;
    private Runnable pressMethod;

    protected int x, y, u, v, sx, sy, dx, dy;
    // dx, dy : Drawing Positions
    private ResourceLocation texture;



    public ButtonGraphic(Integer x, Integer y, ResourceLocation texture, int u, int v, int sx, int sy) {
        super(x, y);
        this.texture = texture;
        this.x = x; this.y = y;
        this.u = u; this.v = v;
        this.sx = sx; this.sy = sy;
    }

    public void AddCallback(Runnable pressMethod) {
        this.pressMethod = pressMethod;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x; this.y = y;
    }

    @Override
    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY, Screen screen) {
        dx = x+guiPositionX; dy = y+guiPositionY;

        boolean isHovered = isHovered(mouseX, mouseY);
        if (isHovered != hovered) { hovered = isHovered; }


        if (hovered) {
            int hoverColor = 0x10FFFFFF; // 25% opacity white

            guiGraphics.fill(dx, dy, dx + sx, dy + sy, hoverColor);
            guiGraphics.blit(texture, dx, dy, u, v, sx, sy);
        } else {
            guiGraphics.blit(texture, dx, dy, u, v, sx, sy);
        }




    }

    @Override
    public void OnMouseDown(int mouseX, int mouseY, int button) {
        if (this.hovered) { // 0 corresponds to the left mouse button
            down = true;
            this.button = button;
            // Play click sound
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public void OnMouseUp(int mouseX, int mouseY, int button) {
        if (this.hovered) {
            if (pressMethod != null) {
                pressMethod.run();
            }

        } down = false; this.button = -1;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        boolean isHovered = mouseX >= dx && mouseX < dx+ sx &&
                mouseY >= dy && mouseY < dy + sy;
        return isHovered;
    };




}
