package net.redegs.digitizerplus.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.api.SimpleScreen;
import net.redegs.digitizerplus.api.graphics.ButtonGraphic;
import net.redegs.digitizerplus.api.graphics.ImageGraphic;
import net.redegs.digitizerplus.api.graphics.Position;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.misc.robot.Instruction;
import net.redegs.digitizerplus.misc.robot.InstructionGraphic;


import java.util.HashMap;
import java.util.Stack;

public class HumanoidRobotScreen extends SimpleScreen {
    private static final ResourceLocation MAIN_TEX = new ResourceLocation(DigitizerPlus.MOD_ID, "textures/gui/robot_gui.png");
    private static int imageWidth = 176; private static int imageHeight = 218;
    private static HumanoidRobot robot;

    private ButtonGraphic addInstructionButton = new ButtonGraphic(93, 11, MAIN_TEX, 0, 234, 74, 16);
    private ButtonGraphic executeButton = new ButtonGraphic(93, 108, MAIN_TEX, 74, 218, 74, 16);

    private HashMap<String, Integer> extraData = new HashMap<>();
    private Stack<Instruction> instructionStack = new Stack<Instruction>();



    public HumanoidRobotScreen(HumanoidRobot robot) {
        super(MAIN_TEX, 10, 10, imageWidth, imageHeight, "My Screen");
        this.robot = robot;
        this.extraData.put("entityID", robot.getId());

        this.addEntityRenderer(47, 51, 20, robot);


        this.executeButton.AddCallback(() -> {prnt("hello");});
        this.addInstructionButton.AddCallback(() -> {newInstruction();});

        this.addInventoryGrid(robot.getInventory(), 3, 3, 9, 69);
        this.addButton(this.addInstructionButton); this.addButton(this.executeButton);


        this.addPlayerInventory(robot.level().getNearestPlayer(robot, 4), 8, 136);
        this.addPlayerHotbar(robot.level().getNearestPlayer(robot, 4), 8, 194);

    }


    private Runnable removeInstruction(Instruction instruction) {
        this.instructionStack.remove(instruction);
        for (Instruction instructionI : instructionStack) {
            InstructionGraphic bg = instructionI.graphic;
            bg.setPosition(93, 11 + ((instructionStack.indexOf(instructionI)-1) * 16)); // Stack buttons vertically
        }
        addInstructionButton.setPosition(93, 11 + ((instructionStack.size()) * 16)); // Set 'add instruction' button position
        graphicsToRemove.add(instruction.graphic);
        robot.prnt("removed");
        return null;
    }

    private Runnable newInstruction() {
        // First, update the position of each existing button based on its index
        for (Instruction instruction : instructionStack) {
            InstructionGraphic bg = instruction.graphic;
            bg.setPosition(93, 11 + (instructionStack.indexOf(instruction) * 16)); // Stack buttons vertically
        }

        // Adjust the position of the 'add instruction' button and the new button
        addInstructionButton.setPosition(93, 11 + ((instructionStack.size()+1) * 16)); // Set 'add instruction' button position
        InstructionGraphic instructionGraphic = new InstructionGraphic(93, 11 + (instructionStack.size()) * 16); // New instruction button position


        // Add the new instruction to the stack and queue it for rendering
        Instruction instruction = new Instruction(instructionStack, instructionGraphic);
        instruction.LinkButtons(() -> {removeInstruction(instruction);});
        instructionStack.add(instruction);

        // Add the new button to the list of graphics to add
        graphicsToAdd.add(instructionGraphic);
        robot.prnt(String.valueOf(instructionStack.size()));
        return null;
    }



    private Runnable prnt(String str) {
        robot.prnt(str);
        return null;
    }

}






