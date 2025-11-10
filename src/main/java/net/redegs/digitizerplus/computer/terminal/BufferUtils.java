package net.redegs.digitizerplus.computer.terminal;

import java.util.Arrays;

public class BufferUtils {
    private static final int TAB_WIDTH = 4;

    /** Insert a single character at (x, y). */
    public static void insertChar(char[][] buffer, int x, int y, char c) {
        if (y < 0 || y >= buffer.length) return;
        if (x < 0 || x >= buffer[y].length) return;
        buffer[y][x] = c;
    }

    /** Write text starting at (x, y). Wraps onto next lines if needed. */
    public static void writeText(char[][] buffer, int x, int y, String text) {
        int row = y;
        int col = x;

        for (int i = 0; i < text.length(); i++) {
            if (row >= buffer.length) return; // out of rows
            buffer[row][col] = text.charAt(i);
            col++;
            if (col >= buffer[row].length) {
                col = 0;
                row++;
            }
        }
    }

    /** Write a whole line at row y (overwrites existing content). */
    public static void writeLineAt(char[][] buffer, int y, String text) {
        if (y < 0 || y >= buffer.length) return;
        Arrays.fill(buffer[y], ' ');
        for (int i = 0; i < text.length() && i < buffer[y].length; i++) {
            buffer[y][i] = text.charAt(i);
        }
    }

    /** Clear the entire buffer (fill with spaces). */
    public static void clearBuffer(char[][  ] buffer) {
        for (int y = 0; y < buffer.length; y++) {
            Arrays.fill(buffer[y], ' ');
        }
    }

    /** Scroll the buffer up by 1 row, clear last row. */
    public static void scrollBufferUp(char[][] buffer) {
        for (int y = 1; y < buffer.length; y++) {
            buffer[y - 1] = Arrays.copyOf(buffer[y], buffer[y].length);
        }
        Arrays.fill(buffer[buffer.length - 1], ' ');
    }

    /** Utility: return index of last non-space character in a row, or -1 if empty. */
    public static int lastFilledIndex(char[][] buffer, int y) {
        if (y < 0 || y >= buffer.length) return -1;
        for (int x = buffer[y].length - 1; x >= 0; x--) {
            if (buffer[y][x] != ' ' && buffer[y][x] != '\0') {
                return x;
            }
        }
        return -1;
    }

    public static int[] findNextPosition(char[][] buffer) {
        for (int y = 0; y < buffer.length; y++) {
            for (int x = 0; x < buffer[y].length; x++) {
                if (buffer[y][x] == ' ' || buffer[y][x] == '\0') {
                    return new int[] {x, y};
                }
            }
        }
        // buffer full → return last cell
        return new int[] {buffer[0].length - 1, buffer.length - 1};
    }

    public static String BuildString(char[][] buffer) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : buffer) {
            for (char c : row) {
                sb.append(c);
            }
            sb.append('\n'); // Add a newline at the end of each row
        }
        return sb.toString();
    }
}
