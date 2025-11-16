package net.redegs.digitizerplus.client.screen.robot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.redegs.digitizerplus.client.screen.ModMenuTypes;
import net.redegs.digitizerplus.entity.HumanoidRobot;

public class RobotMenu extends AbstractContainerMenu {

    private final Container container;
    private final HumanoidRobot robot;
    private final Level level;

    // Register menu type with @ObjectHolder or DeferredRegister
//    @ObjectHolder("digitizerplus:robot_menu")
//    public static final MenuType<RobotMenu> TYPE = null;

    public RobotMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, (HumanoidRobot) playerInv.player.level().getEntity(extraData.readInt()));
    }

    public RobotMenu(int id, Inventory playerInv, HumanoidRobot robot) {
        super(ModMenuTypes.ROBOT_MENU.get(), id);

        this.robot = robot;
        this.container = robot.getInventory();
        this.level = robot.level();

        // Robot inventory (3x9 like a chest)
        int startX = 8;
        int startY = 80;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, startX + col * 18, startY + row * 18));
            }
        }


        // Hand Slots
        this.addSlot(new Slot(new Container() {
            @Override public int getContainerSize() { return 1; }
            @Override public boolean isEmpty() { return robot.getMainHandItem().isEmpty(); }
            @Override public ItemStack getItem(int i) { return robot.getMainHandItem(); }
            @Override public ItemStack removeItem(int i, int count) {
                return robot.getMainHandItem().split(count);
            }
            @Override public ItemStack removeItemNoUpdate(int i) {
                ItemStack stack = robot.getMainHandItem();
                robot.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                return stack;
            }
            @Override public void setItem(int i, ItemStack stack) { robot.setItemSlot(EquipmentSlot.MAINHAND, stack); }
            @Override public void setChanged() {}
            @Override public boolean stillValid(Player player) { return true; }
            @Override public void clearContent() { robot.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY); }
        }, 0, 66, 58));
        this.addSlot(new Slot(new Container() {
            @Override public int getContainerSize() { return 1; }
            @Override public boolean isEmpty() { return robot.getOffhandItem().isEmpty(); }
            @Override public ItemStack getItem(int i) { return robot.getOffhandItem(); }
            @Override public ItemStack removeItem(int i, int count) {
                return robot.getOffhandItem().split(count);
            }
            @Override public ItemStack removeItemNoUpdate(int i) {
                ItemStack stack = robot.getOffhandItem();
                robot.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                return stack;
            }
            @Override public void setItem(int i, ItemStack stack) { robot.setItemSlot(EquipmentSlot.OFFHAND, stack); }
            @Override public void setChanged() {}
            @Override public boolean stillValid(Player player) { return true; }
            @Override public void clearContent() { robot.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY); }
        }, 1, 8, 58));


        // Armor Slots
        this.addSlot( new Slot(new Container() {
            final EquipmentSlot slotType = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 3);

            @Override
            public int getContainerSize() { return 1; }
            @Override
            public boolean isEmpty() { return robot.getItemBySlot(slotType).isEmpty(); }
            @Override
            public ItemStack getItem(int index) { return robot.getItemBySlot(slotType); }
            @Override
            public ItemStack removeItem(int index, int count) {
                ItemStack stack = robot.getItemBySlot(slotType).split(count);
                robot.setItemSlot(slotType, robot.getItemBySlot(slotType));
                return stack;
            }
            @Override
            public ItemStack removeItemNoUpdate(int index) {
                ItemStack stack = robot.getItemBySlot(slotType);
                robot.setItemSlot(slotType, ItemStack.EMPTY);
                return stack;
            }
            @Override
            public void setItem(int index, ItemStack stack) { robot.setItemSlot(slotType, stack); }
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public void setChanged() {}
            @Override
            public boolean stillValid(Player player) { return true; }
            @Override
            public void clearContent() { robot.setItemSlot(slotType, ItemStack.EMPTY); }
        }, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 3);
            }
        }); // HELMET

        this.addSlot( new Slot(new Container() {
            final EquipmentSlot slotType = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 2);

            @Override
            public int getContainerSize() { return 1; }
            @Override
            public boolean isEmpty() { return robot.getItemBySlot(slotType).isEmpty(); }
            @Override
            public ItemStack getItem(int index) { return robot.getItemBySlot(slotType); }
            @Override
            public ItemStack removeItem(int index, int count) {
                ItemStack stack = robot.getItemBySlot(slotType).split(count);
                robot.setItemSlot(slotType, robot.getItemBySlot(slotType));
                return stack;
            }
            @Override
            public ItemStack removeItemNoUpdate(int index) {
                ItemStack stack = robot.getItemBySlot(slotType);
                robot.setItemSlot(slotType, ItemStack.EMPTY);
                return stack;
            }
            @Override
            public void setItem(int index, ItemStack stack) { robot.setItemSlot(slotType, stack); }
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public void setChanged() {}
            @Override
            public boolean stillValid(Player player) { return true; }
            @Override
            public void clearContent() { robot.setItemSlot(slotType, ItemStack.EMPTY); }
        }, 0, 8, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 2);
            }
            @Override
            public int getMaxStackSize() { return 1; }
        }); // CHESTPLATE

        this.addSlot( new Slot(new Container() {
            final EquipmentSlot slotType = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 1);

            @Override
            public int getContainerSize() { return 1; }
            @Override
            public boolean isEmpty() { return robot.getItemBySlot(slotType).isEmpty(); }
            @Override
            public ItemStack getItem(int index) { return robot.getItemBySlot(slotType); }
            @Override
            public ItemStack removeItem(int index, int count) {
                ItemStack stack = robot.getItemBySlot(slotType).split(count);
                robot.setItemSlot(slotType, robot.getItemBySlot(slotType));
                return stack;
            }
            @Override
            public ItemStack removeItemNoUpdate(int index) {
                ItemStack stack = robot.getItemBySlot(slotType);
                robot.setItemSlot(slotType, ItemStack.EMPTY);
                return stack;
            }
            @Override
            public void setItem(int index, ItemStack stack) { robot.setItemSlot(slotType, stack); }
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public void setChanged() {}
            @Override
            public boolean stillValid(Player player) { return true; }
            @Override
            public void clearContent() { robot.setItemSlot(slotType, ItemStack.EMPTY); }
        },  0, 66, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 1);
            }
            @Override
            public int getMaxStackSize() { return 1; }
        }); // LEGGINGS

        this.addSlot( new Slot(new Container() {
            final EquipmentSlot slotType = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 0);

            @Override
            public int getContainerSize() { return 1; }
            @Override
            public boolean isEmpty() { return robot.getItemBySlot(slotType).isEmpty(); }
            @Override
            public ItemStack getItem(int index) { return robot.getItemBySlot(slotType); }
            @Override
            public ItemStack removeItem(int index, int count) {
                ItemStack stack = robot.getItemBySlot(slotType).split(count);
                robot.setItemSlot(slotType, robot.getItemBySlot(slotType));
                return stack;
            }
            @Override
            public ItemStack removeItemNoUpdate(int index) {
                ItemStack stack = robot.getItemBySlot(slotType);
                robot.setItemSlot(slotType, ItemStack.EMPTY);
                return stack;
            }
            @Override
            public void setItem(int index, ItemStack stack) { robot.setItemSlot(slotType, stack); }
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public void setChanged() {}
            @Override
            public boolean stillValid(Player player) { return true; }
            @Override
            public void clearContent() { robot.setItemSlot(slotType, ItemStack.EMPTY); }
        }, 0, 66, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 0);
            }
            @Override
            public int getMaxStackSize() { return 1; }
        }); // BOOTS


        // Player inventory
        int playerInvY = 147;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * 18, playerInvY + row * 18));
            }
        }

        // Hotbar
        int hotbarY = 205;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, startX + col * 18, hotbarY));
        }
    }




    @Override
    public boolean stillValid(Player player) {
        return robot.isAlive() && player.distanceTo(robot) < 8.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int robotSlots = 27;

            if (index < robotSlots) {
                if (!this.moveItemStackTo(itemstack1, robotSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, robotSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public HumanoidRobot getRobot() {
        return robot;
    }
}
