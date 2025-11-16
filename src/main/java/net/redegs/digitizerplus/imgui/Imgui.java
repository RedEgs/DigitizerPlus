package net.redegs.digitizerplus.imgui;

import imgui.*;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.client.screen.imgui.ImGuiScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

@Mod.EventBusSubscriber(modid = DigitizerPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Imgui {
    private static boolean initialized = false;
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public static ArrayList<GuiContext> renderList = new ArrayList<>();
    private static ImFontAtlas fontAtlas;

    public static boolean screenFocused = false;
    public static ImGuiScreen guiScreen = new ImGuiScreen(Component.literal("ImGui Screen"));

    public static void Init(long windowHandle) {
        if (initialized) return; // Prevent re-initialisation

        ImGui.createContext(); // Create imgui context
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null); // disables .ini file
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);     // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);   // Enable Multi-Viewport / Platform Windows
        io.setConfigViewportsNoTaskBarIcon(true);

        // Ranom font shii -------------
        fontAtlas = io.getFonts();
        final ImFontConfig fontConfig = new ImFontConfig(); // Natively allocated object, should be explicitly destroyed

//        fontAtlas.setFreeTypeRenderer(true); // can't access FreeType, couse it's in versions 1.87+, which seem to be incompatible

        fontAtlas.addFontDefault();

        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide
        rangesBuilder.addRanges(fontAtlas.getGlyphRangesDefault());
        rangesBuilder.addRanges(fontAtlas.getGlyphRangesCyrillic());
        rangesBuilder.addRanges(fontAtlas.getGlyphRangesJapanese());

        fontConfig.setMergeMode(true); // When enabled, all fonts added with this config would be merged with the previously added font
//        fontConfig.setPixelSnapH(true);

        final short[] glyphRanges = rangesBuilder.buildRanges();

        fontAtlas.build();
        fontConfig.destroy();
        // -------------

        // Window style settings ----
        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
        // ----------------------

//        // -- KEYMAP CORRECTION SHIT



        // Attach to the current GLFW window handle
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init();

        io.setKeyMap(ImGuiKey.Tab, GLFW.GLFW_KEY_TAB);
        io.setKeyMap(ImGuiKey.LeftArrow, GLFW.GLFW_KEY_LEFT);
        io.setKeyMap(ImGuiKey.RightArrow, GLFW.GLFW_KEY_RIGHT);
        io.setKeyMap(ImGuiKey.UpArrow, GLFW.GLFW_KEY_UP);
        io.setKeyMap(ImGuiKey.DownArrow, GLFW.GLFW_KEY_DOWN);
        io.setKeyMap(ImGuiKey.Backspace, GLFW.GLFW_KEY_BACKSPACE);
        io.setKeyMap(ImGuiKey.Delete, GLFW.GLFW_KEY_DELETE);
        io.setKeyMap(ImGuiKey.Enter, GLFW.GLFW_KEY_ENTER);
        io.setKeyMap(ImGuiKey.Escape, GLFW.GLFW_KEY_ESCAPE);


        initialized = true;
        DigitizerPlus.LOGGER.info("Initialised ImGUI!");



    }

    public static void Render() {
        if (!initialized) return; // Only render if ImGui is initialised

        imGuiGlfw.newFrame();
        ImGui.newFrame();


        // UI CODE GOES BETWEEN HERE >>> ---------------------------
        for (GuiContext context: renderList) {
            if (context.Active) {
                context.Main();
                CheckFocus();
            }
        }



        ///  --------------------------------------


        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }

    }

    public static boolean CheckFocus() {
        if (!initialized) return false;

        boolean focused = ImGui.isWindowFocused(ImGuiFocusedFlags.AnyWindow) || ImGui.isAnyItemFocused();

        if (!screenFocused && focused) {
            screenFocused = true;
            guiScreen.open();
        } else if (screenFocused && !focused) {
            screenFocused = false;
            Minecraft.getInstance().setScreen(null);
        }

        return screenFocused;
    }


    private static void focusContext(int ContextID) {
        if (!initialized) return;

        try{
            if (renderList.get(ContextID) != null) {
                screenFocused = true;
                GuiContext context = renderList.get(ContextID);

    //            System.out.println("OPening");
                ImGui.setNextWindowFocus();
                //ImGui.getIO().setMousePos(ImGui.getWindowPosX(), ImGui.getWindowPosY()) ;

                context.Active = true;
                guiScreen.open();
            }
        } catch (IndexOutOfBoundsException e) {
            DigitizerPlus.LOGGER.warn("Tried to access ImGui render list  index out of bounds: " + e.getMessage());

        }
    }

    public static void DestroyGuiContext(int ContextID) {
        if (!initialized) return;
        if (renderList.get(ContextID) != null) {
            GuiContext context = renderList.get(ContextID);
            context.Destroy();
        }
    }

    public static void DestroyGuiContext(GuiContext context) {
        DestroyGuiContext(context.ContextID-1);
    }

    public static void DestroyAllContexts() {
        for (GuiContext context : renderList) {
            context.Destroy();
        }
    }

    public static void FocusGuiContext(int ContextID) {
        focusContext(ContextID);
    }
    public static void FocusGuiContext(@NotNull GuiContext context) {
        focusContext(context.ContextID-1);
    }
    @Nullable
    public static GuiContext getConext(int ContextID) {
        if (!initialized) return null;

        if (renderList.get(ContextID) != null) {
            return renderList.get(ContextID);
        }

        return null;
    }


    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (renderList.size() >= 0) {
            return;
        }

        Level level = (Level) event.getLevel();
        if (!level.isClientSide) {
            DestroyAllContexts();
        }
    }
}
