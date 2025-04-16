import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

// fileds we usually have for datatype
class Field {
    String name;
    String type; 
    boolean required;
    boolean unique;

    public Field(String name, String type, boolean required, boolean unique) {
        this.name = name;
        this.type = type.toLowerCase();
        this.required = required;
        this.unique = unique;
    }
}

class Record {
    Map<String, Object> values;

    public Record() {
        this.values = new HashMap<>();
    }

    public void setValue(String fieldName, Object value) {
        values.put(fieldName, value);
    }

    public Object getValue(String fieldName) {
        return values.get(fieldName);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}

class DataType {
    String name;
    List<Field> fields;
    List<Record> records;

    public DataType(String name) {
        this.name = name;
        this.fields = new ArrayList<>();
        this.records = new ArrayList<>();
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public void addRecord(Record record) {
        records.add(record);
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<Record> getRecords() {
        return records;
    }
}

public class Jasonic{

    private Map<String, DataType> dataTypes = new HashMap<>();

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
                    insertData(type, body);
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

    private void createDatatype(String typeName , String json){
        if (dataTypes.containsKey(typeName)) {
            System.out.println("Error: Type " + typeName + " already exists.");
            return;
        }
    
        DataType dataType = new DataType(typeName);
        json = json.trim();
    
        String[] fieldDefs = json.split(",");
        for (String def : fieldDefs) {
            String[] parts = def.trim().split(":");
            if (parts.length < 2) {
                System.out.println("Invalid field definition: " + def);
                return;
            }
    
            String fieldName = parts[0].trim().replaceAll("\"", "");
            String[] typeAndFlags = parts[1].trim().replaceAll("\"", "").split("\\s+");
    
            String fieldType = typeAndFlags[0];
            boolean required = false;
            boolean unique = false;
    
            for (int i = 1; i < typeAndFlags.length; i++) {
                if (typeAndFlags[i].equalsIgnoreCase("required")) required = true;
                if (typeAndFlags[i].equalsIgnoreCase("unique")) unique = true;
            }
    
            // اعتبارسنجی نوع
            if (!fieldType.matches("string|int|dbl|bool")) {
                System.out.println("Unsupported type: " + fieldType);
                return;
            }
    
            Field field = new Field(fieldName, fieldType, required, unique);
            dataType.addField(field);
        }
    
        dataTypes.put(typeName, dataType);
        System.out.println("Fields in type " + typeName + "created:");
        for (Field f : dataType.getFields()) {
            System.out.println("- " + f.name + ": " + f.type + ", required=" + f.required + ", unique=" + f.unique);
        }
    }
    

    private void insertData(String typeName, String json) {
        if (!dataTypes.containsKey(typeName)) {
            System.out.println("Error: Type " + typeName + " does not exist.");
            return;
        }
    
        DataType dataType = dataTypes.get(typeName);
        Record record = new Record();
    
        json = json.trim();
        json = json.substring(1, json.length() - 1);
        String[] keyValuePairs = json.split(",");
    
        Map<String, String> inputValues = new HashMap<>();
    
        for (String pair : keyValuePairs) {
            String[] parts = pair.trim().split(":");
            if (parts.length != 2) continue;
            String key = parts[0].trim().replaceAll("\"", "");
            String value = parts[1].trim().replaceAll("\"", "");
            inputValues.put(key, value);
        }
    
        for (Field field : dataType.getFields()) {
            String value = inputValues.get(field.name);
    
            if (field.required && (value == null || value.isEmpty())) {
                System.out.println("Error: Field '" + field.name + "' is required.");
                return;
            }
    
            Object convertedValue = getDefaultValue(field.type);
            if (value != null) {
                try {
                    convertedValue = parseValue(field.type, value);
                } catch (Exception e) {
                    System.out.println("Invalid value for field '" + field.name + "'");
                    return;
                }
            }
    
            if (field.unique) {
                for (Record existing : dataType.getRecords()) {
                    Object existingValue = existing.getValue(field.name);
                    if (existingValue != null && existingValue.equals(convertedValue)) {
                        System.out.println("Error: Duplicate value for unique field '" + field.name + "'");
                        return;
                    }
                }
            }
    
            record.setValue(field.name, convertedValue);
        }
    
        dataType.addRecord(record);
        System.out.println("Data inserted into " + typeName + ": " + record);
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

    // Helper Methods
    private Object parseValue(String type, String value) {
        switch (type) {
            case "int":
                return Integer.parseInt(value);
            case "dbl":
                return Double.parseDouble(value);
            case "bool":
                return Boolean.parseBoolean(value);
            case "string":
            default:
                return value;
        }
    }
    
    private Object getDefaultValue(String type) {
        switch (type) {
            case "int":
                return 0;
            case "dbl":
                return 0.0;
            case "bool":
                return false;
            case "string":
            default:
                return "";
        }
    }
}