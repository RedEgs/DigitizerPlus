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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ProgrammerItem extends Item {
    public Level levelInstance;

    public ProgrammerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Advanced tooltip (shown only when Shift is held)
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.literal("Lol").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Press Shift for more info.").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos clickPos = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        ItemStack item = pContext.getItemInHand();
        CompoundTag nbt = item.getOrCreateTag();



        return InteractionResult.SUCCESS;
    }
}
