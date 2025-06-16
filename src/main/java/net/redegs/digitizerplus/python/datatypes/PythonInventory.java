package net.redegs.digitizerplus.python.datatypes;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PythonInventory {
    private Container inventory;
    public int size;
    public List<PythonItemStack> list;

    public PythonInventory(Container inventory) {
        this.inventory = inventory;
        this.size = inventory.getContainerSize();
        this.list = this.asList();
    }

    @Nullable
    public PythonItemStack getItem(int slot) {
        if (!inventory.getItem(slot).isEmpty()) {
            return this.list.get(slot);
        } else if (slot > inventory.getContainerSize() || slot < 0) {
            throw new ArrayIndexOutOfBoundsException("Slot number is out of range.");
        }
        return null;
    }

    public int getInventorySize() {
        return inventory.getContainerSize();
    }
    public List<Integer> getUsedSlots() {
        List<Integer> slots  = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {  // Fixed the condition here
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                slots.add(i);
            }
        }
        return slots;
    }
    public boolean isEmpty() { return inventory.isEmpty(); }

    public List<PythonItemStack> asList() {
        List<PythonItemStack> itemList = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {  // Fixed the condition here
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                itemList.add(new PythonItemStack(item));
            }
        }
        return itemList;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < inventory.getContainerSize(); i++) {  // Fixed the condition here
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                str.append(item.getDisplayName().getString())  // Appends the item name
                        .append(" x")  // Adds the quantity label
                        .append(item.getCount())  // Adds the item count
                        .append(", ");  // Adds a separator after each item
            }
        }

        // Remove the last comma and space, if any, before returning the string
        if (str.length() > 0) {
            str.setLength(str.length() - 2);  // Trim the final ", "
        }

        return str.toString();
    }

    public Container getInventory() {
        return this.inventory;
    }
}
