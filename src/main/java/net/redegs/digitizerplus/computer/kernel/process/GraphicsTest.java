package net.redegs.digitizerplus.computer.kernel.process;

import net.redegs.digitizerplus.computer.kernel.device.DisplayDevice;

import java.util.Random;

public class GraphicsTest implements KernelProcess {
    private final DisplayDevice display;
    private final Random random = new Random();

    public GraphicsTest(DisplayDevice display) {
        this.display = display;
    }

    @Override
    public void run() {
        while (true) {
            int color = random.nextInt(0xFFFFFF + 1) | 0x000000; // rgb hex color (0xRRGGBB)

            display.clear(0xFFFFFF);

            try {
                Thread.sleep(2000); // 2 seconds
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
