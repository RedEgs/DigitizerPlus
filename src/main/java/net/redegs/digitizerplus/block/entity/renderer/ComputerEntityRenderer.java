package net.redegs.digitizerplus.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.client.DynamicTextureWrapper;
import net.redegs.digitizerplus.computer.terminal.Cell;
import net.redegs.digitizerplus.util.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ComputerEntityRenderer implements BlockEntityRenderer<ComputerEntity> {
    private DynamicTextureWrapper SCREEN_TEXTURE;
    private final Font font = Minecraft.getInstance().font;

    public ComputerEntityRenderer(BlockEntityRendererProvider.Context context) {
       this.SCREEN_TEXTURE = new DynamicTextureWrapper("test_tex", 1179, 2096);
       this.SCREEN_TEXTURE.setImage("textures/gui/shadow.jpg");
    }

    @Override
    public void render(ComputerEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();

        Direction facing = pBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

//         Rotate around center to place quad on block's front face
        pPoseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case NORTH -> pPoseStack.mulPose(Axis.YP.rotationDegrees(360));
            case SOUTH -> pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST  -> pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST  -> pPoseStack.mulPose(Axis.YP.rotationDegrees(-90));
        }
        pPoseStack.translate(-0.5, -0.5, -0.5);



//        VertexConsumer builder =  pBuffer.getBuffer(RenderType.entitySolid(SCREEN_TEXTURE.getTexture()));

//        RenderUtils.TexQuad(matrix4f, builder, pPoseStack, new Vec3(0.5, 0.5, -0.0001), .5f);

        Matrix4f matrix4f = pPoseStack.last().pose();
        //RenderUtils.Quad(pPoseStack, pBuffer,  new Vec3(0, 0, 0), new Vec3(0.5, 0.5, -0.0001), .5f);

        pPoseStack.translate(1.125f, 1f, -0.001); // Set the text matrix to start in the top-left
        pPoseStack.scale(-.01f, -.01f, 0); // Scale the text down

        Cell[][] buffer = pBlockEntity.terminal.getBuffer();
        for (int y = 0; y < buffer.length; y++) {

            int drawX = 8;
            int drawY = 8 + y * 10;

            for (int x = 0; x < buffer[y].length; x++) {
                Cell cell = buffer[y][x];

                // Draw background color if needed
//                if (cell.bgColor != 0x00000000) { // if the cell bg is not transparent
//                    gfx.fill(drawX, drawY - 1, drawX + font.width(Character.toString(cell.ch)), drawY + 12 - 2, cell.bgColor);
//                }

                // Draw character
                if (cell.ch != '\0') {
//                    gfx.drawString(font, Character.toString(cell.ch), drawX, drawY, cell.fgColor);
                    font.drawInBatch(Character.toString(cell.ch), drawX, drawY, 0xFFFFFF, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0x000000, 0xF000F0);
                }
                drawX += font.width(Character.toString(cell.ch));
            }
        }

        //font.drawInBatch("Terminal", 0, 0, 0xFFFFFF, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0x000000, 0xF000F0);




        pPoseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ComputerEntity pBlockEntity) {
        return BlockEntityRenderer.super.shouldRenderOffScreen(pBlockEntity);
    }

    @Override
    public int getViewDistance() {
        return BlockEntityRenderer.super.getViewDistance();
    }

    @Override
    public boolean shouldRender(ComputerEntity pBlockEntity, Vec3 pCameraPos) {
        return BlockEntityRenderer.super.shouldRender(pBlockEntity, pCameraPos);
    }
}
