package net.redegs.digitizerplus.screen;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.api.SimpleMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, DigitizerPlus.MOD_ID);

    public static final RegistryObject<MenuType<DigitizerMenu>> DIGITIZER_MENU =
            registerMenuType("digitizer_menu", DigitizerMenu::new);

    public static final RegistryObject<MenuType<StorageBlockMenu>> STORAGE_BLOCK_MENU =
            registerMenuType("storage_block_menu", StorageBlockMenu::new);

    public static final RegistryObject<MenuType<SimpleMenu>> SIMPLE_MENU =
            MENUS.register("simple_menu", () -> IForgeMenuType.create(SimpleMenu::new));


    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

}
