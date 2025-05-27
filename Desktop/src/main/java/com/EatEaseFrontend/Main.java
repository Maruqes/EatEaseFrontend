package com.EatEaseFrontend;

import com.EatEaseFrontend.SideBarViews.DashboardView;
import com.EatEaseFrontend.SideBarViews.EmployeeView;
import com.EatEaseFrontend.SideBarViews.IngredientsView;
import com.EatEaseFrontend.SideBarViews.ItemView;
import com.EatEaseFrontend.SideBarViews.MenuView;
import com.EatEaseFrontend.SideBarViews.MesasView;
import com.EatEaseFrontend.SideBarViews.PedidosView;
import com.EatEaseFrontend.SideBarViews.PopUp;
import com.EatEaseFrontend.SideBarViews.QRCodesView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    private DashboardView dashboardView;
    private EmployeeView employeeView;
    private IngredientsView ingredientsView;
    private ItemView itemView;
    private MenuView menuView;
    private MesasView mesasView;
    private PedidosView pedidosView;
    private QRCodesView qrCodesView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        StageManager.setPrimaryStage(stage);

        // Permitir que o utilizador redimensione
        stage.setResizable(true);
        // Define limites mínimos, se quiseres
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        stage.setScene(LoginPage.createScene(stage, this::fazerLoginEListarFuncionarios));
        stage.setTitle("EatEase - Login");
        stage.show();
    }

    private void fazerLoginEListarFuncionarios(String username, String password) {
        String jsonBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
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
        HBox topBar = createTopBar(username);
        root.setTop(topBar);
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        Text welcomeText = new Text("Bem-vindo ao EatEase Admin");
        welcomeText.getStyleClass().add("welcome-text");
        contentArea.getChildren().add(welcomeText);
        root.setCenter(contentArea);

        // 2) Inicializa todas as views AQUI, antes de criar os botões
        dashboardView = new DashboardView(contentArea, http);
        employeeView = new EmployeeView(contentArea, http);
        ingredientsView = new IngredientsView(contentArea, http);
        itemView = new ItemView(contentArea, http);
        menuView = new MenuView(contentArea, http); // ← aqui
        mesasView = new MesasView(contentArea, http);
        pedidosView = new PedidosView(contentArea, http);
        qrCodesView = new QRCodesView(contentArea, http);

        // **Cria a cena com tamanho fixo inicial**
        Scene dashboardScene = new Scene(root, 1024, 768);
        dashboardScene.getStylesheets().add(
                getClass().getResource("/css/modern_style.css").toExternalForm());

        // **Aplica ao stage**
        primaryStage.setTitle("EatEase - Painel de Administração");
        primaryStage.setScene(dashboardScene);

        // Garante que o utilizador pode continuar a redimensionar
        primaryStage.setResizable(true);
        // (Opcional) mantém os limites mínimos
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Se não quiseres obrigar o maximize inicial, podes remover isto:
        // primaryStage.setMaximized(true);
    }

    private HBox createTopBar(String username) {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setPrefHeight(60);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        // Branding on left
        Label brandLabel = new Label("EatEase");
        brandLabel.getStyleClass().add("brand-label");

        // User info with icon
        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER);

        // User icon
        FontIcon userIcon = new FontIcon(MaterialDesign.MDI_ACCOUNT_CIRCLE);
        userIcon.setIconColor(Color.WHITE);
        userIcon.setIconSize(22);

        // User info
        Label userLabel = new Label("Olá, " + username);
        userLabel.getStyleClass().add("user-label");

        userBox.getChildren().addAll(userIcon, userLabel);

        // Modern logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setOnAction(e -> logout());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(brandLabel, spacer, userBox, logoutBtn);
        topBar.setPadding(new Insets(5, 20, 5, 20));

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
        Button qrCodesBtn = createMenuButton("QR Codes", MaterialDesign.MDI_QRCODE);
        Button workersBtn = createMenuButton("Funcionários", MaterialDesign.MDI_ACCOUNT_MULTIPLE);
        Button reportsBtn = createMenuButton("Relatórios", MaterialDesign.MDI_CHART_BAR);
        Button settingsBtn = createMenuButton("Configurações", MaterialDesign.MDI_SETTINGS);

        // Update actions for menu items
        dashboardBtn.setOnAction(e -> showDashboardView());
        workersBtn.setOnAction(e -> showEmployeesView());
        ingredientesBtn.setOnAction(e -> showIngredientsView());
        itemBtn.setOnAction(e -> showItemView());
        menusBtn.setOnAction(e -> showMenuView());
        tablesBtn.setOnAction(e -> showMesasView());
        ordersBtn.setOnAction(e -> showPedidosView());
        qrCodesBtn.setOnAction(e -> showQRCodesView());

        // Add menu items to sidebar
        sidebar.getChildren().addAll(
                createSidebarHeader(),
                dashboardBtn,
                menusBtn,
                ingredientesBtn,
                itemBtn,
                ordersBtn,
                tablesBtn,
                qrCodesBtn,
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

        // Desativar a atualização automática de todas as views quando sair da tela
        if (mesasView != null) {
            mesasView.dispose();
        }
        if (pedidosView != null) {
            pedidosView.dispose();
        }
        // Adicione outros dispose() aqui se outras views tiverem recursos a serem
        // liberados

        // Clear content area
        contentArea.getChildren().clear();

        // Show placeholder text
        Text placeholderText = new Text("Página " + section + " em desenvolvimento");
        placeholderText.getStyleClass().add("welcome-text");
        contentArea.getChildren().add(placeholderText);
    }

    /**
     * Show the dashboard view with statistics and quick actions
     */
    private void showDashboardView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        dashboardView.show();
    }

    /**
     * Show the employees view with cards for each employee
     */
    private void showEmployeesView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        employeeView.show();
    }

    /**
     * Show the ingredients view with cards for each ingredient
     */
    private void showIngredientsView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        ingredientsView.show();
    }

    /**
     * Show the items view with cards for each menu item
     */
    private void showItemView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        itemView.show();
    }

    /**
     * Show the menu view with cards for each menu
     */
    private void showMenuView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        menuView.show();
    }

    /**
     * Show the mesas view with tables and their status
     */
    private void showMesasView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        mesasView.show();
    }

    /**
     * Show the pedidos view with orders and their details
     */
    private void showPedidosView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        pedidosView.show();
    }

    /**
     * Show the QR Codes view to generate QR codes for tables
     */
    private void showQRCodesView() {
        // Desativar a atualização automática das views quando sair da tela
        disposeAllViews();
        qrCodesView.show();
    }

    private void logout() {
        // Desativar todas as atualizações automáticas antes do logout
        disposeAllViews();

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

    /**
     * Desativa todas as views para liberar recursos antes de mostrar uma nova view
     */
    private void disposeAllViews() {
        System.out.println("Desativando todas as views ativas");

        if (mesasView != null) {
            mesasView.dispose();
        }

        if (pedidosView != null) {
            pedidosView.dispose();
        }

        if (qrCodesView != null) {
            qrCodesView.dispose();
        }

        // Adicione outras views aqui se elas tiverem um método dispose()
    }

    public static void main(String[] args) {
        launch();
    }
}
