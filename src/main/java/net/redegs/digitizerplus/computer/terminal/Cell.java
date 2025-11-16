package net.redegs.digitizerplus.computer.terminal;

public class Cell {
    public char ch;
    public int fgColor; // Foreground (text) color
    public int bgColor; // Background color (new!)

    public Cell(char ch, int fgColor, int bgColor) {
        this.ch = ch;
        this.fgColor = fgColor; // RGB (FF FF FF)
        this.bgColor = bgColor; // ARGB (FF FF FF FF)
    }

    public static final Cell EMPTY = new Cell(' ', 0xFFFFFFFF, 0x00000000);
}