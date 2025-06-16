package net.redegs.digitizerplus.python;

import jep.SharedInterpreter;
import jep.python.PyObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.misc.Python;
import net.redegs.digitizerplus.python.datatypes.PythonBlock;
import net.redegs.digitizerplus.python.datatypes.PythonEntity;
import net.redegs.digitizerplus.python.datatypes.PythonInventory;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos;

public class RobotPythonWrapper {
    private final HumanoidRobot robotParent;

    public RobotPythonWrapper(HumanoidRobot robotParent) {
        this.robotParent = robotParent;
    }

    public boolean isAlive() {return robotParent.isAlive(); }


    public class Movement {
        public boolean navigateTo(int x, int y, int z) throws Exception {
            // Returns true once the entity has reached the target position
            // Returns false if they're still travelling or haven't reached yet

            if (robotParent.blockPosition().closerThan(new Vec3i(x, y, z), Math.pow(HumanoidRobot.VIEWDISTANCE, 2))) {
                class PathOutOfRangeException extends Exception {
                    public PathOutOfRangeException(String m) {
                        super(m);
                    }
                }

                throw new PathOutOfRangeException("Navigated path must be within " + Math.pow(HumanoidRobot.VIEWDISTANCE, 2) + " blocks of the robot.");
            }

            if (robotParent.blockPosition().equals(new BlockPos(x, y, z))) {
                return true;
            } else {
                robotParent.moveEntityToLocation(x, y, z, 1.0);
                return false;
            }
        }
        public void stopNavigating() {
            try {
                robotParent.getNavigation().stop();
                System.out.println("Navigation Stopped");
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        public void jump() {
            if (robotParent.onGround()) {
                robotParent.jump();
            }
        }
        public void forward() {
            Vec3 look = robotParent.getLookAngle().normalize();
            Vec3 currentPos = robotParent.position();
            Vec3 targetPos = currentPos.add(look.x, 0, look.z); // 1 block forward, horizontal only

            robotParent.moveEntityToLocation((int) targetPos.x, (int) targetPos.y, (int) targetPos.z, 1.0);

            robotParent.hurtMarked = true;
        }
        public void backward() {
            Vec3 look = robotParent.getLookAngle().normalize().reverse();
            Vec3 currentPos = robotParent.position();
            Vec3 targetPos = currentPos.add(look.x, 0, look.z); // 1 block forward, horizontal only

            robotParent.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0); // 1.0 is movement speed

            robotParent.hurtMarked = true;
        }

        public void lookAtPosition(int x, int y, int z) {
            robotParent.facePosition((double) x,(double) y,(double) z);
        }
        public void lookAt(float yaw, float pitch) {
            robotParent.setEntityRotation(yaw, pitch);
        }

        public void lookNorth() {
            robotParent.setEntityRotation(180.0F, 0.0F);
        }
        public void lookEast() {
            robotParent.setEntityRotation(-90.0F, 0.0F);
        }
        public void lookSouth() {
            robotParent.setEntityRotation(0.0F, 0.0F);
        }
        public void lookWest() { robotParent.setEntityRotation(90.0F, 0.0F);}
        public void lookLeft() { robotParent.setEntityRotation(robotParent.getYRot() + 90.0F, 0.0F);}
        public void lookRight() { robotParent.setEntityRotation(robotParent.getYRot() - 90.0F, 0.0F);}

        public void faceUp() { robotParent.setEntityRotation(0.0F, -85.0F);}
        public void faceDown() { robotParent.setEntityRotation(0.0F, 85.F);}

        public void crouch() {
            robotParent.crouch();
        }
        public void uncrouch() {
            robotParent.standup();
        }
        public void standup() {
            robotParent.standup();
        }

    }

    public class Spatial {
        public Object getPosition() {
            Vec3 pos = robotParent.position();
            return new Object[] {pos.x, pos.y, pos.z};
        }
        public Object getPos() {
            return getPosition();
        }
        public float getDistanceToPosition(int x, int y, int z) {
            return (float) robotParent.position().distanceTo(new Vec3(x, y, z));
        }

        public String getDimension() {
            return robotParent.level().dimension().location().toShortLanguageKey();
        }
        public boolean getRaining() {
            return robotParent.level().isRainingAt(robotParent.blockPosition());
        }

        public float getFacingDegrees() {
            // Minecraft's yaw: 0=South, 90=West, 180=North, 270=East
            float yaw = robotParent.getYRot();

            // Normalize to 0-360 range
            yaw = yaw % 360;
            if (yaw < 0) yaw += 360;

            // Convert to mathematical degrees (0=East, 90=North, 180=West, 270=South)
            float mathDegrees = (360 - yaw + 90) % 360;

            return mathDegrees;
        }
        public String getFacingCardinal() {
            // Get the entity's yaw rotation (0-360 degrees)
            float yaw = robotParent.getYRot();

            // Normalize the yaw to 0-360 range
            yaw = yaw % 360;
            if (yaw < 0) yaw += 360;

            // Determine cardinal direction
            if (yaw >= 315 || yaw < 45) {
                return Direction.SOUTH.name();
            } else if (yaw >= 45 && yaw < 135) {
                return Direction.WEST.name();
            } else if (yaw >= 135 && yaw < 225) {
                return Direction.NORTH.name();
            } else {
                return Direction.EAST.name();
            }
        }
        public Object getFacingVector() {
            float yawRadians = (robotParent.getYRot() - 90) * ((float)Math.PI / 180);

            // Return normalized vector (x, 0, z)
            Vec3 facingVec = new Vec3(Math.cos(yawRadians), 0, Math.sin(yawRadians)).normalize();
            return new Object[] {facingVec.x, facingVec.y, facingVec.z};
        }

        public Object getLookingPosition() {
            BlockHitResult blockHit = (BlockHitResult) robotParent.getLookingRayResult();
            BlockPos blockPos = blockHit.getBlockPos();
            
            return new Object[] {blockPos.getX(), blockPos.getY(), blockPos.getZ()};
        }
        public Object getLookingBlock() {
            HitResult hitResult = robotParent.getLookingRayResult();
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHit.getBlockPos();

            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = blockHit.getBlockPos();
                Level level = robotParent.level();

                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();
                BlockEntity blockEntity = level.getBlockEntity(pos);
                return new PythonBlock(state, blockEntity, pos, hitResult);
            }

            return null;
        }
        public Object getLookingEntity() {
            EntityHitResult hitResult = robotParent.getLookingRayEntityResult();
            if (hitResult != null) {
                return new PythonEntity(hitResult.getEntity());
            }
            return null;
        }

        public Object getVisibleEntities() {
            List<Entity> visibleEntities = robotParent.getEntitiesWithinFOV();
            List<PythonEntity> visiblePyEntities = new ArrayList<>();

            for (Entity e: visibleEntities) {
                visiblePyEntities.add(new PythonEntity(e));
            }

            return visiblePyEntities;
        }
        public Object getVisibleBlocks() {
            List<BlockPos> visiblePosition = robotParent.getBlockPosWithinFOV();
            List<PythonBlock> visiblePyBlocks = new ArrayList<>();

            for (BlockPos p : visiblePosition) {
                BlockEntity blockEntity = robotParent.level().getBlockEntity(p);
                visiblePyBlocks.add(new PythonBlock(robotParent.level().getBlockState(p), blockEntity, p, null));
            }

            return visiblePyBlocks;
        }

        public boolean hasInventory(PythonBlock block) {
            if (!robotParent.level().isClientSide) {

                BlockPos blockPos = block.getBlockPosition();

                BlockEntity blockEntity = robotParent.level().getBlockEntity(blockPos);
                BlockState blockState = robotParent.level().getBlockState(blockPos);
                System.out.println(blockEntity);
                System.out.println(blockState);
                if (blockEntity != null) {
                    LazyOptional<?> itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
                    return itemHandler.isPresent();
                }

                //return false;
            }
            return false;
        }
    }

    public class Logistic {
        public void shouldPickupLoot(boolean dropPickup) { robotParent.canPickupLoot = dropPickup; }
        public boolean willPickupLoot() { return robotParent.canPickupLoot; }

        public Object getInventory() { return new PythonInventory(robotParent.getInventory()); }


    }

    public class Status {
        public float getHealth() {
            return robotParent.getHealth();
        }
        public float getMaxHealth() { return robotParent.getMaxHealth(); }

        public boolean isOnGround() { return robotParent.onGround(); }
        public boolean isCrouching () { return robotParent.isCrouching(); }
        public boolean isNavigating () { return robotParent.getNavigation().isInProgress(); }
        public boolean isMoving () { return robotParent.getDeltaMovement().horizontalDistanceSqr() > 0.001 * 0.001; }


    }







}
