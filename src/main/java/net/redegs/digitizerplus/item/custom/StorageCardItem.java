package net.redegs.digitizerplus.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class StorageCardItem extends Item {
    public StorageCardItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Advanced tooltip (shown only when Shift is held)

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            if (GetStorageCardID(stack) != null) {
                tooltip.add(Component.literal("UUID: " + GetStorageCardID(stack)).withStyle(ChatFormatting.DARK_RED));
            } else {
                tooltip.add(Component.literal("No UUID assigned yet").withStyle(ChatFormatting.DARK_RED));
            }
        }

        Integer count =  GetStorageCardItemCount(stack);
        if (count == null) { count = 0; }

        tooltip.add(Component.literal("Stored Stacks: " + count + " / 1000").withStyle(ChatFormatting.GRAY));
    }

    public static String AssignStorageCardID(ItemStack stack) {
        UUID id = UUID.randomUUID();

        CompoundTag nbt = stack.getOrCreateTag(); // Get or create the NBT tag for the item stack
        nbt.putString("StorageCardID", id.toString()); // Store the custom ID in the NBT tag
        nbt.putInt("ItemCount", 0);
        stack.setTag(nbt); // Set the NBT tag back to the item stack

        return id.toString();

    }

    public static String GetStorageCardID(ItemStack stack) {
        if (stack.hasTag()) { // Check if the item stack has an NBT tag
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("StorageCardID")) { // Check if the custom ID exists
                return nbt.getString("StorageCardID"); // Return the custom ID
            }
        }
        return null; // Return null if no custom ID is found
    }

    public static UUID GetStorageCardUUID(ItemStack stack) {
        String id = GetStorageCardID(stack);

        if (id != null) {
            return UUID.fromString(id);
        } else {
            return null;
        }
    }

    public static Integer GetStorageCardItemCount(ItemStack stack) {
        if (stack.hasTag()) { // Check if the item stack has an NBT tag
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("ItemCount")) { // Check if the custom ID exists
                return nbt.getInt("ItemCount"); // Return the custom ID
            }
        }
        return null;
    }

    public static void AddToItemCount(ItemStack stack, Integer amount) {
        if (stack.hasTag()) { // Check if the item stack has an NBT tag
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("ItemCount")) { // Check if the custom ID exists
                nbt.putInt("ItemCount", nbt.getInt("ItemCount") + amount);
            }
        }
    }

    public static void RemoveFromItemCount(ItemStack stack, Integer amount) {
        if (stack.hasTag()) { // Check if the item stack has an NBT tag
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("ItemCount")) { // Check if the custom ID exists
                nbt.putInt("ItemCount", nbt.getInt("ItemCount") - amount);
            }
        }
    }

    public static void ResetItemCount(ItemStack stack) {
        if (stack.hasTag()) { // Check if the item stack has an NBT tag
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("ItemCount")) { // Check if the custom ID exists
                nbt.putInt("ItemCount", 0);
            }
        }
    }


}
