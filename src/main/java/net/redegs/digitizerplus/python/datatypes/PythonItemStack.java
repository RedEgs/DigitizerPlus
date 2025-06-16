package net.redegs.digitizerplus.python.datatypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PythonItemStack {
    private final ItemStack itemStack;
    public String name;
    public String id;
    public int count;


    public PythonItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            throw new IllegalArgumentException("Cannot create PythonItemStack from Empty ItemStack.");
        }

        this.itemStack = itemStack;
        this.name = itemStack.getDisplayName().getString();
        this.id = String.valueOf(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
        this.count = itemStack.getCount();
    }

    public int getCount() { return itemStack.getCount(); }
    public int getStackSize() { return getCount(); }
    public int getMaxStackSize() { return itemStack.getMaxStackSize();}

    public String getName() { return name; }
    public String getItemID() { return id; }
    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        for (TagKey<Item> tag: itemStack.getTags().toList()) {
            tags.add(tag.toString());
        }
        return tags;
    }
    public String getRarity() { return itemStack.getRarity().name(); }

    public boolean isStackable() { return itemStack.isStackable(); }


    public ItemStack getOriginalStack() {
        return itemStack;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
