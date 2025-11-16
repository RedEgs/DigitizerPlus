package net.redegs.digitizerplus;

import com.mojang.logging.LogUtils;
import dan200.computercraft.api.peripheral.IPeripheral;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.redegs.digitizerplus.block.ModBlocks;
import net.redegs.digitizerplus.block.entity.DigitizerEntity;
import net.redegs.digitizerplus.block.entity.ModBlockEntities;
import net.redegs.digitizerplus.block.entity.renderer.ComputerEntityRenderer;
import net.redegs.digitizerplus.client.Keybindings;
import net.redegs.digitizerplus.client.renderer.RobotDebugRenderer;
import net.redegs.digitizerplus.client.screen.ModMenuTypes;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.entity.ModEntities;
import net.redegs.digitizerplus.entity.renderer.HumanoidRobotRenderer;
import net.redegs.digitizerplus.imgui.Imgui;
import net.redegs.digitizerplus.item.ModCreativeModTabs;
import net.redegs.digitizerplus.item.ModItems;
import net.redegs.digitizerplus.misc.commands.Python;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.compat.cctweaked.peripheral.DigitizerPeripheral;
import net.redegs.digitizerplus.client.screen.*;
import net.redegs.digitizerplus.client.screen.digitizer.DigitizerScreen;
import net.redegs.digitizerplus.client.screen.robot.RobotScreen;
import net.redegs.digitizerplus.client.screen.storageblock.StorageBlockScreen;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Function;


// TODO -------

// MAKE THE WRAPPERS USEFULL
// MAKE THE DEFAULT COMPUTERS RESUME SCRIPT WHEN LOADING WORLD
// MAKE COMPUTERS DROP THEMSELVES WITH THEIR UUID.


// GRAPHICS MODE ON THE TERMINAL
// COMPONENT BASED BALANCING
// CRAFTING RECIPES
// CODE TO NOTEBOOK AND VICE VERSA (UPLOADIGN OF CODE (CLIENT<->SERVER))
// ROBOT LIKE FILE SYSTEM (SAVING, LOADING, VIRTUAL DIRECTORIES...)
// PSUEDO STATE MACHINE
// MAKE VEC3s NATIVE TO PYTHON
// FINISH EQUIPPING OF ARMOUR
// MAKE ROBOTS INVENTORIES READABLE
// ALLOW BUILDING
// ALLOW MACHINES AND WORKBENCHES TO BE USED
// ALLOW FAKE PLAYER INTERACTION
// ADD THE ABILITY TO READ SCHEMATICS
// IDENTIFY AND WORK WITH OTHER ROBOTS
// EVEN COOLER GUI





@Mod(DigitizerPlus.MOD_ID)
public class DigitizerPlus {
    public static final String MOD_ID = "digitizerplus";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ComputerManager COMPUTER_MANAGER = new ComputerManager();

    public DigitizerPlus(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModEntities.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, DigitizerPlus::attachPeripherals);

    }

    public static void attachPeripherals(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof DigitizerEntity digitizerEntity) {
            PeripheralProvider.attach(event, digitizerEntity, DigitizerPeripheral::new);
        }
    }

    public static final Capability<IPeripheral> CAPABILITY_PERIPHERAL = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation PERIPHERAL = new ResourceLocation(DigitizerPlus.MOD_ID, "peripheral");

    // A {@link ICapabilityProvider} that lazily creates an {@link IPeripheral} when required.
    private static final class PeripheralProvider<O extends BlockEntity> implements ICapabilityProvider {
        private final O blockEntity;
        private final Function<O, IPeripheral> factory;
        private @Nullable LazyOptional<IPeripheral> peripheral;

        private PeripheralProvider(O blockEntity, Function<O, IPeripheral> factory) {
            this.blockEntity = blockEntity;
            this.factory = factory;
        }

        private static <O extends BlockEntity> void attach(AttachCapabilitiesEvent<BlockEntity> event, O blockEntity, Function<O, IPeripheral> factory) {
            var provider = new PeripheralProvider<>(blockEntity, factory);
            event.addCapability(PERIPHERAL, provider);
            event.addListener(provider::invalidate);
        }

        private void invalidate() {
            if (peripheral != null) peripheral.invalidate();
            peripheral = null;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
            if (capability != CAPABILITY_PERIPHERAL) return LazyOptional.empty();
            if (blockEntity.isRemoved()) return LazyOptional.empty();

            var peripheral = this.peripheral;
            return (peripheral == null ? (this.peripheral = LazyOptional.of(() -> factory.apply(blockEntity))) : peripheral).cast();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup logic (if any)
    }


    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Register the BlockEntityRenderer for DigitizerEntity
            MenuScreens.register(ModMenuTypes.DIGITIZER_MENU.get(), DigitizerScreen::new);
            MenuScreens.register(ModMenuTypes.STORAGE_BLOCK_MENU.get(), StorageBlockScreen::new);
            MenuScreens.register(ModMenuTypes.ROBOT_MENU.get(), RobotScreen::new);

            MinecraftForge.EVENT_BUS.register(Python.class);
            MinecraftForge.EVENT_BUS.register(ComputerManager.class);

            MinecraftForge.EVENT_BUS.addListener(RobotDebugRenderer::onRenderWorldLast);
            MinecraftForge.EVENT_BUS.addListener(Imgui::onWorldUnload);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.HUMANOID_ROBOT.get(), HumanoidRobotRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.COMPUTER_BE.get(), ComputerEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(Keybindings.INSTANCE.openServerDebug);
        }

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class CommonModEvents {

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.HUMANOID_ROBOT.get(), HumanoidRobot.createAttributes().build());
        }

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event)  {
            event.enqueueWork(() -> {
                ModNetwork.register();
            });

        }

        @SubscribeEvent
        public void onServerStarting(ServerStartedEvent event) {
            // Server starting logic (if any)
        }

        @SubscribeEvent
        public void onWorldLoad(LevelEvent.Load event) {

        }


    }


}