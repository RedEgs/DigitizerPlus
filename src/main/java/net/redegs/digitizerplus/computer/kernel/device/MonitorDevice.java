package net.redegs.digitizerplus.computer.kernel.device;

import net.minecraft.resources.ResourceLocation;
import net.redegs.digitizerplus.block.ComputerBlock;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.client.DynamicTextureWrapper;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.DisplayDevicePacket;

import java.util.Arrays;

/* TODO
* Could reduce network traffic and computation by only pushing changes, could use a stack or somet for this
* Could use an intermediate buffer CPU side (int[]) to make per pixel changes quicker, then upload changes every frame
*   + Above solution can also be threaded for each MonitorDevice instance
* Reduce colour resolution to reduce texture sizes thus packet sizes
* Custom Shaders for buffer rendering, see here -> https://squiddev.cc/2023/03/18/monitors-again.html
*
 */
public class MonitorDevice implements DisplayDevice {
    public enum DisplayInstructions {
        /* Enum is used to filter command arguments to their correlated instructions so that
           They can be used properly when reconstructed on the client.*/

        /* Manually assign each enum (DisplayInstruction) an integer id as a byte to save on packet sizes */
        SET_PIXEL((byte) 0),
        CLEAR((byte) 1),
        FLUSH((byte) 2),
        DRAW_TEXT((byte) 3);

        public final byte id;
        DisplayInstructions(byte id) { this.id = id; }
    }

    public static class DisplayInstruction {
        /* This class serves as a sort of container for packaging display instructions and their arguments across client <-> server.
           This class is also defined in such a way that it allows for nested instructions, for example when `batch()` is being used.
         */
        public final DisplayInstructions type;
        public final int x, y, color;
        public final String string;

        public DisplayInstruction(DisplayInstructions type, int x, int y, int color, String string) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.color = color;
            this.string = string;
        }

        public DisplayInstruction(DisplayInstructions type, int color, String string) {
            this(type, 0, 0, color, string);
        }

        public DisplayInstruction(DisplayInstructions type) {
            this(type, 0, 0, 0, "");
        }

        public DisplayInstruction(DisplayInstructions type, int x, int y, String string, int color) {
            this(type, x, y, color, string);
        }
    }

    public static class KeyEvent {
        /* Used to handle keyboard inputs coming from the client */
        public final int keyCode;
        public final char character;
        public final boolean pressed;

        public KeyEvent(int keyCode, char character, boolean pressed) {
            this.keyCode = keyCode;
            this.character = character;
            this.pressed = pressed;
        }
    }

    public static class Vector2 {
        public int x;
        public int y;

        public Vector2(int x, int y) {
            this.x = x; this.y = y;
        }

        public int getX() {return x;}

        public int getY() {return y;}
    }

    public final int width = ComputerBlock.MONITOR_W, height = ComputerBlock.MONITOR_H;
    public final int textBufferX = 26, textBufferY = 27; // The size of the text buffer

    private final DynamicTextureWrapper texture;
    private final ComputerEntity computerEntity;
    public String[][] textBuffer = new String[textBufferX][textBufferY];
    private KeyEvent keyBuffer = null;
    private Vector2 clickBuffer = null;

    // The object that issues commands to the monitor
    public MonitorDevice(ComputerEntity blockEntity) {
        texture = new DynamicTextureWrapper("monitor_texture", width, height);
        computerEntity = blockEntity;
//        if (FMLEnvironment.dist == Dist.CLIENT) {
//            Minecraft.getInstance().execute(() -> {
//
//            });
//        }
//        init(null);

    }

    @Override
    public void drawPixel(int x, int y, int color, boolean client) {
        if (client) {
            texture.setPixel(texture.getWidth() - 1 - x, y, color);
        } else if (computerEntity != null) {
            batch(false, new DisplayInstruction(DisplayInstructions.SET_PIXEL, x, y, color, ""));
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

            textBuffer = new String[ComputerBlock.MONITOR_H][ComputerBlock.MONITOR_W];
        }
        else {
            if (computerEntity != null) {
                batch(false, new DisplayInstruction(DisplayInstructions.CLEAR, color, ""));
            }
        }
    }
    public void clear(int color) {
        clear(color, false);
    }
    public void clear() {
        clear(0x000000, false);
    }

    public void drawText(String string, int x, int y, int color, boolean client) {
        if (client) {
            char[] arr = string.toCharArray();
            for (int i=0; i< arr.length && x+i < textBuffer[y].length; i++) {
                textBuffer[y][x+i] = String.valueOf(arr[i]);
                //color[y][x+i] = color;
            }
        } else {
            System.out.println("SENDING BATCH REQ");
            if (computerEntity != null) {
                System.out.println("SENT BATCH REQ");
                batch(false, new DisplayInstruction(DisplayInstructions.DRAW_TEXT,  x, y, string, color));
            }
        }


    }
    public void drawText(String string, int x, int y, int color) {
        drawText(string, x, y, color, false);
    }

    @Override
    public void flush(boolean client) {
        if (client) {
            if (texture != null) texture.getDynamicTexture().upload();
        } else if (computerEntity != null) {
            batch(false, new DisplayInstruction(DisplayInstructions.FLUSH));
        }
    }
    public void flush() {
        flush(false);
    }

    public void batch(boolean client, DisplayInstruction... instructions) {
        /* Allows the user to batch display commands, this reduces server lag, client lag etc.
        *  Work can be done in one go on the client rather than splitting one job into multiple packets.
        *  E.g, 100 `setPixel()` packets can be done in one command, saving on 99 individual packets.
        */
        if (client) {
            for (DisplayInstruction ins : instructions) {
                /* Iterate through all the instructions inside of an instruction since they can be nested */
                switch (ins.type) {
                    case SET_PIXEL -> texture.setPixel(ins.x, ins.y, ins.color);
                    case CLEAR -> clear(ins.color, true);
                    case FLUSH -> texture.getDynamicTexture().upload();
                    case DRAW_TEXT -> drawText(ins.string, ins.x, ins.y, ins.color, true);
                }
            }
        } else if (computerEntity != null && instructions.length > 0) {
            /* Splits large packets into smaller chunks to benefit from batching as much as possible
            *  E.g 1000 `setPixel()` operations may automatically be split into 4 packet batches of 250 `setPixel()`
            *  This is to comply with minecraft's netcode limitations and also benefits those on lower bandwidth connections */

            int chunkSize = 1024; /* Minecraft's packet size limit */
            for (int i = 0; i < instructions.length; i += chunkSize) {
                DisplayInstruction[] chunk = Arrays.copyOfRange(instructions, i, Math.min(i + chunkSize, instructions.length));
                ModNetwork.sendToAllClients(DisplayDevicePacket.batch(computerEntity.getBlockPos(), chunk));
            }
        }
    }

    public void handleKey(int keyCode, char ch, boolean pressed) {
        KeyEvent event = new KeyEvent(keyCode, ch, pressed);

        /* Fire events on the kernel's event bus */
        computerEntity.computerKernel.events.fire("key", event);

        /* Add it to the key queue */
        keyBuffer = event;

    }

    public void handleClick(int x, int y) {
        Vector2 clickPos = new Vector2(x, y);
        computerEntity.computerKernel.events.fire("click", clickPos);
        clickBuffer = clickPos;
    }

    public ResourceLocation getTexture() {
        return texture.getTexture();
    }

}
