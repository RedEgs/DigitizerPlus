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
import net.redegs.digitizerplus.block.ComputerBlock;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.computer.kernel.KernelEngine;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;


import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
            initializeComputer(null);
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
            initializeComputer(computerID);

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
            Path compDir = mainPath.resolve(computerID.toString());

            if (compDir.toFile().exists()) {
                return;
            }

            Files.createDirectory(compDir);




        } catch (IOException e) {
            DigitizerPlus.LOGGER.warn("Failed to create computer directory.");
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public void initializeComputer(UUID uuid) {
        /* Creates the computer ID, file location, kernel and anything else whenever the computer
        *  is instanced for the first time */
        if (uuid != null) {
            computerID = uuid;
        } else {
            computerID = UUID.randomUUID();
        }

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

        if (!this.getBlockState().getValue(ComputerBlock.ON)) {
            return;
        }

        if (computerKernel != null)
            return; // Prevent duplicate kernel instances

        try {
            computerKernel = new KernelEngine(new KernelEngine.VirtualFileSystem(ComputerManager.PathFromUUID(computerID)));
            System.out.println("SPAWNED KERNEL");
        } catch (IOException e) {
            System.out.println("KERNEL SPAWN ERRORED");
            throw new RuntimeException(e);
        }


//
//        Globals lua = JsePlatform.standardGlobals();
//
        String scr = """
        print("Hello world!!!")
        """;
//
//        LuaValue file = lua.load(scr);
//        file.call();


        computerKernel.spawn(() -> {
            KernelEngine.Process p = KernelEngine.current();
            try {
                while (monitorDevice == null) {
                    Thread.sleep(10);
                }
                System.out.println("SPAWNED NEW PROCESS");

                int i = 0;

                monitorDevice.drawText("START", 0, 0, 0xff0000);
                Thread.sleep(1000);
                monitorDevice.clear(0x000000);

                p.onEvent("key", (Object e) -> {
                    monitorDevice.clear(0x000000);
                    MonitorDevice.KeyEvent event = (MonitorDevice.KeyEvent) e;
                    monitorDevice.drawText(String.valueOf(((MonitorDevice.KeyEvent) e).keyCode), 0, 0, 0xff0000);
                });

                p.onEvent("click", (Object e) -> {
                    MonitorDevice.Vector2 vector2 = (MonitorDevice.Vector2) e;
                    monitorDevice.drawPixel(vector2.x, vector2.y, 0xff0000);
                });

            } catch (Throwable t) {
                t.printStackTrace(); // <- shows the real problem
            }
        });


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



    public void onPowerOn() {
        /* Called when powered on */
        System.out.println("Powered On");
        initKernel();
    }

    public void onPowerOff() {
        /* Called when powered off */
        System.out.println("Powered Off");
        computerKernel.shutdown();
    }

    public void onPixelClicked(int x, int y) {
        /* Called when pixel is clicked in world */
        MonitorDevice.Vector2 clickPos = new MonitorDevice.Vector2(x, y);
        computerKernel.events.fire("click", clickPos);
    }
}
