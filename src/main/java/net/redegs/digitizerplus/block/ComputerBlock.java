package net.redegs.digitizerplus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.block.entity.ModBlockEntities;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.computer.kernel.device.OpenMonitorScreenPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ComputerBlock extends BaseEntityBlock  {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING; // For block orientation
    public static final BooleanProperty ON = BooleanProperty.create("on"); // Custom property for linked state


    /* Location of the monitor mapped to the texture of the block
       Determines where the monitor quad is rendered on the block */
    public static final int SCREEN_X = 3;
    public static final int SCREEN_Y = 3;
    public static final int SCREEN_W = 10;
    public static final int SCREEN_H = 7;

    /* The width and height in pixels of the monitor */
    public static final int MONITOR_W = 115;
    public static final int MONITOR_H = 60;


    public static final float PIXEL_SCALE_X = 3.1f; // The size in pixels to render the monitor
    public static final float PIXEL_SCALE_Y = 3.9f;

    /* This ratio determines the in-world character scale to the gui character scale
       E.g 26 characters wide in the world, to 11 characters wide in the GUI.
       These constants are arbritrary and are just derived from my testing, the scale is about ~2.38
    */
    public static final float SCALE_RATIO_X = 26f / 11f;
    public static final float SCALE_RATIO_Y = 50f / 21f;


    public ComputerBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ON, false) // Default linked state
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, ON); // Add the LINKED property
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(ON, false); // Default linked state when placed
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ComputerEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        // Only create a ticker if it's the correct BlockEntity type
        return createTickerHelper(pBlockEntityType, ModBlockEntities.COMPUTER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> {
                    if (pBlockEntity instanceof ComputerEntity) {
                        try {
                            pBlockEntity.tick(pLevel1, pPos, pState1);  // Make sure the tick method is called safely
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult hit) {
        if (!level.isClientSide && hit.getDirection() == state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            if (pPlayer.isCrouching()) {
                ModNetwork.sendToPlayer(new OpenMonitorScreenPacket(pos), (ServerPlayer) pPlayer);
                return InteractionResult.SUCCESS;

            } else {
                Vec3 localHit = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                ComputerEntity entity = (ComputerEntity) level.getBlockEntity(pos);

                if (entity.monitorDevice != null) {
                    Vec2 screenPixel = getScreenPixel(facing, localHit, entity.monitorDevice.width, entity.monitorDevice.height);

                    if (screenPixel != null) {
                        if (screenPixel.equals(new Vec2(-1, -1))) {
                            // On/Off Button was pressed
                            togglePower(state, pos, level, entity);
                            return InteractionResult.SUCCESS;

                        } else {
                            // Pixel was pressed
                            if (getComputerOn(state)) {
                                onPixelClicked((int) screenPixel.x, (int) screenPixel.y);
                                entity.onPixelClicked((int) screenPixel.x, (int) screenPixel.y);
                            }

                            return InteractionResult.SUCCESS;
                        }

                    }

                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            if (level.isClientSide) return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }



    private Vec2 getScreenPixel(Direction facing, Vec3 localHit, int monitorWidth, int monitorHeight) {
        double SCREEN_X_U = 3.0 / 16.0;   // left edge of the 10×7 region
        double SCREEN_Y_V = 3.0 / 16.0;   // top  edge of the 10×7 region
        double SCREEN_W_U = 10.0 / 16.0;  // width of region (10 pixels of 16)
        double SCREEN_H_V = 7.0  / 16.0;  // height of region (7 pixels of 16)

// If you still need to test the off-button at specific texture pixel coords:
        int TEX_SIZE = 16;

        int OFF_BTN_TEX_Y  = 13;
        int OFF_BTN_TEX_X1 = 4;
        int OFF_BTN_TEX_X2 = 3;

        // 1) Compute continuous UV coordinates (0..1) for the face
        double u = 0.0, v = 0.0;
        switch (facing) {
            case NORTH -> { u = localHit.x;      v = 1.0 - localHit.y; }
            case SOUTH -> { u = 1.0 - localHit.x; v = 1.0 - localHit.y; }
            case WEST  -> { u = localHit.z;      v = 1.0 - localHit.y; }
            case EAST  -> { u = 1.0 - localHit.z; v = 1.0 - localHit.y; }
            default -> { return null; }
        }

        switch (facing ){
            case NORTH -> {u = 1.0 - u;}
            case SOUTH -> {u = 1.0 - u;}
            case WEST -> {u = u;}
            case EAST -> {u = u;}
        }

        // 2) Optional: check precise off-button using integer texture pixels
        int texturePixelX = (int) Math.floor(u * TEX_SIZE);
        int texturePixelY = (int) Math.floor(v * TEX_SIZE);
// 0
//        switch (facing) {
//            case NORTH ->  {OFF_BTN_TEX_X1 = 11; OFF_BTN_TEX_X2 = 12;}
//            case SOUTH ->  {OFF_BTN_TEX_X1 = 11; OFF_BTN_TEX_X2 = 12;}
//            case EAST -> {OFF_BTN_TEX_X1 = 4; OFF_BTN_TEX_X2 = 3;}
//            case WEST -> {OFF_BTN_TEX_X1 = 4; OFF_BTN_TEX_X2 = 3;}
//        }

        System.out.println(texturePixelX);
        System.out.println(texturePixelY);

        if ((texturePixelX == OFF_BTN_TEX_X1 || texturePixelX == OFF_BTN_TEX_X2) &&
                texturePixelY == OFF_BTN_TEX_Y) {
            return new Vec2(-1, -1);
        }

        // 3) Check containment in the screen rectangle (UV-space)
        if (u < SCREEN_X_U || u > SCREEN_X_U + SCREEN_W_U ||
                v < SCREEN_Y_V || v > SCREEN_Y_V + SCREEN_H_V) {
            return null; // outside screen
        }

        // 4) Normalize UV to 0..1 inside the screen region
        double nx = (u - SCREEN_X_U) / SCREEN_W_U; // 0..1
        double ny = (v - SCREEN_Y_V) / SCREEN_H_V; // 0..1

        // 5) Scale to target resolution (continuous -> integer)
        // Use Math.floor for consistent mapping; then clamp to last pixel to handle u==1.0 edge
        int mappedX = (int) Math.floor(nx * monitorWidth);
        int mappedY = (int) Math.floor(ny * monitorHeight);

        if (mappedX < 0) mappedX = 0;
        if (mappedY < 0) mappedY = 0;
        if (mappedX >= monitorWidth) mappedX = monitorWidth - 1;
        if (mappedY >= monitorHeight) mappedY = monitorHeight - 1;
//
//        System.out.println(mappedX);
//        System.out.println(mappedY);
        return new Vec2(mappedX, mappedY);
    }




    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        ComputerEntity entity = (ComputerEntity) pLevel.getBlockEntity(pPos);
        if (!pLevel.isClientSide) {
            //ComputerManager.stopThreads(entity.getComputerID());

            if (entity.computerKernel != null) {
                entity.computerKernel.shutdown();
            }

        }

        ItemStack itemStack = new ItemStack(this.asItem());
        CompoundTag tag = new CompoundTag();
        itemStack.getOrCreateTag().putUUID("ComputerID", entity.getComputerID());


        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);


    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        List<ItemStack> drops = new ArrayList<>();
        BlockEntity be = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack itemStack = new ItemStack(this.asItem());

        if (be instanceof ComputerEntity) {
            CompoundTag tag = new CompoundTag();
            itemStack.getOrCreateTag().putUUID("ComputerID", ((ComputerEntity) be).getComputerID());
        }

        drops.add(itemStack);
        return drops;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ComputerEntity computerBE && stack.hasTag()) {
                CompoundTag tag = stack.getTag();

                if (tag.contains("ComputerID")) {
                    UUID id = tag.getUUID("ComputerID");
                    computerBE.markAsPlaced(id);

                }

            }
        }
    }

    public void togglePower(BlockState state, BlockPos pos, Level level, ComputerEntity computer) {
        /* Toggles the computer's power */
        level.setBlock(pos, state.setValue(ON, !state.getValue(ON)), 3);

        if (!state.getValue(ON)) {
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.BLOCKS, .25f, 1.0f);
            onPowerOn();
            computer.onPowerOn();
        } else {
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.BLOCKS, .25f, .25f);
            onPowerOff();
            computer.onPowerOff();
        }
    }

    public boolean getComputerOn(BlockState state) {
        return state.getValue(ON);
    }

    private void onPowerOn() {
        /* Called when powered on */

    }

    private void onPowerOff() {
        /* Called when powered off */

    }

    private void onPixelClicked(int x, int y) {

    }

}
