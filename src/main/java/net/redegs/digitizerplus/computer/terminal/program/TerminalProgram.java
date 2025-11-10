package net.redegs.digitizerplus.computer.terminal.program;

import net.redegs.digitizerplus.computer.terminal.Terminal;

public abstract class TerminalProgram {
    protected Terminal terminal;
    protected boolean running = true;
    protected boolean fullControl = false;

    public TerminalProgram(Terminal terminal) {
        this.terminal = terminal;
    }

    public abstract void start(); // Main loop or entry point

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    // Optional hooks
    public void onKeyDown(int key) {}
    public void onKeyUp(int key) {}
    public void onKey(char c) {}
    public void onEnter() {}
    public void onBackspace() {}
    public void upKey() {}
    public void downKey() {}
    public void leftKey() {}
    public void rightKey() {}
    public void onTab() {}
    public void onLeftShift() {}
    public void onLeftControl() {}
    public void controlX() {}
    public void controlS() {}
    public void controlC() {}
    public void controlV() {}

    public void onNewline(String line) {

    }
    public void afterNewline(String line) {}

    public boolean hasFullControl() {
        return this.fullControl;
    }
}
