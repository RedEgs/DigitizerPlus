package net.redegs.digitizerplus.misc.robot;

import net.redegs.digitizerplus.api.graphics.ButtonGraphic;
import net.redegs.digitizerplus.api.graphics.ImageGraphic;

public class Instruction {
    public int stackIndex;
    public String instName;
    public InstructionGraphic linkedBackground;

    public Instruction(int stackIndex, InstructionGraphic graphic) {
        this.stackIndex = stackIndex;
        this.linkedBackground = graphic;
        this.instName = "ins" + stackIndex;
        graphic.setInstruction(this);
    }





}
