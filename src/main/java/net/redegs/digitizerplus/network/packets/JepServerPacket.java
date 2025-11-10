package net.redegs.digitizerplus.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.python.RobotPythonRunner;

import java.util.Objects;
import java.util.function.Supplier;

public class JepServerPacket {
    private final Integer robot;
    private final String code;


    public JepServerPacket(Integer robotID, String code) {
        this.robot = robotID;
        this.code = code;
    }

    public static void encode(JepServerPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.robot);
        buf.writeUtf(packet.code);
    }

    public static JepServerPacket decode(FriendlyByteBuf buf) {
        int robot = buf.readInt();
        String code = buf.readUtf();

        return new JepServerPacket(robot, code);
    }

    public static void handle(JepServerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ServerLevel level = server.overworld();

                Entity entity = level.getEntity(packet.robot);
                HumanoidRobot robot = (HumanoidRobot) entity;

                if (robot != null) {
                    System.out.println("Found entity");

                    if (Objects.equals(packet.code, "STOP")){
                        try {
                            robot.stopAllPythonThreads();
                            //ModNetwork.sendToAllClients(new SyncRobotCodeState(packet.robot, false));

                            robot.setCodeExecuting(false);
                            System.out.println("Stopped running thread");
                        } catch (Exception e){
                            //ModNetwork.sendToAllClients(new SyncRobotCodeState(packet.robot, false));

                            robot.setCodeExecuting(false);
                            System.out.println("Error when stopping python thread.");
                            System.out.println(e);
                        }
                    } else {
                        RobotPythonRunner runner = new RobotPythonRunner(packet.code, robot, context.getSender());
                        Thread thread = new Thread(runner);

                        thread.start();
                        robot.pythonThreads.put(thread, runner);
                        robot.setCodeExecuting(true);


                        //ModNetwork.sendToAllClients(new SyncRobotCodeState(packet.robot, true));
                    }
                }


            }

        });
        context.setPacketHandled(true);
    }

}
