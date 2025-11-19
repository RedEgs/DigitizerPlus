package net.redegs.digitizerplus.computer.kernel.device;

import net.redegs.digitizerplus.computer.kernel.KernelEngine;

public interface DisplayDevice extends KernelEngine.Device {
    void drawPixel(int x, int y, int color, boolean client);

    void clear(int color, boolean client);

    void clear(int color);

    void flush(boolean client);
}
