package com.EatEaseFrontend;

import com.EatEaseFrontend.SideBarViews.EmployeeView;
import com.EatEaseFrontend.SideBarViews.IngredientsView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main extends Application {

    // cookies + http partilhados
    private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    // guardo a Stage para trocar de cena depois do login
    private Stage primaryStage;
    // Main content area that will be updated based on navigation
    private StackPane contentArea;

    // Views
    private EmployeeView employeeView;
    private IngredientsView ingredientsView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setScene(LoginPage.createScene(stage, this::fazerLoginEListarFuncionarios));
        stage.setTitle("EatEase - Login");
        stage.show();
    }

    private void fazerLoginEListarFuncionarios(String username, String password) {
        String form = "username=" + username + "&password=" + password;

        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/login")))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        http.sendAsync(loginReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    cookieManager.getCookieStore().getCookies()
                            .forEach(System.out::println);
                    if (resp.statusCode() != 200)
                        throw new RuntimeException("Login falhou: " + resp.body());
                    return null;
                })
                .thenCompose(v -> {
                    HttpRequest getFuncReq = HttpRequest.newBuilder()
                            .uri(URI.create(AppConfig.getApiEndpoint("/auth/getAllFuncionarios")))
                            .GET()
                            .build();
                    return http.sendAsync(getFuncReq, HttpResponse.BodyHandlers.ofString());
                })
                .thenAccept(resp -> {
                    System.out.println("Funcionários -> " + resp.body());
                    // troca de cena tem de correr no FX Thread
                    Platform.runLater(() -> mostrarAdminPanel(username));
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro de Login");
                        alert.setHeaderText("Falha na autenticação");
                        alert.setContentText("Não foi possível fazer login. Verifique suas credenciais.");
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    private void mostrarAdminPanel(String username) {
        BorderPane root = new BorderPane();

        // Create top header with logout button
        HBox topBar = createTopBar(username);
        root.setTop(topBar);

        // Create sidebar with navigation options
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Create main content area (initially empty with welcome message)
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        Text welcomeText = new Text("Bem-vindo ao EatEase Admin");
        welcomeText.getStyleClass().add("welcome-text");
        contentArea.getChildren().add(welcomeText);

        root.setCenter(contentArea);

        // Inicializar views
        employeeView = new EmployeeView(contentArea, http);
        ingredientsView = new IngredientsView(contentArea, http);

        // Set scene and show
        Scene dashboardScene = new Scene(root, 1024, 768);
        dashboardScene.getStylesheets().add(getClass().getResource("/css/css.css").toExternalForm());
        primaryStage.setTitle("EatEase - Painel de Administração");
        primaryStage.setScene(dashboardScene);
        primaryStage.setMaximized(true);
    }

    private HBox createTopBar(String username) {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setPrefHeight(50);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        // Branding on left
        Label brandLabel = new Label("EatEase");
        brandLabel.getStyleClass().add("brand-label");

        // User info and logout in center-right
        Label userLabel = new Label("Usuário: " + username);
        userLabel.getStyleClass().add("user-label");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setOnAction(e -> logout());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(brandLabel, spacer, userLabel, logoutBtn);
        topBar.setPadding(new Insets(5, 15, 5, 15));

        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        // Create menu items with Material Design icons
        Button dashboardBtn = createMenuButton("Dashboard", MaterialDesign.MDI_VIEW_DASHBOARD);
        Button menusBtn = createMenuButton("Menus", MaterialDesign.MDI_FOOD);
        Button ingredientesBtn = createMenuButton("Ingredientes", MaterialDesign.MDI_FOOD_APPLE);
        Button itemBtn = createMenuButton("Item", MaterialDesign.MDI_FOOD_VARIANT);
        Button ordersBtn = createMenuButton("Pedidos", MaterialDesign.MDI_RECEIPT);
        Button tablesBtn = createMenuButton("Mesas", MaterialDesign.MDI_TABLE);
        Button workersBtn = createMenuButton("Funcionários", MaterialDesign.MDI_ACCOUNT_MULTIPLE);
        Button reportsBtn = createMenuButton("Relatórios", MaterialDesign.MDI_CHART_BAR);
        Button settingsBtn = createMenuButton("Configurações", MaterialDesign.MDI_SETTINGS);

        // Update actions for menu items
        workersBtn.setOnAction(e -> showEmployeesView());
        ingredientesBtn.setOnAction(e -> showIngredientsView());

        // Add menu items to sidebar
        sidebar.getChildren().addAll(
                createSidebarHeader(),
                dashboardBtn,
                menusBtn,
                ingredientesBtn,
                itemBtn,
                ordersBtn,
                tablesBtn,
                workersBtn,
                reportsBtn,
                settingsBtn);

        return sidebar;
    }

    private Label createSidebarHeader() {
        Label header = new Label("NAVEGAÇÃO");
        header.getStyleClass().add("sidebar-header");
        return header;
    }

    private Button createMenuButton(String text, MaterialDesign icon) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-menu-button");

        // Create icon
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconColor(Color.WHITE);
        fontIcon.setIconSize(18);

        btn.setGraphic(fontIcon);
        btn.setPrefWidth(200);
        btn.setMaxWidth(Double.MAX_VALUE);

        // Set action to show placeholder for now
        btn.setOnAction(e -> showPlaceholder(text));

        return btn;
    }

    private void showPlaceholder(String section) {
        System.out.println("Navegou para: " + section);
        // Clear content area
        contentArea.getChildren().clear();

        // Show placeholder text
        Text placeholderText = new Text("Página " + section + " em desenvolvimento");
        placeholderText.getStyleClass().add("welcome-text");
        contentArea.getChildren().add(placeholderText);
    }

    /**
     * Show the employees view with cards for each employee
     */
    private void showEmployeesView() {
        employeeView.show();
    }

    /**
     * Show the ingredients view with cards for each ingredient
     */
    private void showIngredientsView() {
        ingredientsView.show();
    }

    private void logout() {
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/logout")))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        http.sendAsync(loginReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    System.out.println(resp.body());
                    return null;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });

        cookieManager.getCookieStore().removeAll();
        start(primaryStage);
    }

    public static void main(String[] args) {
        launch();
    }
}
