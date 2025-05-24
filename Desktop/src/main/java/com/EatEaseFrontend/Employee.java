package com.EatEaseFrontend;

/**
 * Model class for Employee data from the API.
 */
public class Employee {
    private int id;
    private String nome;
    private String username;
    private String password; // This would normally not be included in a real app
    private String email;
    private String telefone;
    private int cargoId;

    // Default constructor
    public Employee() {
    }

    // Constructor with fields
    public Employee(int id, String nome, String username, String password,
            String email, String telefone, int cargoId) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.password = password;
        this.email = email;
        this.telefone = telefone;
        this.cargoId = cargoId;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getCargoId() {
        return cargoId;
    }

    public void setCargoId(int cargoId) {
        this.cargoId = cargoId;
    }

    public String getCargoName() {
        Cargo cargo = Cargo.getById(cargoId);
        return cargo != null ? cargo.getDisplayName() : "Desconhecido";
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", cargoId=" + cargoId +
                '}';
    }
}