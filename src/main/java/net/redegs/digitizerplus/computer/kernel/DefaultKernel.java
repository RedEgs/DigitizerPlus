package net.redegs.digitizerplus.computer.kernel;

public class DefaultKernel implements Runnable{
    /* This kernel is the default "Operating System" that runs on the computer.
       It should handle w/r to files, peripherals and able to launch processes to lua files.
     */

    private static KernelEngine kernel;

    public DefaultKernel(KernelEngine kernel) {
        this.kernel = kernel;
    }

    @Override
    public void run() {




    }
}
