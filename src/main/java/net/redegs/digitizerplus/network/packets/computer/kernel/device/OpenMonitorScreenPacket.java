package net.redegs.digitizerplus.network.packets.computer.kernel.device;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.client.screen.computer.kernel.MonitorScreen;

import java.util.function.Supplier;

public class OpenMonitorScreenPacket {
    private static BlockPos blockPos;

    public OpenMonitorScreenPacket(BlockPos pos) {
        blockPos = pos;
    }

    public static void encode(OpenMonitorScreenPacket pkt, FriendlyByteBuf buf) {
       buf.writeBlockPos(blockPos);

    }

    public static OpenMonitorScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenMonitorScreenPacket(buf.readBlockPos());
    }

    public static void handle(OpenMonitorScreenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                Level level = Minecraft.getInstance().level;
                Minecraft.getInstance().setScreen(new MonitorScreen(blockPos));
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
