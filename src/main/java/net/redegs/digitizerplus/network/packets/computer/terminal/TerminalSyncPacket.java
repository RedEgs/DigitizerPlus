package net.redegs.digitizerplus.network.packets.computer.terminal;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.computer.terminal.Cell;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.client.screen.computer.terminal.TerminalScreen;

import java.util.function.Supplier;

public class TerminalSyncPacket {
    private final int rows, cols;
    private final char[] chars;     // rows * cols
    private final int[] fgColors;   // rows * cols
    private final int[] bgColors;   // rows * cols
    private final int cursorX, cursorY;

    public TerminalSyncPacket(Cell[][] buffer, int cursorX, int cursorY) {
        this.rows = buffer.length;
        this.cols = buffer[0].length;
        this.chars = new char[rows * cols];
        this.fgColors = new int[rows * cols];
        this.bgColors = new int[rows * cols];

        int i = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Cell cell = buffer[y][x];
                this.chars[i] = cell.ch;
                this.fgColors[i] = cell.fgColor;
                this.bgColors[i] = cell.bgColor;
                i++;
            }
        }

        this.cursorX = cursorX;
        this.cursorY = cursorY;
    }

    private TerminalSyncPacket(int rows, int cols, char[] chars, int[] fgColors, int[] bgColors, int cursorX, int cursorY) {
        this.rows = rows;
        this.cols = cols;
        this.chars = chars;
        this.fgColors = fgColors;
        this.bgColors = bgColors;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
    }

    public static void encode(TerminalSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.rows);
        buf.writeVarInt(pkt.cols);

        for (char c : pkt.chars) {
            buf.writeChar(c);
        }
        for (int fg : pkt.fgColors) {
            buf.writeInt(fg);
        }
        for (int bg : pkt.bgColors) {
            buf.writeInt(bg);
        }

        buf.writeVarInt(pkt.cursorX);
        buf.writeVarInt(pkt.cursorY);
    }

    public static TerminalSyncPacket decode(FriendlyByteBuf buf) {
        int rows = buf.readVarInt();
        int cols = buf.readVarInt();
        int size = rows * cols;

        char[] chars = new char[size];
        int[] fgColors = new int[size];
        int[] bgColors = new int[size];

        for (int i = 0; i < size; i++) {
            chars[i] = buf.readChar();
        }
        for (int i = 0; i < size; i++) {
            fgColors[i] = buf.readInt();
        }
        for (int i = 0; i < size; i++) {
            bgColors[i] = buf.readInt();
        }

        int cursorX = buf.readVarInt();
        int cursorY = buf.readVarInt();

        return new TerminalSyncPacket(rows, cols, chars, fgColors, bgColors, cursorX, cursorY);
    }

    public static void handle(TerminalSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();

                Cell[][] buffer = new Cell[pkt.rows][pkt.cols];
                int i = 0;
                for (int y = 0; y < pkt.rows; y++) {
                    for (int x = 0; x < pkt.cols; x++) {
                        buffer[y][x] = new Cell(pkt.chars[i], pkt.fgColors[i], pkt.bgColors[i]);
                        i++;
                    }
                }

                if (mc.screen instanceof TerminalScreen ts) {
                    Terminal term = ts.getTerminal();
                    term.setBuffer(buffer);
                    term.setCursor(pkt.cursorX, pkt.cursorY);
                }
            }
        });
        context.setPacketHandled(true);
    }
}