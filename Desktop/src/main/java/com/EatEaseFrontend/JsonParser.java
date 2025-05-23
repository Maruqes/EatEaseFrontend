package com.EatEaseFrontend;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.EatEaseFrontend.Menu;
import com.EatEaseFrontend.TipoMenu;

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
     * Parse JSON array of menus into a List of Menu objects
     *
     * @param json The JSON string from the API
     * @return List of Menu objects
     */
    public static List<Menu> parseMenus(String json) {
        List<Menu> menus = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]*)\\}");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String menuJson = matcher.group(0);
            Menu menu = parseMenu(menuJson);
            menus.add(menu);
        }
        return menus;
    }

    /**
     * Parse JSON array of tipoMenu into a List of TipoMenu objects
     *
     * @param json The JSON string from the API
     * @return List of TipoMenu objects
     */
    public static List<TipoMenu> parseTipoMenus(String json) {
        List<TipoMenu> tipos = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]*)\\}");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String tipoJson = matcher.group(0);
            TipoMenu tipo = parseTipoMenu(tipoJson);
            tipos.add(tipo);
        }
        return tipos;
    }

    private static TipoMenu parseTipoMenu(String json) {
        TipoMenu tipo = new TipoMenu();
        // Extract id
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            tipo.setId(Integer.parseInt(idMatcher.group(1)));
        }
        // Extract nome
        tipo.setNome(extractString(json, "nome"));
        return tipo;
    }

    private static Menu parseMenu(String json) {
        Menu menu = new Menu();
        // Extract id
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            menu.setId(Integer.parseInt(idMatcher.group(1)));
        }
        // Extract nome
        menu.setNome(extractString(json, "nome"));
        // Extract descricao
        menu.setDescricao(extractString(json, "descricao"));
        // Extract tipoMenuId
        Pattern tipoPattern = Pattern.compile("\"tipoMenu\"\\s*:\\s*(\\d+)");
        Matcher tipoMatcher = tipoPattern.matcher(json);
        if (tipoMatcher.find()) {
            menu.setTipoMenuId(Integer.parseInt(tipoMatcher.group(1)));
        }
        return menu;
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

    /**
     * Parse JSON array of menu item IDs
     * 
     * @param json The JSON string from the API containing menu item IDs
     * @return List of menu item IDs as integers
     */
    public static List<Integer> parseMenuItemIds(String json) {
        List<Integer> itemIds = new ArrayList<>();

        // Regular expression to find all item IDs in the JSON array
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            try {
                int itemId = Integer.parseInt(matcher.group());
                itemIds.add(itemId);
            } catch (NumberFormatException e) {
                System.err.println("Erro ao converter ID de item: " + e.getMessage());
            }
        }

        return itemIds;
    }

    /**
     * Parse JSON array of mesas into a List of Mesa objects
     *
     * @param json The JSON string from the API
     * @return List of Mesa objects
     */
    public static List<Mesa> parseMesas(String json) {
        List<Mesa> mesas = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]*)\\}");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String mesaJson = matcher.group(0);
            Mesa mesa = parseMesa(mesaJson);
            mesas.add(mesa);
        }
        return mesas;
    }

    /**
     * Parse a single mesa JSON object
     * 
     * @param json The JSON string for one mesa
     * @return Mesa object
     */
    private static Mesa parseMesa(String json) {
        Mesa mesa = new Mesa();

        // Extract id
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            mesa.setId(Integer.parseInt(idMatcher.group(1)));
        }

        // Extract numero
        Pattern numeroPattern = Pattern.compile("\"numero\"\\s*:\\s*(\\d+)");
        Matcher numeroMatcher = numeroPattern.matcher(json);
        if (numeroMatcher.find()) {
            mesa.setNumero(Integer.parseInt(numeroMatcher.group(1)));
        }

        // Extract estadoLivre
        Pattern estadoPattern = Pattern.compile("\"estadoLivre\"\\s*:\\s*(true|false)");
        Matcher estadoMatcher = estadoPattern.matcher(json);
        if (estadoMatcher.find()) {
            mesa.setEstadoLivre(Boolean.parseBoolean(estadoMatcher.group(1)));
        }

        return mesa;
    }

    /**
     * Parse JSON array of pedidos into a List of Pedido objects
     *
     * @param json The JSON string from the API
     * @return List of Pedido objects
     */
    public static List<Pedido> parsePedidos(String json) {
        List<Pedido> pedidos = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^\\{\\}]+)\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String pedidoJson = matcher.group(0);
            Pedido pedido = parsePedido(pedidoJson);
            pedidos.add(pedido);
        }

        return pedidos;
    }

    /**
     * Parse a single pedido JSON object
     * 
     * @param json The JSON string for one pedido
     * @return Pedido object
     */
    private static Pedido parsePedido(String json) {
        Pedido pedido = new Pedido();

        // Extract id
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            pedido.setId(Integer.parseInt(idMatcher.group(1)));
        }

        // Extract itensIds
        Pattern itensPattern = Pattern.compile("\"itensIds\"\\s*:\\s*\\[(.*?)\\]");
        Matcher itensMatcher = itensPattern.matcher(json);
        if (itensMatcher.find()) {
            String itensIdsString = itensMatcher.group(1);
            List<Integer> itensIds = new ArrayList<>();

            Pattern itemIdPattern = Pattern.compile("(\\d+)");
            Matcher itemIdMatcher = itemIdPattern.matcher(itensIdsString);
            while (itemIdMatcher.find()) {
                itensIds.add(Integer.parseInt(itemIdMatcher.group(1)));
            }

            pedido.setItensIds(itensIds);
        }

        // Extract estadoPedido_id
        Pattern estadoPattern = Pattern.compile("\"estadoPedido_id\"\\s*:\\s*(\\d+)");
        Matcher estadoMatcher = estadoPattern.matcher(json);
        if (estadoMatcher.find()) {
            pedido.setEstadoPedido_id(Integer.parseInt(estadoMatcher.group(1)));
        }

        // Extract mesa_id
        Pattern mesaPattern = Pattern.compile("\"mesa_id\"\\s*:\\s*(\\d+)");
        Matcher mesaMatcher = mesaPattern.matcher(json);
        if (mesaMatcher.find()) {
            pedido.setMesa_id(Integer.parseInt(mesaMatcher.group(1)));
        }

        // Extract funcionario_id
        Pattern funcPattern = Pattern.compile("\"funcionario_id\"\\s*:\\s*(\\d+)");
        Matcher funcMatcher = funcPattern.matcher(json);
        if (funcMatcher.find()) {
            pedido.setFuncionario_id(Integer.parseInt(funcMatcher.group(1)));
        }

        // Extract dataHora
        pedido.setDataHora(extractString(json, "dataHora"));

        // Extract observacao
        pedido.setObservacao(extractString(json, "observacao"));

        // Extract ingredientesRemover
        Pattern ingredRemoverPattern = Pattern.compile("\"ingredientesRemover\"\\s*:\\s*\\[(.*?)\\]");
        Matcher ingredRemoverMatcher = ingredRemoverPattern.matcher(json);
        if (ingredRemoverMatcher.find()) {
            String ingredRemoverString = ingredRemoverMatcher.group(1);
            List<Integer> ingredRemover = new ArrayList<>();

            if (!ingredRemoverString.isEmpty()) {
                Pattern ingredIdPattern = Pattern.compile("(\\d+)");
                Matcher ingredIdMatcher = ingredIdPattern.matcher(ingredRemoverString);
                while (ingredIdMatcher.find()) {
                    ingredRemover.add(Integer.parseInt(ingredIdMatcher.group(1)));
                }
            }

            pedido.setIngredientesRemover(ingredRemover);
        }

        return pedido;
    }
}