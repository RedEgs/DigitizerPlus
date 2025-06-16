package net.redegs.digitizerplus.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.imgui.Imgui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class GLFWMixin {
    @Shadow @Final private long window;

    @Inject(at = @At("TAIL"),method = "<init>",remap = false)
    private void onGLFWInit(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, String string, String string2, CallbackInfo ci){
        Imgui.Init(window);
    }
}
