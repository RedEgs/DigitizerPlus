package net.redegs.digitizerplus.computer.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KernelEngine {
    public static class Process {
        // Run's user's code and other operating system processes.

        final int pid; // Process-ID assigned by the kernel's `nextPid` int
        final Runnable entry; // The process/code to run
        final KernelEngine kernel; // The kernel its running on
        volatile boolean alive = true;

        Process(int pid, Runnable entry, KernelEngine kernel) {
            this.pid = pid;
            this.entry = entry;
            this.kernel = kernel;
        }

        // Syscalls
        public void writeFile(String path, String text) {
            try { kernel.vfs.write(path, text.getBytes()); } catch (IOException ignored) {}
        }

        public String readFile(String path) {
            try {
                byte[] d = kernel.vfs.read(path);
                return d != null ? new String(d) : null;
            } catch (IOException ignored) {}
            return null;
        }

        public Device openDevice(String id) { return kernel.getDevice(id); }
    }
    static class Yield extends RuntimeException {
        final long delayMs;
        Yield(long delay) { this.delayMs = delay; }
    }


    public interface VirtualFileSystemInterface {
        void write(String path, byte[] data) throws IOException;
        byte[] read(String path) throws IOException;
    }
    public static class VirtualFileSystem implements VirtualFileSystemInterface {
        private final Path root; // The location where the machine is stored in the minecraft save file

        public VirtualFileSystem(Path rootDir) throws IOException {
            this.root = rootDir;
            if (!Files.exists(root)) Files.createDirectories(root);
        }

        @Override
        public void write(String path, byte[] data) throws IOException {
            // Writes data to files through the VFSi

            Path p = root.resolve(path.replaceFirst("^/", ""));
            Files.createDirectories(p.getParent());
            Files.write(p, data);
        }

        @Override
        public byte[] read(String path) throws IOException {
            // Reads data to files through the VFS

            Path p = root.resolve(path.replaceFirst("^/", ""));
            if (!Files.exists(p)) return null;
            return Files.readAllBytes(p);
        }
    }


    public interface Device {
        Object call(String method, Object... args);
    }
    public void registerDevice(String id, Device device) { devices.put(id, device); }
    public Device getDevice(String id) { return devices.get(id); }



    private final ExecutorService exec = Executors.newCachedThreadPool(); // Scheduler which executes the processes programs
    private final Map<Integer, Process> processes = new ConcurrentHashMap<>(); // Process map containing the process and their id
    private final AtomicInteger nextPid = new AtomicInteger(1); // Process counter used to assign process their ids
    private static final ThreadLocal<Process> CURRENT = new ThreadLocal<>(); // Creates a "scope" which keeps all variables local to that thread

    private final VirtualFileSystemInterface vfs; // Virtual File System

    private final Map<String, Device> devices = new ConcurrentHashMap<>();


    public KernelEngine(VirtualFileSystemInterface vfs) {
        this.vfs = vfs;
    }


    public int spawn(Runnable program) {
        int pid = nextPid.getAndIncrement();
        Process p = new Process(pid, program, this);
        processes.put(pid, p);

        exec.submit(() -> {
            CURRENT.set(p);
            try {
                program.run(); // runs indefinitely if needed
            } finally {
                CURRENT.remove();
                p.alive = false;
            }
        });

        return pid;
    }


    public void kill(int pid) {
        // Stops a process from running and removes it from the process map
        Process p = processes.remove(pid);
        if (p != null) p.alive = false;
    }

    public void shutdown() {
        // Stops the whole process pool killing all running processes.
        exec.shutdownNow();
    }

    public static void yield(long ms) {
        // Pauses a processes executing to allow for the next
        throw new Yield(ms);
    }

    public static Process current() { return CURRENT.get(); }








}
