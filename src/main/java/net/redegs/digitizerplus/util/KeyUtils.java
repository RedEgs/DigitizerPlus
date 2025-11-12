package net.redegs.digitizerplus.util;

import org.lwjgl.glfw.GLFW;

public class KeyUtils {
    public static char KeyToChar(int key, boolean shift, boolean capsLock) {
        // Handle letters A-Z
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            char base = (char) ('a' + (key - GLFW.GLFW_KEY_A));
            boolean upper = shift ^ capsLock; // XOR
            return upper ? Character.toUpperCase(base) : base;
        }

        // Handle numbers 0-9 (note: shift changes to symbols)
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            char[] noShift = {'0','1','2','3','4','5','6','7','8','9'};
            char[] withShift = {')','!','@','#','$','%','^','&','*','('};
            int idx = key - GLFW.GLFW_KEY_0;
            return shift ? withShift[idx] : noShift[idx];
        }

        // Handle common punctuation/symbols
        switch (key) {
            case GLFW.GLFW_KEY_SPACE: return ' ';
            case GLFW.GLFW_KEY_MINUS: return shift ? '_' : '-';
            case GLFW.GLFW_KEY_EQUAL: return shift ? '+' : '=';
            case GLFW.GLFW_KEY_LEFT_BRACKET: return shift ? '{' : '[';
            case GLFW.GLFW_KEY_RIGHT_BRACKET: return shift ? '}' : ']';
            case GLFW.GLFW_KEY_BACKSLASH: return shift ? '|' : '\\';
            case GLFW.GLFW_KEY_SEMICOLON: return shift ? ':' : ';';
            case GLFW.GLFW_KEY_APOSTROPHE: return shift ? '"' : '\'';
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return shift ? '~' : '`';
            case GLFW.GLFW_KEY_COMMA: return shift ? '<' : ',';
            case GLFW.GLFW_KEY_PERIOD: return shift ? '>' : '.';
            case GLFW.GLFW_KEY_SLASH: return shift ? '?' : '/';
            default: return '\0'; // Unhandled key
        }
    }
    public static boolean isNonLetterKey(int key) {
        // Return true if the key is NOT a letter (A-Z)
        // GLFW_KEY_A to GLFW_KEY_Z are contiguous
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return false; // It's a letter
        }

        // Common control and special keys
        return switch (key) {
            case
                 GLFW.GLFW_KEY_BACKSPACE,
                 GLFW.GLFW_KEY_ENTER,
                 GLFW.GLFW_KEY_TAB,
                 GLFW.GLFW_KEY_ESCAPE,
                 GLFW.GLFW_KEY_LEFT_SHIFT,
                 GLFW.GLFW_KEY_RIGHT_SHIFT,
                 GLFW.GLFW_KEY_LEFT_CONTROL,
                 GLFW.GLFW_KEY_RIGHT_CONTROL,
                 GLFW.GLFW_KEY_LEFT_ALT,
                 GLFW.GLFW_KEY_RIGHT_ALT,
                 GLFW.GLFW_KEY_LEFT_SUPER,
                 GLFW.GLFW_KEY_RIGHT_SUPER,
                 GLFW.GLFW_KEY_CAPS_LOCK,
                 GLFW.GLFW_KEY_NUM_LOCK,
                 GLFW.GLFW_KEY_SCROLL_LOCK,
                 GLFW.GLFW_KEY_PRINT_SCREEN,
                 GLFW.GLFW_KEY_PAUSE,
                 GLFW.GLFW_KEY_INSERT,
                 GLFW.GLFW_KEY_DELETE,
                 GLFW.GLFW_KEY_HOME,
                 GLFW.GLFW_KEY_END,
                 GLFW.GLFW_KEY_PAGE_UP,
                 GLFW.GLFW_KEY_PAGE_DOWN,
                 GLFW.GLFW_KEY_UP,
                 GLFW.GLFW_KEY_DOWN,
                 GLFW.GLFW_KEY_LEFT,
                 GLFW.GLFW_KEY_RIGHT,
                 GLFW.GLFW_KEY_MENU,
                 GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F2, GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4,
                 GLFW.GLFW_KEY_F5, GLFW.GLFW_KEY_F6, GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F8,
                 GLFW.GLFW_KEY_F9, GLFW.GLFW_KEY_F10, GLFW.GLFW_KEY_F11, GLFW.GLFW_KEY_F12 -> true;
            default -> false; // Assume non-letter unless explicitly a letter
        };
    }

    public static int CharToKey(char c) {
        // Handle uppercase letters
        if (c >= 'A' && c <= 'Z') {
            return GLFW.GLFW_KEY_A + (c - 'A');
        }
        // Handle lowercase letters
        if (c >= 'a' && c <= 'z') {
            return GLFW.GLFW_KEY_A + (c - 'a');
        }
        // Handle digits
        if (c >= '0' && c <= '9') {
            return GLFW.GLFW_KEY_0 + (c - '0');
        }

        // Handle punctuation and symbols
        switch (c) {
            case ' ': return GLFW.GLFW_KEY_SPACE;
            case '-': return GLFW.GLFW_KEY_MINUS;
            case '=': return GLFW.GLFW_KEY_EQUAL;
            case '[': return GLFW.GLFW_KEY_LEFT_BRACKET;
            case ']': return GLFW.GLFW_KEY_RIGHT_BRACKET;
            case '\\': return GLFW.GLFW_KEY_BACKSLASH;
            case ';': return GLFW.GLFW_KEY_SEMICOLON;
            case '\'': return GLFW.GLFW_KEY_APOSTROPHE;
            case ',': return GLFW.GLFW_KEY_COMMA;
            case '.': return GLFW.GLFW_KEY_PERIOD;
            case '/': return GLFW.GLFW_KEY_SLASH;
            case '`': return GLFW.GLFW_KEY_GRAVE_ACCENT;
            default: return -1;
        }
    }
}
