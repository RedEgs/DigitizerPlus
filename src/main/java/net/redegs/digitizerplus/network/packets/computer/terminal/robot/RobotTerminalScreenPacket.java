package net.redegs.digitizerplus.network.packets.computer.terminal.robot;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.client.screen.computer.terminal.TerminalScreen;

import java.util.function.Supplier;

public class RobotTerminalScreenPacket {
    private final boolean OpenScreen;
    private final int robotID;


    public RobotTerminalScreenPacket(boolean openScreen, int robotID) {
        this.OpenScreen = openScreen;
        this.robotID = robotID;
    }

    public static void encode(RobotTerminalScreenPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.OpenScreen);
        buf.writeInt(pkt.robotID);
    }

    public static RobotTerminalScreenPacket decode(FriendlyByteBuf buf) {
        boolean OpenScreen = buf.readBoolean();
        int robotID = buf.readInt();

        return new RobotTerminalScreenPacket(OpenScreen, robotID);
    }

    public static void handle(RobotTerminalScreenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                if (pkt.OpenScreen) {


                    Minecraft mc = Minecraft.getInstance();
                    HumanoidRobot robot = (HumanoidRobot) mc.level.getEntity(pkt.robotID);

                    mc.setScreen(new TerminalScreen(robot.terminal));
                }
            } else if (context.getDirection().getReceptionSide().isServer()) {
                if (!pkt.OpenScreen) {
                    Minecraft mc = Minecraft.getInstance();
                    HumanoidRobot robot = (HumanoidRobot) mc.level.getEntity(pkt.robotID);

                    robot.terminal.removeWatcher(context.getSender());
                }
            }

        });
        ctx.get().setPacketHandled(true);
    }

}
