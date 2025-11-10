package net.redegs.digitizerplus.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.*;

import java.io.IOException;
import java.nio.file.Path;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.terminal.RobotTerminal;
import net.redegs.digitizerplus.imgui.guis.RobotUI;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.JepServerPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.robot.RobotTerminalScreenPacket;
import net.redegs.digitizerplus.network.packets.computer.terminal.TerminalSyncPacket;
import net.redegs.digitizerplus.python.RobotPythonRunner;
import net.redegs.digitizerplus.python.wrappers.PythonRobotWrapper;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HumanoidRobot extends Mob {

    public static final double MOVEMENT_SPEED = 0.25;
    public static final double VIEWDISTANCE = 4.0;
    public static final double FOV = 70.0;
    public static final double INTERACT_RANGE = 4.5;
    public static final int INVENTORY_SIZE = 27;
    public static final int MAX_ENERGY = 1000;

    public boolean canPickupLoot = false;
    public Vec3 lastLookBlock;
    private boolean queueScript = false;
    private boolean pendingResume = false;

    private Level level;
    public RobotUI robotUI;

    public Terminal terminal;



    public PythonRobotWrapper pythonWrapper;
    public HashMap<Thread, RobotPythonRunner> pythonThreads;

    private static final EntityDataAccessor<Boolean> CODE_EXECUTING =
            SynchedEntityData.defineId(HumanoidRobot.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ENERGY =
            SynchedEntityData.defineId(HumanoidRobot.class, EntityDataSerializers.INT);



    public HumanoidRobot(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);

        // Set default attributes
        this.level = level;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);

        this.setCanPickUpLoot(this.canPickupLoot);

//        this.buffer = new char[13][35];
//        this.terminalScreen = new TerminalScreen(this.buffer);

        this.terminal = new RobotTerminal(this, 12, 35);

        if (level.isClientSide) {
            robotUI = new RobotUI(this);

        } else {
            pythonWrapper = new PythonRobotWrapper(this);
            pythonThreads = new HashMap<>();

        }

//        try {
//            Path mainPath = ComputerManager.getMainPath();
//            Files.createDirectory(mainPath.resolve(robotUUID.toString()));
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CODE_EXECUTING, false);
        this.entityData.define(ENERGY, 0);
    }

    private void createRobotFileLocation() {
        try {
            Path mainPath = DigitizerPlus.COMPUTER_MANAGER.getMainPath();
            Files.createDirectory(mainPath.resolve(getUUID().toString()));

        } catch (IOException e) {
            DigitizerPlus.LOGGER.warn("FAILED TO CREATE ROBOT DIR");
            throw new RuntimeException(e);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @org.jetbrains.annotations.Nullable SpawnGroupData pSpawnData, @org.jetbrains.annotations.Nullable CompoundTag pDataTag) {
        super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);

        createRobotFileLocation();

        return pSpawnData;
    }

    public void addEnergy(int amount) {
        if (!this.level.isClientSide) {
            this.entityData.set(ENERGY, Math.max(0, Math.min(this.MAX_ENERGY, this.entityData.get(ENERGY) + amount)));
        }
    }
    public void removeEnergy(int amount) {
        if (!this.level.isClientSide) {
            this.entityData.set(ENERGY, Math.max(0, Math.min(this.MAX_ENERGY, this.entityData.get(ENERGY) - amount)));
        }
    }
    public boolean consumeEnergy(int amount) {
        if (!this.level.isClientSide) {
            if (this.entityData.get(ENERGY) < (this.entityData.get(ENERGY) - amount)) {
                return false;
            } else {
                removeEnergy(amount);
                return true;
            }
        }
        return false;
    }
    public float getEnergyPercentage() {
        return ((float) this.entityData.get(ENERGY) / MAX_ENERGY) * 100;
    }
    public int getEnergy() {return this.entityData.get(ENERGY); }
    public void setEnergy(int amount) {
        this.entityData.set(ENERGY, amount);
    }

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE); // 9 slots (e.g., a small chest)


    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);

        this.setCanPickUpLoot(false);
        this.canPickupLoot = false;
        if (!level.isClientSide()) {
            pythonWrapper = null;
            stopAllPythonThreads();

        } else {
            robotUI.Destroy();
        }

    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
//
        if (hand == InteractionHand.MAIN_HAND) {
            if (player.isCrouching()) {
                 // example: 25x80 terminal
                //try {
                    if (!this.level.isClientSide) {
                        ModNetwork.sendToPlayer(new TerminalSyncPacket(this.terminal.getBuffer(), this.terminal.cursorX, this.terminal.cursorY), (ServerPlayer) player);

                        ModNetwork.sendToPlayer(new RobotTerminalScreenPacket( true, getId()), (ServerPlayer) player);
                        terminal.addWatcher((ServerPlayer) player);
                    }
                    //Minecraft.getInstance().setScreen(new TerminalScreen(terminal));
//                } catch (Exception e){
//                    DigitizerPlus.LOGGER.warn("CRASHED WHEN OPENING UI");
//                }

                //Imgui.FocusGuiContext(robotUI);
//            } else if (!level.isClientSide) {
//                ModNetwork.sendToPlayer(new SyncRobotEnergyState(this.getId(), this.getEnergy()), (ServerPlayer) player);
//                NetworkHooks.openScreen((ServerPlayer) player,
//                        new SimpleMenuProvider((id, inv, p) -> new RobotMenu(id, inv, this),
//                                Component.literal("Robot")),
//                        buf -> buf.writeInt(this.getId()) // write entity ID here
//                );
//
//            }
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

        ItemStack helmet = getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 1));
        ItemStack chestplate = getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 2));
        ItemStack leggings = getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 3));
        ItemStack boots = getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 0));

        if (!helmet.isEmpty() && !EnchantmentHelper.hasVanishingCurse(helmet)) {
            this.spawnAtLocation(helmet);
        }
        if (!chestplate.isEmpty() && !EnchantmentHelper.hasVanishingCurse(chestplate)) {
            this.spawnAtLocation(chestplate);
        }
        if (!leggings.isEmpty() && !EnchantmentHelper.hasVanishingCurse(leggings)) {
            this.spawnAtLocation(leggings);
        }
        if (!boots.isEmpty() && !EnchantmentHelper.hasVanishingCurse(boots  )) {
            this.spawnAtLocation(boots);
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

        // Save inventory
        ListTag listTag = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stack.save(stackTag);
                stackTag.putInt("Slot", i); // store slot index
                listTag.add(stackTag);
            }
        }
        tag.put("inventory", listTag);
        tag.putInt("energyLevel", this.getEnergy());
        tag.putBoolean("codeExecuting", this.isCodeExecuting());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        inventory.clearContent();

        if (tag.contains("inventory", 9)) { // 9 = ListTag
            ListTag listTag = tag.getList("inventory", 10); // 10 = CompoundTag
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag stackTag = listTag.getCompound(i);
                int slot = stackTag.getInt("Slot"); // restore slot index
                if (slot >= 0 && slot < inventory.getContainerSize()) {
                    inventory.setItem(slot, ItemStack.of(stackTag));
                }
            }
        }

        setEnergy(tag.getInt("energyLevel"));

        this.entityData.set(CODE_EXECUTING, tag.getBoolean("codeExecuting"));

        DigitizerPlus.LOGGER.info("CODE EXECUTING VALUE = {}", this.isCodeExecuting());

        if (isCodeExecuting() && !level().isClientSide) {
            pendingResume = true; // mark to resume later
        }

    }

    @Override
    public void tick() {
        super.tick();

        // Queue script exectuion if the bot was running a script before shutdown of the last world instance
        if (!level().isClientSide && pendingResume && Minecraft.getInstance().getConnection() != null) {
            pendingResume = false;

            DigitizerPlus.LOGGER.warn("RESUMING SCRIPT for {}", getUUID());
            Path recent = ComputerManager.getRecentFile(getUUID().toString()).toAbsolutePath();
            try {
                String code = Files.readString(recent);
                // Send script back to your script executor
                ModNetwork.sendToServer(new JepServerPacket(getId(), code));
            } catch (IOException e) {
                DigitizerPlus.LOGGER.error("Failed to read script for {}", getUUID(), e);
            }
        }
    }


    public Container getInventory() { return this.inventory;}
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
    public RobotPythonRunner getDefaultThread() {
        if (!level.isClientSide) {
            RobotPythonRunner runner = pythonThreads.entrySet().stream().toList().get(0).getValue();
            return runner;
        }
        return null;
    }

    /* Required for threaded use */
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        CompletableFuture<BlockEntity> future = new CompletableFuture<>();

        level.getServer().execute(() -> {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                future.complete(blockEntity);
            }

        });

        try {
            // Wait up to 200ms for the result
            return future.get(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | CommandLine.ExecutionException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
            return null;
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
            net.minecraft.world.level.pathfinder.Path path = navigation.createPath(targetPos, 0); // 0 is the accuracy (0 = exact)

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

    public Vec3 getApproximateFacingBlock(double distance) {
        Vec3 eyePos = this.getEyePosition(1.0F);
        Vec3 look = this.getViewVector(1.0F);

        Vec3 target = eyePos.add(look.scale(distance));
        return new Vec3(target.x, target.y, target.z);
    }

    public void lookAtPosition(Vec3 pos) {
        if (!this.level().isClientSide()) {
            Vec3 eyePos = this.getEyePosition(1.0F);

            // If target is below the eyes, correct the Y to avoid nodding
            if (pos.y < eyePos.y) {
                pos = new Vec3(pos.x, eyePos.y - 0.1, pos.z);
            }

            this.lookAt(EntityAnchorArgument.Anchor.EYES, pos);
        }
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

    public void dropItemEntity(ItemStack stack) {
        double d0 = this.getEyeY() - (double)0.3F;

        if (!this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(this.level, this.position().x, d0, this.position().z, stack);
            itemEntity.setPickUpDelay(40);


            itemEntity.setThrower(this.getUUID());

            float f7 = 0.3F;
            float f8 = Mth.sin(this.getXRot() * ((float) Math.PI / 180F));
            float f2 = Mth.cos(this.getXRot() * ((float) Math.PI / 180F));
            float f3 = Mth.sin(this.getYRot() * ((float) Math.PI / 180F));
            float f4 = Mth.cos(this.getYRot() * ((float) Math.PI / 180F));
            float f5 = this.random.nextFloat() * ((float) Math.PI * 2F);
            float f6 = 0.02F * this.random.nextFloat();
            itemEntity.setDeltaMovement((double) (-f3 * f2 * 0.3F) + Math.cos((double) f5) * (double) f6, (double) (-f8 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double) (f4 * f2 * 0.3F) + Math.sin((double) f5) * (double) f6);
            this.level.addFreshEntity(itemEntity);
        }


//        itemEntity.setDeltaMovement(lookVec.scale(.2));
    }

    public boolean withinInteractionRange(BlockPos blockPos) {
        return !(position().distanceTo(blockPos.getCenter()) > INTERACT_RANGE);
    }
    public boolean withinInteractionRange(Vec3 pos) {
        return position().distanceTo(pos) > INTERACT_RANGE;
    }

   @Override
    public boolean isCrouching() {
        return this.getPose() == Pose.CROUCHING || this.isShiftKeyDown();
    }

    public boolean isCodeExecuting() {
        return this.entityData.get(CODE_EXECUTING);
    }

    public void setCodeExecuting(boolean value) {
        this.entityData.set(CODE_EXECUTING, value);
    }

}