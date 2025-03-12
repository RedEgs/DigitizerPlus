package net.redegs.digitizerplus.api.graphics;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class SlotGraphic extends Graphic{
    private int x, y;
    private Slot slot;

    public SlotGraphic(Integer x, Integer y, Slot slot) {
        super(x, y);
        this.slot = slot;
    }

    @Override
    public void Draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaT, int guiPositionX, int guiPositionY) {
        //super.Draw(guiGraphics, mouseX, mouseY, deltaT, guiPositionX, guiPositionY);
        RenderSystem.enableDepthTest();
        Lighting.setupForFlatItems();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(guiPositionX, guiPositionY, 0);
        guiGraphics.renderItem(slot.getItem(), slot.x, slot.y);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, slot.getItem(), slot.x, slot.y, null);
        guiGraphics.pose().popPose();

        Lighting.setupFor3DItems();

        if (this.isSlotHovered(guiPositionX, guiPositionY, mouseX, mouseY)) {
            highlightSlot(guiGraphics, guiPositionX, guiPositionY);
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public void OnMouseDown(int mouseX, int mouseY, int button) {
        super.OnMouseDown(mouseX, mouseY, button);

    }

    private boolean isSlotHovered(int guiPositionX, int guiPositionY, int mouseX, int mouseY) {
        int adjustedMouseX = mouseX - guiPositionX;
        int adjustedMouseY = mouseY - guiPositionY;
        return adjustedMouseX >= slot.x && adjustedMouseX < slot.x + 16 && adjustedMouseY >= slot.y && adjustedMouseY < slot.y + 16;
    }

    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!slot.getItem().isEmpty()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, slot.getItem(), mouseX, mouseY);
        }
    }

    private void highlightSlot(GuiGraphics guiGraphics, int guiPositionX, int guiPositionY) {
        RenderSystem.disableDepthTest();
        guiGraphics.fillGradient(RenderType.guiOverlay(), slot.x + guiPositionX, slot.y + guiPositionY,
                slot.x + guiPositionX + 16, slot.y + guiPositionY + 16, -2130706433, -2130706433, 0);
    }







}
