package net.redegs.digitizerplus.computer.terminal.program;

import com.sun.jna.platform.FileUtils;
import net.redegs.digitizerplus.DigitizerPlus;
import net.redegs.digitizerplus.computer.ComputerManager;
import net.redegs.digitizerplus.computer.terminal.Terminal;
import net.redegs.digitizerplus.python.PythonRunner;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class DefaultProgram extends TerminalProgram {
    private Path currentDirectory;
    private Path rootDirectory;
    private boolean runningScript;

    public DefaultProgram(Terminal terminal) {
        super(terminal);

        runningScript = false;

    }

    @Override
    public void start() {
        if (terminal.getComputerID() != null) {
            currentDirectory = ComputerManager.PathFromUUID(terminal.getComputerID());
            rootDirectory = ComputerManager.PathFromUUID(terminal.getComputerID());
        }

        terminal.setAcceptingInput(false);
        terminal.println("RedOS v0.1", true);
        terminal.print("> ", true);
        terminal.setPromptLength(2);
        terminal.setAcceptingInput(true);
        terminal.syncWatchers();
    }

    @Override
    public void onNewline(String line) {
        if (!line.isEmpty()) {
            String[] command = terminal.parseCommand(line);

            if (command.length == 1) {

                if (command[0].equals("clear") || command[0].equals("cls")) {
                    terminal.clearBuffer();

                } else if (command[0].equals("uuid")) {
                    print(terminal.getComputerID().toString(), 0xFF00FF);

                } else if (command[0].equals("ls")) {
                    ls();
                } else if (command[0].equals("cmds") || command[0].equals("?") || command[0].equals("help")) {
                    print("Available commands: ", 0x00acff);
                    for (String cmd: getCommandNames()) {
                        print("    " + cmd, 0x00ffff);
                    }
                    print("", 0xffffff);
                }

            } else if (command.length == 2) {

                if (command[0].equals("mkdir") || command[0].equals("mkd")) {
                    String dirName = command[1];
                    mkdir(dirName);

                } else if (command[0].equals("new")) {
                    String fileName = command[1];
                    newFile(fileName);

                } else if (command[0].equals("cd")) {
                    String dirName = command[1];
                    cd(dirName);

                } else if (command[0].equals("open") || command[0].equals("edit")) {
                    String fileName = command[1];
                    openFile(fileName);

                } else if (command[0].equals("rm") || command[0].equals("remove")) {
                    String fileName = command[1];
                    removeFile(fileName);

                } else if (command[0].equals("run") || command[0].equals("exec") || command[0].equals("python") || command[0].equals("py")) {
                    String fileName = command[1];
                    runScript(fileName);


                }



            } else {
                print("Unknown command/program", 0xFF0000);
            }





        }
    }

    @Override
    public void afterNewline(String line) {;
        terminal.beginInputLine("> ");
    }

    @DefaultProgramCommand(name = "ls")
    private void ls() {
        Path mainPath;
        try {
            mainPath = ComputerManager.PathFromUUID(terminal.getComputerID().toString());
            if (!Files.exists(mainPath)) {
                print("Computer path hasn't been generated or has been deleted", 0xFF0000);
                return;
            }

        } catch ( Exception e ) {
            DigitizerPlus.LOGGER.info(e.toString());
            print("The computer path hasn't been generated or has been deleted", 0xFF0000);
            return;
        }

        File currentDir = currentDirectory.toFile();
        File[] files = currentDir.listFiles();

        if (files.length > 0) {
            for (File file: files) {
                print(file.getName().toString(), 0x00ffff);
            }
        } else {
            print("No files exist yet.", 0x00ffff);
        }
    }

    @DefaultProgramCommand(name = "mkdir")
    private void mkdir(String dirName) {
        try {
            Path newPath = currentDirectory.resolve(dirName);
            newPath.toFile().mkdirs();

            print("Created directory '".concat(dirName).concat("'"), 0x00FF1A);

        } catch (Exception e) {
            print("Failed to create directory", 0xFF0000);
        }



    }

    @DefaultProgramCommand(name = "new")
    private void newFile(String fileName) {
        try {
            Path newPath = currentDirectory.resolve(fileName);
            if (newPath.toFile().createNewFile()) {
                print("Created file '".concat(fileName).concat("'"), 0x00FF1A);
            }
        } catch (Exception e) {
            print("Failed to create new file", 0xFF0000);
        }
    }

    @DefaultProgramCommand(name = "rm")
    private void removeFile(String fileName) {
        try {
            Path newPath = currentDirectory.resolve(fileName);
            FileUtils fileUtils = FileUtils.getInstance();

            if (fileUtils.hasTrash()) {
                try {
                    fileUtils.moveToTrash(newPath.toFile());
                    print("Trashed '".concat(fileName).concat("'"), 0xFF0000);
                } catch (Exception e) {
                    newPath.toFile().delete();
                    print("Deleted '".concat(fileName).concat("'"), 0xFF0000);
                }
            }


        } catch (Exception e) {
            print("Failed to trash or delete file.", 0xFF0000);
        }
    }

    @DefaultProgramCommand(name = "open")
    private void openFile(String fileName) {
        try {
            Path filePath = currentDirectory.resolve(fileName);

            if (Files.exists(filePath)) {
                terminal.stopProgram();
                terminal.startProgram(new TextEditorProgram(terminal, filePath, this));
            } else {
                print("Failed to open file", 0xFF0000);
                return;
            }



        } catch (Exception e) {
            print("Failed to open file", 0xFF0000);
        }



    }

    @DefaultProgramCommand(name = "cd")
    private void cd(String dirName) {
        if (dirName.matches("\\.+") || dirName.equals(".")) {
            DigitizerPlus.LOGGER.info("traversing backwards..");
            int moves = dirName.length();

            File filePath;
            for (filePath = new File(currentDirectory.toString()); moves > 0; filePath = filePath.getParentFile()) {
                if (filePath.toPath().equals(rootDirectory)) break;
                moves--;
            }
            if (filePath.toPath().equals(rootDirectory)) {
                currentDirectory = rootDirectory;
            } else {
                currentDirectory = filePath.toPath();
            }



        } else if (dirName.equals("/")) {
            currentDirectory = rootDirectory;

        } else {
            if (Files.exists(currentDirectory.resolve(dirName))) {
                currentDirectory = currentDirectory.resolve(dirName);

            } else {
                print("Folder '".concat(dirName).concat("' does not exist."), 0xFF0000);
            }

        }

    }

    @DefaultProgramCommand(name = "run")
    private void runScript(String fileName) {
        if (Files.exists(currentDirectory.resolve(fileName))) {
            File script = currentDirectory.resolve(fileName).toFile();
            String code;

            try { FileInputStream fis = new FileInputStream(script.getAbsolutePath());
                try { code = IOUtils.toString(fis); }
                finally { fis.close(); }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            if (!code.isEmpty()) {

                HashMap<Thread, PythonRunner> threadMap = new HashMap<>();
                PythonRunner runner = new PythonRunner(this.terminal.getComputerID(), this.terminal);
                runner.setCode(code);

                Thread thread = new Thread(runner);
                thread.start();


                threadMap.put(thread, runner);
                ComputerManager.stopThreads(this.terminal.getComputerID());
                ComputerManager.putThread(this.terminal.getComputerID(), threadMap);

                runningScript = true;
                terminal.setAcceptingInput(false);



            }

        }
    }




    public void print(String string, int color) {
        int prev = terminal.getPromptLength();

        terminal.setAcceptingInput(false);
        terminal.setCurrentColor(color);
        terminal.setPromptLength(0);
        terminal.println(string, true);
        terminal.setPromptLength(prev);
        terminal.resetCurrentColor();
        terminal.setAcceptingInput(true);
    }

    public List<String> getCommandNames() {
        List<String> commandNames = new ArrayList<>();

        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(DefaultProgramCommand.class)) {
                DefaultProgramCommand annotation = method.getAnnotation(DefaultProgramCommand.class);
                commandNames.add(annotation.name());
            }
        }

        return commandNames;
    }
}