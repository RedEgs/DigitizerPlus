package net.redegs.digitizerplus.python.wrappers;

import net.redegs.digitizerplus.python.PythonRunner;

public class PythonThreadWrapper {
    private PythonRunner runner;

    public PythonThreadWrapper(PythonRunner runner) {
        this.runner = runner;
    }

    public void close() {
        this.runner.stop();
    }


}
