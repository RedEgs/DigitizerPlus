package net.redegs.digitizerplus.imgui.guis;

import imgui.*;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.entity.HumanoidRobot;
import net.redegs.digitizerplus.imgui.GuiContext;
import net.redegs.digitizerplus.network.ModNetwork;
import net.redegs.digitizerplus.network.packets.JepServerPacket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class RobotUI extends GuiContext {
    protected HumanoidRobot RobotParent;

    private ImString _code;
    private TextEditor codeEditor;
    private static String _boilerplate = """
        # Python Code Here >>>
                
                
                
                
                
                
                
                
                
                
                
        """;

    public boolean threadRunning = false;
    public ImBoolean detailedDebug = new ImBoolean(false);

    public RobotUI(HumanoidRobot robotParent) {
        super();
        Active = false;
        RobotParent = robotParent;

        _code = new ImString();
        codeEditor = new TextEditor();
        codeEditor.setText(_boilerplate);

    }

    @Override
    public void Main() {
        ImGui.setNextWindowSize(500, 400);
        ImGui.begin("Robot " + ContextID + " Information", ImGuiWindowFlags.MenuBar);

        Path filePath;
        try {
            filePath = DigitizerPlus.COMPUTER_MANAGER.PathFromUUID(RobotParent.getUUID());
        } catch (Exception e) {
            DigitizerPlus.LOGGER.warn("couldn't resolve file path");
            throw new RuntimeException(e);
        }



        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                // File code shi
                if (ImGui.menuItem("Save File", "Ctrl+S")) {
                    try {
                        Path recentFilePath = filePath.resolve("recent.py");
                        if (!Files.exists(recentFilePath)) {

                            Files.createFile(recentFilePath);
                        }

                        Files.writeString(filePath.resolve("recent.py"), codeEditor.getText());
                    }
                    catch (IOException e) {
                        System.err.println("An error occurred: " + e.getMessage());
                    }
                }

                if (ImGui.menuItem("Load File")) {
                    try {
                        if (!Files.exists(filePath.resolve("recent.py"))) {
                            codeEditor.setText(_boilerplate);
                        } else {
                            String text = Files.readString(filePath.resolve("recent.py"));
                            codeEditor.setText(text);
                        }
                    }  catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                ImGui.endMenu();


            }

            if (!RobotParent.isCodeExecuting()){
                if (ImGui.beginMenu("Run")) {
                    // Execute code shii
                    if (ImGui.menuItem("Execute", "F5") && !this.threadRunning) {
                        // Sends python code to server for server-side execution

                        try {
                            ModNetwork.sendToServer(new JepServerPacket(RobotParent.getId(), codeEditor.getText()));

                            Path recentFilePath = filePath.resolve("recent.py");
                            if (!Files.exists(recentFilePath)) {
                                Files.createFile(recentFilePath);
                            }
                            Files.writeString(filePath.resolve("recent.py"), codeEditor.getText());

                        } catch (Exception e) {
                            this.threadRunning = false;
                            System.out.println(e);
                        }

                        this.threadRunning = true;
                    }
                    ImGui.endMenu();
                }
            } else {
                if (ImGui.beginMenu("Stop")) {

                    if (ImGui.menuItem("Stop", "F5")) {
                        // Sends python code to server for server-side execution

                        try {
                            System.out.println("send stop packet");
                            ModNetwork.sendToServer(new JepServerPacket(RobotParent.getId(), "STOP"));
                            this.threadRunning = false;

                        } catch (Exception e) {
                            this.threadRunning = true;
                            System.out.println(e);
                        }
                    }

                    ImGui.endMenu();
                }

            }


            if (ImGui.beginMenu("Edit")) {
                final boolean ro = codeEditor.isReadOnly();
                if (ImGui.menuItem("Undo", "ALT-Backspace", !ro && codeEditor.canUndo())) {
                    codeEditor.undo(1);
                }
                if (ImGui.menuItem("Redo", "Ctrl-Y", !ro && codeEditor.canRedo())) {
                    codeEditor.redo(1);
                }

                ImGui.separator();

                if (ImGui.menuItem("Copy", "Ctrl-C", codeEditor.hasSelection())) {
                    codeEditor.copy();
                }
                if (ImGui.menuItem("Cut", "Ctrl-X", !ro && codeEditor.hasSelection())) {
                    codeEditor.cut();
                }
                if (ImGui.menuItem("Delete", "Del", !ro && codeEditor.hasSelection())) {
                    codeEditor.delete();
                }
                if (ImGui.menuItem("Paste", "Ctrl-V", !ro && ImGui.getClipboardText() != null)) {
                    codeEditor.paste();
                }

                ImGui.endMenu();
            }



            ImGui.endMenuBar();
        }

        codeEditor.render("Code codeEditor");
        ImGui.checkbox("Enable detailed errors", this.detailedDebug);

        ImGui.end();
    }

    public void showError(Integer line, String error) {
        HashMap<Integer, String> errorMarker = new HashMap<Integer, String>();
        errorMarker.put(line, error);
        codeEditor.setErrorMarkers(errorMarker);
    }

    public void showError(HashMap<Integer, String> errorMarker) {
        codeEditor.setErrorMarkers(errorMarker);
    }


}
