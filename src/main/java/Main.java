import java.util.*;
import java.io.*;

public class Main {

    static class Job {
        int id;
        Process p;
        String cmd;

        Job(int id, Process p, String cmd) {
            this.id = id;
            this.p = p;
            this.cmd = cmd;
        }
    }

    static void reapJobs(List<Job> jobs) {
        int maxId = -1;
        int secondMaxId = -1;

        for (Job j : jobs) {
            if (j.id > maxId) {
                secondMaxId = maxId;
                maxId = j.id;
            } else if (j.id > secondMaxId) {
                secondMaxId = j.id;
            }
        }

        Iterator<Job> it = jobs.iterator();

        while (it.hasNext()) {
            Job j = it.next();

            if (!j.p.isAlive()) {
                char mark = ' ';

                if (j.id == maxId) {
                    mark = '+';
                } else if (j.id == secondMaxId) {
                    mark = '-';
                }

                System.out.printf("[%d]%c  %-24s %s%n",
                        j.id,
                        mark,
                        "Done",
                        j.cmd);

                it.remove();
            }
        }
    }

    static int getNextJobId(List<Job> jobs) {
        int maxId = 0;

        for (Job j : jobs) {
            maxId = Math.max(maxId, j.id);
        }

        return maxId + 1;
    }

    static List<String> parse(String s) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean sq = false;
        boolean dq = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (dq && c == '\\') {
                if (i + 1 < s.length()) {
                    char n = s.charAt(i + 1);

                    if (n == '"' || n == '\\') {
                        cur.append(n);
                        i++;
                    } else {
                        cur.append('\\');
                    }
                } else {
                    cur.append('\\');
                }
            }

            else if (!sq && !dq && c == '\\') {
                if (i + 1 < s.length()) {
                    cur.append(s.charAt(++i));
                }
            }

            else if (c == '\'' && !dq) {
                sq = !sq;
            }

            else if (c == '"' && !sq) {
                dq = !dq;
            }

            else if (Character.isWhitespace(c) && !sq && !dq) {
                if (cur.length() > 0) {
                    res.add(cur.toString());
                    cur.setLength(0);
                }
            }

            else {
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

        List<String> b = List.of("echo", "exit", "type", "pwd", "cd", "jobs");
        
        List<Job> jobs = new ArrayList<>();
        
        String pEnv = System.getenv("PATH");
        String[] paths = pEnv != null ? pEnv.split(File.pathSeparator) : new String[0];

        String cur = System.getProperty("user.dir");

        while (true) {
            reapJobs(jobs);
            
            System.out.print("$ ");

            String in = sc.nextLine();

            if (in.contains("|")) {
                String[] cmds = in.split("\\|", 2);

                List<String> left = parse(cmds[0].trim());
                List<String> right = parse(cmds[1].trim());

                Process p1 = new ProcessBuilder(left)
                        .directory(new File(cur))
                        .start();

                Process p2 = new ProcessBuilder(right)
                        .directory(new File(cur))
                        .start();

                Thread pipeThread = new Thread(() -> {
                    try (
                            InputStream is = p1.getInputStream();
                            OutputStream os = p2.getOutputStream()
                    ) {
                        is.transferTo(os);
                        os.flush();
                        os.close();
                    } catch (IOException ignored) {
                    }
                });

                pipeThread.start();

                Thread outThread = new Thread(() -> {
                    try {
                        p2.getInputStream().transferTo(System.out);
                    } catch (IOException ignored) {
                    }
                });

                Thread errThread = new Thread(() -> {
                    try {
                        p2.getErrorStream().transferTo(System.err);
                    } catch (IOException ignored) {
                    }
                });

                outThread.start();
                errThread.start();

                p1.waitFor();
                pipeThread.join();

                p2.waitFor();
                outThread.join();
                errThread.join();

                continue;
            }

            List<String> parts = parse(in);

            if (parts.isEmpty()) {
                continue;
            }

            String outFile = null;
            String errFile = null;
            boolean appendOut = false;
            boolean appendErr = false;

        for (int i = 0; i < parts.size(); i++) {
            String t = parts.get(i);

            if (t.equals(">") || t.equals("1>")) {
                outFile = parts.get(i + 1);
                appendOut = false;
                parts = new ArrayList<>(parts.subList(0, i));
                break;
            }

            if (t.equals(">>") || t.equals("1>>")) {
                outFile = parts.get(i + 1);
                appendOut = true;
                parts = new ArrayList<>(parts.subList(0, i));
                break;
            }

            if (t.equals("2>")) {
                errFile = parts.get(i + 1);
                appendErr = false;
                parts = new ArrayList<>(parts.subList(0, i));
                break;
            }

            if (t.equals("2>>")) {
                errFile = parts.get(i + 1);
                appendErr = true;
                parts = new ArrayList<>(parts.subList(0, i));
                break;
            }
        }

            if (parts.isEmpty()) {
                continue;
            }

            String cmd = parts.get(0);

            boolean bg = false;

            if (parts.get(parts.size() - 1).equals("&")) {
                bg = true;
                parts.remove(parts.size() - 1);
            }

            if (cmd.equals("exit")) {
                break;
            }

            else if (cmd.equals("echo")) {
                String out = "";

                if (parts.size() > 1) {
                    out = String.join(" ", parts.subList(1, parts.size()));
                }

                if (outFile != null) {
                    try (PrintWriter pw = new PrintWriter(new FileWriter(outFile, appendOut))) {
                        pw.println(out);
                    }
                } else {
                    System.out.println(out);
                }

                if (errFile != null) {
                    new PrintWriter(new FileWriter(errFile, appendErr)).close();
                }
            }

            else if (cmd.equals("pwd")) {
                if (outFile != null) {
                    try (PrintWriter pw = new PrintWriter(new FileWriter(outFile, appendOut))) {
                        pw.println(cur);
                    }
                } else {
                    System.out.println(cur);
                }

                if (errFile != null) {
                    new PrintWriter(new FileWriter(errFile, appendErr)).close();                }
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
                    String msg = "cd: " + dir + ": No such file or directory";

                    if (errFile != null) {
                        try (PrintWriter pw = new PrintWriter(new FileWriter(errFile, appendErr))) {
                            pw.println(msg);
                        }
                    } else {
                        System.out.println(msg);
                    }
                }
            }

            else if (cmd.equals("jobs")) {

                int maxId = -1;
                int secondMaxId = -1;

                for (Job j : jobs) {
                    if (j.id > maxId) {
                        secondMaxId = maxId;
                        maxId = j.id;
                    } else if (j.id > secondMaxId) {
                        secondMaxId = j.id;
                    }
                }

                List<Job> doneJobs = new ArrayList<>();

                for (Job j : jobs) {
                    char mark = ' ';

                    if (j.id == maxId) {
                        mark = '+';
                    } else if (j.id == secondMaxId) {
                        mark = '-';
                    }

                    if (j.p.isAlive()) {
                        System.out.printf("[%d]%c  %-24s %s &%n",
                                j.id,
                                mark,
                                "Running",
                                j.cmd);
                    } else {
                        System.out.printf("[%d]%c  %-24s %s%n",
                                j.id,
                                mark,
                                "Done",
                                j.cmd);

                        doneJobs.add(j);
                    }
                }

                jobs.removeAll(doneJobs);
            }

            else if (cmd.equals("type")) {
                if (parts.size() < 2) {
                    continue;
                }

                String t = parts.get(1);

                String ans;

                if (b.contains(t)) {
                    ans = t + " is a shell builtin";
                } else {
                    String fp = null;

                    for (String p : paths) {
                        File f = new File(p, t);

                        if (f.exists() && f.canExecute()) {
                            fp = f.getAbsolutePath();
                            break;
                        }
                    }

                    ans = (fp != null) ? t + " is " + fp : t + ": not found";
                }

                if (outFile != null) {
                    try (PrintWriter pw = new PrintWriter(new FileWriter(outFile, appendOut))) {
                        pw.println(ans);
                    }
                } else {
                    System.out.println(ans);
                }

                if (errFile != null) {
                    new PrintWriter(new FileWriter(errFile, appendErr)).close();
                }
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
                    ProcessBuilder pb = new ProcessBuilder(parts)
                            .directory(new File(cur));

                        if (outFile != null) {
                            if (appendOut) {
                                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(outFile)));
                            } else {
                                pb.redirectOutput(new File(outFile));
                            }
                        }

                    if (errFile != null) {
                        if (appendErr) {
                            pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(errFile)));
                        } else {
                            pb.redirectError(new File(errFile));
                        }
                    }

                    if (bg) {
                        if (outFile == null) {
                            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                        }

                        if (errFile == null) {
                            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        }

                    Process pr = pb.start();

                    int jobId = getNextJobId(jobs);

                    jobs.add(
                        new Job(
                            jobId,
                            pr,
                            String.join(" ", parts)
                        )
                    );

                    System.out.println("[" + jobId + "] " + pr.pid());
                    } else {
                        Process pr = pb.start();

                        if (outFile == null) {
                            pr.getInputStream().transferTo(System.out);
                        }

                        if (errFile == null) {
                            pr.getErrorStream().transferTo(System.err);
                        }

                        pr.waitFor();
                    }
                } else {
                    System.out.println(cmd + ": command not found");
                }
            }
        }

        sc.close();
    }
}