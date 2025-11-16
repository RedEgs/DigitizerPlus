package net.redegs.digitizerplus.client.screen.robot;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;

import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.api.graphics.EntityRenderer;
import net.redegs.digitizerplus.entity.HumanoidRobot;

public class RobotScreen extends AbstractContainerScreen<RobotMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/robot_gui.png");

    private HumanoidRobot robot;
    private EntityRenderer entityRenderer;

    private int barU = 176;
    private int barV = 46;

    private int barX = 99;
    private int barY = 36;


    public RobotScreen(RobotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 229;

        robot = menu.getRobot();
        this.entityRenderer = new EntityRenderer(45 , 58, 21, robot);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        this.entityRenderer.Draw(guiGraphics, mouseX, mouseY, partialTicks, x, y, this);

        int barHeight= (int) Math.round((robot.getEnergyPercentage() / 100.0) * 33.0);
        int yOffset = 33 - barHeight;
        guiGraphics.blit(TEXTURE, x+barX, y+barY+yOffset, barU, barV, 9, barHeight);

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 7, 6, 4210752, false);
        //guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }
}
