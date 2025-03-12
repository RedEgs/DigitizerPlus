package net.redegs.digitizerplus.api.graphics;

import net.minecraft.client.gui.GuiGraphics;

public class Graphic {
    protected int x, y;

    public Graphic(Integer x, Integer y) {
        this.x = x; this.y = y;
    }

    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY) {
        ;
    }

    public void OnMouseDown(int mouseX, int mouseY, int button) {
        ;
    }

    public void OnMouseUp(int mouseX, int mouseY, int button) {
        ;
    }

    public void setPosition(int x, int y) {
        this.x = x; this.y = y;
    }

    public Position getPosition() {
        return new Position(this.x, this.y);
    }

}
