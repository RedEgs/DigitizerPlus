package net.redegs.digitizerplus.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.block.entity.ModBlockEntities;
import net.redegs.digitizerplus.item.ModItems;
import net.redegs.digitizerplus.item.custom.LinkerItem;
import org.jetbrains.annotations.Nullable;

public class DigitizerBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING; // For block orientation
    public static final BooleanProperty LINKED = BooleanProperty.create("linked"); // Custom property for linked state
    public static final BooleanProperty RGB = BooleanProperty.create("rgb"); // Custom property for linked state


    public DigitizerBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LINKED, false) // Default linked state
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
        pBuilder.add(FACING, LINKED, RGB); // Add the LINKED property
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(LINKED, false) // Default linked state when placed
                .setValue(RGB, false); // Default linked state when placed
    }


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack item = pPlayer.getItemInHand(pHand);
        BlockPos playerPos = new BlockPos(pPlayer.getBlockX(), pPlayer.getBlockY(), pPlayer.getBlockZ());
        DigitizerEntity digitizerEntity = (DigitizerEntity) pLevel.getBlockEntity(pPos);


        if (!pLevel.isClientSide()) {
            if (item.getItem() == ModItems.LINKER.get()) {
                if (digitizerEntity.containsLinker()) {
                    digitizerEntity.dropStoredLinker();
                    pPlayer.sendSystemMessage(Component.literal("Dropping Stored Linker"));

                    pLevel.playSound(null, playerPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);


                } else {
                    digitizerEntity.insertStoredLinker(item);

                    LinkerItem linker = (LinkerItem) pPlayer.getItemInHand(pHand).getItem();
                    digitizerEntity.LoadLinkerData(pLevel);

                    pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
                    pPlayer.sendSystemMessage(Component.literal("Inserted Stored Linker"));


                    pLevel.playSound(null, playerPos, SoundEvents.ITEM_FRAME_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

                }


            }
            else if (item.getItem() == ItemStack.EMPTY.getItem()) {
                if (pPlayer.isShiftKeyDown()) {
                    digitizerEntity.dropStoredLinker();
                } else {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, (DigitizerEntity) digitizerEntity, pPos);
                }

            }
        }



        return InteractionResult.SUCCESS;
    }


    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (!pLevel.isClientSide()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

            if (blockEntity instanceof DigitizerEntity digitizerEntity) {
                digitizerEntity.dropStoredLinker();
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DigitizerEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide()) {
            return null;
        }

        // Only create a ticker if it's the correct BlockEntity type
        return createTickerHelper(pBlockEntityType, ModBlockEntities.DIGITIZER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> {
                    if (pBlockEntity instanceof DigitizerEntity) {
                        try {
                            pBlockEntity.tick(pLevel1, pPos, pState1);  // Make sure the tick method is called safely
                        } catch (IndexOutOfBoundsException e) {
                            // Handle IndexOutOfBoundsException in case of invalid inventory access
                            //devPrint("Error in BlockEntity ticker: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

}