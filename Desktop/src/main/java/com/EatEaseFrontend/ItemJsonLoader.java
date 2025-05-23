package com.EatEaseFrontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Utilitário estático: converte o JSON completo para a lista de Item.
 */
public final class ItemJsonLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ItemJsonLoader() {
        /* Impede instanciar */ }

    public static List<Item> parseItems(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<List<Item>>() {
        });
    }
}
