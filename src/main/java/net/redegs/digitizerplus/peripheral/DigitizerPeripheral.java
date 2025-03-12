package net.redegs.digitizerplus.peripheral;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dan200.computercraft.api.peripheral.AttachedComputerSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.redegs.digitizerplus.block.DigitizerBlock;
import net.redegs.digitizerplus.block.ModBlocks;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.block.entity.StorageBlockEntity;
import net.redegs.digitizerplus.item.custom.StorageCardItem;
import net.redegs.digitizerplus.misc.DigitalRegister;
import net.redegs.digitizerplus.misc.DigitalStack;
import net.redegs.digitizerplus.misc.DigitizerDataSaver;

import javax.annotation.Nullable;
import java.util.*;

public class DigitizerPeripheral implements IPeripheral {
    private final AttachedComputerSet computers = new AttachedComputerSet();
    private final DigitizerEntity digitizerEntity;

    public List<BlockPos> linkerPos;
    public List<Container> linkerContainer;

    private Map<Integer, DigitalRegister> digitalRegisters = new HashMap<>();
    private Map<Integer, ItemStack> MountedCards;


    private boolean mounted = false;
    private boolean attached = false;
    private boolean mountedCards = false;

    private StorageBlockEntity storageBlockEntity;

    public DigitizerPeripheral(DigitizerEntity digitizerEntity) {
        this.digitizerEntity = digitizerEntity;

        for (int i = 1; i < 4; i++) {
            digitalRegisters.put(i, new DigitalRegister(i));
        }

    }

    @Override
    public String getType() {
        return "digitizer";
    }


    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        this.attached = digitizerEntity.attached;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }



    @LuaFunction
    public final Boolean hasLinker() {
        return this.digitizerEntity.containsLinker();
    }

    @LuaFunction
    public final void ejectLinker() {
        this.digitizerEntity.dropStoredLinker();
    }

    @LuaFunction
    public final boolean MountLinker() {
        if (this.hasLinker()) {
            linkerContainer = this.digitizerEntity.linkerContainer;
            linkerPos = this.digitizerEntity.linkerPos;
            this.mounted = true;

            return true;
        }
        return false;
    }



    private boolean errorCheck() throws LuaException {
        if (!hasLinker()) {
           throw new LuaException("Digitizer has no linker inserted.");
        }
        if (!mounted) {
            throw new LuaException("Linker has not been mounted.");
        }

        return true;
    }

    private boolean storageDriveCheck() throws LuaException {
        if (!attached) {
            throw new LuaException("This operation requires a storage drive. No drive availible.");
        }

        return true;
    }

    private boolean storageCardCheck(int cardIndex) throws LuaException {
        if (storageDriveCheck()) {
            if (!mountedCards) {
                throw new LuaException("Storage Cards have not been mounted yet.");
            }

            if (cardIndex > MountedCards.size() || cardIndex < 0) {
                throw new LuaException("Invalid card index. (Card doesn't exist or out of range)");
            }

            try {
                ItemStack card = MountedCards.get(cardIndex);
                UUID id = StorageCardItem.GetStorageCardUUID(card);
            } catch (NullPointerException e) {
                throw new LuaException("No card in that slot, or doesn't exist");
            }

            return true;
        } else {
            throw new LuaException("Storage drive has not been mounted.");
        }

    }

    private boolean RegistryCheck(int registryIndex) throws LuaException {
        errorCheck();
        if (registryIndex < 4 && registryIndex > 0) {
            return true;
        } else {
            throw new LuaException("Registry Index Invalid. (Out of range)");
        }
    }

    private boolean storageCardRegistryCheck(int cardIndex, int registryIndex) throws LuaException {
        if (storageCardCheck(cardIndex) && (registryIndex < 4 && registryIndex > 0)) {
            return true;
        } else {
            throw new LuaException("Registry Index Invalid. (Out of range)");
        }
    }



    // Inventory Management

    @LuaFunction
    public final MethodResult getInventoryCount() throws LuaException {
        if (this.errorCheck()) {
            return MethodResult.of(linkerContainer.size());
        }
        return MethodResult.of(null);
    }

    @LuaFunction
    public final MethodResult getLinkedInventories() throws LuaException {
        errorCheck();

        List<String> map = new ArrayList<>();

        if (this.digitizerEntity.containsLinker()) {
            for (Container container : linkerContainer) {
                map.add("Container " + linkerContainer.indexOf(container));
            }

            return MethodResult.of(map);
        } else { return  MethodResult.of(null); }
    }

    @LuaFunction
    public MethodResult getInventoryData(IArguments arguments) throws LuaException {
        errorCheck();

        // Returns a map of all the items in a linked inventory.
        int index = arguments.getInt(0);
        ItemStack linkerItem = this.digitizerEntity.getStoredLinker();

        if (!digitizerEntity.containsLinker()) { return MethodResult.of(null); }
        if (linkerContainer.isEmpty()) { return MethodResult.of(null); }

        if (linkerContainer.get(index) != null) {
            Container container = linkerContainer.get(index);
            Map<String, Object> ContainerData = new HashMap<>();
            BlockPos blockPos = linkerPos.get(index);
            BlockState blockState = digitizerEntity.getLevel().getBlockState(blockPos);

            Map<String, Integer> Position = new HashMap<>();
            Position.put("x", blockPos.getX());
            Position.put("y", blockPos.getX());
            Position.put("z", blockPos.getZ());

            ContainerData.put("container_size", container.getContainerSize());
            ContainerData.put("position", Position);
            ContainerData.put("block", new ItemStack(blockState.getBlock().asItem()).getDisplayName().getString().substring(1, new ItemStack(blockState.getBlock().asItem()).getDisplayName().getString().length() - 1));
            ContainerData.put("container_index", index);

            return MethodResult.of(ContainerData);
        }
        return MethodResult.of(null);
    }

    @LuaFunction
    public final MethodResult getInventoryContents(IArguments arguments) throws LuaException {
        errorCheck();

        // Returns a map of all the items in a linked inventory.
        int index = arguments.getInt(0);
        Map<Integer, Map<String, Object>> items = new HashMap<>();
        // Escape clause

        Container container = linkerContainer.get(index);
        if (container == null) { throw new LuaException( "Inventory index not available"); }

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            Map<String, Object> itemMap = new HashMap<>();

            if (!stack.isEmpty()) { ;
                ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(stack.getItem());

                itemMap.put("name", stack.getDisplayName().getString().substring(1, stack.getDisplayName().getString().length() - 1));
                itemMap.put("id", itemID.toString());
                itemMap.put("count", stack.getCount());
                itemMap.put("slot", i);
                items.put(i+1, itemMap);
            }
        }
        return MethodResult.of(items);
    }

    @LuaFunction
    public final MethodResult getAllContents(IArguments arguments) throws LuaException {
        errorCheck();

        Map<Integer, Map<String, Object>> Items = new HashMap<>();
        int c = 0;
        for (Container container : linkerContainer) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                Map<String, Object> itemMap = new HashMap<>();

                if (!stack.isEmpty()) {
                    ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(stack.getItem());

                    itemMap.put("name", stack.getDisplayName().getString().substring(1, stack.getDisplayName().getString().length() - 1));
                    itemMap.put("id", itemID);
                    itemMap.put("count", stack.getCount());
                    itemMap.put("slot", i);
                    itemMap.put("container_index", c);
                    itemMap.put("total_index", c+i);
                    Items.put(c+i, itemMap);
                }
            }
            c++;
        }

        return MethodResult.of(Items);
    }

    @LuaFunction
    public final MethodResult getItemData(IArguments arguments) throws LuaException {
        // ARG 1 : INT - Slot index
        // ARG 2 : INT - Container index

        errorCheck();

        int containerIndex = arguments.getInt(0);
        int slotIndex = arguments.getInt(1);

        Container container = linkerContainer.get(containerIndex);
        ItemStack item = container.getItem(slotIndex);

        if (!item.isEmpty()) {
            ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(item.getItem());
            Map<String, Object> itemMap = new HashMap<>();

            itemMap.put("name", item.getDisplayName().getString().substring(1, item.getDisplayName().getString().length() - 1));
            itemMap.put("id", itemID.toString());
            itemMap.put("count", item.getCount());
            itemMap.put("slot", slotIndex);
            itemMap.put("container_index", containerIndex);

            return MethodResult.of(itemMap);
        }

        return MethodResult.of(null);
    }

    @LuaFunction
    public final MethodResult searchItem(IArguments arguments) throws LuaException {
        // ARG 1 : STRING - Item Name
        // Returns every instance of a found item

        errorCheck();

        String searchTerm = arguments.getString(0);

        Map<Integer, Map<String, Object>> Items = new HashMap<>();
        int c = 0;
        for (Container container : linkerContainer) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                Map<String, Object> itemMap = new HashMap<>();


                String name = stack.getDisplayName().getString().substring(1, stack.getDisplayName().getString().length() - 1);


                if (!stack.isEmpty() && name.toLowerCase().contains(searchTerm.toLowerCase())) {
                    ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(stack.getItem());

                    itemMap.put("name", stack.getDisplayName().getString().substring(1, stack.getDisplayName().getString().length() - 1));
                    itemMap.put("id", itemID.toString());
                    itemMap.put("count", stack.getCount());
                    itemMap.put("slot", i);
                    itemMap.put("container_index", c);
                    Items.put(c+i, itemMap);
                }
            }
            c++;
        }

        return MethodResult.of(Items);

    }


    // Physical Operations
    @LuaFunction
    public final MethodResult MoveStack(IArguments arguments) throws LuaException {
        // ARG 1 : CONTAINER INDEX 1
        // ARG 2 : SLOT INDEX 1
        // ARG 3 : CONTAINER INDEX 2
        // ARG 4 : SLOT INDEX 2

        errorCheck();

        boolean TransferSuccessful = false;

        if (this.errorCheck()) {

            int containerIndex1 = arguments.getInt(0); int slotIndex1 = arguments.getInt(1);
            int containerIndex2 = arguments.getInt(2);


            if (containerIndex1 > linkerContainer.size()-1 || containerIndex1 < 0){
                throw new LuaException("Invalid container index for argument 1. Exceeds maximum or minimum container index");
            } else if (containerIndex2 > linkerContainer.size()-1 || containerIndex2 < 0) {
                throw new LuaException("Invalid container index for argument 3. Exceeds maximum or minimum container index");
            } else if (slotIndex1 > linkerContainer.get(containerIndex1).getContainerSize()) {
                throw new LuaException("Invalid slot index for argument 2. Exceeds maximum or minimum slot count");
            }

            Container container1 = linkerContainer.get(containerIndex1); ItemStack item1 = container1.getItem(slotIndex1);
            Container container2 = linkerContainer.get(containerIndex2);

            if (item1.isEmpty()) {
                throw new LuaException("Invalid ItemStack for argument 2. (Empty Slot)");
            }

            if (arguments.getInt(3) == -1) {
                for (int i = 0; i < container2.getContainerSize(); i++) {
                    ItemStack item = container2.getItem(i);
                    if (item.isEmpty()) {
                        container2.setItem(i, item1);
                        container1.setItem(slotIndex1, ItemStack.EMPTY);

                        TransferSuccessful = true;
                        break;
                    }
                }
            } else {
                int targetSlot = arguments.getInt(3);
                if (container2.getItem(targetSlot).isEmpty()) {
                    container2.setItem(targetSlot, item1);
                    TransferSuccessful = true;
                } else {
                    throw new LuaException("Target Slot Is Occupied!");
                }
            }
        }

        if (!TransferSuccessful) {
            throw new LuaException("Target Container Full or Invalid Slots.");
        }

        return MethodResult.of(TransferSuccessful);

    }







    // Digital Operations

    @LuaFunction
    public final MethodResult DigitizeStack(IArguments arguments) throws LuaException {
        // ARG 1 : CONTAINER INDEX 1
        // ARG 2 : SLOT INDEX 1
        // ARG 3 : Register Index

        storageDriveCheck();
        boolean Successful = false;

        int containerIndex = arguments.getInt(0);
        int slotIndex = arguments.getInt(1);
        int registerIndex = arguments.getInt(2);

        Container container = linkerContainer.get(containerIndex);
        ItemStack item1 = container.getItem(slotIndex);

        if (containerIndex > linkerContainer.size() - 1 || containerIndex < 0) {
                throw new LuaException("Invalid container index for argument 1. Exceeds maximum or minimum container index");
            } else if (item1.isEmpty()) {
            throw new LuaException("Invalid ItemStack for argument 2. (Empty Slot)");
        }
        if (registerIndex > 4) { throw new LuaException("Invalid Register, Only 1 - 4 Acceptable."); }

        digitalRegisters.get(registerIndex).writeRegister(new DigitalStack(item1));
        container.setItem(slotIndex, ItemStack.EMPTY);

        Successful = true;


        return MethodResult.of(Successful);
    }

    @LuaFunction
    public final MethodResult GetDigitalRegister(IArguments arguments) throws LuaException {
        storageDriveCheck();
        RegistryCheck(arguments.getInt(0));

        int registerIndex = arguments.getInt(0);
        return MethodResult.of(digitalRegisters.get(registerIndex).readRegister().getHashmap());


    }

    @LuaFunction
    public final void ClearDigitalRegister(IArguments arguments) throws LuaException {
        RegistryCheck(arguments.getInt(0));

        digitalRegisters.get(arguments.getInt(0)).ClearRegister();
    }

    @LuaFunction
    public final MethodResult RealizeStack(IArguments arguments) throws LuaException {
        // ARG 1 : CONTAINER INDEX 1
        // ARG 2 : SLOT INDEX 1
        // ARG 3 : REgister index

        storageDriveCheck();
        boolean Successful = false;

        int containerIndex = arguments.getInt(0);
        int registerIndex = arguments.getInt(2);
        Container container = linkerContainer.get(containerIndex);

        if (containerIndex > linkerContainer.size() - 1 || containerIndex < 0) {
            throw new LuaException("Invalid container index for argument 1. Exceeds maximum or minimum container index");
        }  if (registerIndex > 4) { throw new LuaException("Invalid Register, Only 1 - 4 Acceptable."); }


        if (arguments.getInt(1) == -1) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack item = container.getItem(i);
                if (item.isEmpty()) {
                    container.setItem(i, digitalRegisters.get(registerIndex).readRegister().originStack);
                    digitalRegisters.get(registerIndex).ClearRegister();

                    Successful = true;
                    break;
                }
            }
        } else {
            int targetSlot = arguments.getInt(1);
            if (container.getItem(targetSlot).isEmpty()) {
                container.setItem(targetSlot, digitalRegisters.get(registerIndex).readRegister().originStack);
                Successful = true;
            } else {
                throw new LuaException("Target Slot Is Occupied!");
            }
        }

        return MethodResult.of(Successful);
    }




    // Storage Drive Methods
    @LuaFunction
    public final MethodResult MountStorageCards(IArguments arguments) throws LuaException {
        storageDriveCheck();

        MountedCards = new HashMap<>();
        List<ItemStack> storageCards = this.digitizerEntity.attachedStorageBlock.GetStorageCards();

        for (int i = 0; i < 4; i++) {
            if (storageCards.get(i) != null) {
                if (StorageCardItem.GetStorageCardID(storageCards.get(i)) == null) {
                    StorageCardItem.AssignStorageCardID(storageCards.get(i));
                }

                MountedCards.put(i, storageCards.get(i));
            } else {
                MountedCards.put(i, null);
            }
        }

        mountedCards = true;
        return MethodResult.of(true);



    }

    @LuaFunction
    public final MethodResult GetStorageCardsCount(IArguments arguments) throws LuaException {
        storageDriveCheck();

        return MethodResult.of(this.digitizerEntity.attachedStorageBlock.GetStorageCards().size());
    }

    @LuaFunction
    public final MethodResult GetStorageCards(IArguments arguments) throws LuaException {
        storageDriveCheck();

        HashMap<Integer, HashMap<String, Object>> table = new HashMap<>();

        for (int i = 0; i < MountedCards.size(); i++) {
            HashMap<String, Object> cardMap = new HashMap<>();
            if (MountedCards.get(i) != null) {
                cardMap.put("id", StorageCardItem.GetStorageCardID(MountedCards.get(i)));
                cardMap.put("index", i);
                cardMap.put("item_count", StorageCardItem.GetStorageCardItemCount(MountedCards.get(i)) );
            }
            table.put(i, cardMap);
        }

        return MethodResult.of(table);
    }

    @LuaFunction
    public final MethodResult ReadStorageCardContents(IArguments arguments) throws LuaException {
        storageCardCheck(arguments.getInt(0));


        HashMap<Integer, HashMap<String, Object>> table = new HashMap<>();

        int cardIndex = arguments.getInt(0);
        ItemStack card = MountedCards.get(cardIndex);
        UUID id = StorageCardItem.GetStorageCardUUID(card);


        if (digitizerEntity.getLevel() instanceof ServerLevel serverLevel) {
            DigitizerDataSaver data = DigitizerDataSaver.get(serverLevel);
            List<DigitalStack> digitalStackList = data.getDigitalStacks(id);

            devPrint(digitalStackList.toString());
            for (DigitalStack dStack : digitalStackList) {
                HashMap<String, Object> itemMap = new HashMap<>();

                ItemStack stack = dStack.originStack;
                ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(stack.getItem());


                itemMap.put("name", stack.getDisplayName().getString().substring(1, stack.getDisplayName().getString().length() - 1));
                itemMap.put("id", itemID);
                itemMap.put("count", stack.getCount());
                itemMap.put("index", digitalStackList.indexOf(dStack));

                table.put(digitalStackList.indexOf(dStack), itemMap);
            }


        }

        return MethodResult.of(table);
    }




    @LuaFunction
    public final MethodResult SaveDigitalRegister(IArguments arguments) throws LuaException {
        // ARG 1 : Register
        // ARG 2 : Card
        storageCardRegistryCheck(arguments.getInt(1), arguments.getInt(0));

        int registerIndex = arguments.getInt(0);
        int cardIndex = arguments.getInt(1);

        ItemStack card = MountedCards.get(cardIndex);
        UUID id = StorageCardItem.GetStorageCardUUID(card);

        if (digitizerEntity.getLevel() instanceof ServerLevel serverLevel) {
            DigitizerDataSaver data = DigitizerDataSaver.get(serverLevel);

            StorageCardItem.AddToItemCount(card, 1);
            data.addDigitalStack(id, digitalRegisters.get(registerIndex).readRegister());
            digitalRegisters.get(registerIndex).ClearRegister();


            devPrint("Serialised.");

            return MethodResult.of(true);
        }

        return MethodResult.of(false);
    }

    @LuaFunction
    public final MethodResult LoadDigitalRegister(IArguments arguments) throws LuaException {

        storageCardRegistryCheck(arguments.getInt(1), arguments.getInt(0));

        int registerIndex = arguments.getInt(0);
        int cardIndex = arguments.getInt(1);

        ItemStack card = MountedCards.get(cardIndex);
        UUID id = StorageCardItem.GetStorageCardUUID(card);


        if (digitizerEntity.getLevel() instanceof ServerLevel serverLevel) {
            DigitizerDataSaver data = DigitizerDataSaver.get(serverLevel);

            StorageCardItem.RemoveFromItemCount(card, 1);
            digitalRegisters.get(registerIndex).      writeRegister( data.getDigitalStacks(id).get(0) );
            data.removeDigitalStack(id, digitalRegisters.get(registerIndex).readRegister());

            devPrint("Deserialised.");

            return MethodResult.of(true);
        }
        return MethodResult.of(false);
    }



    @LuaFunction
    public final void ResetStorageDrive(IArguments arguments) throws LuaException {
        storageDriveCheck();

        if (this.digitizerEntity.getLevel() instanceof ServerLevel serverLevel) {
            DigitizerDataSaver data = DigitizerDataSaver.get(serverLevel);
            data.clearAll();
        }

    }

    @LuaFunction
    public final void FormatStorageCard(IArguments arguments) throws LuaException {
        storageCardCheck(arguments.getInt(0));

        int cardIndex = arguments.getInt(0);
        ItemStack card = MountedCards.get(cardIndex);
        UUID id = StorageCardItem.GetStorageCardUUID(card);


        if (this.digitizerEntity.getLevel() instanceof ServerLevel serverLevel) {
            DigitizerDataSaver data = DigitizerDataSaver.get(serverLevel);
            data.clearSection(StorageCardItem.GetStorageCardUUID(card));
            StorageCardItem.ResetItemCount(card);
        }
    }





    private void devPrint(String str) {
        digitizerEntity.getLevel().players().get(0).sendSystemMessage(Component.literal(str));
    }


    // Misc
    @LuaFunction
    public final void RGB() {
        Level level = digitizerEntity.getLevel();
        Boolean rgb_state = level.getBlockState(digitizerEntity.getBlockPos()).getValue(DigitizerBlock.RGB);
        level.setBlock(digitizerEntity.getBlockPos(), digitizerEntity.getBlockState().setValue(DigitizerBlock.RGB, !rgb_state), Block.UPDATE_ALL);
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof DigitizerPeripheral o && digitizerEntity == o.digitizerEntity;
    }

}