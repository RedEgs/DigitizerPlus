package net.redegs.digitizerplus.block.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.computer.kernel.KernelEngine;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ComputerEntity extends BlockEntity {
    private UUID computerID; /* The ID given to the computer to identify this particular computer */

    private boolean computerInitialized = false; /* Used to tell if the computer has an ID, Kernel etc */

    //public HashMap<Thread, PythonRunner> pythonThreads;
    public KernelEngine computerKernel; /* The (virtual) brain of the computer, controls processes, connects to devices etc. (ALL purely virtual) */
    public MonitorDevice monitorDevice; /* The device that interfaces with the quad in the entity renderer */

    public ComputerEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.COMPUTER_BE.get(), pPos, pBlockState);

        Minecraft.getInstance().execute(() -> {
            monitorDevice = new MonitorDevice(this);
        });

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            //pythonWrapper = new PythonTerminalWrapper(this.terminal);
            //pythonThreads = new HashMap<>();

        } else {

        }

    }

//    public void OpenTerminal(ServerPlayer player) {
//        ModNetwork.sendToPlayer(new TerminalSyncPacket(this.terminal.getBuffer(), this.terminal.cursorX, this.terminal.cursorY), (ServerPlayer) player);
//        ModNetwork.sendToPlayer(new TerminalScreenPacket( true, this.getBlockPos()), (ServerPlayer) player);
//        terminal.addWatcher((ServerPlayer) player);
//    }


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

        /* Sometimes load and unload methods can be unreliable depending on if the chunk has requested the block
        *  to be loaded so i brute force it here, so as soon as its loaded/can tick it loads the computer ID, Kernel etc */
        if (!computerInitialized && !level.isClientSide) {
            initializeComputer();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        /* Save the computer ID to the block entity, ABSOLUTELY necessary for it to persist anything */
        if (computerID != null) {
            tag.putUUID("ComputerID", computerID);
        }

     }

    @Override
    public void load(CompoundTag tag) {
        /* Load ID from NBT, marking computer as initialised & load the kernel */
        super.load(tag);
        if (tag.hasUUID("ComputerID")) {
            computerID = tag.getUUID("ComputerID");
            ComputerManager.putComputerEntity(computerID, this);

            computerInitialized = true;


        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        /* Saves ID to NBT for chunk loading/unloading, used for client server nbt sync */
        CompoundTag tag = super.getUpdateTag();
        if (computerID != null) {
            tag.putUUID("ComputerID", computerID);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        /* Used when syncing NBT for chunk updates etc */
        super.handleUpdateTag(tag);
        if (tag.hasUUID("ComputerID")) {
            computerID = tag.getUUID("ComputerID");
            ComputerManager.putComputerEntity(computerID, this);
            initKernel();

            computerInitialized = true;
        }
    }

    public UUID getComputerID() {
        return computerID;
    }

    private void createComputerFileLocation() {
        /* When the computer first gets instanced this method creates the
        *  directory inside the `DigitizerPlus` folder inside the world save */

        try {
            Path mainPath = DigitizerPlus.COMPUTER_MANAGER.getMainPath();
            Files.createDirectory(mainPath.resolve(computerID.toString()));

        } catch (IOException e) {
            DigitizerPlus.LOGGER.warn("FAILED TO CREATE COMPUTER DIR");
            throw new RuntimeException(e);
        }
    }

    public void initializeComputer() {
        /* Creates the computer ID, file location, kernel and anything else whenever the computer
        *  is instanced for the first time */
        if (computerID == null) {
            computerID = UUID.randomUUID();
            createComputerFileLocation();
            ComputerManager.putComputerEntity(computerID, this);
            initKernel();

            computerInitialized = true;

            // mark dirty so it actually saves to NBT
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void markAsPlaced(UUID uuid) {
        /* Similar to initialise computer, just loads the computer and its essentials */
        computerID = uuid;
        ComputerManager.putComputerEntity(computerID, this);
        initKernel();

        computerInitialized = true;

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void initKernel() {
        /* Creates the kernel which handles all the processes, devices and code that
        *  gets ran by the user. Essentially the (virtual) brain of the computer. */

        if (this.level == null || this.level.isClientSide)
            return; // Only run on server

        if (computerKernel != null)
            return; // Prevent duplicate kernel instances

        try {
            computerKernel = new KernelEngine(new KernelEngine.VirtualFileSystem(ComputerManager.PathFromUUID(computerID)));
            System.out.println("SPAWNED KERNEL");
        } catch (IOException e) {
            System.out.println("KERNEL SPAWN ERRORED");
            throw new RuntimeException(e);
        }

//        computerKernel.spawn(() -> {
//            System.out.println("SPAWNED NEW PROCESS");
//            Random random = new Random();
//            while (true) {
//
//                ArrayList<MonitorDevice.DisplayInstruction> ins = new ArrayList<>();
//
//                for (int y = 0; y < monitorDevice.height; y++) {
//                    for (int x = 0; x < monitorDevice.width; x++) {
//                        int color = random.nextInt(0xFFFFFF + 1);
//                        ins.add(new MonitorDevice.DisplayInstruction(MonitorDevice.DisplayInstructions.SET_PIXEL, x, y, color));
//                        //monitorDevice.drawPixel(x, y, color);
//                    }
//                }
//
//                monitorDevice.batch(false, ins.toArray(new MonitorDevice.DisplayInstruction[0]));
//                monitorDevice.flush();
//
//                System.out.println("Next iteration");
//
//                try { Thread.sleep(2000); } catch (InterruptedException e) { break; }
//            }
//        });
    }
}
