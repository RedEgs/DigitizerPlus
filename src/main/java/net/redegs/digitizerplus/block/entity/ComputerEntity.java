package net.redegs.digitizerplus.block.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.client.screen.computer.TerminalScreen;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.computer.kernel.KernelEngine;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.DisplayDevicePacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalScreenPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalSyncPacket;
import net.redegs.digitizerplus.python.PythonRunner;
import net.redegs.digitizerplus.python.RobotPythonRunner;
import net.redegs.digitizerplus.python.wrappers.PythonRobotWrapper;
import net.redegs.digitizerplus.python.wrappers.PythonTerminalWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ComputerEntity extends BlockEntity {
    private UUID computerID;
    private boolean freshlyPlaced = false;
    public boolean monitorInit = false;
    private boolean computerInitialized = false;

    public HashMap<Thread, PythonRunner> pythonThreads;
    public KernelEngine computerKernel;
    public final MonitorDevice monitorDevice = new MonitorDevice();

    public ComputerEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.COMPUTER_BE.get(), pPos, pBlockState);


        monitorDevice.init(this);
        this.monitorInit = true;

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            //pythonWrapper = new PythonTerminalWrapper(this.terminal);
            pythonThreads = new HashMap<>();
        }

    }

//    public void OpenTerminal(ServerPlayer player) {
//        ModNetwork.sendToPlayer(new TerminalSyncPacket(this.terminal.getBuffer(), this.terminal.cursorX, this.terminal.cursorY), (ServerPlayer) player);
//        ModNetwork.sendToPlayer(new TerminalScreenPacket( true, this.getBlockPos()), (ServerPlayer) player);
//        terminal.addWatcher((ServerPlayer) player);
//    }


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!computerInitialized && !level.isClientSide) {
            initializeComputer();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (computerID != null) {
            tag.putUUID("ComputerID", computerID);
        }
     }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        DigitizerPlus.LOGGER.info("LOAD CALLED");


        if (tag.hasUUID("ComputerID")) {
            computerID = tag.getUUID("ComputerID");
            DigitizerPlus.LOGGER.info("Computer ID found = {}", computerID);
            computerInitialized = true;
            ComputerManager.putComputerEntity(computerID, this);
            initKernel();

        } else {
            DigitizerPlus.LOGGER.warn("NO PREVIOUS UUID HAS BEEN FOUND");
        }



    }

    @Override
    public void onLoad() {
        super.onLoad();

//        DigitizerPlus.LOGGER.info("CALLING ON LOAD = {}", computerID);
//        if (!this.freshlyPlaced) terminal = new Terminal(getBlockPos(), computerID, 12, 42);

    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (computerID != null) {
            tag.putUUID("ComputerID", computerID);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
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
        try {
            Path mainPath = DigitizerPlus.COMPUTER_MANAGER.getMainPath();
            Files.createDirectory(mainPath.resolve(computerID.toString()));

        } catch (IOException e) {
            DigitizerPlus.LOGGER.warn("FAILED TO CREATE COMPUTER DIR");
            throw new RuntimeException(e);
        }
    }

    public void initializeComputer() {

        if (computerID == null) {
            computerID = UUID.randomUUID();
            createComputerFileLocation();
            computerInitialized = true;

            //terminal = new Terminal(getBlockPos(), computerID, 12, 42);
            ComputerManager.putComputerEntity(computerID, this);
            initKernel();

            // mark dirty so it actually saves to NBT
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void markAsPlaced(UUID uuid) {
        freshlyPlaced = true; computerInitialized = true;
        computerID = uuid;
        ComputerManager.putComputerEntity(computerID, this);
       //terminal = new Terminal(getBlockPos(), computerID, 12, 42);
        initKernel();

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void initKernel() {
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

        computerKernel.spawn(() -> {
            System.out.println("SPAWNED NEW PROCESS");
            Random random = new Random();
            while (true) {
                int color = random.nextInt(0xFFFFFF + 1);

//                for (int y = 0; y < 8; y++)
//                    for (int x = 0; x < 8; x++)
//                        ModNetwork.sendToAllClients(new DisplayDevicePacket(getBlockPos(), x, y, color));
                //ModNetwork.sendToAllClients(new DisplayDevicePacket(getBlockPos(), 0, 0, color, true));
                monitorDevice.clear(color); // <- Server only atm
                monitorDevice.flush();      // ⚠ Needs client sync via packet
                System.out.println("NEW COLOUR " + color);

                try { Thread.sleep(2000); } catch (InterruptedException e) { break; }
            }
        });
    }
}
