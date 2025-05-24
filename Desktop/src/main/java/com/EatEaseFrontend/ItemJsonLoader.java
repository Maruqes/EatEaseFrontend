package com.EatEaseFrontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Utilitário estático: converte o JSON completo para a lista de Item.
 */
public final class ItemJsonLoader {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    private ItemJsonLoader() {
        /* Impede instanciar */ }

    public static List<Item> parseItems(String json) throws Exception {
        System.out.println("[DEBUG] JSON recebido para parsing: " + json);
        List<Item> items = mapper.readValue(json, new TypeReference<List<Item>>() {
        });

        // Debug de cada item parseado
        for (Item item : items) {
            System.out.println("[DEBUG] Item parseado: " + item.getNome() +
                    " - eComposto: " + item.isEComposto());
        }

        return items;
    }
}
