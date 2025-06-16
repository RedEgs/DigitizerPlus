package net.redegs.digitizerplus.python;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

public class MCWrapper {
    Minecraft minecraft = Minecraft.getInstance();
    ClientLevel level = minecraft.level;

    public void print(String msg) {
        // Send message to all players on the server
        level.players().forEach(player ->
                player.sendSystemMessage(Component.literal(msg))
        );
    }
}
