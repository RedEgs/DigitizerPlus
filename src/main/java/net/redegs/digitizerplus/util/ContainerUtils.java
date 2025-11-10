package net.redegs.digitizerplus.util;

import net.minecraft.world.Container;

import javax.annotation.Nullable;

public class ContainerUtils {

    @Nullable
    public static int FindNextEmptySlot(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

}
