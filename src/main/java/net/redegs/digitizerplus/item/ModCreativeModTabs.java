package net.redegs.digitizerplus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DigitizerPlus.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DIGITIZER_BLOCK.get()))
                    .title(Component.translatable("creativetab.creative_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.DIGITIZER_BLOCK.get());
                        pOutput.accept(ModItems.LINKER.get());
                        pOutput.accept(ModBlocks.STORAGE_BLOCK.get());
                        pOutput.accept(ModItems.STORAGE_CARD.get());
                        pOutput.accept(ModItems.PROGRAMMER_ITEM.get());
                        pOutput.accept(ModItems.ROBOT_SPAWNER.get());
                        pOutput.accept(ModBlocks.COMPUTER_BLOCK.get());

                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}