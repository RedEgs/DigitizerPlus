package net.redegs.digitizerplus.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.redegs.digitizerplus.client.screen.ModMenuTypes;

public class SimpleMenu extends AbstractContainerMenu {
    public Container internalContainer;
    private Level level;

    public SimpleMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.SIMPLE_MENU.get(), containerId);

        this.internalContainer = new SimpleContainer(extraData.readInt());
        for (int i = 0; i < extraData.readInt(); i++) {
            this.addSlot(new Slot(internalContainer, i, 0, 0)); // use real x/y later
        }

    }


    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }


}
