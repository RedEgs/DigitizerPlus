package net.redegs.digitizerplus.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RenderUtils {

    public static void Quad(Matrix4f matrix, VertexConsumer consumer, Vec3 color, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {
        // tl - top left
        // tr - top right
        // bl - bottom left
        // bt - bottom right
        consumer.vertex(matrix, (float) tl.x, (float) tl.y, (float) tl.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)  // RGBA
                .uv(0, 0)                   // Texture UV
                .uv2(0xF000F0)              // Lightmap (max brightness)
                .normal(0, 0, 1)            // Normal vector (facing z+)
                .endVertex();

        consumer.vertex(matrix, (float) tr.x, (float) tr.y, (float) tr.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(0, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, (float) bl.x, (float) bl.y, (float) bl.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, (float) br.x, (float) br.y, (float) br.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 0)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }
    public static void Quad(Matrix4f matrix, VertexConsumer consumer, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {
        Quad(matrix, consumer, new Vec3(255, 255, 255), tl, tr, bl, br);
    }
    public static void Quad(Matrix4f matrix, VertexConsumer consumer, PoseStack poseStack, Vec3 color, Vec3 position, float size) {
        poseStack.translate(position.x, position.y, position.z);
        consumer.vertex(matrix, -size, -size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)  // RGBA
                .uv(0, 0)                   // Texture UV
                .uv2(0xF000F0)              // Lightmap (max brightness)
                .normal(0, 0, 1)            // Normal vector (facing z+)
                .endVertex();

        consumer.vertex(matrix, -size, size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(0, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, size, size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, size, -size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 0)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }
    public static void Quad(PoseStack poseStack, MultiBufferSource MultiBufferSource, Vec3 color, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {
        VertexConsumer consumer =  MultiBufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        consumer.vertex(matrix, (float) tl.x, (float) tl.y, (float) tl.z)
                .color((int)color.x, (int) color.y, (int) color.z, 255)  // RGBA
                .uv(0, 0)                   // Texture UV
                .uv2(0xF000F0)              // Lightmap (max brightness)
                .normal(0, 0, 1)            // Normal vector (facing z+)
                .endVertex();

        consumer.vertex(matrix, (float) tr.x, (float) tr.y, (float) tr.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(0, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, (float) bl.x, (float) bl.y, (float) bl.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, (float) br.x, (float) br.y, (float) br.z)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 0)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }
    public static void Quad(PoseStack poseStack, MultiBufferSource MultiBufferSource, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {
        Quad(poseStack, MultiBufferSource, new Vec3(255, 255, 255), tl, tr, bl, br);
    }
    public static void Quad(PoseStack poseStack, MultiBufferSource MultiBufferSource, Vec3 color, Vec3 position, float size) {
        poseStack.pushPose();

        VertexConsumer consumer =  MultiBufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        poseStack.translate(position.x, position.y, position.z);
        consumer.vertex(matrix, -size, -size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)  // RGBA
                .uv(0, 0)                   // Texture UV
                .uv2(0xF000F0)              // Lightmap (max brightness)
                .normal(0, 0, 1)            // Normal vector (facing z+)
                .endVertex();

        consumer.vertex(matrix, -size, size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(0, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, size, size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 1)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, size, -size, 0f)
                .color((int) color.x, (int) color.y, (int) color.z, 255)
                .uv(1, 0)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        poseStack.popPose();
    }

    public static void TexQuad(PoseStack poseStack, MultiBufferSource MultiBufferSource, ResourceLocation texture, Vec3 tl, Vec3 tr, Vec3 br, Vec3 bl) {
        VertexConsumer consumer =  MultiBufferSource.getBuffer(RenderType.entitySolid(texture));
        Matrix4f matrix = poseStack.last().pose();

        consumer.vertex(matrix, (float) tl.x, (float) tl.y, (float) tl.z)
                .color(255, 255, 255, 255)  // RGBA
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)// Texture UV
                .uv2(0xF000F0)              // Lightmap (max brightness)
                .normal(0, 0, 1)            // Normal vector (facing z+)
                .endVertex();

        consumer.vertex(matrix, (float) tr.x, (float) tr.y, (float) tr.z)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, (float) bl.x, (float) bl.y, (float) bl.z)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, (float) br.x, (float) br.y, (float) br.z)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }
    public static void TexQuad(PoseStack poseStack, MultiBufferSource MultiBufferSource, ResourceLocation texture, Vec3 position, float size) {
        VertexConsumer consumer =  MultiBufferSource.getBuffer(RenderType.entitySolid(texture));
        Matrix4f matrix = poseStack.last().pose();

        poseStack.translate(position.x, position.y, position.z);
        consumer.vertex(matrix, -size, -size, 0f)
                .color(255, 255, 255, 255)  // RGBA
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)// Texture UV
                .uv2(0xF000F0)              // Lightmap (max brightness)
                .normal(0, 0, 1)            // Normal vector (facing z+)
                .endVertex();

        consumer.vertex(matrix, -size, size, 0f)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, size, size, 0f)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, size, -size, 0f)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }
    public static void TexQuad(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture, Vec3 position, float width, float height) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(texture));
        Matrix4f matrix = poseStack.last().pose();

        poseStack.translate(position.x, position.y, position.z);

        // Bottom-left
        consumer.vertex(matrix, 0, 0, 0f)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        // Top-left
        consumer.vertex(matrix, 0, height, 0f)
                .color(255, 255, 255, 255)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        // Top-right
        consumer.vertex(matrix, width, height, 0f)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        // Bottom-right
        consumer.vertex(matrix, width, 0, 0f)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }
    public static void TexQuadFitted(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture, Vec3 position, float quadWidth, float quadHeight, int textureWidth, int textureHeight) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(texture));
        Matrix4f matrix = poseStack.last().pose();

        poseStack.translate(position.x, position.y, position.z);

        // Calculate scale to cover the quad
        float scaleX = quadWidth / textureWidth;
        float scaleY = quadHeight / textureHeight;
        float scale = Math.max(scaleX, scaleY);

        // Visible texture region (top-left anchored)
        float visibleTexWidth = quadWidth / scale;
        float visibleTexHeight = quadHeight / scale;

        // Compute UV bounds (with texture region starting at 0,0)
        float u1 = 0f;
        float v1 = 0f;
        float u2 = visibleTexWidth / textureWidth;
        float v2 = visibleTexHeight / textureHeight;

        // Render quad with corrected UVs
        consumer.vertex(matrix, 0, 0, 0f)
                .color(255, 255, 255, 255)
                .uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, 0, quadHeight, 0f)
                .color(255, 255, 255, 255)
                .uv(u1, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, quadWidth, quadHeight, 0f)
                .color(255, 255, 255, 255)
                .uv(u2, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();

        consumer.vertex(matrix, quadWidth, 0, 0f)
                .color(255, 255, 255, 255)
                .uv(u2, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(0, 0, 1)
                .endVertex();
    }

    public static void FaceCamera(PoseStack poseStack) {
        /* Use this before drawing quads to make them face the camera:

        ```
            VertexConsumer builder =  pBuffer.getBuffer(RenderType.entitySolid(SCREEN_TEXTURE.getTexture()));
            Matrix4f matrix4f = pPoseStack.last().pose();

            RenderUtils.FaceCamera(pPoseStack);
            RenderUtils.TexQuad(matrix4f, builder, pPoseStack, new Vec3(0.5, 0.5, -0.001), 1);
        ```
        */

        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));


    }

    public static float coordsToPixels(int pixels) {
        /* Returns the coordinate in pixel units. (coord / tex resolution) = (1 / 16) */
        return (1f / 16f) * ((float) pixels);
    }
    public static Vec3 coordsToPixels(Vec3 pixelVector) {
        /* Returns the coordinate in pixel units for the whole vector. (coord / tex resolution) = (1 / 16) */
        float x = coordsToPixels((int) pixelVector.x);
        float y = coordsToPixels((int) pixelVector.y);
        float z = coordsToPixels((int) pixelVector.z);
        return new Vec3(x, y, z);
    }

     public static void blitNineSlicedSized(GuiGraphics gfx, ResourceLocation texture, int x, int y, int width, int height, int sliceSize, int uWidth, int vHeight, int uOffset, int vOffset,
                                      int textureWidth, int textureHeight)
    {
        blitNineSlicedSized(gfx, texture, x, y, width, height, sliceSize, sliceSize, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public static void blitNineSlicedSized(GuiGraphics gfx, ResourceLocation texture, int x, int y, int width, int height, int sliceWidth, int sliceHeight, int uWidth, int vHeight,
                                     int uOffset, int vOffset, int textureWidth, int textureHeight)
    {
        blitNineSlicedSized(gfx, texture, x, y, width, height, sliceWidth, sliceHeight, sliceWidth, sliceHeight, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public static void blitNineSlicedSized(GuiGraphics gfx, ResourceLocation texture, int x, int y, int width, int height, int cornerWidth, int cornerHeight, int edgeWidth, int edgeHeight,
                                     int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight)
    {
        cornerWidth = Math.min(cornerWidth, width / 2);
        edgeWidth = Math.min(edgeWidth, width / 2);
        cornerHeight = Math.min(cornerHeight, height / 2);
        edgeHeight = Math.min(edgeHeight, height / 2);

        GuiGraphics self = gfx;

        self.blit(texture, x, y, uOffset, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
        self.blit(texture, x + cornerWidth, y, width - edgeWidth - cornerWidth, cornerHeight, uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, cornerHeight, textureWidth, textureHeight);
        self.blit(texture, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, cornerHeight, textureWidth, textureHeight);
        self.blit(texture, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, cornerWidth, edgeHeight, textureWidth, textureHeight);
        self.blit(texture, x + cornerWidth, y + height - edgeHeight, width - edgeWidth - cornerWidth, edgeHeight, uOffset + cornerWidth, vOffset + vHeight - edgeHeight, uWidth - edgeWidth - cornerWidth, edgeHeight, textureWidth, textureHeight);
        self.blit(texture, x + width - edgeWidth, y + height - edgeHeight, uOffset + uWidth - edgeWidth, vOffset + vHeight - edgeHeight, edgeWidth, edgeHeight, textureWidth, textureHeight);
        self.blit(texture, x, y + cornerHeight, cornerWidth, height - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
        self.blit(texture, x + cornerWidth, y + cornerHeight, width - edgeWidth - cornerWidth, height - edgeHeight - cornerHeight, uOffset + cornerWidth, vOffset + cornerHeight, uWidth - edgeWidth - cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
        self.blit(texture, x + width - edgeWidth, y + cornerHeight, cornerWidth, height - edgeHeight - cornerHeight, uOffset + uWidth - edgeWidth, vOffset + cornerHeight, edgeWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
    }




}
