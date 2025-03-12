package net.redegs.digitizerplus.item.custom;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.redegs.digitizerplus.block.DigitizerBlock;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LinkerItem extends Item {
    public Level levelInstance;

    public LinkerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Advanced tooltip (shown only when Shift is held)
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            int containerIndex = 1;
            for (BlockPos blockPos: getLinkedPositions(stack)) {
                int x = blockPos.getX();
                int y = blockPos.getY();
                int z = blockPos.getZ();

                tooltip.add(Component.literal("Container " + containerIndex + " At X:" + x + " Y:" + y + " Z:" + z).withStyle(ChatFormatting.GRAY));
                containerIndex++;
            }
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

        if (!pContext.getLevel().isClientSide()) {
            boolean isInventory = false;

            if (level.getBlockEntity(clickPos) instanceof Container && !(level.getBlockEntity(clickPos) instanceof DigitizerEntity)) {
                Container container;
                boolean containerExists = false;
                ListTag containerList;

                if (nbt.contains("containers", Tag.TAG_LIST)) {
                    containerList =  nbt.getList("containers", Tag.TAG_COMPOUND);
                }
                else { containerList = new ListTag(); }

                if (level.getBlockEntity(clickPos) instanceof ChestBlockEntity chestEntity) {
                    container = ChestBlock.getContainer((ChestBlock) chestEntity.getBlockState().getBlock(), chestEntity.getBlockState(), level, clickPos, true);
                }
                else {
                    container = (Container) level.getBlockEntity(clickPos);
                }

                // ---------

                for (BlockPos containerPos : getLinkedPositions(item)) {
                    if (containerPos == clickPos || containerPos.equals(clickPos)) {
                        containerExists = true;
                        break;
                    }
                }

                for (Container existingContainer : getLinkedContainers(item, level)) {
                    if (container == existingContainer || existingContainer.equals(container)) {
                        containerExists = true;
                        break;
                    }
                }

                if (!containerExists) {
                    CompoundTag containerTag = new CompoundTag();
                    containerTag.putInt("x", clickPos.getX());
                    containerTag.putInt("y", clickPos.getY());
                    containerTag.putInt("z", clickPos.getZ());
                    containerList.add(containerTag);

                    nbt.put("containers", containerList);
                }



            }
        }

        return InteractionResult.SUCCESS;
    }



    public List<BlockPos> getLinkedPositions(ItemStack item) {
        List<BlockPos> positions = new ArrayList<>();

        if (item.hasTag()) {
            CompoundTag nbt = item.getTag();
            if (nbt.contains("containers", Tag.TAG_LIST)) {
                ListTag containerList = nbt.getList("containers", Tag.TAG_COMPOUND);
                Integer containerIndex = 0;

                // Iterate through the ListTag and convert each CompoundTag to a BlockPos
                for (Tag tag : containerList) {
                    CompoundTag posTag = (CompoundTag) tag;
                    int x = posTag.getInt("x");
                    int y = posTag.getInt("y");
                    int z = posTag.getInt("z");
                    positions.add(new BlockPos(x, y, z));
                    containerIndex++;
                }
            }
        }
        return positions;
    }

    public List<Container> getLinkedContainers(ItemStack item, Level level) {
        List<BlockPos> positions = getLinkedPositions(item);
        List<Container> containers = new ArrayList<>();

        for (BlockPos pos: positions) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Container) {
                containers.add(positions.indexOf(pos), (Container) blockEntity);
            }
        }
        return containers;
    }




}
