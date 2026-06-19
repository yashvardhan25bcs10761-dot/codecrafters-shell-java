import java.util.*;
import java.io.*;

public class Main {

    static List<String> parse(String s) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean sq = false;
        boolean dq = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\'' && !dq) {
                sq = !sq;
            } else if (c == '"' && !sq) {
                dq = !dq;
            } else if (Character.isWhitespace(c) && !sq && !dq) {
                if (cur.length() > 0) {
                    res.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }

        if (cur.length() > 0) {
            res.add(cur.toString());
        }

        return res;
    }

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

            List<String> parts = parse(in);

            if (parts.isEmpty()) {
                continue;
            }

            String cmd = parts.get(0);

            if (cmd.equals("echo")) {
                if (parts.size() > 1) {
                    System.out.println(String.join(" ", parts.subList(1, parts.size())));
                } else {
                    System.out.println();
                }
            }

            else if (cmd.equals("pwd")) {
                System.out.println(cur);
            }

            else if (cmd.equals("cd")) {
                if (parts.size() < 2) {
                    continue;
                }

                String dir = parts.get(1);
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

            else if (cmd.equals("type")) {
                if (parts.size() < 2) {
                    continue;
                }

                String t = parts.get(1);

                if (b.contains(t)) {
                    System.out.println(t + " is a shell builtin");
                } else {
                    String fp = null;

                    for (String p : paths) {
                        File f = new File(p, t);

                        if (f.exists() && f.canExecute()) {
                            fp = f.getAbsolutePath();
                            break;
                        }
                    }

                    if (fp != null) {
                        System.out.println(t + " is " + fp);
                    } else {
                        System.out.println(t + ": not found");
                    }
                }
            }

            else if (cmd.equals("exit")) {
                break;
            }

            else {
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

        sc.close();
    }
}