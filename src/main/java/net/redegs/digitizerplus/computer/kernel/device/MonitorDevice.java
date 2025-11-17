package net.redegs.digitizerplus.computer.kernel.device;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.client.DynamicTextureWrapper;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.DisplayDevicePacket;

public class MonitorDevice implements DisplayDevice{
    private final int width = 8, height = 8;
    private DynamicTextureWrapper texture;

    private ComputerEntity driver; // The object that issues commands to the monitor

    public void init() {
        init(null);
    }

    public void init(ComputerEntity blockEntity) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Minecraft.getInstance().execute(() -> {
                texture = new DynamicTextureWrapper("monitor_texture", width, height);
            });
        }
        driver = blockEntity;
    }


    @Override
    public void drawPixel(int x, int y, int color, boolean client) {
        if (client) {
            texture.setPixel(x, y, color);
        } else if (driver != null) {
            ModNetwork.sendToAllClients(new DisplayDevicePacket(driver.getBlockPos(), x, y, color));
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


        }  else {
            if (driver != null) {
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++)
                        ModNetwork.sendToAllClients(new DisplayDevicePacket(driver.getBlockPos(), x, y, color));
            }
        }
    }
    public void clear(int color) {
        clear(color, false);
    }


    @Override
    public void flush(boolean client) {
        if (client) {
            if (texture != null)
                texture.getDynamicTexture().upload();
        } else if (driver != null) {
            ModNetwork.sendToAllClients(new DisplayDevicePacket(driver.getBlockPos(), 0, 0, 0, true));
        }
    }
    public void flush() {
        flush(false);
    }

    public ResourceLocation getTexture() {
        return texture != null ? texture.getTexture() : null;
    }

    @Override
    public Object call(String method, Object... args) {
        switch (method) {
            case "pixel": drawPixel((int) args[0], (int) args[1], (int) args[2]); return true;
            case "clear": clear((int) args[0]); return true;
            case "flush": flush(); return true;
        }
        return null;
    }


}
