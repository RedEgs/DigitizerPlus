package net.redegs.digitizerplus.computer.terminal.program;

import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.terminal.Cell;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TextEditorProgram extends TerminalProgram {
    private List<String> lines = new ArrayList<>();
    private int editorCursorX = 0;
    private int editorCursorY = 0;
    private int scrollOffset = 0;

    private final Path filePath;
    private final String filename;
    private TerminalProgram previousProgram;

    private boolean inMenu = false;
    private int menuSelector = 0;

    private String statusText = null;
    private final int textColor = 0xFFFFFF;
    private final int statusColor = 0x00FF00;

    // === Python Syntax Colors ===
    private static final int COLOR_KEYWORD = 0xFFAA00;
    private static final int COLOR_STRING  = 0x00FF00;
    private static final int COLOR_NUMBER  = 0x00AAFF;
    private static final int COLOR_COMMENT = 0x888888;
    private static final int COLOR_BUILTIN = 0xFF55FF;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "def", "return", "if", "elif", "else", "for", "while", "break", "continue",
            "import", "from", "as", "class", "try", "except", "finally", "raise", "with",
            "pass", "lambda", "yield", "assert", "del", "global", "nonlocal", "True",
            "False", "None", "and", "or", "not", "in", "is"
    ));

    private static final Set<String> BUILTINS = new HashSet<>(Arrays.asList(
            "print", "len", "range", "int", "float", "str", "list", "dict", "set",
            "tuple", "open", "type", "dir", "input", "sum", "max", "min", "abs", "map",
            "filter", "zip", "enumerate", "any", "all", "sorted"
    ));

    public TextEditorProgram(Terminal terminal, Path filePath, TerminalProgram previousProgram) throws IOException {
        super(terminal);
        this.fullControl = true;
        this.filePath = filePath;
        this.filename = filePath.getFileName().toString();
        this.previousProgram = previousProgram;

        this.lines = Files.readAllLines(filePath);
        if (lines.isEmpty()) lines.add("");
    }

    @Override
    public void start() {
        render();
    }


    public void render() {
        Cell[][] buf = terminal.getBuffer();
        int rows = buf.length;
        int cols = buf[0].length;

        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                buf[y][x] = new Cell(' ', textColor);

        int contentRows = rows - 1;
        for (int i = 0; i < contentRows; i++) {
            int fileLine = scrollOffset + i;
            if (fileLine >= lines.size()) break;

            String line = lines.get(fileLine);
            Cell[] cellRow = new Cell[cols];
            for (int x = 0; x < cols; x++) {
                char c = (x < line.length()) ? line.charAt(x) : ' ';
                cellRow[x] = new Cell(c, textColor);
            }

            // Apply Python syntax highlighting
            applyPythonHighlight(cellRow, line);

            for (int x = 0; x < cols; x++)
                buf[i][x] = cellRow[x];
        }

        // Status bar
        String status;
        if (inMenu) {
            if (menuSelector == 0)
                status = " [Save File] |  Run  |  Exit  ";
            else if (menuSelector == 1)
                status = "  Save File  | [Run] |  Exit  ";
            else
                status = "  Save File  |  Run  | [Exit] ";
        } else {
            status = (statusText == null)
                    ? filename + " | Ln " + (editorCursorY + 1) + ", Col " + (editorCursorX + 1)
                    : statusText;
        }

        for (int x = 0; x < Math.min(status.length(), cols); x++)
            buf[rows - 1][x] = new Cell(status.charAt(x), statusColor);

        int screenY = editorCursorY - scrollOffset;
        if (screenY >= 0 && screenY < contentRows)
            terminal.setCursor(editorCursorX, screenY);
        else
            terminal.setCursor(0, rows - 1);
    }

    private void applyPythonHighlight(Cell[] lineCells, String text) {
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);

            // Comments #
            if (c == '#') {
                setColor(lineCells, i, text.length(), COLOR_COMMENT);
                break;
            }

            // Strings '...' or "..."
            else if (c == '"' || c == '\'') {
                char quote = c;
                int start = i++;
                while (i < text.length()) {
                    if (text.charAt(i) == quote) {
                        if (i + 2 < text.length() && text.charAt(i + 1) == quote && text.charAt(i + 2) == quote)
                            i += 3; // triple quote end
                        else i++;
                        break;
                    }
                    i++;
                }
                setColor(lineCells, start, Math.min(i, text.length()), COLOR_STRING);
            }

            // Numbers
            else if (Character.isDigit(c)) {
                int start = i++;
                while (i < text.length() && (Character.isDigit(text.charAt(i)) || text.charAt(i) == '.'))
                    i++;
                setColor(lineCells, start, i, COLOR_NUMBER);
            }

            // Keywords or builtins
            else if (Character.isLetter(c) || c == '_') {
                int start = i++;
                while (i < text.length() && (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_'))
                    i++;
                String word = text.substring(start, i);
                if (KEYWORDS.contains(word))
                    setColor(lineCells, start, i, COLOR_KEYWORD);
                else if (BUILTINS.contains(word))
                    setColor(lineCells, start, i, COLOR_BUILTIN);
            } else i++;
        }
    }

    private void setColor(Cell[] line, int start, int end, int color) {
        for (int i = start; i < end && i < line.length; i++)
            line[i].color = color;
    }


    public void onPaste() {
        // Get Clipbaord text
        String cbText = new String();
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                cbText = (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cbText.isEmpty()) return;



        // Split pasted content into lines
        String[] pasteLines = cbText.split("\n", -1);

        String currentLine = lines.get(editorCursorY);
        String before = currentLine.substring(0, editorCursorX);
        String after = currentLine.substring(editorCursorX);

        if (pasteLines.length == 1) {
            // Single-line paste
            lines.set(editorCursorY, before + pasteLines[0] + after);
            editorCursorX += pasteLines[0].length();
        } else {
            // Multi-line paste
            lines.set(editorCursorY, before + pasteLines[0]);

            int insertAt = editorCursorY + 1;
            for (int i = 1; i < pasteLines.length - 1; i++) {
                lines.add(insertAt++, pasteLines[i]);
            }

            // Last line merges with whatever was after cursor
            lines.add(insertAt, pasteLines[pasteLines.length - 1] + after);

            editorCursorY = insertAt;
            editorCursorX = pasteLines[pasteLines.length - 1].length();
        }

        render();
    }


    @Override
    public void onKeyDown(int key) {
        if (terminal.keysDown.contains(GLFW.GLFW_KEY_LEFT_ALT)) {
            openMenu();
        }

        if (terminal.keysDown.contains(GLFW.GLFW_KEY_LEFT_CONTROL)) {
//            if (key == GLFW.GLFW_KEY_V) {
            DigitizerPlus.LOGGER.info("pRESSED PASTE");
            onPaste();
//            }

        }
    }

    @Override
    public void onKeyUp(int key) {

    }

    @Override
    public void onKey(char c) {
        String line = lines.get(editorCursorY);
        StringBuilder sb = new StringBuilder(line);
        sb.insert(editorCursorX, c);
        lines.set(editorCursorY, sb.toString());
        editorCursorX++;
        render();
    }

    @Override
    public void onBackspace() {
        if (editorCursorX > 0) {
            String line = lines.get(editorCursorY);
            StringBuilder sb = new StringBuilder(line);
            sb.deleteCharAt(editorCursorX - 1);
            lines.set(editorCursorY, sb.toString());
            editorCursorX--;
        } else if (editorCursorY > 0) {
            String prev = lines.get(editorCursorY - 1);
            String curr = lines.remove(editorCursorY);
            editorCursorY--;
            editorCursorX = prev.length();
            lines.set(editorCursorY, prev + curr);
        }
        render();
    }

    @Override
    public void onNewline(String ignored) {
        if (inMenu) {
            if (menuSelector == 0) {
                save();
                inMenu = false;
            } else if (menuSelector == 2) {
                terminal.clearBuffer();
                this.stop();
                return;
            }
            render();
            return;
        }

        String line = lines.get(editorCursorY);
        String before = line.substring(0, editorCursorX);
        String after = line.substring(editorCursorX);
        lines.set(editorCursorY, before);
        lines.add(editorCursorY + 1, after);

        editorCursorY++;
        editorCursorX = 0;
        render();
    }

    public void onTab() {
        int tabWidth = 4; // or pull from terminal if you want
        String line = lines.get(editorCursorY);
        StringBuilder sb = new StringBuilder(line);

        // insert spaces until next multiple of tabWidth
        int spaces = tabWidth - (editorCursorX % tabWidth);
        for (int i = 0; i < spaces; i++) {
            sb.insert(editorCursorX, ' ');
            editorCursorX++;
        }
        lines.set(editorCursorY, sb.toString());
        render();
    }


    public void moveCursorUp() {
        if (inMenu) {
            render();
            return;
        }

        if (editorCursorY > 0) {
            editorCursorY--;
            if (editorCursorX > lines.get(editorCursorY).length()) {
                editorCursorX = lines.get(editorCursorY).length();
            }
            if (editorCursorY < scrollOffset) {
                scrollOffset--;
            }
        }
        render();
    }

    public void moveCursorDown() {
        if (inMenu) {
            render();
            return;
        }

        if (editorCursorY < lines.size() - 1) {
            editorCursorY++;
            if (editorCursorX > lines.get(editorCursorY).length()) {
                editorCursorX = lines.get(editorCursorY).length();
            }
            int visibleRows = terminal.getBuffer().length - 1;
            if (editorCursorY >= scrollOffset + visibleRows) {
                scrollOffset++;
            }
        }
        render();
    }

    public void moveCursorLeft() {
        if (inMenu) {
            if (menuSelector == 0) menuSelector = 2;
            else menuSelector -= 1;

            render();
            return;
        }

        if (editorCursorX > 0) {
            editorCursorX--;
        } else if (editorCursorY > 0) {
            editorCursorY--;
            editorCursorX = lines.get(editorCursorY).length();
        }
        render();
    }

    public void moveCursorRight() {
        if (inMenu) {
            if (menuSelector == 2) menuSelector = 0;
            else menuSelector += 1;

            render();
            return;
        }

        if (editorCursorX < lines.get(editorCursorY).length()) {
            editorCursorX++;
        } else if (editorCursorY < lines.size() - 1) {
            editorCursorY++;
            editorCursorX = 0;
        }
        render();
    }

    @Override
    public void leftKey() {
        moveCursorLeft();
    }

    @Override
    public void rightKey() {
        moveCursorRight();
    }

    @Override
    public void downKey() {
        moveCursorDown();
    }

    @Override
    public void upKey() {
        moveCursorUp();
    }

    @Override
    public void controlX() {
        this.stop();
    }

    public void openMenu() {
        inMenu = !inMenu;
        render();
    }


    public void save() {
        try {
            Files.write(filePath, lines);
            statusText = "Saved changes to file.";
            render();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                statusText = null;
                render();
            }, 1, TimeUnit.SECONDS);
        } catch (IOException e) {
            terminal.println("[Error saving file: " + e.getMessage() + "]", true);
        }
    }

    @Override
    public void stop() {
        this.terminal.startProgram(previousProgram);
    }
}
