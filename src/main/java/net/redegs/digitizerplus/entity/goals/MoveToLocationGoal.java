package net.redegs.digitizerplus.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.redegs.digitizerplus.entity.HumanoidRobot;

import java.util.EnumSet;

public class MoveToLocationGoal extends Goal {
    private final Mob entity;
    private final HumanoidRobot robot;
    private final BlockPos targetPos;

    public MoveToLocationGoal(Mob entity, HumanoidRobot robot, BlockPos targetPos) {
        this.entity = entity;
        this.robot = robot;
        this.targetPos = targetPos;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        if (hasReachedTarget()) {
            entity.getNavigation().moveTo((double) targetPos.getX(), (double)  targetPos.getY(), (double) targetPos.getZ(), 1.0);
        }


    }

    @Override
    public boolean canContinueToUse() {
        return hasReachedTarget();
    }

    @Override
    public void stop() {
        entity.getNavigation().stop();
    }

    public boolean hasReachedTarget() {
        return !robot.blockPosition().equals(targetPos);
    }
}
