package net.redegs.digitizerplus.entity;


import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.redegs.digitizerplus.DigitizerPlus;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DigitizerPlus.MOD_ID);

    public static final RegistryObject<EntityType<HumanoidRobot>> HUMANOID_ROBOT =
            ENTITY_TYPES.register("humanoid_robot",
                    () -> EntityType.Builder.of(HumanoidRobot::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F) // Width and height of the NPC
                            .build(new ResourceLocation(DigitizerPlus.MOD_ID, "humanoid_robot").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}