import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = List.of("echo", "exit", "type");
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv != null ? pathEnv.split(File.pathSeparator) : new String[0];
        
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            
            if (input.equals("exit")) break;
            
            if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            } else if (input.startsWith("type ")) {
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
                    System.out.println(foundPath != null ? cmd + " is " + foundPath : cmd + ": not found");
                }
            } else {
                System.out.println(input + ": command not found");
            }
        }
    }
}