package net.redegs.digitizerplus.screen;


import jep.Interpreter;
import jep.SharedInterpreter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.api.SimpleScreen;
import net.redegs.digitizerplus.api.graphics.ButtonGraphic;
import net.redegs.digitizerplus.api.graphics.ImageGraphic;
import net.redegs.digitizerplus.api.graphics.Position;
import net.redegs.digitizerplus.entity.HumanoidRobot;


import java.util.HashMap;
import java.util.Stack;

public class HumanoidRobotScreen extends SimpleScreen {
    private static final ResourceLocation MAIN_TEX = new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/robot_gui.png");
    private static int imageWidth = 223; private static int imageHeight = 218;
    private static HumanoidRobot robot;


    public HumanoidRobotScreen(HumanoidRobot robot) {
        super(MAIN_TEX, 10, 10, imageWidth, imageHeight, "My Screen");
        this.robot = robot;
        this.extraData.put("entityID", robot.getId());

        this.addEntityRenderer(47, 51, 20, robot);


//        this.executeButton.AddCallback(() -> {
//            try (Interpreter interp = new SharedInterpreter()) {
//                interp.exec("from java.lang import System");
//                interp.exec("s = 'Hello World'");
//                interp.exec("System.out.println(s)");
//                interp.exec("print(s)");
//                interp.exec("print(s[1:-1])");
//            }
//
//
//
//
////            LuaExecutor.runLuaScript("RedEgs");
//
//
//
//        });
//        this.addButton(this.executeButton);
//        this.addInstructionButton.AddCallback(() -> {newInstruction();});

        this.addInventoryGrid(robot.getInventory(), 3, 3, 9, 69);


        //this.addButton(this.addInstructionButton);


        this.addPlayerInventory(robot.level().getNearestPlayer(robot, 4), 8, 136);
        this.addPlayerHotbar(robot.level().getNearestPlayer(robot, 4), 8, 194);

//        if (robot.getInstructionStack() != null ) {
//            this.instructionStack = robot.getInstructionStack();
//            loadInstructionStack();
//        }

    }

    @Override
    public void onClose() {
        super.onClose();
        //robot.setInstructionStack(this.instructionStack);
    }

//    private Runnable removeInstruction(Instruction instruction) {
//        this.instructionStack.remove(instruction);
//        graphicsToRemove.add(instruction.graphic);
//
//        // Re-stack ALL remaining instructions by new index
//        for (int i = 0; i < instructionStack.size(); i++) {
//            Instruction instructionI = instructionStack.get(i);
//            instructionI.graphic.setPosition(93, 11 + (i * 16)); // Forces update
//            //robot.prnt("Updated position of: " + instructionI.instName + " to Y=" + (11 + (i * 16)));
//        }
//
//        addInstructionButton.setPosition(93, 11 + ((instructionStack.size()) * 16));
//
//
//        robot.prnt("Removed: " + instruction.instName);
//        return null;
//    }
//
//    private Runnable newInstruction() {
//        // Create new instruction at the bottom
//        int newY = 11 + (instructionStack.size() * 16);
//        InstructionGraphic instructionGraphic = new InstructionGraphic(93, newY);
//        Instruction instruction = new Instruction(instructionStack, instructionGraphic);
//        instruction.LinkButtons(() -> removeInstruction(instruction));
//        instructionStack.add(instruction);
//
//        // Update 'add instruction' button
//        addInstructionButton.setPosition(93, 11 + ((instructionStack.size()) * 16));
//
//        graphicsToAdd.add(instructionGraphic);
//        robot.prnt("Added: " + instruction.instName);
//        return null;
//    }

//    private void loadInstructionStack() {
//        for (Instruction instruction : instructionStack) {
//            graphicsToAdd.add(instruction.graphic);
//        }
//        this.addInstructionButton.setPosition(93, 11 + ((instructionStack.size()) * 16));
//    }

//    private Runnable prnt(String str) {
//        robot.prnt(str);
//        return null;
//    }

}






