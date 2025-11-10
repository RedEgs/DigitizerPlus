package net.redegs.digitizerplus.python.wrappers;

import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.terminal.Terminal;

public class PythonTerminalWrapper {
    private Terminal terminal;

    public PythonTerminalWrapper (Terminal terminal) {
        this.terminal = terminal;
    }

    public void println(String string) {
        int prev = terminal.getPromptLength();
        boolean val = terminal.isAcceptingInput();

        terminal.setAcceptingInput(false);
//        terminal.setCurrentColor(color);
        terminal.setPromptLength(0);
        terminal.println(string, true);
        terminal.setPromptLength(prev);
        terminal.resetCurrentColor();
        terminal.setAcceptingInput(val);

        this.terminal.syncWatchers();
    }

    public void print(String string) {
        int prev = terminal.getPromptLength();
        boolean val = terminal.isAcceptingInput();

        terminal.setAcceptingInput(false);
        terminal.setPromptLength(0);
        terminal.println(string, true);
        terminal.setPromptLength(prev);
        terminal.resetCurrentColor();
        terminal.setAcceptingInput(val);

        this.terminal.syncWatchers();
    }

    public void clear() {
        this.terminal.clearBuffer();
        this.terminal.syncWatchers();
    }




    public void setCursorVisibility(boolean state) {
        this.terminal.setCursorHidden(state);
    }

    public boolean getCursorVisibility() {
        return this.terminal.isCursorHidden();
    }



    public void log(String string) {
        DigitizerPlus.LOGGER.info(string);
    }

}
