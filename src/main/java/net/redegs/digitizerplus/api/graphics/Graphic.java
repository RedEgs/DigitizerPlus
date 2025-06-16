package net.redegs.digitizerplus.api.graphics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class Graphic {
    protected int x, y;
    protected List<Graphic> children = new ArrayList<>();

    public Graphic(Integer x, Integer y) {
        this.x = x; this.y = y;
    }

    public void addChild(Graphic child) {
        children.add(child);
    }

    public List<Graphic> collectAllGraphics() {
        List<Graphic> all = new ArrayList<>();
        all.add(this);
        for (Graphic child : children) {
            all.addAll(child.collectAllGraphics());
        }
        return all;
    }

    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY, Screen screen) {
        ;
    }

    public void OnMouseDown(int mouseX, int mouseY, int button) {
        ;
    }

    public void OnMouseUp(int mouseX, int mouseY, int button) {
        ;
    }

    public void OnMouseScroll(int mouseX, int mouseY, double delta) {
        ;
    }

    public void OnScreenClose() {
        ;
    }

    public void setPosition(int x, int y) {
        this.x = x; this.y = y;
    }

    public Position getPosition() {
        return new Position(this.x, this.y);
    }

}
