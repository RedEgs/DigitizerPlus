package net.redegs.digitizerplus.python.datatypes;

import jep.python.PyObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.redegs.digitizerplus.python.RobotPythonRunner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PythonItemStack {
    private ItemStack itemStack;
    private String name;
    private String id;
    private boolean copy;
    private int count;


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
    public boolean isCopy() { return copy; }
    public ItemStack getItemStack(){
        if (!RobotPythonRunner.getJepStatus()) {
            System.out.println("JEP ACCESS VIOLATION");
            return null;
        } else {
            System.out.println("MAIN ACCESS ACCEPTED");
            return itemStack;
        }
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        return itemStack.equals(obj);
    }
    public void destroy() {
        if (RobotPythonRunner.getJepStatus()) {
            if (!itemStack.isEmpty()) {
                return;
            }

            this.itemStack = null;
            this.name = null;
            this.count = -1;
            this.id = null;
        }
    }
    public void ResyncItemStack(ItemStack itemStack) {
        if (RobotPythonRunner.getJepStatus()) {
            if (itemStack.isEmpty()) {
                throw new IllegalArgumentException("Cannot create PythonItemStack from Empty ItemStack.");
            }

            this.itemStack = itemStack;
            this.name = itemStack.getDisplayName().getString();
            this.id = String.valueOf(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
            this.count = itemStack.getCount();
        }
    }
}
