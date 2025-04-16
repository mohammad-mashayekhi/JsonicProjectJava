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

// Main class
// This is the main class that will handle the Jasonic commands
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
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Exiting...");
                break;
            }
            processCommand(input);
        }
        scanner.close();
    }

    public void processCommand(String input) {

        Pattern pattern = Pattern.compile("^(\\w+)\\s+(\\w+)(\\s*\\(.*\\))?(\\s*\\{(.*)\\})?$");
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.find()) {
            String command = matcher.group(1);
            String type = matcher.group(2);
            String parameters = matcher.group(3);
            String body = matcher.group(5);

            switch(command.toLowerCase()) {
                case "create":
                    if (body == null) {
                        System.out.println("Error: Missing JSON input for create.");
                        return;
                    }
                    createDatatype(type, body);
                    break;
                case "insert":
                    if (body == null) {
                        System.out.println("Error: Missing JSON input for insert.");
                        return;
                    }
                    insertData(type, body);
                    break;
                case "update":
                    if (body == null) {
                        System.out.println("Error: Missing JSON input for update.");
                        return;
                    }
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

    private void updateData(String typeName , String parameters , String json ){
        if (!dataTypes.containsKey(typeName)) {
            System.out.println("Error: Type " + typeName + " not found.");
            return;
        }
    
        DataType dataType = dataTypes.get(typeName);
        List<Record> records = dataType.getRecords();
    
        Pattern condPattern = Pattern.compile("\\((\\w+)\\s*(=|>|<)\\s*\"?([^\")]+)\"?\\)");
        Matcher condMatcher = condPattern.matcher(parameters.trim());
    
        if (!condMatcher.find()) {
            System.out.println("Invalid update condition.");
            return;
        }
    
        String fieldName = condMatcher.group(1);
        String operator = condMatcher.group(2);
        String rawValue = condMatcher.group(3);
    
        Field condField = null;
        for (Field f : dataType.getFields()) {
            if (f.name.equals(fieldName)) {
                condField = f;
                break;
            }
        }
    
        if (condField == null) {
            System.out.println("Condition field not found.");
            return;
        }
    
        Object searchValue;
        try {
            searchValue = parseValue(condField.type, rawValue);
        } catch (Exception e) {
            System.out.println("Invalid value type in condition.");
            return;
        }
    
        Map<String, String> updateValues = new HashMap<>();
        json = json.trim();
        json = json.substring(1, json.length() - 1);
        String[] keyValuePairs = json.split(",");
        for (String pair : keyValuePairs) {
            String[] parts = pair.trim().split(":");
            if (parts.length != 2) continue;
            String key = parts[0].trim().replaceAll("\"", "");
            String value = parts[1].trim().replaceAll("\"", "");
            updateValues.put(key, value);
        }
    
        int updateCount = 0;
        for (Record record : records) {
            Object val = record.getValue(fieldName);
            if (val == null) continue;
    
            boolean match = switch (operator) {
                case "=" -> val.equals(searchValue);
                case ">" -> val instanceof Comparable && ((Comparable) val).compareTo(searchValue) > 0;
                case "<" -> val instanceof Comparable && ((Comparable) val).compareTo(searchValue) < 0;
                default -> false;
            };
    
            if (match) {
                for (String updateKey : updateValues.keySet()) {
                    Field updateField = null;
                    for (Field f : dataType.getFields()) {
                        if (f.name.equals(updateKey)) {
                            updateField = f;
                            break;
                        }
                    }
    
                    if (updateField != null) {
                        try {
                            Object newValue = parseValue(updateField.type, updateValues.get(updateKey));
                            if (updateField.unique) {
                                boolean duplicate = false;
                                for (Record other : records) {
                                    if (other != record && newValue.equals(other.getValue(updateKey))) {
                                        duplicate = true;
                                        break;
                                    }
                                }
                                if (duplicate) {
                                    System.out.println("Duplicate value for unique field: " + updateKey);
                                    return;
                                }
                            }
                            record.setValue(updateKey, newValue);
                        } catch (Exception e) {
                            System.out.println("Invalid value for field: " + updateKey);
                        }
                    }
                }
                updateCount++;
            }
        }
    
        System.out.println(updateCount + " record(s) updated in " + typeName + ".");
    }
    

    private void searchData(String typeName, String parameters) {
        if (!dataTypes.containsKey(typeName)) {
            System.out.println("Error: Type " + typeName + " not found.");
            return;
        }
    
        DataType dataType = dataTypes.get(typeName);
        List<Record> records = dataType.getRecords();
        if (records.isEmpty()) {
            System.out.println("No data found in " + typeName);
            return;
        }
    
        List<Record> filtered = new ArrayList<>();
        
        if (parameters == null || parameters.trim().isEmpty()) {
            filtered = records;
        } else {
            Pattern condPattern = Pattern.compile("\\((\\w+)\\s*(=|>|<)\\s*\"?([^\")]+)\"?\\)");
            Matcher condMatcher = condPattern.matcher(parameters.trim());
    
            if (!condMatcher.find()) {
                System.out.println("Invalid search filter.");
                return;
            }
    
            String fieldName = condMatcher.group(1);
            String operator = condMatcher.group(2);
            String rawValue = condMatcher.group(3);
    
            Field field = null;
            for (Field f : dataType.getFields()) {
                if (f.name.equals(fieldName)) {
                    field = f;
                    break;
                }
            }
    
            if (field == null) {
                System.out.println("Field '" + fieldName + "' not found in type.");
                return;
            }
    
            Object searchValue;
            try {
                searchValue = parseValue(field.type, rawValue);
            } catch (Exception e) {
                System.out.println("Invalid value type for field.");
                return;
            }
    
            for (Record r : records) {
                Object val = r.getValue(fieldName);
                if (val == null) continue;
    
                boolean match = switch (operator) {
                    case "=" -> val.equals(searchValue);
                    case ">" -> val instanceof Comparable && ((Comparable) val).compareTo(searchValue) > 0;
                    case "<" -> val instanceof Comparable && ((Comparable) val).compareTo(searchValue) < 0;
                    default -> false;
                };
    
                if (match) filtered.add(r);
            }
        }
    
        if (filtered.isEmpty()) {
            System.out.println("No matching data found.");
        } else {
            printTable(dataType.getFields(), filtered);
        }
    }
    
    private void deleteData(String typeName , String parameters ){
        if (!dataTypes.containsKey(typeName)) {
            System.out.println("Error: Type " + typeName + " not found.");
            return;
        }
    
        DataType dataType = dataTypes.get(typeName);
        if (parameters == null || parameters.trim().isEmpty()) {
            dataType.getRecords().clear();
            System.out.println("All records of type " + typeName + " deleted.");
            return;
        }
    
        Pattern condPattern = Pattern.compile("\\((\\w+)\\s*(=|>|<)\\s*\"?([^\")]+)\"?\\)");
        Matcher condMatcher = condPattern.matcher(parameters.trim());
    
        if (!condMatcher.find()) {
            System.out.println("Invalid delete condition.");
            return;
        }
    
        String fieldName = condMatcher.group(1);
        String operator = condMatcher.group(2);
        String rawValue = condMatcher.group(3);
    
        Field field = null;
        for (Field f : dataType.getFields()) {
            if (f.name.equals(fieldName)) {
                field = f;
                break;
            }
        }
    
        if (field == null) {
            System.out.println("Field '" + fieldName + "' not found in type.");
            return;
        }
    
        Object searchValue;
        try {
            searchValue = parseValue(field.type, rawValue);
        } catch (Exception e) {
            System.out.println("Invalid value type for field.");
            return;
        }
    
        List<Record> toRemove = new ArrayList<>();
        for (Record r : dataType.getRecords()) {
            Object val = r.getValue(fieldName);
            if (val == null) continue;
    
            boolean match = switch (operator) {
                case "=" -> val.equals(searchValue);
                case ">" -> val instanceof Comparable && ((Comparable) val).compareTo(searchValue) > 0;
                case "<" -> val instanceof Comparable && ((Comparable) val).compareTo(searchValue) < 0;
                default -> false;
            };
    
            if (match) toRemove.add(r);
        }
    
        dataType.getRecords().removeAll(toRemove);
        System.out.println("Deleted " + toRemove.size() + " matching record(s) from " + typeName + ".");
    }
    
   
    // Helper Methods
    public void println(){
        System.out.println("jasonic");
    }

    private void printTable(List<Field> fields, List<Record> records) {
        if (records == null || records.isEmpty()) {
            System.out.println("No data to display.");
            return;
        }
    
        System.out.print("|");
        for (Field field : fields) {
            System.out.print(" " + field.name + " |");
        }
        System.out.println();
    
        System.out.print("|");
        for (int i = 0; i < fields.size(); i++) {
            System.out.print("----|");
        }
        System.out.println();
    
        for (Record record : records) {
            System.out.print("|");
            for (Field field : fields) {
                Object val = record.getValue(field.name);
                System.out.print(" " + (val != null ? val.toString() : "") + " |");
            }
            System.out.println();
        }
    }
    
    public String toString(){
        return "jasonic";
    }

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