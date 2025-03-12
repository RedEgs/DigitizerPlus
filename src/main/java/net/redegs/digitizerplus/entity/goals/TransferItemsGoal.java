package net.redegs.digitizerplus.entity.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.redegs.digitizerplus.entity.HumanoidRobot;

import java.util.EnumSet;
import java.util.List;

public class TransferItemsGoal extends Goal {
    private final Mob entity;
    private final HumanoidRobot robot;
    private List<BlockPos> waypoints;
    private int waypointIndex = 0;

    public TransferItemsGoal(Mob entity, double speedModifier) {
        this.entity = entity;
        this.robot = (HumanoidRobot) entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return robot.hasProgrammer();
    }

    @Override
    public void start() {
        if (robot.hasProgrammer()) return;



        if (!waypoints.isEmpty()) {
            waypointIndex = 0;
            moveToNextWaypoint();
        } else {
            prnt("No waypoints available!");
            this.stop();
        }
    }

    @Override
    public void tick() {
        if (robot.hasProgrammer() || waypoints.isEmpty()) {
            stop();
            return;
        }

        BlockPos targetPosition = waypoints.get(waypointIndex);
        double distance = entity.distanceToSqr(targetPosition.getX() + 0.5, targetPosition.getY(), targetPosition.getZ() + 0.5);

        if (distance < 2.25D) { // Stop slightly before reaching the chest (1.5 block distance)
            entity.getNavigation().stop(); // Ensure it doesn't walk onto the chest
            selectNextWaypoint();
        } else if (entity.getNavigation().isDone()) {
            entity.getNavigation().moveTo(targetPosition.getX() + 0.5, targetPosition.getY(), targetPosition.getZ() + 0.5, 1.0D);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return robot.hasProgrammer() && waypointIndex < waypoints.size();
    }

    @Override
    public void stop() {
        prnt("Stopping goal...");
        entity.getNavigation().stop();
    }

    private void selectNextWaypoint() {
        if (waypointIndex < waypoints.size() - 1) {
            waypointIndex++;
            moveToNextWaypoint();
        } else {
            prnt("All waypoints completed.");
            stop();
        }
    }

    private void moveToNextWaypoint() {
        if (waypoints.isEmpty()) {
            stop();
            return;
        }

        BlockPos targetPosition = waypoints.get(waypointIndex);
        prnt("Moving to chest: " + targetPosition);
        entity.getNavigation().moveTo(targetPosition.getX() + 0.5, targetPosition.getY(), targetPosition.getZ() + 0.5, 1.0D);
    }

    private void prnt(String message) {
        if (!entity.level().players().isEmpty()) {
            entity.level().players().get(0).sendSystemMessage(Component.literal(message));
        }
    }
}
