package net.redegs.digitizerplus.client.screen.computer.kernel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.ComputerBlock;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;
import net.redegs.digitizerplus.util.RenderUtils;

import java.util.Random;

public class MonitorScreen extends Screen {
    /* The size in pixels to render the monitor at */
    private final int RENDER_SCALE = ComputerBlock.RENDER_SCALE;
    private final int RENDER_WIDTH = ComputerBlock.MONITOR_W * RENDER_SCALE;
    private final int RENDER_HEIGHT = ComputerBlock.MONITOR_H * RENDER_SCALE;

    private final float DRAW_X = ((float)this.width - (float) RENDER_WIDTH) / 2f;
    private final float DRAW_Y = ((float)this.height - (float) RENDER_HEIGHT) / 2f;

    private final ResourceLocation MONITOR_SLICE = new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/monitor_slice.png");
    private final int TEX_SIZE = 18;
    private final int TEX_CORNER_SIZE = 8;

    private final DynamicTexture TERMINAL_FRAME_TEXTURE = new DynamicTexture(RENDER_WIDTH, RENDER_HEIGHT, true);
    private final ResourceLocation TERMINAL_FRAME_LOCATION;

    private final MonitorDevice monitorDevice;

    public MonitorScreen(MonitorDevice monitorDevice) {
        super(Component.literal("Monitor"));
        this.monitorDevice = monitorDevice;

        TERMINAL_FRAME_LOCATION = Minecraft.getInstance().getTextureManager().register("monitor/frame", TERMINAL_FRAME_TEXTURE);

    }

    private void renderMonitorTexture(GuiGraphics gfx) {
        gfx.pose().pushPose(); /* Push pose to not mess with matrix */
        var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()); /* Construct Buffer Source */
        var builder = bufferSource.getBuffer(RenderType.gui()); /* Make sure to build as a gui render type, absolutely essential */

        /* Now we can just use our regular TexQuad like we do for the monitor, and its already transformed to screen space */
        RenderUtils.TexQuad(gfx.pose(), bufferSource, monitorDevice.getTexture(), new Vec3(
                ((this.width - RENDER_WIDTH)/2f) + RENDER_WIDTH,
                ((this.height - RENDER_HEIGHT)/2f) + RENDER_HEIGHT,
                0),
                -RENDER_WIDTH, -RENDER_HEIGHT);


        bufferSource.endBatch(); /* End batch or the renderer will crash */
        gfx.pose().popPose(); /* Return the pose so GUI is placed normally again */
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.renderBackground(gfx);



        renderMonitorTexture(gfx);

//        gfx.blitNineSlicedSized(
//                MONITOR_SLICE,
//                dx-TEX_CORNER_SIZE, dy-TEX_CORNER_SIZE,
//                RENDER_WIDTH+(TEX_CORNER_SIZE*2), RENDER_HEIGHT+(TEX_CORNER_SIZE*2), 8,
//                TEX_SIZE, TEX_SIZE,
//                0, 0,
//                TEX_SIZE, TEX_SIZE);

        super.render(gfx, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
        // The same coordinates used when rendering the texture
        float renderX = (this.width - RENDER_WIDTH) / 2f;
        float renderY = (this.height - RENDER_HEIGHT) / 2f;

        float renderX2 = renderX + RENDER_WIDTH;
        float renderY2 = renderY + RENDER_HEIGHT;

        // First: check if inside screen
        if (mouseX >= renderX && mouseX <= renderX2 &&
                mouseY >= renderY && mouseY <= renderY2) {

            // Convert GUI pixel → monitor pixel
            int monitorX = (int) ((mouseX - renderX) / RENDER_SCALE);
            int monitorY = (int) ((mouseY - renderY) / RENDER_SCALE);

            System.out.println("MONITOR PIXEL = " + monitorX + ", " + monitorY);

            // You can now send packet or update monitor
        }

        return super.mouseClicked(mouseX, mouseY, pButton);
    }

}
