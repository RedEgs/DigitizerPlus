package net.redegs.digitizerplus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.item.ModItems;
import net.redegs.digitizerplus.item.custom.LinkerItem;

@Mod.EventBusSubscriber(modid = DigitizerPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TextRenderer {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            Player player = Minecraft.getInstance().player;
            ItemStack linker = player.getMainHandItem();

            if (player.getMainHandItem().getItem() == ModItems.LINKER.get().asItem()) {
                LinkerItem linkerItem = (LinkerItem) linker.getItem();

                int i = 1;
                for (BlockPos pos : linkerItem.getLinkedPositions(linker)) {
                    renderTextAtBlockPosition(event, "Container " + i, pos);
                    i++;
                }
            }
        }
    }

    private static void renderTextAtBlockPosition(RenderLevelStageEvent event, String text, BlockPos pos) {
        // Get the Minecraft instance
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // Define the block position where you want to render the text
        BlockPos blockPos = pos; // Example position

        // Get the camera position
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();

        // Calculate the relative position from the camera to the block position
        double x = blockPos.getX() + 0.5 - cameraPos.x; // Center the text on the block
        double y = blockPos.getY() + 1.5 - cameraPos.y; // Offset above the block
        double z = blockPos.getZ() + 0.5 - cameraPos.z;

        // Get the PoseStack and MultiBufferSource
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        // Push the pose stack to apply transformations
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Make the text face the player (billboard effect)
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025f, -0.025f, 0.025f); // Scale the text down

        int color = 0xFFFFFF; // White color
        float backgroundOpacity = 0.4f; // Background opacity (optional)
        int packedLight = 15728880; // Full brightness

        // Draw the text with a background (optional)
        font.drawInBatch(
                text,
                -font.width(text) / 2f, // Center the text horizontally
                0,
                color,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        // Draw the background (optional)
        int padding = 1;
        int backgroundColor = (int) (backgroundOpacity * 255) << 24; // Black background with opacity
        font.drawInBatch(
                text,
                -font.width(text) / 2f,
                0,
                backgroundColor,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        // Finish rendering
        bufferSource.endBatch();

        // Pop the pose stack to revert transformations
        poseStack.popPose();
    }
}