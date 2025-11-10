package net.redegs.digitizerplus.network.packets.computer.terminal.robot;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.computer.terminal.Terminal;

import java.util.function.Supplier;

public class RobotTerminalKeypressPacket {
    private final char key; // single char pressed
    private final boolean up; // whether key is down or up
    public int robotID;

    public RobotTerminalKeypressPacket(char key, boolean up, int robotID) {
        this.key = key;
        this.up = up;
        this.robotID = robotID;
    }

    public char getKey() {
        return key;
    }

    // Encode
    public static void encode(RobotTerminalKeypressPacket pkt, FriendlyByteBuf buf) {
        buf.writeChar(pkt.key);
        buf.writeBoolean(pkt.up);
        buf.writeInt(pkt.robotID);
    }

    // Decode
    public static RobotTerminalKeypressPacket decode(FriendlyByteBuf buf) {
        return new RobotTerminalKeypressPacket(buf.readChar(), buf.readBoolean(), buf.readInt());
    }

    // Handle (runs on server!)
    public static void handle(RobotTerminalKeypressPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                Minecraft mc = Minecraft.getInstance();
                HumanoidRobot robot = (HumanoidRobot) mc.level.getEntity(pkt.robotID);
                Terminal term = robot.terminal;

                char key = pkt.getKey();
                if (key == '\b') term.backspace();
                else if (key == '\n') term.newline(false);
                else if (key == '\t') term.tab();
                else if (key == '\u0001') term.leftKey();
                else if (key == '\u0002') term.rightKey();
//                else if (key == '\u0003') term.upKey();
//                else if (key == '\u0004') term.downKey();
                else term.insertChar(key, false);

                term.syncWatchers();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
