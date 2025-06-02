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

        System.out.println("Parsing menu item IDs from JSON: " + json);

        // Remove any brackets, quotes and whitespace to get a clean list of numbers
        if (json.startsWith("[") && json.endsWith("]")) {
            // Clean the string by removing brackets
            String cleaned = json.substring(1, json.length() - 1).trim();

            // Split by comma if there's content
            if (!cleaned.isEmpty()) {
                String[] idStrings = cleaned.split(",");
                for (String idStr : idStrings) {
                    try {
                        // Trim and parse each ID
                        int itemId = Integer.parseInt(idStr.trim());
                        itemIds.add(itemId);
                        System.out.println("Added item ID: " + itemId);
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao converter ID de item '" + idStr + "': " + e.getMessage());
                    }
                }
            }
        } else {
            // Fallback to regex approach if the JSON is not a simple array
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(json);

            while (matcher.find()) {
                try {
                    int itemId = Integer.parseInt(matcher.group());
                    itemIds.add(itemId);
                    System.out.println("Added item ID (regex): " + itemId);
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter ID de item: " + e.getMessage());
                }
            }
        }

        System.out.println("Total de " + itemIds.size() + " IDs de itens encontrados: " + itemIds);
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

        // Extract capacidade
        Pattern capacidadePattern = Pattern.compile("\"capacidade\"\\s*:\\s*(\\d+)");
        Matcher capacidadeMatcher = capacidadePattern.matcher(json);
        if (capacidadeMatcher.find()) {
            mesa.setCapacidade(Integer.parseInt(capacidadeMatcher.group(1)));
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

    /**
     * Parse JSON array of pedidos rapidos into a List of PedidoRapid objects
     * Uses Jackson ObjectMapper for proper JSON deserialization
     *
     * @param json The JSON string from the API
     * @return List of PedidoRapid objects
     */
    public static List<PedidoRapid> parsePedidosRapid(String json) {
        List<PedidoRapid> pedidos = new ArrayList<>();

        try {
            // Use ItemJsonLoader's ObjectMapper to parse the complete response
            pedidos = ItemJsonLoader.parsePedidosRapid(json);
        } catch (Exception e) {
            System.err.println("Erro ao parsear pedidos rápidos com Jackson: " + e.getMessage());
            e.printStackTrace();

            // Fallback to manual parsing if Jackson fails
            try {
                pedidos = parsePedidosRapidManual(json);
            } catch (Exception fallbackEx) {
                System.err.println("Erro também no fallback manual: " + fallbackEx.getMessage());
                fallbackEx.printStackTrace();
            }
        }

        return pedidos;
    }

    /**
     * Fallback manual parsing method
     */
    private static List<PedidoRapid> parsePedidosRapidManual(String json) {
        List<PedidoRapid> pedidos = new ArrayList<>();

        try {
            // Remove outer brackets and split by objects
            String cleanJson = json.trim();
            if (cleanJson.startsWith("[") && cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1);
            }

            // Find individual pedido objects using bracket counting
            List<String> pedidoJsons = extractJsonObjects(cleanJson);

            for (String pedidoJson : pedidoJsons) {
                PedidoRapid pedido = parsePedidoRapido(pedidoJson.trim());
                if (pedido != null) {
                    pedidos.add(pedido);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao parsear pedidos rápidos manualmente: " + e.getMessage());
            e.printStackTrace();
        }

        return pedidos;
    }

    /**
     * Extract individual JSON objects from a JSON array string
     */
    private static List<String> extractJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '{') {
                if (braceCount == 0) {
                    start = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && start != -1) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        return objects;
    }

    /**
     * Parse a single pedido rapido JSON object
     */
    private static PedidoRapid parsePedidoRapido(String json) {
        try {
            PedidoRapid pedido = new PedidoRapid();

            // Extract id
            Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
            Matcher idMatcher = idPattern.matcher(json);
            if (idMatcher.find()) {
                pedido.setId(Integer.parseInt(idMatcher.group(1)));
            }

            // Extract estadoPedido_id
            Pattern estadoPattern = Pattern.compile("\"estadoPedido_id\"\\s*:\\s*(\\d+)");
            Matcher estadoMatcher = estadoPattern.matcher(json);
            if (estadoMatcher.find()) {
                pedido.setEstadoPedido_id(Integer.parseInt(estadoMatcher.group(1)));
            }

            // Extract mesa_number
            Pattern mesaPattern = Pattern.compile("\"mesa_number\"\\s*:\\s*(\\d+)");
            Matcher mesaMatcher = mesaPattern.matcher(json);
            if (mesaMatcher.find()) {
                pedido.setMesa_number(Integer.parseInt(mesaMatcher.group(1)));
            }

            // Extract funcionario
            pedido.setFuncionario(extractString(json, "funcionario"));

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

                if (!ingredRemoverString.trim().isEmpty()) {
                    Pattern ingredIdPattern = Pattern.compile("(\\d+)");
                    Matcher ingredIdMatcher = ingredIdPattern.matcher(ingredRemoverString);
                    while (ingredIdMatcher.find()) {
                        ingredRemover.add(Integer.parseInt(ingredIdMatcher.group(1)));
                    }
                }

                pedido.setIngredientesRemover(ingredRemover);
            }

            // Extract itensIds (complex nested objects)
            Pattern itensPattern = Pattern
                    .compile("\"itensIds\"\\s*:\\s*\\[(.*?)\\](?=\\s*,\\s*\"estadoPedido_id\"|\\s*}\\s*$)");
            Matcher itensMatcher = itensPattern.matcher(json);
            if (itensMatcher.find()) {
                String itensString = itensMatcher.group(1);
                List<Item> itens = parseItensFromString(itensString);
                pedido.setItensIds(itens);
            }

            return pedido;
        } catch (Exception e) {
            System.err.println("Erro ao parsear pedido individual: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse items from the itensIds string
     */
    private static List<Item> parseItensFromString(String itensString) {
        List<Item> itens = new ArrayList<>();

        try {
            // Extract individual item objects
            List<String> itemJsons = extractJsonObjects(itensString);

            for (String itemJson : itemJsons) {
                Item item = parseItemFromJson(itemJson);
                if (item != null) {
                    itens.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao parsear itens: " + e.getMessage());
        }

        return itens;
    }

    /**
     * Parse a single item from JSON string
     */
    private static Item parseItemFromJson(String json) {
        try {
            Item item = new Item();

            // Extract id
            Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
            Matcher idMatcher = idPattern.matcher(json);
            if (idMatcher.find()) {
                item.setId(Integer.parseInt(idMatcher.group(1)));
            }

            // Extract nome
            item.setNome(extractString(json, "nome"));

            // Extract tipoPrato_id
            Pattern tipoPattern = Pattern.compile("\"tipoPrato_id\"\\s*:\\s*(\\d+)");
            Matcher tipoMatcher = tipoPattern.matcher(json);
            if (tipoMatcher.find()) {
                item.setTipoPratoId(Integer.parseInt(tipoMatcher.group(1)));
            }

            // Extract preco
            Pattern precoPattern = Pattern.compile("\"preco\"\\s*:\\s*([\\d.]+)");
            Matcher precoMatcher = precoPattern.matcher(json);
            if (precoMatcher.find()) {
                item.setPreco(Double.parseDouble(precoMatcher.group(1)));
            }

            // Extract ingredientesJson and parse it
            Pattern ingredientesPattern = Pattern.compile("\"ingredientesJson\"\\s*:\\s*\"(.*?)\"(?=\\s*,|\\s*})");
            Matcher ingredientesMatcher = ingredientesPattern.matcher(json);
            if (ingredientesMatcher.find()) {
                String ingredientesJson = ingredientesMatcher.group(1);
                // Unescape the JSON string properly
                ingredientesJson = ingredientesJson.replace("\\\"", "\"")
                        .replace("\\\\", "\\");
                // Use the Item's internal method to parse ingredients
                try {
                    java.lang.reflect.Method method = Item.class.getDeclaredMethod("unpackIngredientes", String.class);
                    method.setAccessible(true);
                    method.invoke(item, ingredientesJson);
                } catch (Exception e) {
                    System.err.println(
                            "Erro ao processar ingredientes para item " + item.getNome() + ": " + e.getMessage());
                    System.err.println("JSON original: " + ingredientesJson);
                }
            }

            // Extract eComposto
            Pattern compostoPattern = Pattern.compile("\"eComposto\"\\s*:\\s*(true|false)");
            Matcher compostoMatcher = compostoPattern.matcher(json);
            if (compostoMatcher.find()) {
                item.setEComposto(Boolean.parseBoolean(compostoMatcher.group(1)));
            }

            // Extract stockAtual
            Pattern stockPattern = Pattern.compile("\"stockAtual\"\\s*:\\s*(\\d+)");
            Matcher stockMatcher = stockPattern.matcher(json);
            if (stockMatcher.find()) {
                item.setStockAtual(Integer.parseInt(stockMatcher.group(1)));
            }

            // Extract foto
            String foto = extractString(json, "foto");
            if (!"null".equals(foto) && !foto.isEmpty()) {
                item.setFoto(foto);
            }

            return item;
        } catch (Exception e) {
            System.err.println("Erro ao parsear item individual: " + e.getMessage());
            return null;
        }
    }
}