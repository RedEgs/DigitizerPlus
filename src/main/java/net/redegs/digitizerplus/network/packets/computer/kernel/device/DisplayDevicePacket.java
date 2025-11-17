package net.redegs.digitizerplus.network.packets.computer.kernel.device;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;

import java.util.function.Supplier;

public class DisplayDevicePacket {
    private final BlockPos blockEntityPos;
    private final int x, y, color;
    private final boolean flush;

    public DisplayDevicePacket(BlockPos blockEntityPos, int x, int y, int color) {
        this.blockEntityPos = blockEntityPos;
        this.x = x;
        this.y = y;
        this.color = color;
        this.flush = false;
    }

    public DisplayDevicePacket(BlockPos blockEntityPos, int x, int y, int color, boolean flush) {
        this.blockEntityPos = blockEntityPos;
        this.x = x;
        this.y = y;
        this.color = color;
        this.flush = flush;
    }

    public static void encode(DisplayDevicePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.blockEntityPos);
        buf.writeInt(pkt.x);
        buf.writeInt(pkt.y);
        buf.writeInt(pkt.color);
        buf.writeBoolean(pkt.flush);
    }

    public static DisplayDevicePacket decode(FriendlyByteBuf buf) {
        BlockPos blockEntityPos = buf.readBlockPos();
        int x = buf.readInt();
        int y = buf.readInt();
        int color = buf.readInt();
        boolean flush = buf.readBoolean();
        return new DisplayDevicePacket(blockEntityPos, x, y, color, flush);
    }

    public static void handle(DisplayDevicePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    var blockEntity = mc.level.getBlockEntity(pkt.blockEntityPos);
                    MonitorDevice monitor = ((ComputerEntity) blockEntity).monitorDevice;
                    System.out.println("RECIEVED PACKET FOR " + pkt.color);

                    if (pkt.flush) {
                        monitor.flush(true);
                        return;
                    }
                    monitor.drawPixel(pkt.x, pkt.y, pkt.color, true);

                }

            }
        });
        ctx.get().setPacketHandled(true);
    }
}
