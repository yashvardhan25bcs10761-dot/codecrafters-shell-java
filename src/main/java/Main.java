package main;

import java.util.Scanner;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = List.of("echo", "exit", "type");
        
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            
            if (input.equals("exit")) break;
            
            if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            } else if (input.startsWith("type ")) {
                String cmd = input.substring(5);
                System.out.println(builtins.contains(cmd) ? cmd + " is a shell builtin" : cmd + ": not found");
            } else {
                System.out.println(input + ": command not found");
            }
        }
    }
}