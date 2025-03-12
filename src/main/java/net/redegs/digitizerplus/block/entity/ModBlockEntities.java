package net.redegs.digitizerplus.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DigitizerPlus.MOD_ID);

    public static final RegistryObject<BlockEntityType<DigitizerEntity>> DIGITIZER_BE =
            BLOCK_ENTITIES.register("digitizer_be", () ->
                    BlockEntityType.Builder.of(DigitizerEntity::new,
                            ModBlocks.DIGITIZER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<StorageBlockEntity>> STORAGE_BE =
            BLOCK_ENTITIES.register("storage_be", () ->
                    BlockEntityType.Builder.of(StorageBlockEntity::new,
                            ModBlocks.STORAGE_BLOCK.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}