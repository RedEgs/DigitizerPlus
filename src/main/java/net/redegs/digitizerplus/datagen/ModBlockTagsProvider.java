package net.redegs.digitizerplus.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.tags.TagEntry.tag;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, DigitizerPlus.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Add your blocks to the appropriate tags
       this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
               .add(ModBlocks.DIGITIZER_BLOCK.get(),
               ModBlocks.STORAGE_BLOCK.get());
    }
}