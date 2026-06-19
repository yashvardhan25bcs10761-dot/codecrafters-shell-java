import java.util.Scanner;
import java.util.List;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        List<String> builtins = List.of("echo", "exit", "type", "pwd", "cd");

        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv != null ? pathEnv.split(File.pathSeparator) : new String[0];

        String currentDir = System.getProperty("user.dir");

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                break;
            }

            if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            }

            else if (input.equals("pwd")) {
                System.out.println(currentDir);
            }

            else if (input.startsWith("cd ")) {
                String dir = input.substring(3);

                File newDir = new File(dir);

                if (newDir.isDirectory()) {
                    currentDir = newDir.getAbsolutePath();
                } else {
                    System.out.println("cd: " + dir + ": No such file or directory");
                }
            }

            else if (input.startsWith("type ")) {
                String cmd = input.substring(5);

                if (builtins.contains(cmd)) {
                    System.out.println(cmd + " is a shell builtin");
                } else {
                    String foundPath = null;

                    for (String p : paths) {
                        File f = new File(p, cmd);

                        if (f.exists() && f.canExecute()) {
                            foundPath = f.getAbsolutePath();
                            break;
                        }
                    }

                    if (foundPath != null) {
                        System.out.println(cmd + " is " + foundPath);
                    } else {
                        System.out.println(cmd + ": not found");
                    }
                }
            }

            else {
                String[] parts = input.split(" ");
                String command = parts[0];

                String executablePath = null;

                for (String p : paths) {
                    File f = new File(p, command);

                    if (f.exists() && f.canExecute()) {
                        executablePath = f.getAbsolutePath();
                        break;
                    }
                }

                if (executablePath != null) {
                    Process process = new ProcessBuilder(parts)
                            .directory(new File(currentDir))
                            .redirectErrorStream(true)
                            .start();

                    process.getInputStream().transferTo(System.out);
                    process.waitFor();
                } else {
                    System.out.println(command + ": command not found");
                }
            }
        }

        scanner.close();
    }
}