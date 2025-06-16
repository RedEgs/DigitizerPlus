package net.redegs.digitizerplus.api.graphics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class ImageGraphic extends Graphic{
    protected int x, y, u, v, sx, sy, dx, dy;
    private ResourceLocation texture;

    public ImageGraphic(int x, int y, ResourceLocation texture, int u, int v, int sx, int sy) {
        super(x, y);
        this.texture = texture;
        this.x = x; this.y = y;
        this.u = u; this.v = v;
        this.sx = sx; this.sy = sy;
    }

    @Override
    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY, Screen screen) {
        dx = x+guiPositionX; dy = y+guiPositionY;
        guiGraphics.blit(texture, dx, dy, u, v, sx, sy);
    }


}
