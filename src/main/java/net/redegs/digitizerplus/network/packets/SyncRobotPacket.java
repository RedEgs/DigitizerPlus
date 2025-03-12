package net.redegs.digitizerplus.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SyncRobotPacket {
    private final Map<String, Integer> extraData;
    private final List<ItemStack> inventoryContents;

    public SyncRobotPacket(Map<String, Integer> extraData, List<ItemStack> inventoryContents) {
        this.extraData = extraData;
        this.inventoryContents = inventoryContents;
    }

    public static void encode(SyncRobotPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.extraData.size());
        for (Map.Entry<String, Integer> entry : packet.extraData.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }

        buf.writeInt(packet.inventoryContents.size());
        for (ItemStack stack : packet.inventoryContents) {
            buf.writeItem(stack);
        }

    }

    public static SyncRobotPacket decode(FriendlyByteBuf buf) {
        int mapSize = buf.readInt();
        Map<String, Integer> mapData = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            String key = buf.readUtf();
            int value = buf.readInt();
            mapData.put(key, value);
        }

        int size = buf.readInt();
        NonNullList<ItemStack> decodedInventoryContents = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            decodedInventoryContents.set(i, buf.readItem());
        }

        return new SyncRobotPacket(mapData, decodedInventoryContents);
    }

    public static void handle(SyncRobotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // This is run on the main thread

            if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                ClientLevel level = mc.level;
                Player player = mc.player;

                if (level != null) {
                    Entity entity = level.getEntity(packet.extraData.get("entityID"));
                    if (entity instanceof HumanoidRobot robot) {
                        for (int i = 0; i < packet.inventoryContents.size(); i++) {
                            robot.getInventory().setItem(i, packet.inventoryContents.get(i));
                        }
                    }
                }
            }

        });
        context.setPacketHandled(true);
    }
}