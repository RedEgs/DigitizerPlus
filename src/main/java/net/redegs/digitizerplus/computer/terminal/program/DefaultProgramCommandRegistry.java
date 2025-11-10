package net.redegs.digitizerplus.computer.terminal.program;

import com.mojang.brigadier.Command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultProgramCommandRegistry {
    private static final Map<String, Method> commandMap = new HashMap<>();

    public static void registerCommands(Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(DefaultProgramCommand.class)) {
                DefaultProgramCommand cmd = method.getAnnotation(DefaultProgramCommand.class);
                commandMap.put(cmd.name(), method);
            }
        }
    }

    public static void runCommand(String name, Object instance) {
        Method method = commandMap.get(name);
        if (method != null) {
            try {
                method.invoke(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Command not found: " + name);
        }
    }

    public static Set<String> getCommandNames() {
        return commandMap.keySet();
    }
}
