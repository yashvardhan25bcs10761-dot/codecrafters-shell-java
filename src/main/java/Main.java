import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Set<String> builtins = Set.of("echo", "exit", "type");

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();

            if (input.startsWith("type ")) {
                String cmd = input.substring(5);

                if (builtins.contains(cmd)) {
                    System.out.println(cmd + " is a shell builtin");
                } else {
                    System.out.println(cmd + ": not found");
                }
            }
        }
    }
}