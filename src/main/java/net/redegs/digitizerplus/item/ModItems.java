package net.redegs.digitizerplus.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.item.custom.LinkerItem;
import net.redegs.digitizerplus.item.custom.ProgrammerItem;
import net.redegs.digitizerplus.item.custom.RobotSpawnerItem;
import net.redegs.digitizerplus.item.custom.StorageCardItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DigitizerPlus.MOD_ID);

    public static final RegistryObject<Item> LINKER = ITEMS.register("linker",
            () -> new LinkerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STORAGE_CARD = ITEMS.register("storage_card",
            () -> new StorageCardItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ROBOT_SPAWNER = ITEMS.register("robot_spawner",
            () -> new RobotSpawnerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PROGRAMMER_ITEM = ITEMS.register("programmer",
            () -> new ProgrammerItem(new Item.Properties().stacksTo(1)));





    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}