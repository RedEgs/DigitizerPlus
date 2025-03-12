package net.redegs.digitizerplus.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.redegs.digitizerplus.block.DigitizerBlock;
import net.redegs.digitizerplus.item.custom.StorageCardItem;
import net.redegs.digitizerplus.screen.DigitizerMenu;
import net.redegs.digitizerplus.screen.StorageBlockMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageBlockEntity extends BlockEntity implements MenuProvider {
    public boolean attached = false;
    public DigitizerEntity attachedDigitizer;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) { // 1 slot for the linker
        @Override
        protected void onContentsChanged(int slot) {
            setChanged(); // Mark the block entity as dirty when the inventory changes
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); // Sync changes to the client

            }
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;

    public StorageBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.STORAGE_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return 0;
            }

            @Override
            public void set(int i, int i1) {
                // Passs
                ;
            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }


    public void AttachDigitizer(DigitizerEntity digitizerEntity) {
        this.attachedDigitizer = digitizerEntity;
        this.attached = true;
    }

    public void DetachDigitizer() {
        this.attachedDigitizer = null;
        this.attached = false;
    }


    public List<ItemStack> GetStorageCards() {
        List<ItemStack> itemList = new ArrayList<>();

       for (int i = 0; i < 4; i++) {
            ItemStack item = this.getItem(i);
            if (!item.isEmpty()) {
                itemList.add(item);
            } else {
                itemList.add(null);
            }
       }

       return itemList;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }


    private void devPrint(String str) {
        this.getLevel().players().get(0).sendSystemMessage(Component.literal(str));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata(); // Sync the entire block entity data to the client
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag); // Load the synced data
    }

    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot); // Get the item in the specified slot
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.digitizerplus.storage_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new StorageBlockMenu(i, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("Inventory", itemHandler.serializeNBT()); // Save the inventory to NBT
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("Inventory")) {
            itemHandler.deserializeNBT(pTag.getCompound("Inventory")); // Load the inventory from NBT
        }
    }
}
