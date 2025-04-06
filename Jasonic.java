import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jasonic{
    public static void main(String[] args) {
        Jasonic jasonic = new Jasonic();
        jasonic.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine().trim();;
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Exiting...");
                break;
            }
            processCommand(input);
        }
        scanner.close();
    }

    public void processCommand(String input) {

        String commandLine = "int sum         (int a, int b) { return a + b; }";

        Pattern pattern = Pattern.compile("^(\\w+)\\s+(\\w+)(\\s*\\(.*\\))?\\s*\\{(.*)\\}$");
        Matcher matcher = pattern.matcher(commandLine);
        if (matcher.find()) {
            String command = matcher.group(1);
            String type = matcher.group(2);
            String parameters = matcher.group(3);
            String body = matcher.group(4);

            switch(command) {
                case "create":
                    System.out.println("Command is an integer type.");
                    break;
                case "update":
                    System.out.println("Command is a void type.");
                    break;
                case "create":
                    System.out.println("Command is a string type.");
                    break;
                case "create":
                    System.out.println("Command is a boolean type.");
                    break;
                default:
                    System.out.println("Unknown command type.");
                    break;
            }

            // System.out.println("command: " + command);
            // System.out.println("type: " + type);
            // System.out.println("Parameters: " + parameters);
            // System.out.println("Body: " + body);
        } else {
            System.out.println("No match found.");
        }

        // System.out.println("Processing command: " + command);
    }
}