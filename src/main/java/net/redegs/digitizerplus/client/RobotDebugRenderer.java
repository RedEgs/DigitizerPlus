package net.redegs.digitizerplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.python.datatypes.PythonBlock;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = DigitizerPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RobotDebugRenderer {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        //Camera mainCamera = mc.gameRenderer.getMainCamera();
        if (!mc.options.renderDebug) return;
        ClientLevel level = mc.level;

        Vec3 cameraPosition = mc.gameRenderer.getMainCamera().getPosition();
        RenderSystem.disableDepthTest();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof HumanoidRobot robot) {
                HitResult hr = robot.getLookingRayResult();
                BlockHitResult blockHit = (BlockHitResult) hr ;
                EntityHitResult entityHit = robot.getLookingRayEntityResult();

                Vec3 rayLocation = blockHit.getLocation();

                Vec3 entityHitLocation;
                if (entityHit == null) { entityHitLocation = rayLocation;}
                else {entityHitLocation = entityHit.getLocation(); }

                Vec3 eyePosition = robot.position();

                rayLocation = rayLocation.subtract(cameraPosition);
                entityHitLocation = entityHitLocation.subtract(cameraPosition);
                eyePosition = eyePosition.add(0, robot.getEyeHeight(), 0).subtract(cameraPosition);

                drawSimpleBox(event.getPoseStack(), 0.5, rayLocation.x,rayLocation.y,rayLocation.z, 1.0F, blockAtRay(hr, robot), 0.0F, 1.0F);

                if (entityHit == null) {
                    drawSimpleBox(event.getPoseStack(), 0.25, entityHitLocation.x, entityHitLocation.y, entityHitLocation.z, 0.0F, 0.0F, 1.0F, 1.0F);
                } else {
                    Entity hEntity = entityHit.getEntity();
                    entityHitLocation = entityHit.getLocation().subtract(cameraPosition).add(0, hEntity.getBbHeight()/2, 0);
                    drawSimpleBox(event.getPoseStack(), 1, entityHitLocation.x, entityHitLocation.y, entityHitLocation.z, 0.0F, 1.0F, 1.0F, 1.0F);
                }


                for (Entity e: robot.getEntitiesWithinFOV()) {
                    Vec3 pos = e.position().subtract(cameraPosition);
                    drawSimpleBox(event.getPoseStack(), 1.0, pos.x, pos.y, pos.z, 0.0F, 1.0F, 1.0F, 1.0F);
                }


                drawLineManual(event.getPoseStack(), eyePosition, rayLocation, 1.0F, 0.0F, 1.0F, 1.0F);
                //drawCircle(event.getPoseStack(), entity.position().subtract(cameraPosition), (float) (Math.pow(HumanoidRobot.VIEWDISTANCE, 2) + HumanoidRobot.VIEWDISTANCE), 64, 0.0F, 1.0F, 1.0F, 1.0F);
                drawVisionCone(event.getPoseStack(), entity.position().subtract(cameraPosition), robot.getLookAngle().normalize(), (float) HumanoidRobot.FOV * 2, (float) (Math.pow(HumanoidRobot.VIEWDISTANCE, 2) + HumanoidRobot.VIEWDISTANCE), 128, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        RenderSystem.enableDepthTest();
    }

    private static float blockAtRay(HitResult hr, HumanoidRobot robot) {
        BlockHitResult blockHit = (BlockHitResult) hr ;
        BlockPos blockPos = blockHit.getBlockPos();

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            Level level = robot.level();

            BlockState state = level.getBlockState(pos);
            return 1.0F;
        }
        return 0.0F;
    }

    public static void drawLineBox(PoseStack matrixStack, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        RenderSystem.disableDepthTest();
        VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, x1,y1,z1,x2,y2,z2, r, g, b, a);
        RenderSystem.enableDepthTest();
    }
    public static void drawSimpleBox(PoseStack matrixStack, double size, double x, double y, double z, float r, float g, float b, float a) {
        // Calculate the bounding box coordinates based on size and position
        double halfSize = size / 2;
        double x1 = x - halfSize;
        double y1 = y - halfSize;
        double z1 = z - halfSize;
        double x2 = x + halfSize;
        double y2 = y + halfSize;
        double z2 = z + halfSize;

        // Draw the box
        drawLineBox(matrixStack, x1, y1, z1, x2, y2, z2, r, g, b, a);
    }
    public static void drawLineManual(PoseStack poseStack, Vec3 start, Vec3 end, float r, float g, float b, float a) {
        VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        double minX = Math.min(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = Math.max(start.y, end.y);
        double maxZ = Math.max(start.z, end.z);

        LevelRenderer.renderLineBox(
                poseStack,
                buffer,
                minX, minY, minZ,
                maxX, maxY, maxZ,
                r, g, b, a
        );

        //LevelRenderer.renderLineBox(poseStack, pBuffer, aabb.minX, (double)(pEntity.getEyeHeight() - 0.01F), aabb.minZ, aabb.maxX, (double)(pEntity.getEyeHeight() + 0.01F), aabb.maxZ, 1.0F, 0.0F, 0.0F, 1.0F);
    }
    public static void drawCircle(PoseStack poseStack, Vec3 center, float radius, int segments, float r, float g, float b, float a) {
        VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        Matrix4f matrix = poseStack.last().pose();

        double angleStep = 2.0 * Math.PI / segments;

        for (int i = 0; i < segments; i++) {
            double angle1 = i * angleStep;
            double angle2 = (i + 1) * angleStep;

            float x1 = (float)(center.x + Math.cos(angle1) * radius);
            float z1 = (float)(center.z + Math.sin(angle1) * radius);
            float x2 = (float)(center.x + Math.cos(angle2) * radius);
            float z2 = (float)(center.z + Math.sin(angle2) * radius);
            float y = (float)center.y;

            buffer.vertex(matrix, x1, y, z1).color(r, g, b, a).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, x2, y, z2).color(r, g, b, a).normal(0, 1, 0).endVertex();
        }
    }

    public static void drawVisionCone(PoseStack poseStack, Vec3 origin, Vec3 direction, float fovDegrees, float length, int segments, float r, float g, float b, float a) {
        VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        Matrix4f matrix = poseStack.last().pose();

        float halfFovRad = (float)Math.toRadians(fovDegrees / 2.0);
        Vec3 forward = direction.normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize(); // Perpendicular on XZ
        Vec3 up = new Vec3(0, 1, 0); // Keep cone flat on XZ

        // Start drawing segments from -FOV/2 to +FOV/2
        for (int i = 0; i < segments; i++) {
            float angle1 = -halfFovRad + i * (2 * halfFovRad / segments);
            float angle2 = -halfFovRad + (i + 1) * (2 * halfFovRad / segments);

            Vec3 dir1 = forward
                    .scale(Math.cos(angle1))
                    .add(right.scale(Math.sin(angle1)))
                    .normalize()
                    .scale(length);

            Vec3 dir2 = forward
                    .scale(Math.cos(angle2))
                    .add(right.scale(Math.sin(angle2)))
                    .normalize()
                    .scale(length);

            Vec3 p1 = origin;
            Vec3 p2 = origin.add(dir1);
            Vec3 p3 = origin.add(dir2);

            // Outer arc segment
            buffer.vertex(matrix, (float) p2.x, (float) p2.y, (float) p2.z).color(r, g, b, a).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, (float) p3.x, (float) p3.y, (float) p3.z).color(r, g, b, a).normal(0, 1, 0).endVertex();

            // Ray lines from origin
            if (i == 0 || i == segments - 1) {
                buffer.vertex(matrix, (float) origin.x, (float) origin.y, (float) origin.z).color(r, g, b, a).normal(0, 1, 0).endVertex();
                buffer.vertex(matrix, (float) p2.x, (float) p2.y, (float) p2.z).color(r, g, b, a).normal(0, 1, 0).endVertex();
            }
        }
    }
}
