package net.redegs.digitizerplus.computer.terminal;

import net.redegs.digitizerplus.entity.HumanoidRobot;

public class RobotTerminal extends Terminal {
    public HumanoidRobot robot;

    public RobotTerminal(HumanoidRobot robot, int rows, int cols) {
        super(rows, cols);
        this.robot = robot;
    }
}
