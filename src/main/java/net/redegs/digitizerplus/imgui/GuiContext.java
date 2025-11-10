package net.redegs.digitizerplus.imgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

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
        this.Active = false;
        Imgui.renderList.remove(this.ContextID-1);
    }

}
