package net.redegs.digitizerplus.network.packets.computer.kernel.device;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalKeypressPacket;

import java.util.function.Supplier;


public class MonitorKeypressedPacket {
    private final BlockPos pos;
    private final int keyCode;
    private final char character;
    private final boolean pressed;

    public MonitorKeypressedPacket(BlockPos pos, int keyCode, char character, boolean pressed) {
        this.pos = pos;
        this.keyCode = keyCode;
        this.character = character;
        this.pressed = pressed;
    }

    public static void encode(MonitorKeypressedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.keyCode);
        buf.writeChar(pkt.character);
        buf.writeBoolean(pkt.pressed);
    }

    public static MonitorKeypressedPacket decode(FriendlyByteBuf buf) {
        return new MonitorKeypressedPacket(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readChar(),
                buf.readBoolean()
        );
    }

    public static void handle(MonitorKeypressedPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;


            if (player.level().getBlockEntity(pkt.pos) instanceof ComputerEntity computer) {
                computer.monitorDevice.handleKey(pkt.keyCode, pkt.character, pkt.pressed);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
