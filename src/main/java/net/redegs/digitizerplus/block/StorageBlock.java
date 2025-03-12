package net.redegs.digitizerplus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.block.entity.StorageBlockEntity;
import org.jetbrains.annotations.Nullable;

public class StorageBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING; // For block orientation

    public StorageBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING); // Add the LINKED property
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        BlockPos playerPos = new BlockPos(pPlayer.getBlockX(), pPlayer.getBlockY(), pPlayer.getBlockZ());
        StorageBlockEntity storageBlockEntity = (StorageBlockEntity) pLevel.getBlockEntity(pPos);

        if (!pLevel.isClientSide()) {
            if (pPlayer.isShiftKeyDown()) {
                if (pLevel.getBlockEntity(pPos.below(1)) instanceof DigitizerEntity) {
                    DigitizerEntity digitizerEntity = (DigitizerEntity) pLevel.getBlockEntity(pPos.below(1));


                    digitizerEntity.AttachStorageDrive(storageBlockEntity);
                    storageBlockEntity.AttachDigitizer(digitizerEntity);



                    pPlayer.sendSystemMessage(Component.literal("Attached!"));

                }
            } else {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, (StorageBlockEntity) storageBlockEntity, pPos);
            }
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (!pLevel.isClientSide()) {
            StorageBlockEntity blockEntity = (StorageBlockEntity) pLevel.getBlockEntity(pPos);

            if (blockEntity != null && blockEntity.attached) {
                blockEntity.attachedDigitizer.DetachStorageDrive();
                blockEntity.DetachDigitizer();
            }

        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof StorageBlockEntity) {
                ((StorageBlockEntity) blockEntity).drops();
            }
        }


        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new StorageBlockEntity(blockPos, blockState);
    }


    // Make ts survival friendly
    // Make it work
    // Add recipes for some shi
    // Make example program with example system




}
