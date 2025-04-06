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
            System.out.print("Enter command please: ");
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
                    System.out.println("Command is an create type.");
                    break;
                case "insert":
                    System.out.println("Command is a insert type.");
                    break;
                case "update":
                    System.out.println("Command is a update type.");
                    break;
                case "search":
                    System.out.println("Command is a search type.");
                    break;
                case "delete":
                    System.out.println("Command is a delete type.");
                    break;
                default:
                    System.out.println("Unknown command type.");
                    break;
            }
        } else {
            System.out.println("Template Command Incorrect!");
        }
    }


    public void println(){
        System.out.println("jasonic");
    }

    public String toString(){
        return "jasonic";
    }
}