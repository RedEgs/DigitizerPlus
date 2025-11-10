package net.redegs.digitizerplus.computer.terminal;

public class Cell {
    public char ch;
    public int color;

    public Cell(char ch, int color) {
        this.ch = ch;
        this.color = color;
    }

    public static final Cell EMPTY = new Cell('\0', 0xFFFFFFFF);
}
