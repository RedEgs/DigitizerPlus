package net.redegs.digitizerplus.client.screen.computer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.terminal.Cell;
import net.redegs.digitizerplus.computer.terminal.RobotTerminal;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalKeypressPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalScreenPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.robot.RobotTerminalKeypressPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.robot.RobotTerminalScreenPacket;
import net.redegs.digitizerplus.util.KeyUtils;
import org.lwjgl.glfw.GLFW;

public class TerminalScreen extends Screen {
    private final Terminal terminal;
    private final Font font = Minecraft.getInstance().font;

    private static final long CURSOR_BLINK_INTERVAL = 500; // milliseconds

    public TerminalScreen(Terminal terminal) {
        super(Component.literal("Terminal"));
        this.terminal = terminal;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.renderBackground(gfx);

        int frameX = (this.width - 253) / 2;
        int frameY = (this.height - 136) / 2;

        // Terminal background
        gfx.fill(frameX + 8, frameY + 8, frameX + 246, frameY + 128, 0xFF000000);

        Cell[][] buffer = terminal.getBuffer();

        // Render text buffer (monospace grid approach)
        for (int y = 0; y < buffer.length; y++) {

            int drawX = frameX + 8;
            int drawY = frameY + 8 + y * 10;

            for (int x = 0; x < buffer[y].length; x++) {
                Cell cell = buffer[y][x];

                // Draw background color if needed
                if (cell.bgColor != 0x00000000) { // if the cell bg is not transparent
                    gfx.fill(drawX, drawY - 1, drawX + font.width(Character.toString(cell.ch)), drawY + 12 - 2, cell.bgColor);
                }

                // Draw character
                if (cell.ch != '\0') {
                    gfx.drawString(font, Character.toString(cell.ch), drawX, drawY, cell.fgColor);
                }
                drawX += font.width(Character.toString(cell.ch));
            }
        }

        // Draw cursor
        int cursorPx = frameX + 8;
        for (int i = 0; i < terminal.cursorX; i++) {
            if (i >= 42) continue; cursorPx += font.width(Character.toString(buffer[terminal.cursorY][i].ch));

        } if (!terminal.isCursorHidden()) {
            gfx.drawString(font, "_", cursorPx, frameY + 8 + terminal.cursorY * 10, 0xFFFFFF);
        }

        // Terminal UI overlay
        gfx.blit(new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/terminal_ui.png"),
                frameX, frameY, 0, 0, 253, 136);

        super.render(gfx, mouseX, mouseY, delta);
    }

    private boolean isCursorVisible() {
        long time = System.currentTimeMillis();
        return (time / CURSOR_BLINK_INTERVAL % 2) == 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(null);
            return true;
        }
        sendKeyPacket(keyCode, 0, modifiers);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        sendKeyPacket(keyCode, 1, modifiers);
        return true;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (c >= 32 && c <= 126) {
            sendKeyPacket(c, 2, modifiers);
        }
        return true;
    }

    private void sendKeyPacket(int keyOrChar, int type, int modifiers) {
        if (terminal.blockEntityPos != null) {
            ModNetwork.sendToServer(new TerminalKeypressPacket(keyOrChar, type, modifiers, terminal.blockEntityPos));
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        if (terminal.blockEntityPos == null) {
            ModNetwork.sendToServer(new RobotTerminalScreenPacket(false, ((RobotTerminal) terminal).robot.getId()));
        } else {
            ModNetwork.sendToServer(new TerminalScreenPacket(false, terminal.blockEntityPos));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public Terminal getTerminal() {
        return this.terminal;
    }
}