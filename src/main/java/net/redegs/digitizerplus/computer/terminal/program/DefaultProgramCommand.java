package net.redegs.digitizerplus.computer.terminal.program;
import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DefaultProgramCommand {
    String name();
}


