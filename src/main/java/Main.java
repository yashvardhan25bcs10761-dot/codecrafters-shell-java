import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        List<String> b = List.of("echo", "exit", "type", "pwd", "cd");

        String pEnv = System.getenv("PATH");
        String[] paths = pEnv != null ? pEnv.split(File.pathSeparator) : new String[0];

        String cur = System.getProperty("user.dir");

        while (true) {
            System.out.print("$ ");
            String in = sc.nextLine();

            if (in.equals("exit")) {
                break;
            }

            else if (in.startsWith("echo ")) {
                System.out.println(in.substring(5));
            }

            else if (in.equals("pwd")) {
                System.out.println(cur);
            }

            else if (in.startsWith("cd ")) {
                String dir = in.substring(3);
                File f;

                if (dir.equals("~")) {
                    f = new File(System.getenv("HOME"));
                } else if (new File(dir).isAbsolute()) {
                    f = new File(dir);
                } else {
                    f = new File(cur, dir);
                }

                if (f.exists() && f.isDirectory()) {
                    cur = f.getCanonicalPath();
                } else {
                    System.out.println("cd: " + dir + ": No such file or directory");
                }
            }

            else if (in.startsWith("type ")) {
                String cmd = in.substring(5);

                if (b.contains(cmd)) {
                    System.out.println(cmd + " is a shell builtin");
                } else {
                    String fp = null;

                    for (String p : paths) {
                        File f = new File(p, cmd);

                        if (f.exists() && f.canExecute()) {
                            fp = f.getAbsolutePath();
                            break;
                        }
                    }

                    if (fp != null) {
                        System.out.println(cmd + " is " + fp);
                    } else {
                        System.out.println(cmd + ": not found");
                    }
                }
            }

            else {
                String[] parts = in.split(" ");
                String cmd = parts[0];

                String exe = null;

                for (String p : paths) {
                    File f = new File(p, cmd);

                    if (f.exists() && f.canExecute()) {
                        exe = f.getAbsolutePath();
                        break;
                    }
                }

                if (exe != null) {
                    Process pr = new ProcessBuilder(parts)
                            .directory(new File(cur))
                            .redirectErrorStream(true)
                            .start();

                    pr.getInputStream().transferTo(System.out);
                    pr.waitFor();
                } else {
                    System.out.println(cmd + ": command not found");
                }
            }
        }
    }
}