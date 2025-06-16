package net.redegs.digitizerplus.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.redegs.digitizerplus.imgui.Imgui;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import imgui.*;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    public void setup(long l, CallbackInfo ci) {
//        ImguiLoader.onGlfwInit(l); // this is broken in 1.21.5
    }

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void keyPress(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (Imgui.screenFocused) {
            ci.cancel();

            ImGuiIO io = ImGui.getIO();
            io.setKeysDown(i, k == InputConstants.PRESS);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void charTyped(long l, int i, int j, CallbackInfo ci) {
        if (Imgui.screenFocused) {
            ci.cancel();
        }
    }
}
