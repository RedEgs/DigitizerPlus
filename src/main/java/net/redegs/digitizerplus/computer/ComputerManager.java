package net.redegs.digitizerplus.computer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.python.PythonRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mod.EventBusSubscriber(modid = DigitizerPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ComputerManager {

    private static Path mainPath;
    private static HashMap<UUID, Object> computers = new HashMap<>();
    private static HashMap<UUID, HashMap<Thread, PythonRunner>> computerThreads = new HashMap<>();

    public ComputerManager() {

    }

    @SubscribeEvent
    public static void OnRobotSpawn(MobSpawnEvent event) throws IOException {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof HumanoidRobot) {
            HumanoidRobot robot = (HumanoidRobot) event.getEntity();
            if (!computers.containsKey(robot.getUUID())) {
                computers.put(robot.getUUID(), robot);

            }
        }
    }

    @SubscribeEvent
    public static void OnChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;

        if (event.isNewChunk()) {
            Object[] positions = event.getChunk().getBlockEntitiesPos().toArray();
            for (Object object : positions) {
                BlockPos pos = (BlockPos) object;
                BlockEntity blockEntity = event.getChunk().getBlockEntity(pos);

                if (blockEntity instanceof ComputerEntity && !computers.containsKey(((ComputerEntity) blockEntity).getComputerID())) {
                    computers.put(((ComputerEntity) blockEntity).getComputerID(), (ComputerEntity) blockEntity);
                }

            }
        }
    }


    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) throws IOException {
        DigitizerPlus.LOGGER.info("DIGITIZER CREATING DIR..");
        Path worldPath = event.getServer().getWorldPath(LevelResource.ROOT);
        Path targetPath = worldPath.resolve("DigitizerPlus");

        if (!Files.exists(targetPath)) {
            Files.createDirectory(targetPath); // Create the main folder
        }
        mainPath = targetPath;
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) throws IOException {
        for (Map.Entry<UUID, Object> set : computers.entrySet()) {
            Object value = set.getValue();
            if (value instanceof ComputerEntity) {
                ComputerEntity computer = (ComputerEntity) value;

            } else if (value instanceof  HumanoidRobot) {
                HumanoidRobot robot = (HumanoidRobot) value;

            }



//            if (robot.codeExecuting) {
//                Path recent = getRecentFile(robot.getUUID().toString());
//                ModNetwork.sendToServer(new JepServerPacket(robot.getId(), Files.readString(recent)));
//            }


        }
        computers.clear();
    }

    public Path getMainPath() {
        return this.mainPath;
    }

    public static void putThread(UUID uuid, HashMap<Thread, PythonRunner> threadMap) {
        DigitizerPlus.LOGGER.info("Placed thread for = ", uuid.toString());
        computerThreads.put(uuid, threadMap);
    }

    public static HashMap<Thread, PythonRunner> getThread(UUID uuid) {
        //DigitizerPlus.LOGGER.info("Removed thread for = ", uuid.toString());
        return computerThreads.get(uuid);
    }

    public static void removeThread(UUID uuid) {
        DigitizerPlus.LOGGER.info("Removed thread for = ", uuid.toString());
        computerThreads.remove(uuid);
    }

    public static void stopThreads(UUID uuid) {

        if (computerThreads.get(uuid) != null) {
            DigitizerPlus.LOGGER.info("Stopping thread for = ", uuid.toString());
            for (PythonRunner runner : computerThreads.get(uuid).values()) {
                runner.stop();
            }
            computerThreads.remove(uuid);
        }

    }



    public static Path getRecentFile(String uuid) {
        Path robotPath = PathFromUUID(uuid);
        Path recentFile = robotPath.resolve("recent.py");

        if (Files.exists(recentFile)) {
            return recentFile;
        }

        return null;
    }

    public static ComputerEntity getComputerAsComputerEntity(UUID uuid) {
        // Returns the computer as it original object e.g Robot, ComputerBlockEntity
        if (computers.containsKey(uuid)) {
            return (ComputerEntity) computers.get(uuid);
        }

        return null;
    }

    public static Object getComputerAs(UUID uuid) {
        // Returns the computer as it original object e.g Robot, ComputerBlockEntity
        if (computers.containsKey(uuid)) {
            return computers.get(uuid);
        }

        return null;
    }


    public static Path PathFromUUID(String uuid) {
        if (!Files.exists(mainPath.resolve(uuid))) {
            return null;
        } else {
            return mainPath.resolve(uuid);
        }
    }

    public static Path PathFromUUID(UUID uuid) {
        if (!Files.exists(mainPath.resolve(uuid.toString()))) {
            return null;
        } else {
            return mainPath.resolve(uuid.toString());
        }
    }


    public static void putComputerEntity(UUID uuid, ComputerEntity entity) {
        DigitizerPlus.LOGGER.info("Put into map = ", uuid );
        computers.put(uuid, entity);
    }

}
