package net.redegs.digitizerplus.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.redegs.digitizerplus.entity.goals.MoveToLocationGoal;
import net.redegs.digitizerplus.entity.goals.TransferItemsGoal;
import net.redegs.digitizerplus.imgui.Imgui;
import net.redegs.digitizerplus.imgui.guis.RobotUI;
import net.redegs.digitizerplus.python.RobotPythonRunner;
import net.redegs.digitizerplus.python.RobotPythonWrapper;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.SyncRobotPacket;

import javax.annotation.Nullable;
import java.util.*;

public class HumanoidRobot extends Mob {

    public static final double MOVEMENT_SPEED = 0.25;
    public static final double VIEWDISTANCE = 4.0;
    public static final double FOV = 70.0;
    public static final int INVENTORY_SIZE = 27;

    public boolean canPickupLoot = true;

    private Level level;
    public RobotUI robotUI;

    public RobotPythonWrapper pythonWrapper;
    public HashMap<Thread, RobotPythonRunner> pythonThreads;

    public HumanoidRobot(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);

        // Set default attributes
        this.level = level;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);

        this.setCanPickUpLoot(this.canPickupLoot);
        if (level.isClientSide) {
            robotUI = new RobotUI(this);
        } else {
            pythonWrapper = new RobotPythonWrapper(this);
            pythonThreads = new HashMap<>();
        }


    }

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE); // 9 slots (e.g., a small chest)


    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);

        if (!level.isClientSide()) {
            pythonWrapper = null;
            stopAllPythonThreads();

        } else {
            robotUI.Destroy();
        }

    }

//    @Override
//    protected void registerGoals() {
////        this.MoveToLocationGoal = new MoveToLocationGoal(this, this, new BlockPos(0, 0, 0));
////        this.goalSelector.addGoal(0, this.MoveToLocationGoal); // Custom goal to collect nearby items
//    }


    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
//
        if (hand == InteractionHand.MAIN_HAND) {
            if (level.isClientSide) {
                Imgui.FocusGuiContext(robotUI);
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    protected void dropEquipment() {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
                this.spawnAtLocation(itemstack);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // Health
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED); // Movement speed
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // Save the main inventory
        tag.put("inventory", this.inventory.createTag());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // Clear existing contents before loading
        this.inventory.clearContent();

        // Load the main inventory
        if (tag.contains("inventory", 9)) { // 9 = TagType.LIST
            this.inventory.fromTag(tag.getList("inventory", 10)); // 10 = TagType.COMPOUND
        }

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.readAdditionalSaveData(tag);
        syncInventory();
    }

    public void syncInventory() {
        if (!level.isClientSide) {
            HashMap<String, Integer> extraData = new HashMap<>();
            extraData.put("entityID", this.getId());

            ModNetwork.sendToAllClients(new SyncRobotPacket(extraData, this.getItems()));
        }
    }

    public Container getInventory() {
        return this.inventory;
    }
    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>(inventory.getContainerSize());
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            items.add(inventory.getItem(i).copy());
        }
        return items;
    }


    @Override
    public void aiStep() {
        super.aiStep();


        this.setCanPickUpLoot(this.canPickupLoot);
        if (!this.level().isClientSide) {
            this.pickUpNearbyItems();
        }

    }

    // -------
    @Override
    public void pickUpItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (inventory.canAddItem(stack)) {
            int count = stack.getCount();

            inventory.addItem(stack.copy()); // Add a copy to inventory
            this.take(itemEntity, count); // Trigger pickup animation/sound
            itemEntity.discard(); // Remove the item from world
        }
    }
    public void pickUpNearbyItems() {
        AABB box = this.getBoundingBox().inflate(this.getPickRadius());

        List<ItemEntity> drops = level().getEntitiesOfClass(ItemEntity.class, box,
                item -> !item.hasPickUpDelay() && item.isAlive());

        for (ItemEntity item : drops) {
            pickUpItem(item);
        }
    }

    // ---------
    public void stopAllPythonThreads() {
        if (!level.isClientSide()) {
            System.out.println("stopping all threads.");
            for (Map.Entry<Thread, RobotPythonRunner> entry: pythonThreads.entrySet()) {
                Thread t = entry.getKey();
                RobotPythonRunner r = entry.getValue();
                r.stop();
            }
        }

    }



    // ----------
    public void moveEntityToLocation(int x, int y, int z, double speedModifier) {
        BlockPos targetPos = new BlockPos(x, this.getBlockY(), z);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) System.out.println("Not called on server!");


        // Schedule task on main thread
        server.execute(() -> {
            // Get the entity's path navigation system
            PathNavigation navigation = this.getNavigation();
            // Create a path to the target position
            Path path = navigation.createPath(targetPos, 0); // 0 is the accuracy (0 = exact)

            if (path != null) {
                // Start moving along the path
                navigation.moveTo(path, speedModifier);
            } else {
                // If pathfinding fails, try to move directly (less reliable)
                this.getLookControl().setLookAt(
                        targetPos.getX() + 0.5,
                        targetPos.getY(),
                        targetPos.getZ() + 0.5
                );
                this.getMoveControl().setWantedPosition(
                        targetPos.getX() + 0.5,
                        targetPos.getY(),
                        targetPos.getZ() + 0.5,
                        speedModifier
                );
            }
        });

    }
    public void jump() {
        this.jumpFromGround();
    }
    public void crouch() {
        LivingEntity entity = (LivingEntity) this;

        entity.setPose(Pose.CROUCHING);
        entity.setShiftKeyDown(true);

        entity.refreshDimensions();
        entity.hurtMarked = true;
    }
    public void standup() {
        LivingEntity entity = (LivingEntity) this;

        entity.setPose(Pose.STANDING);
        entity.setShiftKeyDown(false);
        entity.refreshDimensions();
        entity.hurtMarked = true;
    }

    public void setEntityRotation(float yaw, float pitch) {
        this.setYRot(yaw % 360.0F);
        this.setXRot(90.0F);

        // Update head rotation for proper rendering
        this.yHeadRot = yaw;
        this.yBodyRot = yaw;


        // Force movement sync to clients
        this.hurtMarked = true;

    }
    public void facePosition(double x, double y, double z) {
        Entity entity = (Entity) this;
        if (entity.level().isClientSide) return;

        double dX = x - entity.getX();
        double dY = y - (entity.getY() + entity.getEyeHeight());
        double dZ = z - entity.getZ();

        double horizontalDistance = Math.sqrt(dX * dX + dZ * dZ);

        float yaw = (float)(Math.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        float pitch = (float)-(Math.atan2(dY, horizontalDistance) * (180D / Math.PI));

        setEntityRotation(yaw, pitch);
    }

    public HitResult getLookingRayResult() {
        // Simply shoots raycast 3 blocks infront of the entities eyes
        Vec3 eyePos = this.getEyePosition();
        Vec3 look = this.getLookAngle().normalize().multiply(HumanoidRobot.VIEWDISTANCE, 0, HumanoidRobot.VIEWDISTANCE);

        // Trace 1 block forward
        Vec3 targetPos = eyePos.add(look);
        HitResult result = this.level().clip(new ClipContext(
                eyePos,
                targetPos,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.SOURCE_ONLY,
                this
        ));
        return result;
    }
    @Nullable
    public EntityHitResult getLookingRayEntityResult() {
        Entity entity = this;
        Level level = entity.level();

        Vec3 eyePos = this.getEyePosition();
        Vec3 look = this.getLookAngle().normalize().multiply(HumanoidRobot.VIEWDISTANCE, 0, HumanoidRobot.VIEWDISTANCE);
        Vec3 targetPos = eyePos.add(look);

        // Create bounding box for ray

        HitResult obstructResult = this.level().clip(new ClipContext(
                eyePos,
                targetPos,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.SOURCE_ONLY,
                this
        ));

        targetPos = obstructResult.getLocation();
        AABB rayBox = new AABB(eyePos, targetPos).inflate(1.0); // Widen for accuracy

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                entity,
                eyePos,
                targetPos,
                rayBox,
                target -> target instanceof LivingEntity && target != entity
        );


        if (entityHit != null) {
            return entityHit;
        }

        return null;
    }

    public boolean canSeeEntity(Entity entity) {
        // Returns true if the robots vision is not obstructed
        Level level = entity.level();

        Vec3 eyePos = this.getEyePosition();
        Vec3 targetPos = entity.position();

        // Create bounding box for ray

        HitResult obstructResultBody = this.level().clip(new ClipContext(
                eyePos,
                targetPos,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.SOURCE_ONLY,
                this
        ));

        HitResult obstructResultHead = this.level().clip(new ClipContext(
                eyePos,
                targetPos.add(0, entity.getBbHeight()-.5, 0),
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.SOURCE_ONLY,
                this
        ));

        boolean eyesVisible = obstructResultHead.getType() == HitResult.Type.BLOCK;
        boolean bodyVisible = obstructResultBody.getType() == HitResult.Type.BLOCK;

        return (!eyesVisible || !bodyVisible);
    }
    public boolean isInVisionCone(Entity entity, double fov) {
        // Get the look direction of the character (normalize it)
        Vec3 look = this.getLookAngle().normalize();

        // Get the direction from the character to the entity
        Vec3 entityPos = entity.position();
        Vec3 entityDir = entityPos.subtract(this.position()).normalize();  // Correct direction vector

        // Calculate dot product between the look direction and entity direction
        double dotProd = look.dot(entityDir);

        // Clamp the dot product to avoid out of bounds values for Math.acos
        double clampedDot = Math.min(1.0, Math.max(-1.0, dotProd));
        double angleToTarg = Math.acos(clampedDot);  // Calculate the angle to the target

        // Calculate the distance to the entity
        double distToTarg = this.distanceTo(entity);

        // Check if the angle is within FOV and the distance is within range
        return angleToTarg <= Math.toRadians(fov) && distToTarg <= Math.pow(VIEWDISTANCE, 2) + VIEWDISTANCE;  // 16 units is your max vision range
    }
    public boolean isInVisionCone(BlockPos blockPos) {
        // Get the look direction of the character (normalize it)
        Vec3 look = this.getLookAngle().normalize();

        // Get the direction from the character to the entity
        Vec3 blockVec = blockPos.getCenter();
        Vec3 blockDir = blockVec.subtract(this.position()).normalize();  // Correct direction vector

        // Calculate dot product between the look direction and entity direction
        double dotProd = look.dot(blockDir);

        // Clamp the dot product to avoid out of bounds values for Math.acos
        double clampedDot = Math.min(1.0, Math.max(-1.0, dotProd));
        double angleToTarg = Math.acos(clampedDot);  // Calculate the angle to the target

        // Calculate the distance to the entity
        double distToTarg = blockVec.distanceTo(this.position());

        // Check if the angle is within FOV and the distance is within range
        return angleToTarg <= Math.toRadians(FOV) && distToTarg <= Math.pow(VIEWDISTANCE, 2) + VIEWDISTANCE;  // 16 units is your max vision range
    }

    public List<Entity> getEntitiesWithinFOV() {
        BlockPos position = this.blockPosition();
        double radius = Math.pow(VIEWDISTANCE, 2) + VIEWDISTANCE;
        List<Entity> nearbyEntities = level.getEntitiesOfClass(Entity.class, new net.minecraft.world.phys.AABB(position.getX() - radius, position.getY() - radius, position.getZ() - radius,
                position.getX() + radius, position.getY() + radius, position.getZ() + radius));

        List<Entity> visibleEntities = new ArrayList<>();

        for (Entity other : nearbyEntities) {
            if (other != this) {
                if (this.isInVisionCone(other, HumanoidRobot.FOV)) {
                    boolean canSee = this.canSeeEntity(other);
                    if (canSee) {
                       visibleEntities.add(other);
                    }
                }
            }
        }

        return visibleEntities;
    }
    public List<BlockPos> getBlockPosWithinFOV() {
        BlockPos position = this.blockPosition();
        double radius = Math.pow(VIEWDISTANCE, 2) + VIEWDISTANCE;
        List<BlockPos> visibleBlocks = new ArrayList<>();

        // Iterate over blocks within the radius
        for (int x = (int)(position.getX() - radius); x < (int)(position.getX() + radius); x++) {
            for (int y = (int)(position.getY() - radius); y < (int)(position.getY() + radius); y++) {
                for (int z = (int)(position.getZ() - radius); z < (int)(position.getZ() + radius); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockState blockState = level.getBlockState(blockPos);

                    // Skip air blocks or blocks you don't care about
                    if (blockState.getBlock() == Blocks.AIR) {
                        continue;
                    }

                    // Check if the block is within the vision cone (this is just a simple distance check for now)
                    if (isInVisionCone(blockPos)) {
                        visibleBlocks.add(blockPos);
                    }
                }
            }
        }

        return visibleBlocks;
    }

   @Override
    public boolean isCrouching() {
        return this.getPose() == Pose.CROUCHING || this.isShiftKeyDown();
    }

}