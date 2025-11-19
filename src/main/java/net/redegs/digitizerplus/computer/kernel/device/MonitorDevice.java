package net.redegs.digitizerplus.computer.kernel.device;

import com.mojang.datafixers.types.Func;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.redegs.digitizerplus.block.ComputerBlock;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.client.DynamicTextureWrapper;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.DisplayDevicePacket;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/* TODO
* Could reduce network traffic and computation by only pushing changes, could use a stack or somet for this
* Could use an intermediate buffer CPU side (int[]) to make per pixel changes quicker, then upload changes every frame
*   + Above solution can also be threaded for each MonitorDevice instance
* Reduce colour resolution to reduce texture sizes thus packet sizes
* Custom Shaders for buffer rendering, see here -> https://squiddev.cc/2023/03/18/monitors-again.html
*
* */
public class MonitorDevice implements DisplayDevice {
    public enum DisplayInstructions {
        SET_PIXEL((byte) 0),
        CLEAR((byte) 1),
        FLUSH((byte) 2);

        public final byte id;
        DisplayInstructions(byte id) { this.id = id; }
    }

    public static class DisplayInstruction {
        public final DisplayInstructions type;
        public final int x, y, color;

        public DisplayInstruction(DisplayInstructions type, int x, int y, int color) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public DisplayInstruction(DisplayInstructions type, int color) {
            this(type, 0, 0, color);
        }

        public DisplayInstruction(DisplayInstructions type) {
            this(type, 0, 0, 0);
        }
    }




    public final int width = ComputerBlock.MONITOR_W, height = ComputerBlock.MONITOR_H;
    private final DynamicTextureWrapper texture;
    private final ComputerEntity driver;

    // The object that issues commands to the monitor
    public MonitorDevice(ComputerEntity blockEntity) {
        texture = new DynamicTextureWrapper("monitor_texture", width, height);
        driver = blockEntity;
//        if (FMLEnvironment.dist == Dist.CLIENT) {
//            Minecraft.getInstance().execute(() -> {
//
//            });
//        }
//        init(null);

    }

    public MonitorDevice() {
        texture = new DynamicTextureWrapper("monitor_texture", width, height);
        driver = null;
    }

    @Override
    public void drawPixel(int x, int y, int color, boolean client) {
        if (client) {
            texture.setPixel(x, y, color);
        } else if (driver != null) {
            batch(false, new DisplayInstruction(DisplayInstructions.SET_PIXEL, x, y, color));
        }
    }
    public void drawPixel(int x, int y, int color) {
        drawPixel(x, y, color, false);
    }

    @Override
    public void clear(int color, boolean client) {
        if (client) {
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    texture.setPixel(x, y, color);
        }
        else {
            if (driver != null) {
                batch(false, new DisplayInstruction(DisplayInstructions.CLEAR, color));
            }
        }
    }
    public void clear(int color) {
        clear(color, false);
    }

    @Override
    public void flush(boolean client) {
        if (client) {
            if (texture != null) texture.getDynamicTexture().upload();
        } else if (driver != null) {
            batch(false, new DisplayInstruction(DisplayInstructions.FLUSH));
        }
    }
    public void flush() {
        flush(false);
    }

    public void batch(boolean client, DisplayInstruction... instructions) {
        if (client) {
            for (DisplayInstruction ins : instructions) {
                switch (ins.type) {
                    case SET_PIXEL -> texture.setPixel(ins.x, ins.y, ins.color);
                    case CLEAR -> clear(ins.color, true);
                    case FLUSH -> texture.getDynamicTexture().upload();
                }
            }
        } else if (driver != null && instructions.length > 0) {
            // Split into chunks to avoid huge packets
            int chunkSize = 1024; // adjustable
            for (int i = 0; i < instructions.length; i += chunkSize) {
                DisplayInstruction[] chunk = Arrays.copyOfRange(instructions, i, Math.min(i + chunkSize, instructions.length));
                ModNetwork.sendToAllClients(DisplayDevicePacket.batch(driver.getBlockPos(), chunk));
            }
        }
    }


    public ResourceLocation getTexture() {
        return texture.getTexture();
    }

    @Override
    public Object call(String method, Object... args) {
        switch (method)
        {
            case "pixel": drawPixel((int) args[0], (int) args[1], (int) args[2]); return true;
            case "clear": clear((int) args[0]); return true;
            case "flush": flush(); return true;
        }
        return null;
    }
}