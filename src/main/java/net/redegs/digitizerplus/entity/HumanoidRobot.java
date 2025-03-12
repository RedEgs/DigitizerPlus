package net.redegs.digitizerplus.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.redegs.digitizerplus.entity.goals.TransferItemsGoal;
import net.redegs.digitizerplus.item.ModItems;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.SyncRobotPacket;
import net.redegs.digitizerplus.screen.HumanoidRobotScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HumanoidRobot extends Mob {
    private final SimpleContainer inventory = new SimpleContainer(9); // 9 slots (e.g., a small chest)
    private final SimpleContainer programSlot = new SimpleContainer(1); // 9 slots (e.g., a small chest)

    private Level level;
    private TransferItemsGoal TransferGoal;


    public HumanoidRobot(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);

        // Set default attributes
        this.level = level;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D);

    }

    @Override
    protected void registerGoals() {
//        this.TransferGoal = new TransferItemsGoal(this, 1.0D);
//        this.goalSelector.addGoal(0, this.TransferGoal); // Custom goal to collect nearby items
    }


    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND) {
            if (itemStack.getItem() == ModItems.PROGRAMMER_ITEM.get()) {
                this.programSlot.setItem(0, itemStack);
                prnt("set Item");
            } else {
                this.inventory.addItem(itemStack);
                syncInventory();

            }
        }
        if (player.level().isClientSide) {
            syncInventory();
            HumanoidRobotScreen screen = new HumanoidRobotScreen(this);
            screen.open();

        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }
    protected void dropEquipment() {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
                this.spawnAtLocation(itemstack);
            }
        }
        if (!this.programSlot.getItem(0).isEmpty()) {
            this.spawnAtLocation(this.programSlot.getItem(0));
        }
    }



    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // Health
                .add(Attributes.MOVEMENT_SPEED, 0.25D); // Movement speed
    }



    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // Save the main inventory
        tag.put("inventory", this.inventory.createTag());

        // Save the program slot
        tag.put("programslot", this.programSlot.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // Clear existing contents before loading
        this.inventory.clearContent();
        this.programSlot.clearContent();

        // Load the main inventory
        if (tag.contains("inventory", 9)) { // 9 = TagType.LIST
            this.inventory.fromTag(tag.getList("inventory", 10)); // 10 = TagType.COMPOUND
        }

        // Load the program slot
        if (tag.contains("programslot", 9)) {
            this.programSlot.fromTag(tag.getList("programslot", 10));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.readAdditionalSaveData(tag);
        syncInventory();

    }

    public void syncInventory() {
        if (!level.isClientSide) {
            HashMap<String, Integer> extraData = new HashMap<>();
            extraData.put("entityID", this.getId());

            ModNetwork.sendToAllClients(new SyncRobotPacket(extraData, this.getItems()));
        }
    }

    public boolean hasProgrammer() {
        return !this.programSlot.getItem(0).isEmpty();
    }
    public ItemStack getProgrammer() {
        if (hasProgrammer()) { return this.programSlot.getItem(0); }
        else { return null; }
    }
    public Container getProgramSlot() {
        return this.programSlot;
    }




    public Container getInventory() {
        return this.inventory;
    }

    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>(inventory.getContainerSize());
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            items.add(inventory.getItem(i).copy());
        }
        return items;
    }




    public void prnt(String message) {
        if (!this.level().players().isEmpty()) {
            this.level().players().get(0).sendSystemMessage(Component.literal(message));
        }
    }

}