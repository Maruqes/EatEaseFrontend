package com.EatEaseFrontend;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple utility class to parse JSON data into model objects.
 * In a production app, you'd use a library like Jackson or Gson instead.
 */
public class JsonParser {

    /**
     * Parse JSON array of employees into a List of Employee objects
     * 
     * @param json The JSON string from the API
     * @return List of Employee objects
     */
    public static List<Employee> parseEmployees(String json) {
        List<Employee> employees = new ArrayList<>();

        // Simple regex pattern to extract employee objects
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]*)\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String employeeJson = matcher.group(0);
            Employee employee = parseEmployee(employeeJson);
            employees.add(employee);
        }

        return employees;
    }

    /**
     * Parse a single employee JSON object
     * 
     * @param json The JSON string for one employee
     * @return Employee object
     */
    private static Employee parseEmployee(String json) {
        Employee employee = new Employee();

        // Extract id
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            employee.setId(Integer.parseInt(idMatcher.group(1)));
        }

        // Extract nome
        employee.setNome(extractString(json, "nome"));

        // Extract username
        employee.setUsername(extractString(json, "username"));

        // Extract password (would not normally include this)
        employee.setPassword(extractString(json, "password"));

        // Extract email
        employee.setEmail(extractString(json, "email"));

        // Extract telefone
        employee.setTelefone(extractString(json, "telefone"));

        // Extract cargoId
        Pattern cargoPattern = Pattern.compile("\"cargoId\"\\s*:\\s*(\\d+)");
        Matcher cargoMatcher = cargoPattern.matcher(json);
        if (cargoMatcher.find()) {
            employee.setCargoId(Integer.parseInt(cargoMatcher.group(1)));
        }

        return employee;
    }

    /**
     * Extract a string value from JSON for a given field
     * 
     * @param json      The JSON string
     * @param fieldName The field name to extract
     * @return The extracted string value
     */
    private static String extractString(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Parse JSON array of ingredients into a List of Ingredient objects
     * 
     * @param json The JSON string from the API
     * @return List of Ingredient objects
     */
    public static List<Ingredient> parseIngredients(String json) {
        List<Ingredient> ingredients = new ArrayList<>();

        // Simple regex pattern to extract ingredient objects
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]*)\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String ingredientJson = matcher.group(0);
            Ingredient ingredient = parseIngredient(ingredientJson);
            ingredients.add(ingredient);
        }

        return ingredients;
    }

    /**
     * Parse a single ingredient JSON object
     * 
     * @param json The JSON string for one ingredient
     * @return Ingredient object
     */
    private static Ingredient parseIngredient(String json) {
        Ingredient ingredient = new Ingredient();

        // Extract id
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            ingredient.setId(Integer.parseInt(idMatcher.group(1)));
        }

        // Extract nome
        ingredient.setNome(extractString(json, "nome"));

        // Extract stock
        Pattern stockPattern = Pattern.compile("\"stock\"\\s*:\\s*(\\d+)");
        Matcher stockMatcher = stockPattern.matcher(json);
        if (stockMatcher.find()) {
            ingredient.setStock(Integer.parseInt(stockMatcher.group(1)));
        }

        // Extract stock_min
        Pattern stockMinPattern = Pattern.compile("\"stock_min\"\\s*:\\s*(\\d+)");
        Matcher stockMinMatcher = stockMinPattern.matcher(json);
        if (stockMinMatcher.find()) {
            ingredient.setStock_min(Integer.parseInt(stockMinMatcher.group(1)));
        }

        // Extract unidade_id
        Pattern unidadePattern = Pattern.compile("\"unidade_id\"\\s*:\\s*(\\d+)");
        Matcher unidadeMatcher = unidadePattern.matcher(json);
        if (unidadeMatcher.find()) {
            ingredient.setUnidade_id(Integer.parseInt(unidadeMatcher.group(1)));
        }

        return ingredient;
    }
}