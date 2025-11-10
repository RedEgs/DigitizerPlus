package net.redegs.digitizerplus.computer.terminal.program;

import net.redegs.digitizerplus.computer.terminal.Terminal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EchoProgram extends TerminalProgram {

    public EchoProgram(Terminal terminal) {
        super(terminal);
    }

    @Override
    public void start() {
        terminal.setAcceptingInput(false);
        terminal.println("Welcome to Echo!", true);
        terminal.setAcceptingInput(true);

        //terminal.beginInputLine();
        terminal.syncWatchers();
    }

    @Override
    public void onNewline(String line) {
        if (!line.isEmpty()) {
            terminal.println("You said: " + line, true);
            if (line.equals("exit")) {
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

                scheduler.schedule(() -> {
                    terminal.clearBuffer();
                    stop();
                }, 2, TimeUnit.SECONDS);

                terminal.println("Exiting program...", true);


            }

        }

        //terminal.beginInputLine();
        terminal.syncWatchers();
    }


//    @Override
//    public void onEnter() {
//        String input = terminal.getInputLine();
//        terminal.println("You said: " + input, true);
//        terminal.syncWatchers();
//    }
}


