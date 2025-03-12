package net.redegs.digitizerplus.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.redegs.digitizerplus.block.DigitizerBlock;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;

public class DigitizerEntityRenderer implements BlockEntityRenderer<DigitizerEntity> {
    private final ItemRenderer itemRenderer;

    public DigitizerEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(DigitizerEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack linkerItem = blockEntity.getStoredLinker();

        if (!linkerItem.isEmpty()) {
            poseStack.pushPose();
            // Adjust the position and scale of the item
            poseStack.translate(0.5, 0.8, 0.5);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getBlockState().getValue(DigitizerBlock.FACING).toYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

            // Render the item
            itemRenderer.renderStatic(linkerItem, ItemDisplayContext.FIXED, getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockEntity.getLevel(), 1);

            poseStack.popPose();
        }
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}