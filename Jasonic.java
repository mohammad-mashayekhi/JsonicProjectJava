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

        Pattern pattern = Pattern.compile("^(\\w+)\\s+(\\w+)(\\s*\\(.*\\))?\\s*\\{(.*)\\}$");
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.find()) {
            String command = matcher.group(1);
            String type = matcher.group(2);
            String parameters = matcher.group(3);
            String body = matcher.group(4);

            switch(command) {
                case "create":
                    createDatatype(type, body);
                    break;
                case "insert":
                    createDatatype(type, body);
                    break;
                case "update":
                    updateData(type, parameters, body);
                    break;
                case "search":
                    searchData(type, parameters);
                    break;
                case "delete":
                    deleteData(type, parameters);
                    break;
                default:
                    System.out.println("Unknown command type.");
                    break;
            }
        } else {
            System.out.println("Template Command Incorrect!");
        }
    }

    private void createDatatype(String typeName , String jason ){
        System.out.println("createDatatype type: ");
    }

    private void insertData(String typeName , String jason ){
        System.out.println("insertData type: ");
    }

    private void updateData(String typeName , String parameters , String jason ){
        System.out.println("updateData type: ");
    }

    private void searchData(String typeName , String parameters ){
        System.out.println("searchData type: ");
    }

    private void deleteData(String typeName , String parameters ){
        System.out.println("deleteData type: ");
    }

    public void println(){
        System.out.println("jasonic");
    }

    public String toString(){
        return "jasonic";
    }
}