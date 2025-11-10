package net.redegs.digitizerplus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.imgui.Imgui;
import net.redegs.digitizerplus.imgui.guis.RobotDebugUI;

@Mod.EventBusSubscriber(modid = DigitizerPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RobotDebugMenu {
    public static RobotDebugUI robotDebugUI = new RobotDebugUI();

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (Keybindings.INSTANCE.openServerDebug.isDown()) {
            Keybindings.INSTANCE.openServerDebug.consumeClick();

            robotDebugUI.setLevel(Minecraft.getInstance().level);
            robotDebugUI.setPlayer(Minecraft.getInstance().player);
            Imgui.FocusGuiContext(robotDebugUI);



        }
    }



}
