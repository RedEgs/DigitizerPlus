package net.redegs.digitizerplus.python;

import jep.Interpreter;
import jep.JepException;
import jep.SharedInterpreter;
import net.minecraft.server.level.ServerPlayer;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.misc.Python;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.JepEditorPacket;

import java.io.PrintWriter;
import java.io.StringWriter;

import static imgui.ImGui.indent;

public class RobotPythonRunner implements Runnable {
    private volatile boolean running = true;
    private String code;
    private HumanoidRobot robot;
    private ServerPlayer codeOwner;

    public RobotPythonRunner(String code, HumanoidRobot robot, ServerPlayer codeOwner) {
        this.code = code;
        this.robot = robot;
        this.codeOwner = codeOwner;
    }
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try (Interpreter interpreter = new SharedInterpreter()) {
            interpreter.set("mc", new MCWrapper());
            interpreter.exec(Python._pycode);

            interpreter.set("__thread", this);
            interpreter.set("__robot", robot.pythonWrapper);

            RobotPythonWrapper.Movement movement = robot.pythonWrapper.new Movement();
            interpreter.set("__robotmovement", movement);

            RobotPythonWrapper.Status status = robot.pythonWrapper.new Status();
            interpreter.set("__robotstatus", status);

            RobotPythonWrapper.Spatial spatial = robot.pythonWrapper.new Spatial();
            interpreter.set("__robotspatial", spatial);

            RobotPythonWrapper.Logistic logistic = robot.pythonWrapper.new Logistic();
            interpreter.set("__robotlogistic", logistic);


            code = code.replaceAll("(?m)^\\s*while ", "while (__robot.isAlive() and __thread.isRunning()) and ");
            code = code.replaceAll("(?m)^\\s*for ", "for (__robot.isAlive() and __thread.isRunning()) and ");
            String wrappedCode = """
            class RobotWrapper:
                def __init__(self, rbt, rbtmovement, rbtspatial, rbtlogistic):
                    self._robot = rbt
                    self.movement = rbtmovement
                    self.spatial = rbtspatial
                    self.logistic = rbtlogistic
                
                def __getattr__(self, name):
                    return getattr(self._robot, name)
                        
            robot = RobotWrapper(__robot, __robotmovement, __robotspatial, __robotlogistic)
                    
            def __user_main__():
            %s
            
            while (__robot.isAlive() and __thread.isRunning()):
                __user_main__()
                break
            """.formatted(code.indent(4));


            //interpreter.exec("robot.movement = _robotmovement");


            interpreter.exec(wrappedCode);

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
}
