package net.redegs.digitizerplus.misc;

public class DigitalRegister {
    private final int registerIndex;
    public DigitalStack digitalStack;

    public DigitalRegister(int registerIndex) {
        this.registerIndex = registerIndex;
    }

    public void writeRegister(DigitalStack digitalStack) {
        this.digitalStack = digitalStack;
    }

    public DigitalStack readRegister() {
        return this.digitalStack;
    }

    public void ClearRegister() {
        this.digitalStack = null;
    }

}
