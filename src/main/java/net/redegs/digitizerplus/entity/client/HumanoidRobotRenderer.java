package net.redegs.digitizerplus.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.redegs.digitizerplus.entity.HumanoidRobot;

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
        HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(robot, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(robot, InteractionHand.OFF_HAND);

        if (robot.getMainArm() == HumanoidArm.RIGHT) {
            companionModel.rightArmPose = humanoidmodel$armpose;
            companionModel.leftArmPose = humanoidmodel$armpose1;
        } else {
            companionModel.rightArmPose = humanoidmodel$armpose1;
            companionModel.leftArmPose = humanoidmodel$armpose;
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
    public ResourceLocation getTextureLocation(HumanoidRobot humanoidRobot) {
        return TEXTURE;
    }

    protected void scale(HumanoidRobot p_117798_, PoseStack p_117799_, float p_117800_) {
        float f = 0.9375F;
        p_117799_.scale(f, f, f);
    }
}
