package net.redegs.digitizerplus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.entity.ComputerEntity;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.block.entity.ModBlockEntities;
import net.redegs.digitizerplus.block.entity.StorageBlockEntity;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.python.PythonRunner;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ComputerBlock extends BaseEntityBlock  {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING; // For block orientation
    public static final BooleanProperty RGB = BooleanProperty.create("rgb"); // Custom property for linked state

    public ComputerBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(RGB, false) // Default linked state
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, RGB); // Add the LINKED property
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(RGB, false); // Default linked state when placed
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
        if(pLevel.isClientSide()) {
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
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity entity = pLevel.getBlockEntity(pPos);
        ComputerEntity computer = (ComputerEntity) entity;

//        if (!pLevel.isClientSide) {
//            computer.OpenTerminal((ServerPlayer) pPlayer);
//        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        ComputerEntity entity = (ComputerEntity) pLevel.getBlockEntity(pPos);
        if (!pLevel.isClientSide) {
            ComputerManager.stopThreads(entity.getComputerID());
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
                    DigitizerPlus.LOGGER.info("MARKING AS PLACED = {}", id);
                    computerBE.markAsPlaced(id);

                }

            }
        }
    }
}
