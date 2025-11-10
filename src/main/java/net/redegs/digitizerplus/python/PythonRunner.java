package net.redegs.digitizerplus.python;

import jep.Interpreter;
import jep.JepException;
import jep.SharedInterpreter;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.computer.terminal.program.DefaultProgram;
import net.redegs.digitizerplus.python.wrappers.PythonComputerWrapper;
import net.redegs.digitizerplus.python.wrappers.PythonTerminalWrapper;
import net.redegs.digitizerplus.python.wrappers.PythonThreadWrapper;

import java.util.UUID;

public class PythonRunner implements Runnable {
    private final UUID parentUUID;
    private final Terminal parentTerminal;

    private volatile boolean running = true;
    private String code;
    public static ThreadLocal<Boolean> jepInterpreter = ThreadLocal.withInitial(() -> false);

    public PythonRunner(UUID parentUUID, Terminal terminal) {
        this.parentUUID = parentUUID;
        this.parentTerminal = terminal;
        //this.parentTerminal = ((ComputerEntity) ComputerManager.getComputerAs(parentUUID)).terminal;
    }

    @Override
    public void run() {
        try (Interpreter interpreter = new SharedInterpreter()) {
            PythonTerminalWrapper terminalWrapper = new PythonTerminalWrapper(parentTerminal);
            PythonThreadWrapper threadWrapper = new PythonThreadWrapper(this);
            PythonComputerWrapper computerWrapper = new PythonComputerWrapper(ComputerManager.getComputerAsComputerEntity(parentUUID));

            interpreter.set("terminal", terminalWrapper);
            interpreter.set("program", threadWrapper);
            interpreter.set("computer", computerWrapper);
            interpreter.set("__thread", this);

            code = code.replaceAll("(?m)^\\s*while ", "while (__thread.isRunning()) and ");
            String wrappedCode = """
            import time as __time__
                        
            def wait(time):
                __time__.sleep(time)

            def __user_main__():
            %s
           
            while (__thread.isRunning()):
                __user_main__()
                break
                
            __thread.stop()
            """.stripIndent();
            this.code = this.code.stripIndent().indent(4);

            wrappedCode = wrappedCode.formatted(this.code);
            interpreter.exec(wrappedCode);


        } catch(JepException e) {
            DigitizerPlus.LOGGER.info("Caught exception in python thread");
            DigitizerPlus.LOGGER.error(e.toString());

            try {
                DefaultProgram program = (DefaultProgram) parentTerminal.getProgram();
                program.print("Caught exception in python thread: ", 0xFF0000);
                program.print(e.toString(), 0xFF0000);
                parentTerminal.syncWatchers();
            } catch (Exception ignored) {
                ;
            }


            this.stop();
        }
    }

    public void stop() {
        DigitizerPlus.LOGGER.info("Stopping thread...");

        jepInterpreter.set(false);
        running = false;

        ComputerManager.removeThread(parentUUID);
    }

    public boolean isRunning() {
        return running;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
