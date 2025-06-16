package net.redegs.digitizerplus.imgui;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.redegs.digitizerplus.screen.ImGuiScreen;

public class GuiContext {
    public int ContextID;
    public boolean Active = true;

    protected LocalPlayer localPlayer = Minecraft.getInstance().player;
    protected boolean screenFocused = false;

    public GuiContext() {
        if (!Imgui.renderList.contains(this)) {
            Imgui.renderList.add(this);
        }
        ContextID = Imgui.renderList.size();
    }

    public void Main() {
        // GUI CODE GOES HERE

//        ImGui.begin("ImGui UI");
//        ImGui.text("This is ImGui inside Minecraft!");
//        ImGui.end();
    }

    public void Destroy() {
        Imgui.renderList.remove(this.ContextID-1);
    }

}
