package net.redegs.digitizerplus.item.custom;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.entity.ModEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RobotSpawnerItem extends Item {
    public Level levelInstance;

    public RobotSpawnerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        Player player = pContext.getPlayer();


        if (!pContext.getLevel().isClientSide())
        {

            if (!level.isClientSide()) { // Ensure this runs only on the server side
                HumanoidRobot robot = ModEntities.HUMANOID_ROBOT.get().create(level);
                if (robot != null) {
                    robot.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5); // Center the entity at the block position
                    level.addFreshEntity(robot); // Add the entity to the world
                }
            }

        }

        return InteractionResult.SUCCESS;
    }



}
