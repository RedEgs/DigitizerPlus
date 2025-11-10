package net.redegs.digitizerplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.redegs.digitizerplus.DigitizerPlus;

public final class Keybindings {
    public static final Keybindings INSTANCE =  new Keybindings();

    private Keybindings() {}

    public static final String CATEGORY = "key.categories." + DigitizerPlus.MOD_ID;

    public final KeyMapping openServerDebug = new KeyMapping(
            "key." + DigitizerPlus.MOD_ID + ".open_server_debug",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_GRAVE, -1),
            CATEGORY
    );
}
