package net.redegs.digitizerplus.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.redegs.digitizerplus.block.DigitizerBlock;
import net.redegs.digitizerplus.item.custom.LinkerItem;
import net.redegs.digitizerplus.client.screen.digitizer.DigitizerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.List;

public class DigitizerEntity extends BlockEntity implements MenuProvider {
    public List<BlockPos> linkerPos;
    public List<Container> linkerContainer;
    private boolean initialised = false;

    public boolean attached = false;
    public StorageBlockEntity attachedStorageBlock;


    private final ItemStackHandler itemHandler = new ItemStackHandler(1) { // 1 slot for the linker
        @Override
        protected void onContentsChanged(int slot) {
            setChanged(); // Mark the block entity as dirty when the inventory changes
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); // Sync changes to the client
                if (containsLinker()) {
                    level.setBlock(worldPosition, getBlockState().setValue(DigitizerBlock.LINKED, true), Block.UPDATE_ALL);;
                    LoadLinkerData(level);
                } else {
                    level.setBlock(worldPosition, getBlockState().setValue(DigitizerBlock.LINKED, false), Block.UPDATE_ALL);
                    UnloadLinkerData();
                }


            }
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;


    public DigitizerEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DIGITIZER_BE.get(), pPos, pBlockState);
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


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        this.setLevel(pLevel);
        if (!initialised) {
            this.LoadLinkerData(pLevel);
            this.AttachStorageDrive(pLevel);



            devPrint("Initialised!");
            initialised = true;
        }
    }

    public void LoadLinkerData(Level pLevel) {
        if (this.containsLinker()) {

            LinkerItem linkerItem = this.getStoredLinkerItem();
            ItemStack linker = this.getItem(0);

            linkerContainer = linkerItem.getLinkedContainers(linker, pLevel);
            linkerPos = linkerItem.getLinkedPositions(linker);

            devPrint("Loaded Linker Data");

        }
    }

    public void UnloadLinkerData() {
        linkerPos = null; linkerContainer = null;
    }

    public void AttachStorageDrive(Level pLevel) {
        if (pLevel.getBlockEntity(getBlockPos().above(1)) instanceof StorageBlockEntity) {
            attachedStorageBlock = (StorageBlockEntity) pLevel.getBlockEntity(getBlockPos().above(1));

            this.attached = true;
            attachedStorageBlock.AttachDigitizer(this);

            devPrint(this.attachedStorageBlock.toString());
        }
    }

    public void AttachStorageDrive(StorageBlockEntity entity) {
        this.attachedStorageBlock = entity;
        attached = true;
    }

    public void DetachStorageDrive() {
        this.attachedStorageBlock = null;
        attached = false;
    }





    public ItemStack getStoredLinker() {
        return this.itemHandler.getStackInSlot(0);
    }

    public LinkerItem getStoredLinkerItem() {
        if (this.itemHandler.getStackInSlot(0).getItem() instanceof LinkerItem linkerItem) {
            return linkerItem;
        }
        return null;
    }

    public void setStoredLinker(ItemStack item) {
        this.itemHandler.setStackInSlot(0, item);
    }

    public boolean containsLinker() {
        return !this.itemHandler.getStackInSlot(0).isEmpty();
    }

    public void dropStoredLinker() {
        Level level = getLevel();
        BlockPos blockPos = getBlockPos();

        if (!level.isClientSide() && !this.itemHandler.getStackInSlot(0).isEmpty()) {
            double x = blockPos.getX() + 0.5;
            double y = blockPos.getY() + 0.8;
            double z = blockPos.getZ() + 0.5;
            ItemEntity itemEntity = new ItemEntity(level, x, y, z, this.itemHandler.getStackInSlot(0));
            level.addFreshEntity(itemEntity);

            this.itemHandler.setStackInSlot(0, ItemStack.EMPTY); // Clear the slot
            this.UnloadLinkerData();
        }

        if (!level.isClientSide()) {
            level.sendBlockUpdated(blockPos, getBlockState(), getBlockState(), 3); // Sync changes to the client
        }
    }

    public void insertStoredLinker(ItemStack item) {
        if (!level.isClientSide()) {
            this.itemHandler.setStackInSlot(0, item); // Insert the item into the slot
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3); // Sync changes to the client
        }
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
        return Component.translatable("block.digitizerplus.digitizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DigitizerMenu(i, inventory, this, this.data);
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