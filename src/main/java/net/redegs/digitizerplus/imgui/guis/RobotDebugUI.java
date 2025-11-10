package net.redegs.digitizerplus.imgui.guis;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.imgui.GuiContext;
import net.redegs.digitizerplus.imgui.Imgui;

public class RobotDebugUI extends GuiContext {
    private Level level = Minecraft.getInstance().level;
    private Player player = Minecraft.getInstance().player;

    public RobotDebugUI() {
        super();

        Active = false;

    }

    @Override
    public void Main() {
        ImGui.setNextWindowSize(500, 400);

        ImGui.begin("Server Debug Menu", ImGuiWindowFlags.MenuBar);


        for (Entity e : level.getEntities(null, AABB.ofSize(player.position(), 96, 96, 96))) {
            if (e instanceof HumanoidRobot) {
                HumanoidRobot robot = (HumanoidRobot) e;
                if (ImGui.collapsingHeader(robot.getUUID().toString())) {
                    float[] position = {(float) robot.position().x, (float) robot.position().y, (float) robot.position().z};
                    ImGui.inputFloat3("Position", position, "%.3f", ImGuiInputTextFlags.ReadOnly);

                    float health = robot.getHealth();
                    ImGui.inputFloat("Health", new ImFloat(health), 0, 0, "%.1f", ImGuiInputTextFlags.ReadOnly);

                    int[] energy = new int[] { robot.getEnergy() };
                    ImGui.sliderInt("Energy", energy, 0, HumanoidRobot.MAX_ENERGY);
                    //robot.setEnergy(energy[0]);

                    ImGui.separator();

                    Boolean codeExecutionStatus = robot.isCodeExecuting();
                    ImGui.labelText("Is Executing Code: ", String.valueOf(codeExecutionStatus));

                    ImGui.separator();

                    if (ImGui.button("Open Code Editor")) {
                        Imgui.FocusGuiContext(robot.robotUI);
                    }




                }

            }
        }


        ImGui.end();

    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
