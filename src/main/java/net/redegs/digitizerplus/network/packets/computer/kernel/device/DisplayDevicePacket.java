package net.redegs.digitizerplus.network.packets.computer.kernel.device;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.kernel.device.MonitorDevice;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.util.PacketUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos;

public class DisplayDevicePacket {
    private final BlockPos blockEntityPos;
    private byte[] instructionData;

    // Encode a batch of instructions compactly
    public DisplayDevicePacket(BlockPos pos, MonitorDevice.DisplayInstruction... instructions) {
        this.blockEntityPos = pos;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (MonitorDevice.DisplayInstruction ins : instructions) {
            baos.write(ins.type.id);
            switch (ins.type) {
                case SET_PIXEL -> {
                    baos.write((ins.x >> 8) & 0xFF);
                    baos.write(ins.x & 0xFF);
                    baos.write((ins.y >> 8) & 0xFF);
                    baos.write(ins.y & 0xFF);
                    baos.write((ins.color >> 16) & 0xFF);
                    baos.write((ins.color >> 8) & 0xFF);
                    baos.write(ins.color & 0xFF);
                }
                case CLEAR -> {
                    baos.write((ins.color >> 16) & 0xFF);
                    baos.write((ins.color >> 8) & 0xFF);
                    baos.write(ins.color & 0xFF);
                }
                case FLUSH -> { /* no extra data */ }
            }
        }
        instructionData = baos.toByteArray();
    }

    public static DisplayDevicePacket batch(BlockPos pos, MonitorDevice.DisplayInstruction... instructions) {
        return new DisplayDevicePacket(pos, instructions);
    }

    public static void encode(DisplayDevicePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.blockEntityPos);
        buf.writeVarInt(pkt.instructionData.length);
        buf.writeBytes(pkt.instructionData);
    }

    public static DisplayDevicePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int len = buf.readVarInt();
        byte[] data = new byte[len];
        buf.readBytes(data);
        DisplayDevicePacket pkt = new DisplayDevicePacket(pos); // temp
        pkt.instructionData = data; // set raw data
        return pkt;
    }

    private DisplayDevicePacket(BlockPos pos) { this.blockEntityPos = pos; this.instructionData = new byte[0]; }

    public static void handle(DisplayDevicePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    var be = mc.level.getBlockEntity(pkt.blockEntityPos);
                    if (be instanceof ComputerEntity computer) {
                        MonitorDevice monitor = computer.monitorDevice;
                        ByteBuffer buffer = ByteBuffer.wrap(pkt.instructionData);
                        while (buffer.hasRemaining()) {
                            byte typeId = buffer.get();
                            MonitorDevice.DisplayInstructions type = Arrays.stream(MonitorDevice.DisplayInstructions.values())
                                    .filter(t -> t.id == typeId).findFirst().orElseThrow();
                            switch (type) {
                                case SET_PIXEL -> {
                                    int x = buffer.getShort() & 0xFFFF;
                                    int y = buffer.getShort() & 0xFFFF;
                                    int color = ((buffer.get() & 0xFF) << 16) | ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
                                    monitor.drawPixel(x, y, color, true);
                                }
                                case CLEAR -> {
                                    int color = ((buffer.get() & 0xFF) << 16) | ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
                                    monitor.clear(color, true);
                                }
                                case FLUSH -> monitor.flush(true);
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}