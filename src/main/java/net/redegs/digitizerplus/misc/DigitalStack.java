package net.redegs.digitizerplus.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DigitalStack {
    public final ItemStack originStack;
    private final UUID stackID;

    public DigitalStack(ItemStack originStack) {
        this.originStack = originStack;
        this.stackID = UUID.randomUUID();
    }

    // Convert the DigitalStack to NBT
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", originStack.getDisplayName().getString().substring(1, originStack.getDisplayName().getString().length() - 1));
        nbt.putString("id", BuiltInRegistries.ITEM.getKey(originStack.getItem()).toString());
        nbt.putInt("count", originStack.getCount());
        return nbt;
    }

    // Create a DigitalStack from NBT
    public static DigitalStack fromNBT(CompoundTag nbt) {
        String id = nbt.getString("id");
        String name = nbt.getString("name");
        int count = nbt.getInt("count");

        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(id)), count);
        stack.setHoverName(Component.literal(name)); // Set the display name
        return new DigitalStack(stack);
    }

    // Get the HashMap representation (optional, if you still need it)
    public Map<String, Object> getHashmap() {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", originStack.getDisplayName().getString().substring(1, originStack.getDisplayName().getString().length() - 1));
        itemMap.put("id", BuiltInRegistries.ITEM.getKey(originStack.getItem()).toString());
        itemMap.put("count", originStack.getCount());
        return itemMap;
    }

    public UUID getStackID() {
        return this.stackID;
    }
}
