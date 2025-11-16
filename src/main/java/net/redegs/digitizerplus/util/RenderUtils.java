package net.redegs.digitizerplus.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.redegs.digitizerplus.client.DynamicTextureWrapper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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

    public static void TexQuad(Matrix4f matrix, VertexConsumer consumer, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {

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
    public static void TexQuad(PoseStack poseStack, MultiBufferSource MultiBufferSource, ResourceLocation texture, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {
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
    public static void TexQuad(Matrix4f matrix, VertexConsumer consumer, PoseStack poseStack, Vec3 position, float size) {
        poseStack.pushPose();
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

        poseStack.popPose();
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


}
