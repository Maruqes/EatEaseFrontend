package com.EatEaseFrontend;

/**
 * Enum representing different employee roles in the system
 */
public enum Cargo {
    FUNCIONARIO(1, "Funcionário"),
    GERENTE(2, "Gerente"),
    COZINHEIRO(3, "Cozinheiro"),
    LIMPEZA(4, "Limpeza");

    private final int id;
    private final String displayName;

    Cargo(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get Cargo by ID
     * 
     * @param id The cargo ID
     * @return The corresponding Cargo enum, or null if not found
     */
    public static Cargo getById(int id) {
        for (Cargo cargo : values()) {
            if (cargo.getId() == id) {
                return cargo;
            }
        }
        return null;
    }

    /**
     * Get all cargo options for UI selection
     * 
     * @return Array of all Cargo enums
     */
    public static Cargo[] getAllCargos() {
        return values();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
