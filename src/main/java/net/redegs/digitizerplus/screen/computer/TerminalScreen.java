package net.redegs.digitizerplus.screen.computer;

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
    private final Terminal terminal; // comes from robot sync
    private final Font FONT = Minecraft.getInstance().font;

    private boolean ctrlHeld = false;
    private boolean shiftHeld = false;

    public TerminalScreen(Terminal terminal) {
        super(Component.literal("Terminal"));
        this.terminal = terminal;



        // GET ROBOT REFERENCE FROM PACKET
        // FIGURE OUT HOW TO LINK ROBOT AND TERMINAL 

    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.renderBackground(gfx);

        int frameX = (this.width - 253) / 2;
        int frameY = (this.height - 136) / 2;

        // Terminal background
        gfx.fill(frameX + 8, frameY + 8, frameX + 246, frameY + 128, 0xFF000000);

        Cell[][] buffer = terminal.getBuffer();

        // Draw each character with its own color
        for (int y = 0; y < buffer.length; y++) {
            int drawX = frameX + 8;
            int drawY = frameY + 8 + y * 10;

            for (int x = 0; x < buffer[y].length; x++) {
                Cell cell = buffer[y][x];
                if (cell != null && cell.ch!= '\0') {
                    gfx.drawString(font, Character.toString(cell.ch), drawX, drawY, cell.color);
                }
                drawX += font.width(Character.toString(cell.ch));
            }
        }

        // Draw cursor
        int cursorPx = frameX + 8;
        for (int i = 0; i < terminal.cursorX; i++) {
            if (i >= 42) continue;
            cursorPx += font.width(Character.toString(buffer[terminal.cursorY][i].ch));
        }
        if (!terminal.isCursorHidden()) {
            gfx.drawString(font, "_", cursorPx, frameY + 8 + terminal.cursorY * 10, 0xFFFFFF);
        }

        // Terminal UI overlay
        gfx.blit(new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/terminal_ui.png"),
                frameX, frameY, 0, 0, 253, 136);

        super.render(gfx, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Send key down event
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // Close the GUI manually
            assert this.minecraft != null;
            this.minecraft.setScreen(null);
            return true;
        }

        if (terminal.blockEntityPos != null) {
            ModNetwork.sendToServer(new TerminalKeypressPacket(keyCode, 0, modifiers, terminal.blockEntityPos));
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Send key up event
        if (terminal.blockEntityPos != null) {
            ModNetwork.sendToServer(new TerminalKeypressPacket(keyCode, 1, modifiers, terminal.blockEntityPos));
        }
        return true;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (terminal.blockEntityPos != null) {
            // Send typed (printable) event
            ModNetwork.sendToServer(new TerminalKeypressPacket(c, 2, modifiers, terminal.blockEntityPos));
        }
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (terminal.blockEntityPos == null) {
            ModNetwork.sendToServer(new RobotTerminalScreenPacket(false, ((RobotTerminal) terminal).robot.getId()));
        } else {
            ModNetwork.sendToServer(new TerminalScreenPacket(false , terminal.blockEntityPos));
        }
    }

    public Terminal getTerminal() {
        return this.terminal;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}