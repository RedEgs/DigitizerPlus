package net.redegs.digitizerplus.client.screen.computer.kernel;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.ComputerBlock;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.MonitorKeypressedPacket;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.MonitorPixelClickedPacket;
import net.redegs.digitizerplus.util.RenderUtils;
import org.lwjgl.glfw.GLFW;

import java.io.Serializable;
import java.util.Random;


public class MonitorScreen extends Screen implements Serializable {

    /* The size in pixels to render the monitor at */
    private final float RENDER_WIDTH = (ComputerBlock.MONITOR_W * ComputerBlock.PIXEL_SCALE_X);
    private final float RENDER_HEIGHT = (ComputerBlock.MONITOR_H * ComputerBlock.PIXEL_SCALE_Y);


    private transient final ResourceLocation MONITOR_SLICE = new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/monitor_slice.png");
    private final int TEX_SIZE = 18;
    private final int TEX_CORNER_SIZE = 8;

    private transient final MonitorDevice monitorDevice;
    private BlockPos blockPos;

    public MonitorScreen(BlockPos blockPos) {
        super(Component.literal("Monitor"));

        this.blockPos = blockPos;

        ComputerEntity computer = (ComputerEntity) Minecraft.getInstance().level.getBlockEntity(blockPos);
        this.monitorDevice = computer.monitorDevice;


    }

    private void renderMonitorTexture(GuiGraphics gfx) {
        gfx.pose().pushPose(); /* Push pose to not mess with matrix */
        var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()); /* Construct Buffer Source */
        var builder = bufferSource.getBuffer(RenderType.gui()); /* Make sure to build as a gui render type, absolutely essential */

        /* Now we can just use our regular TexQuad like we do for the monitor, and its already transformed to screen space */
        RenderUtils.TexQuad(
                gfx.pose(), bufferSource, monitorDevice.getTexture(),
                new Vec3(
                (((this.width - RENDER_WIDTH)/2f) + RENDER_WIDTH)-1,
                (((this.height - RENDER_HEIGHT)/2f) + RENDER_HEIGHT)-1,
                0
                ),
                -RENDER_WIDTH,
                -RENDER_HEIGHT);


        bufferSource.endBatch(); /* End batch or the renderer will crash */
        gfx.pose().popPose(); /* Return the pose so GUI is placed normally again */
    }

    private void renderTextBuffer(GuiGraphics gfx) {
        /// NEED TO REDNER WITHOUT TEXT SHADOW

        // Renders the text buffer of the monitor device
        gfx.pose().pushPose();

        // Translate to the top-left of the texture
        float tx = ((this.width - RENDER_WIDTH) / 2f)-.5f;
        float ty = (this.height - RENDER_HEIGHT) / 2f;
        gfx.pose().translate(tx, ty, 0);

        var buf = monitorDevice.textBuffer;
        for (int y = 0; y < buf.length; y++) {
            for (int x = 0; x < buf[y].length; x++) {
                String ch = buf[y][x];
                if (ch != null) {
                    // Multiply by character size (6x8) if needed
                    gfx.drawString(font, ch, (x * 6), (y * 8), 0xffffff, false);
                }
            }
        }

        gfx.pose().popPose(); // pop text-space
    }

    private void renderMonitorFrame(GuiGraphics gfx) {
        int dx = (int) ((this.width - RENDER_WIDTH) /2);
        int dy = (int) ((this.height - RENDER_HEIGHT) /2);

        RenderUtils.blitNineSlicedSized(gfx,
                MONITOR_SLICE,
                dx-TEX_CORNER_SIZE, dy-TEX_CORNER_SIZE,
                (int) (RENDER_WIDTH+(TEX_CORNER_SIZE*2)), (int) (RENDER_HEIGHT+(TEX_CORNER_SIZE*2)), 8,
                TEX_SIZE, TEX_SIZE,
                0, 0,
                TEX_SIZE, TEX_SIZE);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.renderBackground(gfx);

        renderMonitorTexture(gfx);
        renderTextBuffer(gfx);
        renderMonitorFrame(gfx);


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
            int monitorX = (int) ((mouseX - renderX) / ComputerBlock.PIXEL_SCALE_X);
            int monitorY = (int) ((mouseY - renderY) / ComputerBlock.PIXEL_SCALE_Y);

            ModNetwork.sendToServer(new MonitorPixelClickedPacket(blockPos, monitorX, monitorY));


            // You can now send packet or update monitor
        }

        return super.mouseClicked(mouseX, mouseY, pButton);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        ModNetwork.sendToServer(
                new MonitorKeypressedPacket(blockPos, -1, c, true)
        );
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(null); // Close the screen
            return true; // Consume the key for closing
        }
        ModNetwork.sendToServer(
                new MonitorKeypressedPacket(blockPos, keyCode, '\0', true)
        );
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        ModNetwork.sendToServer(
                new MonitorKeypressedPacket(blockPos, keyCode, '\0', false)
        );
        return true;
    }
}
