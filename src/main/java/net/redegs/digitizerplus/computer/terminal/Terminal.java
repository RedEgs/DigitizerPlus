package net.redegs.digitizerplus.computer.terminal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.terminal.program.DefaultProgram;
import net.redegs.digitizerplus.computer.terminal.program.TerminalProgram;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalClipboardPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalSyncPacket;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Terminal {
    private Cell[][] buffer;
    public BlockPos blockEntityPos;
    private UUID computerID;

    public int cursorX = 0, cursorY = 0;
    private static final int TAB_WIDTH = 4;
    private int PROMPT_LENGTH = 0;

    private boolean hideCursor = false;
    private boolean acceptingInput = true;
    private int inputLine = 0; // Marks the line of the input
    private int lastInputX = 0, lastInputY = 0;

    public Set<Integer> keysDown;
    public ServerPlayer controlOwner;

    private String clipboard;
    private CompletableFuture<String> clipboardFuture;

    private TerminalProgram currentProgram;
    private ArrayList<ServerPlayer> watchers;

    // Default color for new text
    private int currentColor = 0xFFFFFF;
    private int defaultColor = 0xFFFFFF;

    private int currentBgColor = 0x00000000; // Default transparent
    private int defaultBgColor = 0x00000000;

    public Terminal(int rows, int cols) {
        this.buffer = new Cell[rows][cols];
        this.watchers = new ArrayList<>();
        this.keysDown = new HashSet<>();
        clearBuffer();
    }

    public Terminal(BlockPos blockPos, UUID computerID, int rows, int cols) {
        this.buffer = new Cell[rows][cols];
        this.blockEntityPos = blockPos;
        this.computerID = computerID;
        this.watchers = new ArrayList<>();
        this.keysDown = new HashSet<>();

        clearBuffer();

        DefaultProgram defaultProgram = new DefaultProgram(this);
        startProgram(defaultProgram);
    }

    public void insertChar(char c, boolean override) {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onKey(c);
            return;
        }

        if ((!acceptingInput || cursorY != inputLine) && !override) return;

        if (cursorX < PROMPT_LENGTH) cursorX = PROMPT_LENGTH;

        // place char with currentColor
        if (cursorX < buffer[cursorY].length) {
            buffer[cursorY][cursorX] = new Cell(c, currentColor, currentBgColor);
            cursorX++;
        } else if (cursorY < buffer.length - 1) {
            // wrap to next line
            cursorY++;
            cursorX = PROMPT_LENGTH;
            buffer[cursorY][cursorX] = new Cell(c, currentColor, currentBgColor);
            cursorX++;

            if (!override) {
                inputLine = cursorY;
            }
        }

        // update last input pos if this was injected programmatically
        if (override) {
            lastInputX = cursorX;
            lastInputY = cursorY;
        }

        // let the program know about this key
        if (!override && currentProgram != null && acceptingInput) {
            currentProgram.onKey(c);
        }
    }

    public void keyPressed(int key, int modifiers, boolean typed) {
        keysDown.add(key);
        onKeyDown(key);

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (isAcceptingInput()) {
            if (ctrl && key == GLFW.GLFW_KEY_V) { controlV(); return; }
            if (ctrl && key == GLFW.GLFW_KEY_C) { controlC(); return; }
            if (ctrl && key == GLFW.GLFW_KEY_S) { controlS(); return; }

            switch (key) {
                case GLFW.GLFW_KEY_BACKSPACE -> backspace();
                case GLFW.GLFW_KEY_ENTER -> newline(false);
                case GLFW.GLFW_KEY_TAB -> tab();
                case GLFW.GLFW_KEY_LEFT -> leftKey();
                case GLFW.GLFW_KEY_RIGHT -> rightKey();
                case GLFW.GLFW_KEY_UP -> upKey();
                case GLFW.GLFW_KEY_DOWN -> downKey();
            }
        }
    }

    public void keyReleased(int key, int modifiers) {
        onKeyUp(key);
        keysDown.remove(key);
    }

    public void keyTyped(char c, int modifiers) {
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (ctrl) return; // ignore control shortcuts

        // Normal character input
        insertChar(c, false);
    }

    public void onKeyDown(int key) {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onKeyDown(key);
            return;
        }

        if (currentProgram != null) {
            currentProgram.onKeyDown(key);
        }

    }
    public void onKeyUp(int key) {

        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onKeyUp(key);
            return;
        }

        if (currentProgram != null) {
            currentProgram.onKeyUp(key);
        }
    }


    public void backspace() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onBackspace();
            return;
        }

        if (!acceptingInput || cursorY != inputLine) return;

        if (cursorX > PROMPT_LENGTH) {
            cursorX--;
            buffer[cursorY][cursorX] = new Cell(' ', currentColor, currentBgColor);
        } else if (cursorY > 0) {
            if (cursorY - 1 == inputLine - 1) return;
            cursorY--;
            cursorX = Math.max(lastFilledIndex(cursorY) + 1, PROMPT_LENGTH);
        }

        if (currentProgram != null) {
            currentProgram.onBackspace();
        }
    }
    public void leftKey() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.leftKey();
            return;
        }

        if (cursorX > 0) {
            cursorX--;
        } else if (cursorY > 0) {
            cursorY--;
            cursorX = lastFilledIndex(cursorY) + 1;
        }

        if (currentProgram != null) {
            currentProgram.leftKey();
        }
    }
    public void rightKey() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.rightKey();
            return;
        }

        int limit = lastFilledIndex(cursorY) + 1;
        if (cursorX < limit) {
            cursorX++;
        }

        if (currentProgram != null) {
            currentProgram.rightKey();
        }
    }
    public void upKey() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.upKey();
            return;
        }

        if (currentProgram != null) {
            currentProgram.upKey();
        }
    }
    public void downKey() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.downKey();
            return;
        }

        if (currentProgram != null) {
            currentProgram.downKey();
        }
    }
    public void leftShift() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onLeftShift();
            return;
        }

        if (currentProgram != null) {
            currentProgram.onLeftShift();
        }
    }
    public void leftControl() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onLeftControl();
            return;
        }

        if (currentProgram != null) {
            currentProgram.onLeftControl();
        }
    }
    public void controlX() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.controlX();
            return;
        }

        if (currentProgram != null) {
            currentProgram.controlX();
        }
    }
    public void controlS() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.controlS();
            return;
        }

        if (currentProgram != null) {
            currentProgram.controlS();
        }
    }
    public void controlC() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.controlC();
            return;
        }

        if (currentProgram != null) {
            currentProgram.controlC();
        }
    }
    public void controlV() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.controlV();
            return;
        }

        if (currentProgram != null) {
            currentProgram.controlV();
        }
    }

    public void newline(boolean override) {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onNewline(null);
            currentProgram.afterNewline(null);
            return;
        }

        if (acceptingInput || override) {
            if (cursorY < buffer.length - 1) {
                cursorY++;
            } else {
                scrollBufferUp();
            }
        }


        lastInputX = cursorX;
        lastInputY = cursorY;

        if (!override && currentProgram != null && acceptingInput) {
            DigitizerPlus.LOGGER.info("Fired new line = {}", this.getInputLine());
            currentProgram.onNewline(this.getInputLine());
        }

        cursorX = 0;
        this.inputLine = cursorY;

        if (!override && currentProgram != null && acceptingInput) {
            currentProgram.afterNewline(this.getInputLine());
        }
    }
    public void tab() {
        if (currentProgram != null && currentProgram.hasFullControl()) {
            currentProgram.onTab();
            return;
        }

        int spaces = TAB_WIDTH - (cursorX % TAB_WIDTH);
        for (int i = 0; i < spaces && cursorX < buffer[cursorY].length; i++) {
            cursorX++;
        }
    }

    public void print(String text, boolean override) {
        for (char c : text.toCharArray()) insertChar(c, override);
    }

    public void println(String text, boolean override) {
        setCursor(0, cursorY);
        print(text, override);
        newline(override);
    }

    private void scrollBufferUp() {
        for (int y = 1; y < buffer.length; y++) {
            buffer[y - 1] = Arrays.copyOf(buffer[y], buffer[y].length);
        }
        for (int x = 0; x < buffer[buffer.length - 1].length; x++) {
            buffer[buffer.length - 1][x] = new Cell(' ', currentColor, currentBgColor);
        }

        // 🔑 Adjust cursor and input line after scroll
        if (cursorY > 0) cursorY--;
        if (inputLine > 0) inputLine--;
        if (lastInputY > 0) lastInputY--;
    }


    private int lastFilledIndex(int row) {
        for (int x = buffer[row].length - 1; x >= 0; x--) {
            if (buffer[row][x].ch != ' ' && buffer[row][x].ch != '\0') return x;
        }
        return -1;
    }

    public String getInputLine() {
        StringBuilder sb = new StringBuilder();
        for (int i = PROMPT_LENGTH; i < buffer[inputLine].length; i++) {
            Cell c = buffer[inputLine][i];
            if (i > lastFilledIndex(inputLine)) break;
            sb.append(c.ch);
        }
        return sb.toString();
    }

    public String[] parseCommand(String commandInput) {

        // Split by whitespace
        String[] parts = commandInput.trim().split("\\s+");

        if (parts.length == 0) {
            System.out.println("No command provided.");
            return null;
        }

        String command = parts[0]; // "mkdir"
        String[] arguments = Arrays.copyOfRange(parts, 0, parts.length);

        System.out.println("Command: " + command);
//        for (int i = 0; i < arguments.length; i++) {
//            System.out.println("Arg " + i + ": " + arguments[i]);
//        }

        return arguments;
    }

    public void clearBuffer() {
        for (int y = 0; y < buffer.length; y++) {
            for (int x = 0; x < buffer[y].length; x++) {
                buffer[y][x] = new Cell(' ', currentColor, currentBgColor);
            }
        }

        this.cursorX = 0; this.cursorY = 0;
        this.lastInputX = 0; this.lastInputY = 0;
        this.inputLine = 0;

        this.syncWatchers();
    }

    public Cell[][] getBuffer() {
        return buffer;
    }

    public void setBuffer(Cell[][] buffer) {
        this.buffer = buffer;
        this.syncWatchers();
    }

    public void newBuffer() {
        this.buffer = new Cell[buffer.length][buffer[0].length];
        this.clearBuffer();
    }


    public void setCursor(int x, int  y) {
        cursorX = x; cursorY = y;
        this.syncWatchers();
    }


    public void setPromptLength(int length) {
        this.PROMPT_LENGTH = length;
    }

    public int getPromptLength() {
        return this.PROMPT_LENGTH;
    }

    public void setInputLine(int line) {
        this.inputLine = line;
    }

    public void setClipboard(String text) {
        this.clipboard = text;
    }

    public String getClipboard() { return clipboard; }

    public String requestClipboard(ServerPlayer player) {
        // Requests the clipboard from the client
        clipboardFuture = new CompletableFuture<>();
        // send clipboard request to client
        ModNetwork.sendToPlayer(new TerminalClipboardPacket(blockEntityPos, true), player);

        clipboardFuture
            .orTimeout(2, TimeUnit.SECONDS)
            .thenAccept(clipboard -> {
                if (clipboard == null || clipboard.isEmpty()) {
                    System.out.println("Clipboard empty or timed out.");
                } else {
                    System.out.println("Clipboard: " + clipboard);
                }
            })
            .exceptionally(ex -> {
                System.out.println("Clipboard request failed: " + ex);
                return null;
            });
        return clipboard;
    }

    public void clipboardReceived(String clipboard) {
        if (clipboardFuture != null && !clipboardFuture.isDone()) {
            clipboardFuture.complete(clipboard); // releases the wait above
        }
    }




    public void stopProgram() {
        if (currentProgram != null) {
            currentProgram.stop();
            currentProgram = null;
        }
    }

    public void startProgram(TerminalProgram program) {
        this.currentProgram = program;
        program.start();
    }

    public TerminalProgram getProgram() {
        if (currentProgram != null) {
            return this.currentProgram;
        }
        return null;
    }

    public void beginInputLine(String promptText) {
        int prevLength = getPromptLength();
        setPromptLength(0);
        setAcceptingInput(false);
        setCursor(0, cursorY);
        print(promptText, true);
        setPromptLength(prevLength);

        setCursor(getPromptLength(), cursorY);
        setInputLine(cursorY);
        setAcceptingInput(true);
    }

    public void syncWatchers() {
        for (ServerPlayer player : watchers) {
            DigitizerPlus.LOGGER.info("SYNCING WATCHERS");
            ModNetwork.sendToPlayer(
                    new TerminalSyncPacket(buffer, cursorX, cursorY),
                    player
            );
        }
    }

    public void addWatcher(ServerPlayer player) {
        this.watchers.add(player);
    }

    public void removeWatcher(ServerPlayer player) {
        this.watchers.remove(player);
    }

    public ArrayList<ServerPlayer> getWatchers() {
        return this.watchers;
    }




    public boolean isAcceptingInput() {
        return this.acceptingInput;
    }

    public void setAcceptingInput(boolean state) {
        this.acceptingInput = state;
    }

    public boolean isCursorHidden() { return this.hideCursor; }

    public void setCursorHidden(boolean state) { this.hideCursor = state; }


    public void resetCurrentColor() {
        this.currentColor = defaultColor;
    }

    public void setCurrentColor(int color) {
        this.currentColor = color;
    }

    public void setCurrentBackground(int color) {
        this.currentBgColor = color;
    }

    public void resetCurrentBackground() {
        this.currentBgColor = defaultBgColor;
    }

    public UUID getComputerID() {
        return  this.computerID;
    }

}