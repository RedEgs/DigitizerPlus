package net.redegs.digitizerplus.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.redegs.digitizerplus.block.ModBlocks;
import net.minecraftforge.registries.RegistryObject;


import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output) {
        super(output, Collections.emptySet(), Collections.singletonList(
                new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    public static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // Add your blocks here
            this.dropSelf(ModBlocks.DIGITIZER_BLOCK.get()); // Replace with your block
            this.dropSelf(ModBlocks.STORAGE_BLOCK.get()); // Replace with your block
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
        }
    }
}