package net.redegs.digitizerplus.python.datatypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.python.RobotPythonRunner;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PythonInventory {
    private Container inventory;
    private BlockEntity blockEntity;
    private HumanoidRobot owner;

    private int size;
    public List<PythonItemStack> list;
    private boolean copy = false;


    public PythonInventory(Container inventory, HumanoidRobot robot) {
        this.inventory = inventory;
        this.size = inventory.getContainerSize();
        this.list = this.asList();

        this.owner = robot;
    }
    public PythonInventory(IItemHandler itemHandler, BlockEntity blockEntity, HumanoidRobot robot) {
        this.inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            inventory.setItem(i, stack);
        }
        this.size = inventory.getContainerSize();
        this.list = this.asList();
        this.blockEntity = blockEntity;
        this.owner = robot;

    }

    @Nullable
    public PythonItemStack getItem(int slot) {
        if (!inventory.getItem(slot).isEmpty() || this.list.get(slot) != null) {
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
        if (this.list == null) {
            return Collections.unmodifiableList(RegenerateList());
        }
        return this.list;
    }

    public boolean ownsItem(PythonItemStack item) {
        if (this.list.contains(item)){
            return true;
        } else {
            boolean bool = false;
            ItemStack iStack = RobotPythonRunner.withAccess(item::getItemStack);
            for (PythonItemStack i : this.list) {
                if (i == null) continue;
                if (iStack.equals(RobotPythonRunner.withAccess(i::getItemStack))) {
                    bool = true;
                    break;
                }
            }
            return bool;
        }
    }
    public int getItemSlot(PythonItemStack item) throws Exception {
        if (ownsItem(item)) {
            ItemStack iStack = RobotPythonRunner.withAccess(item::getItemStack);
            for (int i = 0; i < this.list.size(); i++) {
                if (iStack.equals(RobotPythonRunner.withAccess(list.get(i)::getItemStack))) {
                    return i;
                }
            }

            throw new Exception("Couldn't find slot's index, potentially moved to another inventory?");
        }
        throw new Exception("Inventory doesn't own this item!");
    }

    @Nullable
    public PythonItemStack pushItem(PythonItemStack item, PythonInventory inventory, boolean shouldStack, @Nullable String side) throws Exception {
        /*
            Pushes an item from this inventory to another inventory, returns the remainder stack if any.
            Allows for automatic stacking, with the stack arg.
        */

        if (item.isCopy()) {
            throw new Exception("Can't push/pull inventory or item copies.");
        } else if (!this.ownsItem(item)) {
            throw new Exception("Can't push item that is not in inventory!");
        } else if (!owner.withinInteractionRange(blockEntity.getBlockPos())) {
            throw new Exception("Too far away from inventory's parent.");
        }

        ItemStack itemStack = RobotPythonRunner.withAccess(item::getItemStack);
        if (RobotPythonRunner.withAccess(inventory::getBlockEntity) !=  null){
            AtomicReference<PythonItemStack> remainingStack = new AtomicReference<>(null);
            AbstractMap.SimpleEntry<BlockEntity, Optional<IItemHandler>> itemHandlerMap = RobotPythonRunner.withAccess(inventory::getIItemHandler);
            Optional<IItemHandler> opt = itemHandlerMap.getValue();

            opt.ifPresent(handler -> {
                ItemStack working = itemStack.copyAndClear(); // clone and clear original
                ItemStack remaining = working;

                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack target = handler.getStackInSlot(i);
                    if (shouldStack && !target.isEmpty() && ItemHandlerHelper.canItemStacksStack(target, remaining)) {
                        remaining = handler.insertItem(i, remaining, false);
                        if (remaining.isEmpty()) break;
                    }
                }

                // 2. Try empty slots
                if (!remaining.isEmpty()) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (handler.getStackInSlot(i).isEmpty()) {
                            remaining = handler.insertItem(i, remaining, false);
                            if (remaining.isEmpty()) break;
                        }
                    }
                }

                // 3. Couldn't insert all — restore remaining count to original
                if (!remaining.isEmpty()) {
                    itemStack.grow(remaining.getCount());
                }
            });
            RobotPythonRunner.setJepAccess(true);
            inventory.ResyncInventory(itemHandlerMap);
            RobotPythonRunner.setJepAccess(false);
        } else {
            // Inventory is in-memory (no block entity)
            Container container = RobotPythonRunner.withAccess(inventory::getInventory);
            IItemHandler handler = new InvWrapper(container); // wrap as IItemHandler

            ItemStack working = itemStack.copyAndClear();
            ItemStack remaining = working;

            // 1. Stack into existing stacks
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack target = handler.getStackInSlot(i);
                if (shouldStack && !target.isEmpty() && ItemHandlerHelper.canItemStacksStack(target, remaining)) {
                    remaining = handler.insertItem(i, remaining, false);
                    if (remaining.isEmpty()) break;
                }
            }

            // 2. Fill into empty slots
            if (!remaining.isEmpty()) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (handler.getStackInSlot(i).isEmpty()) {
                        remaining = handler.insertItem(i, remaining, false);
                        if (remaining.isEmpty()) break;
                    }
                }
            }

            // 3. Couldn’t fit all: restore to original
            if (!remaining.isEmpty()) {
                itemStack.grow(remaining.getCount());
            }
        }

        System.out.println("Reached sync stage");
        // Reassign inventory *after* resolving the handler
        RobotPythonRunner.setJepAccess(true);
        System.out.println("Syncing item");
        if (!itemStack.isEmpty()) { item.ResyncItemStack(itemStack); }
        System.out.println("Syncing ivnentory");
        this.ResyncInventory(null);
        System.out.println("Syncing ivnentory 2");
        inventory.ResyncInventory(null);
        RobotPythonRunner.setJepAccess(false);

        System.out.println("Synced all");

        if (!itemStack.isEmpty()) {
            return item;
        }
        return null;

    }
    public PythonItemStack pullItem(PythonItemStack item, PythonInventory inventory, boolean shouldStack, @Nullable String side) throws Exception {
        /* Pulls an item from this another inventory into this inventory */
        if (this.ownsItem(item)) {
            throw new Exception("Can't pull item that is already in inventory!");
        }
        return inventory.pushItem(item, this, shouldStack, side);
    }
    public PythonItemStack pushItem(Integer slotIndex,  PythonInventory inventory, boolean shouldStack, @Nullable String side) throws Exception {
        PythonItemStack item = this.getItem(slotIndex);
        if (item != null) {
            return pushItem(item, inventory, shouldStack, side);
        }
        throw new Exception("Inventory slot " + slotIndex + " has no item");
    }
    public PythonItemStack pullItem(Integer slotIndex, PythonInventory inventory, boolean shouldStack, @Nullable String side) throws Exception {
        PythonItemStack item = inventory.getItem(slotIndex);
        if (item != null) {
            return pushItem(item, inventory, shouldStack, side);
        }
        throw new Exception("Inventory slot " + slotIndex + " has no item");
    }

    public void moveSlot(Integer fromSlot, Integer toSlot) throws Exception {
        /* Moves item in this inventory, from one slot to another slot */
        if (!owner.withinInteractionRange(blockEntity.getBlockPos())) {
            throw new Exception("Too far away from inventory's parent.");
        }

        PythonItemStack fromItem = this.getItem(fromSlot);
        if (fromItem == null) {
            throw new Exception("No item in the selected slot (from slot).");
        }
        if (this.getItem(toSlot) != null) {
            throw new Exception("Item already in that slot (to slot).");
        }

        if (this.blockEntity != null) {
            AbstractMap.SimpleEntry<BlockEntity, Optional<IItemHandler>> itemHandlerMap = RobotPythonRunner.withAccess(this::getIItemHandler);
            Optional<IItemHandler> opt = itemHandlerMap.getValue();

            ItemStack itemStack = RobotPythonRunner.withAccess(fromItem::getItemStack);
            AtomicBoolean completed = new AtomicBoolean(false);


            opt.ifPresent(handler -> {
                ItemStack existing = handler.getStackInSlot(toSlot);
                if (existing.isEmpty()) {
                    handler.insertItem(toSlot,  itemStack.copyAndClear(), false);
                    completed.set(true);
                } else {
                    System.out.println(handler.getStackInSlot(toSlot) );
                    completed.set(false);
                }
            });
            if (!completed.get()) { throw new Exception("Item already in that slot (to slot). Transfer failed"); }

            RobotPythonRunner.setJepAccess(true);
            ResyncInventory(itemHandlerMap);
            RobotPythonRunner.setJepAccess(false);
        }



    }


    public AbstractMap.SimpleEntry<BlockEntity, Optional<IItemHandler>> getIItemHandler() {
        /*
           Returns an item block entity and item handler from the main server thread.
           Makes use of batched returns to minimize waiting for the server thread.
         */
        if (RobotPythonRunner.getJepStatus()) {
            if (blockEntity != null) {
                BlockPos blockPos = this.getBlockEntity().getBlockPos();
                BlockEntity be = owner.getBlockEntity(blockPos);
                LazyOptional<IItemHandler> capability = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                Optional<IItemHandler> opt = capability.resolve();

                return new AbstractMap.SimpleEntry<>(be, opt);
            }
        }
        return null;
    }
    private List<PythonItemStack> RegenerateList() {
        List<PythonItemStack> itemList = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {  // Fixed the condition here
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                PythonItemStack pyStack = new PythonItemStack(item);
                itemList.add(pyStack);
            } else {
                itemList.add(null);
            }
        }
        return itemList;
    }
    public void ResyncInventory(@Nullable AbstractMap.SimpleEntry<BlockEntity, Optional<IItemHandler>> itemHandlerMap) {
        if (RobotPythonRunner.getJepStatus()) {
            if (this.blockEntity != null) {
                System.out.println("Inventory Resynced");


                if (itemHandlerMap == null) {
                    AbstractMap.SimpleEntry<BlockEntity, Optional<IItemHandler>> map = this.getIItemHandler(); itemHandlerMap = map;
                }

                Optional<IItemHandler> opt = itemHandlerMap.getValue();

                opt.ifPresent(handler -> {
                    this.inventory = new SimpleContainer(handler.getSlots());
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        inventory.setItem(i, stack);
                    }
                });
                this.list = Collections.unmodifiableList(RegenerateList());
                this.size = inventory.getContainerSize();
                this.blockEntity = itemHandlerMap.getKey();
            }

            this.inventory.setChanged();
        }
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
        if (RobotPythonRunner.getJepStatus()) {
            return this.inventory;
        } else {
            return null;
        }
    }
    public BlockEntity getBlockEntity() {
        if (RobotPythonRunner.getJepStatus()) {
            return this.blockEntity;
        } else {
            return null;
        }
    }
}
