package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Ingredient;
import com.EatEaseFrontend.JsonParser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * View para gerenciar e exibir ingredientes
 */
public class IngredientsView {

    private final StackPane contentArea;
    private final HttpClient httpClient;

    /**
     * Construtor da view de ingredientes
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public IngredientsView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
    }

    /**
     * Carrega e exibe a lista de ingredientes
     */
    public void show() {
        System.out.println("Carregando lista de ingredientes...");

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando ingredientes...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get ingredients
        HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        System.out.println("Ingredientes -> " + resp.body());
                        List<Ingredient> ingredients = JsonParser.parseIngredients(resp.body());

                        Platform.runLater(() -> {
                            displayIngredientsAsCards(ingredients);
                        });
                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar ingredientes");
                            alert.setContentText("Status code: " + resp.statusCode());
                            alert.showAndWait();
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao carregar ingredientes");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Exibe os ingredientes como cards em um FlowPane
     * 
     * @param ingredients Lista de ingredientes a serem exibidos
     */
    private void displayIngredientsAsCards(List<Ingredient> ingredients) {
        contentArea.getChildren().clear();

        // Create scroll pane for ingredient cards
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(20));

        // Use FlowPane for responsive cards layout
        FlowPane ingredientCards = new FlowPane();
        ingredientCards.setHgap(20);
        ingredientCards.setVgap(20);
        ingredientCards.setPadding(new Insets(20));

        // Add section header
        VBox contentBox = new VBox(20);
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label headerLabel = new Label("Ingredientes");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerBox.getChildren().add(headerLabel);

        contentBox.getChildren().add(headerBox);

        // Add ingredient cards
        if (ingredients.isEmpty()) {
            Label noIngredientsLabel = new Label("Nenhum ingrediente encontrado");
            noIngredientsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noIngredientsLabel);
        } else {
            for (Ingredient ingredient : ingredients) {
                VBox card = createIngredientCard(ingredient);
                ingredientCards.getChildren().add(card);
            }
            contentBox.getChildren().add(ingredientCards);
        }

        scrollPane.setContent(contentBox);
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * Cria um card para um ingrediente
     * 
     * @param ingredient Ingrediente para o qual criar o card
     * @return VBox contendo o card do ingrediente
     */
    private VBox createIngredientCard(Ingredient ingredient) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(300);
        card.setPrefHeight(180);
        card.setPadding(new Insets(15));

        // Ingredient name as card title
        Label nameLabel = new Label(ingredient.getNome());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.getStyleClass().add("card-title");

        // Ingredient information
        Label idLabel = new Label("ID: " + ingredient.getId());

        // Stock information with visual indicator
        HBox stockBox = new HBox(10);
        stockBox.setAlignment(Pos.CENTER_LEFT);

        Label stockLabel = new Label("Estoque: " + ingredient.getStock());
        Label stockMinLabel = new Label("Estoque Mínimo: " + ingredient.getStock_min());

        // Add visual indicator for stock level
        Region stockIndicator = new Region();
        stockIndicator.setPrefWidth(15);
        stockIndicator.setPrefHeight(15);

        // Set color based on stock level
        Color indicatorColor;
        if (ingredient.getStock() <= ingredient.getStock_min() * 0.5) {
            // Critical - Red
            indicatorColor = Color.RED;
        } else if (ingredient.getStock() <= ingredient.getStock_min()) {
            // Low - Orange
            indicatorColor = Color.ORANGE;
        } else {
            // Good - Green
            indicatorColor = Color.GREEN;
        }

        stockIndicator.setBackground(new Background(new BackgroundFill(
                indicatorColor, new CornerRadii(7.5), Insets.EMPTY)));

        stockBox.getChildren().addAll(stockIndicator, stockLabel);

        // Unit information
        Label unitLabel = new Label("Unidade ID: " + ingredient.getUnidade_id());

        // Add all elements to card
        card.getChildren().addAll(nameLabel, idLabel, stockBox, stockMinLabel, unitLabel);

        return card;
    }
}