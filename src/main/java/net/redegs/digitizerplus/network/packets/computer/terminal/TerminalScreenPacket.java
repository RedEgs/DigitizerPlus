package net.redegs.digitizerplus.network.packets.computer.terminal;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.client.screen.computer.TerminalScreen;

import java.util.function.Supplier;

public class TerminalScreenPacket {
    private final boolean OpenScreen;
    private final BlockPos blockEntityPos;


    public TerminalScreenPacket(boolean openScreen, BlockPos blockEntityPos) {
        this.OpenScreen = openScreen;
        this.blockEntityPos = blockEntityPos;
    }

    public static void encode(TerminalScreenPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.OpenScreen);
        buf.writeBlockPos(pkt.blockEntityPos);
    }

    public static TerminalScreenPacket decode(FriendlyByteBuf buf) {
        boolean OpenScreen = buf.readBoolean();
        BlockPos blockEntityPos = buf.readBlockPos();

        return new TerminalScreenPacket(OpenScreen, blockEntityPos);
    }

    public static void handle(TerminalScreenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ComputerEntity entity = (ComputerEntity) mc.level.getBlockEntity(pkt.blockEntityPos);

            if (context.getDirection().getReceptionSide().isClient()) {
                if (pkt.OpenScreen) {
                    TerminalScreen screen = new TerminalScreen(entity.terminal);
                    mc.setScreen(screen);
                    entity.screen = screen;
                }
            } else if (context.getDirection().getReceptionSide().isServer()) {
                if (!pkt.OpenScreen) {
                    entity.terminal.removeWatcher(context.getSender());
                }
            }

        });
        ctx.get().setPacketHandled(true);
    }

}