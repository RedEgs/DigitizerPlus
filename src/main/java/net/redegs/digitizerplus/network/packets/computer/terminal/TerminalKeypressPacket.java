package net.redegs.digitizerplus.network.packets.computer.terminal;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.util.KeyUtils;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class TerminalKeypressPacket {
    private final char key; // single char pressed
    private final int type; // whether key is down or up
    public BlockPos blockEntityPos;

    public TerminalKeypressPacket(char key, int type, BlockPos blockEntityPos) {
        this.key = key;
        this.type = type;
        this.blockEntityPos = blockEntityPos;
    }

    public char getKey() {
        return key;
    }

    // Encode
    public static void encode(TerminalKeypressPacket pkt, FriendlyByteBuf buf) {
        buf.writeChar(pkt.key);
        buf.writeInt(pkt.type);
        buf.writeBlockPos(pkt.blockEntityPos);
    }

    // Decode
    public static TerminalKeypressPacket decode(FriendlyByteBuf buf) {
        return new TerminalKeypressPacket(buf.readChar(), buf.readInt(),buf.readBlockPos());
    }

    // Handle (runs on server!)
    public static void handle(TerminalKeypressPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender(); // server-side player
                if (player == null) return;

                // Get the block entity from the SERVER world
                ComputerEntity entity = (ComputerEntity) player.level().getBlockEntity(pkt.blockEntityPos);
                if (entity == null) return;

                Terminal term = entity.terminal;

                int key = (int) pkt.getKey();

                if (pkt.type == 1) {
                    term.keyReleased(key, false);
                } else if (pkt.type == 0) {
                    term.keyPressed(key, false);
                } else if (pkt.type == 3) {
                    term.keyPressed(key, true);
                }



                term.syncWatchers(); // now actually syncs the real BE’s terminal
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
