package net.redegs.digitizerplus.python.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.python.RobotPythonRunner;
import net.redegs.digitizerplus.python.datatypes.PythonBlock;
import net.redegs.digitizerplus.python.datatypes.PythonEntity;
import net.redegs.digitizerplus.python.datatypes.PythonInventory;
import net.redegs.digitizerplus.util.ContainerUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PythonRobotWrapper {
    private final HumanoidRobot robotParent;

    public PythonRobotWrapper(HumanoidRobot robotParent) {
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
        public boolean navigateTo(Vec3 pos) throws Exception {
            return navigateTo((int) pos.x, (int) pos.y, (int) pos.z);
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
                robotParent.consumeEnergy(25);
            }
        }
        public void forward() {
            Vec3 look = robotParent.getLookAngle().normalize();
            Vec3 currentPos = robotParent.position();
            Vec3 targetPos = currentPos.add(look.x, 0, look.z); // 1 block forward, horizontal only

            robotParent.moveEntityToLocation((int) targetPos.x, (int) targetPos.y, (int) targetPos.z, 1.0);
            robotParent.hurtMarked = true;

            robotParent.consumeEnergy(10);
        }
        public void backward() {
            Vec3 look = robotParent.getLookAngle().normalize().reverse();
            Vec3 currentPos = robotParent.position();
            Vec3 targetPos = currentPos.add(look.x, 0, look.z); // 1 block forward, horizontal only

            robotParent.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0); // 1.0 is movement speed

            robotParent.hurtMarked = true;
            robotParent.consumeEnergy(10);
        }

        public void lookAtPosition(int x, int y, int z) {
            Vec3 pos = new Vec3(x, y ,z);
            robotParent.lookAtPosition(pos);
        }
        public void lookAtPosition(Vec3 position) {
            robotParent.lookAtPosition(position);
        }
        public void lookAtEntity(PythonEntity entity) {
            Vec3 pos = entity.getPosition();
            if (robotParent.isInVisionCone(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z))) {
                robotParent.lookAtPosition(pos);
            }
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
            robotParent.consumeEnergy(10);
        }
        public void uncrouch() {
            robotParent.standup();
        }
        public void standup() {
            robotParent.standup();
            robotParent.consumeEnergy(10);
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
        public float getDistanceToPosition(Vec3 pos) {
            return getDistanceToPosition((int) pos.x, (int) pos.y,  (int) pos.z);
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
        public Vec3 getFacingVector() {
            float yawRadians = (robotParent.getYRot() - 90) * ((float)Math.PI / 180);

            // Return normalized vector (x, 0, z)
            Vec3 facingVec = new Vec3(Math.cos(yawRadians), 0, Math.sin(yawRadians)).normalize();
            return facingVec;
        }
        public Vec3 getFacingCoords() {
            return robotParent.getApproximateFacingBlock(99999D).normalize();
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
            BlockPos blockPos = block.getBlockPosition();
            Level level = robotParent.level();

            BlockEntity blockEntity = robotParent.getBlockEntity(blockPos);
            if (blockEntity != null) {
                LazyOptional<?> itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
                return itemHandler.isPresent();
            }

            return false;
        }
    }

    public class Logistic {
        public void pickupLoot(boolean dropPickup) { robotParent.canPickupLoot = dropPickup; }
        public boolean canPickupLoot() { return robotParent.canPickupLoot; }
        public PythonInventory getInventory() {
            /* Gets the robot's inventory */
            return new PythonInventory(robotParent.getInventory(), robotParent);
        }

        @Nullable
        public PythonInventory getBlockInventorySided(PythonBlock block, @Nullable String side) throws Exception {
            /*
                Returns a copy of a block's container content, from that side (can be null).
                The inventory reference can be changed but it won't affect the real container reference
            */
            RobotPythonRunner.setJepAccess(true);
            if (!robotParent.withinInteractionRange(block.getBlockPosition())) {
                class InteractionOutOfRangeException extends Exception {
                    public InteractionOutOfRangeException(String m) {
                        super(m);
                    }
                }
                double range = robotParent.position().distanceTo(block.getBlockPosition().getCenter());
                RobotPythonRunner.setJepAccess(false);
                throw new InteractionOutOfRangeException("Block is " + range + " blocks away, robot's max range is " + robotParent.INTERACT_RANGE + " blocks.");
            }

            BlockEntity blockEntity = robotParent.getBlockEntity(block.getBlockPosition());
            if (blockEntity != null) {
                LazyOptional<?> itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
                if (itemHandler.isPresent()) {
                    LazyOptional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                    Optional<IItemHandler> opt = capability.resolve();

                    AtomicReference<PythonInventory> inventory = new AtomicReference<>();
                    opt.ifPresent(handler -> {
                        inventory.set(new PythonInventory(handler, blockEntity, robotParent));
                    });

                    RobotPythonRunner.setJepAccess(false);
                    return inventory.get();
                }

            }

            RobotPythonRunner.setJepAccess(false);
            return null;
        }
        public PythonInventory getBlockInventory(PythonBlock block) throws Exception {
            return getBlockInventorySided(block, null);
        }

        public void dropSlot(int slot) {
            if (!robotParent.level().isClientSide) {
                ItemStack stack = robotParent.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    robotParent.dropItemEntity(stack.copy());
                    robotParent.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
        }
        public void dropItem(int slot) {
            if (!robotParent.level().isClientSide) {
                ItemStack stack = robotParent.getInventory().getItem(slot);
                if (!stack.isEmpty() && stack.getCount() > 0) {

                    robotParent.dropItemEntity(stack.split(1).copy());
                    //robotParent.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
        }
        public void dropAmount(int slot, int amount) {
            if (!robotParent.level().isClientSide) {
                ItemStack stack = robotParent.getInventory().getItem(slot);
                if (!stack.isEmpty() && stack.getCount() >= amount) {

                    robotParent.dropItemEntity(stack.split(amount).copy());
                    //robotParent.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
        }

        public void equipSlot(int slot) { equipSlot(slot, false); }

        public void equipSlot(int slot, boolean offhand) {
            if (!robotParent.level().isClientSide) {
                ItemStack stack = robotParent.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    if (!offhand && !robotParent.getMainHandItem().isEmpty()) {
                        robotParent.getInventory().setItem(slot, robotParent.getMainHandItem().copyAndClear());
                    } else if (offhand && !robotParent.getOffhandItem().isEmpty()) {
                        robotParent.getInventory().setItem(slot, robotParent.getOffhandItem().copyAndClear());
                    }

                    if (offhand) {
                        robotParent.setItemInHand(InteractionHand.OFF_HAND, stack.copyAndClear());
                    } else {
                        robotParent.equipItemIfPossible(stack.copyAndClear());
                    }


                }
            }
        }
        public void unequip() {
            unequip(false);
        }
        public void unequip(boolean offhand) {
            if (!robotParent.level().isClientSide) {
                ItemStack stack;
                if (offhand && !robotParent.getOffhandItem().isEmpty()) {
                    stack = robotParent.getOffhandItem();
                } else if (!offhand && !robotParent.getMainHandItem().isEmpty()) {
                    stack = robotParent.getMainHandItem();
                } else {
                    return;
                }

                int nextSlot = ContainerUtils.FindNextEmptySlot(robotParent.getInventory());
                if (nextSlot > -1) {
                    robotParent.getInventory().setItem(nextSlot, stack.copyAndClear());
                }
            }

        }

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

        public String getUUID() {
            return robotParent.getUUID().toString();
        }

        public int getEnergy() { return robotParent.getEnergy(); }
        public float getEnergyPercentage() { return robotParent.getEnergyPercentage(); }

        public void addEnergy(int amount) { robotParent.addEnergy(amount);}
        public void removeEnergy(int amount) {robotParent.removeEnergy(amount);}

    }







}
