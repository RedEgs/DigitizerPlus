package net.redegs.digitizerplus.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import jep.Interpreter;
import jep.SharedInterpreter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.redegs.digitizerplus.python.MCWrapper;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;


public class Python {

    public static Interpreter interp = new SharedInterpreter();
    public static String _pycode =
    """
    def log(text):
        System.out.println(text)
    
    def print(text):
        mc.print(text)
    """;

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        interp.exec("from java.lang import System");
        interp.set("mc", new MCWrapper());
        interp.exec(_pycode);

        dispatcher.register(
            Commands.literal("py")
                .then(Commands.argument("code", StringArgumentType.greedyString())
                    .executes(commandContext -> {
                        String msg = StringArgumentType.getString(commandContext, "code");
                        try {
                            interp.exec(msg);
                        } catch(Exception e) {
                            System.out.println(e);
                        }

                        return SINGLE_SUCCESS;
                    })
                )
        );
    }
}
