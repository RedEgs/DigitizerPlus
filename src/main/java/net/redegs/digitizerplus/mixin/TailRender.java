package net.redegs.digitizerplus.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.redegs.digitizerplus.imgui.Imgui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class)
public class TailRender {
    @Inject(at = @At("HEAD"), method="flipFrame")
    private static void runTickTail(CallbackInfo ci) {
        Imgui.Render();
    }
}


