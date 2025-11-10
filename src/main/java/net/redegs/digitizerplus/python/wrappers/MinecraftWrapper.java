package net.redegs.digitizerplus.python.wrappers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

public class MinecraftWrapper {


    public void print(String msg) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        // Send message to all players on the server
        level.players().forEach(player ->
                player.sendSystemMessage(Component.literal(msg))
        );
    }
}
