package net.redegs.digitizerplus.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.network.packets.JepEditorPacket;
import net.redegs.digitizerplus.network.packets.JepServerPacket;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.DisplayDevicePacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalClipboardPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalKeypressPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalScreenPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.robot.RobotTerminalKeypressPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.robot.RobotTerminalScreenPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalSyncPacket;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DigitizerPlus.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;


    public static void register() {
        INSTANCE.registerMessage(packetId++,
                JepServerPacket.class,
                JepServerPacket::encode,
                JepServerPacket::decode,
                JepServerPacket::handle);


        INSTANCE.registerMessage(packetId++,
                JepEditorPacket.class,
                JepEditorPacket::encode,
                JepEditorPacket::decode,
                JepEditorPacket::handle);

        INSTANCE.registerMessage(packetId++,
                TerminalSyncPacket.class,
                TerminalSyncPacket::encode,
                TerminalSyncPacket::decode,
                TerminalSyncPacket::handle
        );

        INSTANCE.registerMessage(packetId++,
                TerminalScreenPacket.class,
                TerminalScreenPacket::encode,
                TerminalScreenPacket::decode,
                TerminalScreenPacket::handle
        );

        INSTANCE.registerMessage(packetId++,
                TerminalKeypressPacket.class,
                TerminalKeypressPacket::encode,
                TerminalKeypressPacket::decode,
                TerminalKeypressPacket::handle
        );



        INSTANCE.registerMessage(packetId++,
                RobotTerminalKeypressPacket.class,
                RobotTerminalKeypressPacket::encode,
                RobotTerminalKeypressPacket::decode,
                RobotTerminalKeypressPacket::handle
        );

        INSTANCE.registerMessage(packetId++,
                RobotTerminalScreenPacket.class,
                RobotTerminalScreenPacket::encode,
                RobotTerminalScreenPacket::decode,
                RobotTerminalScreenPacket::handle
        );

        INSTANCE.registerMessage(packetId++,
                TerminalClipboardPacket.class,
                TerminalClipboardPacket::encode,
                TerminalClipboardPacket::decode,
                TerminalClipboardPacket::handle
        );

        INSTANCE.registerMessage(packetId++,
                DisplayDevicePacket.class,
                DisplayDevicePacket::encode,
                DisplayDevicePacket::decode,
                DisplayDevicePacket::handle
        );


        //INSTANCE.registerMessage(packetId)


    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllClients(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

}