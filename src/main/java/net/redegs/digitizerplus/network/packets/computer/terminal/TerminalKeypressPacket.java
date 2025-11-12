package net.redegs.digitizerplus.network.packets.computer.terminal;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    private final int keyCode;     // GLFW key code or typed char
    private final int type;        // 0 = down, 1 = up, 2 = typed
    private final int modifiers;   // GLFW modifier bitmask
    private final BlockPos blockEntityPos;

    public TerminalKeypressPacket(int keyCode, int type, int modifiers, BlockPos blockEntityPos) {
        this.keyCode = keyCode;
        this.type = type;
        this.modifiers = modifiers;
        this.blockEntityPos = blockEntityPos;
    }

    public int getKey() { return keyCode; }
    public int getType() { return type; }
    public int getModifiers() { return modifiers; }

    public static void encode(TerminalKeypressPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.keyCode);
        buf.writeInt(pkt.type);
        buf.writeInt(pkt.modifiers);
        buf.writeBlockPos(pkt.blockEntityPos);
    }

    public static TerminalKeypressPacket decode(FriendlyByteBuf buf) {
        return new TerminalKeypressPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBlockPos());
    }

    public static void handle(TerminalKeypressPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) return;

            ServerPlayer player = context.getSender();
            if (player == null) return;

            BlockEntity be = player.level().getBlockEntity(pkt.blockEntityPos);
            if (!(be instanceof ComputerEntity entity)) return;

            Terminal term = entity.terminal;
            int key = pkt.getKey();

            switch (pkt.getType()) {
                case 0 -> term.keyPressed(key, pkt.getModifiers(), false);
                case 1 -> term.keyReleased(key, pkt.getModifiers());
                case 2 -> term.keyTyped((char) key, pkt.getModifiers());
            }

            term.controlOwner = player;
            term.syncWatchers();
        });
        ctx.get().setPacketHandled(true);
    }
}