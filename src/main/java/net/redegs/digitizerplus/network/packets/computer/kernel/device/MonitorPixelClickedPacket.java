package net.redegs.digitizerplus.network.packets.computer.kernel.device;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.block.entity.ComputerEntity;

import java.util.function.Supplier;

public class MonitorPixelClickedPacket {
    private final BlockPos pos;
    private final int x;
    private final int y;

    public MonitorPixelClickedPacket(BlockPos pos, int x, int y) {
        this.pos = pos;
        this.x = x; this.y = y;
    }

    public static void encode(MonitorPixelClickedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.x);
        buf.writeInt(pkt.y);
    }

    public static MonitorPixelClickedPacket decode(FriendlyByteBuf buf) {
        return new MonitorPixelClickedPacket(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(MonitorPixelClickedPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;


            if (player.level().getBlockEntity(pkt.pos) instanceof ComputerEntity computer) {
                computer.monitorDevice.handleClick(pkt.x, pkt.y);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
