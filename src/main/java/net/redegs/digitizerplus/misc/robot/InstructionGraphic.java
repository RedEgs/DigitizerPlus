package net.redegs.digitizerplus.misc.robot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.api.graphics.ButtonGraphic;
import net.redegs.digitizerplus.api.graphics.Graphic;
import net.redegs.digitizerplus.api.graphics.Position;

public class InstructionGraphic extends Graphic {
    protected int x, y, dx, dy;
    protected int u = 0, v = 218;
    protected int sx = 74, sy = 16;

    private ResourceLocation texture =  new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/robot_gui.png");
    private Font font = Minecraft.getInstance().font;
    private Instruction instruction;

    public ButtonGraphic removeButton = new ButtonGraphic(x, y, texture, 148, 218, 10, 11);
    ButtonGraphic handleButton = new ButtonGraphic(x, y, texture, 158, 218, 10, 11);

    public InstructionGraphic(int x, int y) {
        super(x, y);
        this.x = x; this.y = y;
        this.u = 0; this.v = 218;
        this.sx = 74; this.sy = 16;


    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }


    @Override
    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY) {
        dx = x+guiPositionX; dy = y+guiPositionY;

        guiGraphics.blit(texture, dx, dy, u, v, sx, sy);
        removeButton.Draw(guiGraphics, mouseX, mouseY, deltaT, dx+52, dy+2);
        handleButton.Draw(guiGraphics, mouseX, mouseY, deltaT, dx+63, dy+2);

        if (this.instruction != null) {
            guiGraphics.drawString(font, String.valueOf(instruction.stack.indexOf(instruction)), dx + 3 , dy + 3, 0xFFFFFF);
            guiGraphics.drawString(font, instruction.instName, dx + 15 , dy + 3, 0xFFFFFF);
        }

    }

    @Override
    public void OnMouseDown(int mouseX, int mouseY, int button) {
        removeButton.OnMouseDown(x, y, button);
        handleButton.OnMouseDown(x, y, button);
    }

    @Override
    public void OnMouseUp(int mouseX, int mouseY, int button) {
        removeButton.OnMouseUp(x, y, button);
        handleButton.OnMouseUp(x, y, button);
    }
}

