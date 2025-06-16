package net.redegs.digitizerplus.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.imgui.Imgui;
import net.redegs.digitizerplus.imgui.guis.RobotUI;
import net.redegs.digitizerplus.python.PythonErrorResolver;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JepEditorPacket {
    private final String stacktrace;
    private final String errorMsg;
    private final Integer entityID;

    public JepEditorPacket(String stacktrace, String errorMsg, Integer guiContext) {
        this.stacktrace = stacktrace;
        this.errorMsg = errorMsg;
        this.entityID = guiContext;
    }

    public static void encode(JepEditorPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.stacktrace);
        buf.writeUtf(packet.errorMsg);
        buf.writeInt(packet.entityID);
    }

    public static JepEditorPacket decode(FriendlyByteBuf buf) {
        String stacktrace = buf.readUtf();
        String errorMsg = buf.readUtf();
        Integer guiContext = buf.readInt();

        return new JepEditorPacket(stacktrace, errorMsg, guiContext);
    }

    public static void handle(JepEditorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                Level level = mc.level;

                Entity entity = level.getEntity(packet.entityID);
                HumanoidRobot robot = (HumanoidRobot) entity;

                RobotUI guiContext = (RobotUI) Imgui.renderList.get(robot.robotUI.ContextID-1);

                HashMap<Integer, String> errorMarker = PythonErrorResolver.resolveError(packet.stacktrace, packet.errorMsg);
                guiContext.threadRunning = false;
                if (errorMarker.size() >= 1) guiContext.showError(errorMarker);



            }

        });
        context.setPacketHandled(true);
    }

}
