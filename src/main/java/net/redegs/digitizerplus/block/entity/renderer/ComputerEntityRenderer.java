package net.redegs.digitizerplus.block.entity.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
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

import java.util.Random;

public class ComputerEntityRenderer implements BlockEntityRenderer<ComputerEntity> {
    private final Font font = Minecraft.getInstance().font;

    public ComputerEntityRenderer(BlockEntityRendererProvider.Context context) {

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

        if (pBlockEntity.monitorInit) {

            // Draw the quad
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, pBlockEntity.monitorDevice.getTexture());
            RenderUtils.TexQuad(pPoseStack, pBuffer, pBlockEntity.monitorDevice.getTexture(),
                    new Vec3(0.5, 0.5, -0.01), 0.5f);
        }


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
