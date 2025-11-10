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
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.network.ModNetwork;
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
import java.util.UUID;

public class ComputerEntity extends BlockEntity {
    public Terminal terminal;
    private UUID computerID;
    private boolean computerInitialized = false;

    public HashMap<Thread, PythonRunner> pythonThreads;

    public ComputerEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.COMPUTER_BE.get(), pPos, pBlockState);

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            //pythonWrapper = new PythonTerminalWrapper(this.terminal);
            pythonThreads = new HashMap<>();
        }

    }

    public void OpenTerminal(ServerPlayer player) {
        ModNetwork.sendToPlayer(new TerminalSyncPacket(this.terminal.getBuffer(), this.terminal.cursorX, this.terminal.cursorY), (ServerPlayer) player);
        ModNetwork.sendToPlayer(new TerminalScreenPacket( true, this.getBlockPos()), (ServerPlayer) player);
        terminal.addWatcher((ServerPlayer) player);
    }


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


        } else {
            DigitizerPlus.LOGGER.warn("NO PREVIOUS UUID HAS BEEN FOUND");
        }



    }

    @Override
    public void onLoad() {
        super.onLoad();
        DigitizerPlus.LOGGER.info("CALLING ON LOAD = {}", computerID);
        terminal = new Terminal(getBlockPos(), computerID, 12, 42);
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

            terminal = new Terminal(getBlockPos(), computerID, 12, 42);
            ComputerManager.putComputerEntity(computerID, this);

            // mark dirty so it actually saves to NBT
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

}
