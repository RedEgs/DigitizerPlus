package net.redegs.digitizerplus.python;

import jep.Interpreter;
import jep.JepException;
import jep.SharedInterpreter;
import net.minecraft.server.level.ServerPlayer;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.misc.commands.Python;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.JepEditorPacket;
import net.redegs.digitizerplus.python.wrappers.PythonTerminalWrapper;
import net.redegs.digitizerplus.python.wrappers.PythonRobotWrapper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

import static imgui.ImGui.indent;

public class RobotPythonRunner implements Runnable {
    private volatile boolean running = true;
    private String code;
    private HumanoidRobot robot;
    private ServerPlayer codeOwner;
    public static ThreadLocal<Boolean> inJep = ThreadLocal.withInitial(() -> false);


    public RobotPythonRunner(String code, HumanoidRobot robot, ServerPlayer codeOwner) {
        this.code = code;
        this.robot = robot;
        this.codeOwner = codeOwner;
    }

    public void stop() {
        inJep.set(false);
        running = false;
        //ModNetwork.sendToAllClients(new SyncRobotCodeState(robot.getId(), false));
    }

    @Override
    public void run() {
        try (Interpreter interpreter = new SharedInterpreter()) {
            interpreter.exec(Python._pycode);

            interpreter.set("__thread", this);
            interpreter.set("__robot", robot.pythonWrapper);


            PythonRobotWrapper.Movement movement = robot.pythonWrapper.new Movement();
            interpreter.set("__robotmovement", movement);

            PythonRobotWrapper.Status status = robot.pythonWrapper.new Status();
            interpreter.set("__robotstatus", status);

            PythonRobotWrapper.Spatial spatial = robot.pythonWrapper.new Spatial();
            interpreter.set("__robotspatial", spatial);

            PythonRobotWrapper.Logistic logistic = robot.pythonWrapper.new Logistic();
            interpreter.set("__robotlogistic", logistic);

            PythonTerminalWrapper terminalWrapper = new PythonTerminalWrapper(robot.terminal);
            interpreter.set("terminal", terminalWrapper);


            code = code.replaceAll("(?m)^\\s*while ", "while (__robot.isAlive() and __thread.isRunning()) and ");
            //code = code.replaceAll("(?m)^\\s*for ", "for (__robot.isAlive() and __thread.isRunning()) and ");

            String wrappedCode = """
            import time as __time__
                        
            def wait(time):
                __time__.sleep(time)
                        
            class RobotWrapper:
                def __init__(self, rbt, rbtmovement, rbtspatial, rbtlogistic, rbtstatus):
                    self._robot = rbt
                    self.movement = rbtmovement
                    self.spatial = rbtspatial
                    self.logistic = rbtlogistic
                    self.status = rbtstatus
               
                def __getattr__(self, name):
                    return getattr(self._robot, name)
                        
            robot = RobotWrapper(__robot, __robotmovement, __robotspatial, __robotlogistic, __robotstatus)
                        
            # Wrap user main as a generator
            def __user_main__():
            %s
           
            while (__robot.isAlive() and __thread.isRunning()):
                __user_main__()
                break
            """.stripIndent();
            this.code = this.code.stripIndent().indent(4);

            wrappedCode = wrappedCode.formatted(this.code);
            interpreter.exec(wrappedCode);

            DigitizerPlus.LOGGER.info("Finished Executing Code");

        }
        catch(JepException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            System.out.println(e);
            ModNetwork.sendToPlayer(new JepEditorPacket(sStackTrace, e.getMessage(), robot.getId()), codeOwner);
        }

    }

    public boolean isRunning() {
        return running;
    }
    public static void setJepAccess(boolean bool) { inJep.set(bool); }
    public static boolean getJepStatus() {return inJep.get(); }
    public static <T> T withAccess(Supplier<T> action) {
        setJepAccess(true);
        try {
            return action.get();
        } finally {
            setJepAccess(false);
        }
    }

    public void serializeInterpreterState() {

    }




}
