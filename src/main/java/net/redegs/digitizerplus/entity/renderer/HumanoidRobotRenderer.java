package net.redegs.digitizerplus.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import org.joml.Matrix4f;

public class HumanoidRobotRenderer extends HumanoidMobRenderer<HumanoidRobot, PlayerModel<HumanoidRobot>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("digitizerplus:textures/entity/humanoid_robot.png");

    public HumanoidRobotRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }


    @Override
    public void render(HumanoidRobot robot, float companionYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        this.setModelProperties(robot);
        super.render(robot, companionYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    private void setModelProperties(HumanoidRobot robot) {
        PlayerModel<HumanoidRobot> companionModel = this.getModel();

        // ✅ This is what triggers crouch animation in the model
        companionModel.crouching = robot.isCrouching();

        HumanoidModel.ArmPose mainPose = getArmPose(robot, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offPose = getArmPose(robot, InteractionHand.OFF_HAND);

        if (robot.getMainArm() == HumanoidArm.RIGHT) {
            companionModel.rightArmPose = mainPose;
            companionModel.leftArmPose = offPose;
        } else {
            companionModel.rightArmPose = offPose;
            companionModel.leftArmPose = mainPose;
        }
    }
    private static HumanoidModel.ArmPose getArmPose(HumanoidRobot robot, InteractionHand hand) {
        ItemStack itemstack = robot.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (robot.getUsedItemHand() == hand && robot.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();

                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.CROSSBOW && hand == robot.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!robot.swinging && itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    @Override
    protected void renderNameTag(HumanoidRobot pEntity, Component pDisplayName, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        String name = pEntity.getUUID().toString();

        double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
        if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(pEntity, d0)) {
            boolean flag = !pEntity.isDiscrete();
            float f = pEntity.getNameTagOffsetY();

            int i = "deadmau5".equals(name) ? -10 : 0;

            pMatrixStack.pushPose();
            pMatrixStack.translate(0.0F, f, 0.0F);
            pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            pMatrixStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = pMatrixStack.last().pose();
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int j = (int)(f1 * 255.0F) << 24;
            Font font = this.getFont();
            float f2 = (float)(-font.width(name) / 2);
            font.drawInBatch(name, f2, (float)i, 553648127, false, matrix4f, pBuffer, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, pPackedLight);
            if (flag) {
                font.drawInBatch(name, f2, (float)i, -1, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight);
            }

            pMatrixStack.popPose();
        }
    }

    @Override
    protected boolean shouldShowName(HumanoidRobot pEntity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug)  {
            return true;
        }
        return super.shouldShowName(pEntity);
    }



    @Override
    public ResourceLocation getTextureLocation(HumanoidRobot humanoidRobot) {
        return TEXTURE;
    }

    protected void scale(HumanoidRobot entity, PoseStack poseStack, float p_117800_) {
        float f = 0.9375F;

//        if (entity.getPose() == Pose.CROUCHING) {
//            poseStack.scale(1, 0.85f, 1); // Flatten slightly
//        } else {
        poseStack.scale(f, f, f);
//        }



    }
}
