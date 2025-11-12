package net.redegs.digitizerplus.network.packets.computer.terminal;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.network.ModNetwork;

import java.util.function.Supplier;

public class TerminalClipboardPacket {
    private String clipboard;
    private boolean request;
    public BlockPos blockEntityPos;

    public TerminalClipboardPacket(BlockPos blockEntityPos, boolean request) {
        this.blockEntityPos = blockEntityPos;
        this.request = request;
        this.clipboard = "";
    }

    public TerminalClipboardPacket(BlockPos blockEntityPos, boolean request, String clipboard) {
        this.blockEntityPos = blockEntityPos;
        this.request = request;
        this.clipboard = clipboard;
    }

    // Decode
    public static void encode(TerminalClipboardPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.blockEntityPos);
        buf.writeBoolean(pkt.request);
        buf.writeUtf(pkt.clipboard);
    }


    public static TerminalClipboardPacket decode(FriendlyByteBuf buf) {
        return new TerminalClipboardPacket(buf.readBlockPos(), buf.readBoolean(), buf.readUtf());
    }

    public static void handle(TerminalClipboardPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ctx.get().enqueueWork(() -> {



            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender(); // server-side player
                if (player == null) return;

                ComputerEntity entity = (ComputerEntity) player.level().getBlockEntity(pkt.blockEntityPos);
                if (entity == null) return;

                Terminal term = entity.terminal;
                term.setClipboard(pkt.clipboard);
                term.clipboardReceived(pkt.clipboard);

            } else if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                String clipboard = "";
                try {
                    clipboard = mc.keyboardHandler.getClipboard();
                } catch (Exception ignored) {

                }
                // Send clipboard back to server
                ModNetwork.sendToServer(new TerminalClipboardPacket(pkt.blockEntityPos, false, clipboard));

            }
        });
        ctx.get().setPacketHandled(true);
    }

}
