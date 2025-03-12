package net.redegs.digitizerplus.misc.robot;

import net.redegs.digitizerplus.api.graphics.ButtonGraphic;
import net.redegs.digitizerplus.api.graphics.ImageGraphic;

import java.util.Stack;

public class Instruction {
    public int stackIndex;
    public Stack stack;


    public InstructionGraphic graphic;

    public String instName;


    public Instruction(Stack stack, InstructionGraphic graphic) {
        this.stack = stack;
        this.stackIndex = stack.size();

        this.graphic = graphic;
        this.instName = "ins" + stackIndex;
        graphic.setInstruction(this);
    }

    public void LinkButtons(Runnable removeCallback) {
        graphic.removeButton.AddCallback(removeCallback);
    }




}
