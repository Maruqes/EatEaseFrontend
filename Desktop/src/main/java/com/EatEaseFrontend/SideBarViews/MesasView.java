package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.AsyncOperationManager;
import com.EatEaseFrontend.LoadingStateManager;
import com.EatEaseFrontend.Mesa;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.SideBarViews.PopUp;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * View para gerenciar e exibir mesas do restaurante
 */
public class MesasView {

    private final StackPane contentArea;
    private final HttpClient httpClient;
    private final AsyncOperationManager asyncManager;
    private final LoadingStateManager loadingManager;
    private final AtomicBoolean isTimerOperationRunning = new AtomicBoolean(false);

    // Temporizador para atualizações automáticas
    private Timer autoUpdateTimer;
    // Intervalo de atualização em segundos (facilmente ajustável)
    private final int updateIntervalSeconds = 15; // Altere esse valor para modificar o intervalo de atualização
    // Flag para controlar se a view está ativa
    private boolean isViewActive = false;

    /**
     * Construtor da view de mesas
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public MesasView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
        this.asyncManager = new AsyncOperationManager();
        this.loadingManager = new LoadingStateManager();
    }

    /**
     * Carrega e exibe a lista de mesas
     */
    public void show() {
        System.out.println("Carregando lista de mesas...");

        // Marcar a view como ativa
        isViewActive = true;

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando mesas...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get mesas
        loadMesas();

        // Iniciar o temporizador de atualização automática se ainda não estiver ativo
        startAutoUpdateTimer();
    }

    /**
     * Carrega as mesas do servidor (CORRIGIDO)
     */
    private void loadMesas() {
        System.out.println("[MESAS] Iniciando carregamento de mesas...");
        
        // Verificar se é uma operação do timer para evitar conflitos
        boolean isTimerOperation = !Platform.isFxApplicationThread();
        String operationId = isTimerOperation ? "autoLoadMesas" : "manualLoadMesas";
        
        // Para operações do timer, usar controle atômico
        if (isTimerOperation) {
            if (!isTimerOperationRunning.compareAndSet(false, true)) {
                System.out.println("[MESAS] Operação automática já em execução, ignorando...");
                return;
            }
        }

        // Criar requisição
        HttpRequest getMesasReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/getAll")))
                .GET()
                .build();

        // Usar AsyncOperationManager para controlar a operação
        asyncManager.executeSimpleOperation(
                () -> {
                    try {
                        System.out.println("[MESAS] Enviando requisição síncrona para carregar mesas...");
                        HttpResponse<String> response = httpClient.send(getMesasReq, HttpResponse.BodyHandlers.ofString());
                        
                        System.out.println("[MESAS] Response status: " + response.statusCode());
                        if (response.statusCode() == 200) {
                            System.out.println("[MESAS] Mesas recebidas com sucesso");
                            return JsonParser.parseMesas(response.body());
                        } else {
                            System.err.println("[MESAS] Erro HTTP ao carregar mesas. Status: " + response.statusCode());
                            throw new RuntimeException("Failed to load mesas: " + response.statusCode());
                        }
                    } catch (Exception e) {
                        System.err.println("[MESAS] Exceção ao carregar mesas: " + e.getMessage());
                        throw new RuntimeException(e);
                    } finally {
                        // Reset do flag para operações do timer
                        if (isTimerOperation) {
                            isTimerOperationRunning.set(false);
                        }
                    }
                },
                () -> {
                    // onComplete - executado na UI thread
                    System.out.println("[MESAS] Carregamento completo, atualizando UI");
                }
        ).thenAccept(mesas -> {
            if (mesas != null) {
                Platform.runLater(() -> displayMesasAsGrid(mesas));
            }
        }).exceptionally(e -> {
            System.err.println("[MESAS] Erro no carregamento assíncrono: " + e.getMessage());
            Platform.runLater(() -> {
                if (!isTimerOperation) { // Só mostrar erro para operações manuais
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Falha ao carregar mesas");
                    alert.setContentText("Erro: " + e.getMessage());
                    alert.showAndWait();
                }
            });
            return null;
        });
    }

    /**
     * Inicia o temporizador para atualização automática das mesas (CORRIGIDO)
     */
    private void startAutoUpdateTimer() {
        // Cancelar qualquer temporizador existente
        stopAutoUpdateTimer();

        System.out.println("[MESAS] Iniciando timer de atualização automática (intervalo: " + updateIntervalSeconds + "s)");
        
        // Criar um novo temporizador
        autoUpdateTimer = new Timer();
        autoUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Verificar se a view ainda está ativa antes de atualizar
                if (isViewActive) {
                    System.out.println("[MESAS] Executando atualização automática...");
                    // Carregar as mesas silenciosamente (sem mostrar indicador de carregamento)
                    silentlyUpdateMesas();
                } else {
                    System.out.println("[MESAS] View inativa, cancelando timer...");
                    this.cancel();
                }
            }
        }, updateIntervalSeconds * 1000, updateIntervalSeconds * 1000);
    }

    /**
     * Para o temporizador de atualização automática
     */
    private void stopAutoUpdateTimer() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    /**
     * Atualiza as mesas silenciosamente (sem mostrar indicador de carregamento)
     */
    private void silentlyUpdateMesas() {
        System.out.println("Realizando atualização automática das mesas...");
        HttpRequest getMesasReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getMesasReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return JsonParser.parseMesas(response.body());
                    } else {
                        throw new RuntimeException("Failed to update mesas: " + response.statusCode());
                    }
                })
                .thenAccept(mesas -> {
                    // Só atualizar a UI se a view estiver ativa
                    if (isViewActive) {
                        System.out.println("Atualizando visualização das mesas automaticamente");
                        Platform.runLater(() -> displayMesasAsGrid(mesas));
                    }
                })
                .exceptionally(e -> {
                    // Falhas silenciosas, apenas log para console
                    System.err.println("Erro na atualização automática: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Exibe as mesas em uma grade
     * 
     * @param mesas Lista de mesas a serem exibidas
     */
    private void displayMesasAsGrid(List<Mesa> mesas) {
        // Garantir que as mesas sejam sempre exibidas em ordem por número
        mesas.sort(Comparator.comparingInt(Mesa::getNumero));

        contentArea.getChildren().clear();

        // Header with title and refresh button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Gestão de Mesas");
        title.getStyleClass().add("welcome-text");

        Button refreshButton = new Button();
        FontIcon refreshIcon = new FontIcon(MaterialDesign.MDI_REFRESH);
        refreshIcon.setIconColor(Color.valueOf("#2a5298"));
        refreshIcon.setIconSize(20);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setTooltip(new Tooltip("Atualizar mesas"));
        refreshButton.setOnAction(e -> show());

        // Adicionar botão de nova mesa
        Button addButton = new Button();
        FontIcon addIcon = new FontIcon(MaterialDesign.MDI_PLUS);
        addIcon.setIconColor(Color.valueOf("#2a5298"));
        addIcon.setIconSize(20);
        addButton.setGraphic(addIcon);
        addButton.getStyleClass().add("icon-button");
        addButton.setTooltip(new Tooltip("Adicionar nova mesa"));
        addButton.setOnAction(e -> showAddMesaDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, addButton, refreshButton);

        // Create content
        FlowPane mesasGrid = new FlowPane();
        mesasGrid.setHgap(20);
        mesasGrid.setVgap(20);
        mesasGrid.setPadding(new Insets(20));
        mesasGrid.setAlignment(Pos.CENTER);

        // Add mesa cards to grid
        for (Mesa mesa : mesas) {
            mesasGrid.getChildren().add(createMesaBox(mesa));
        }

        // Add everything to a scroll pane
        ScrollPane scrollPane = new ScrollPane(mesasGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(header, scrollPane);

        contentArea.getChildren().add(mainLayout);
    }

    /**
     * Cria uma representação visual da mesa
     * 
     * @param mesa Mesa a ser representada
     * @return StackPane contendo a representação visual da mesa
     */
    private StackPane createMesaBox(Mesa mesa) {
        // Create the mesa box
        StackPane mesaBox = new StackPane();
        mesaBox.setPrefSize(200, 200);
        mesaBox.getStyleClass().add("dashboard-card");

        // Background color based on state
        Color mesaColor = mesa.isEstadoLivre() ? Color.valueOf("#4CAF50") : Color.valueOf("#F44336");

        // Mesa table visual (rectangle with rounded corners)
        Rectangle mesaRect = new Rectangle(160, 120);
        mesaRect.setFill(mesaColor);
        mesaRect.setArcWidth(20);
        mesaRect.setArcHeight(20);
        mesaRect.setStroke(Color.valueOf("#37474F"));
        mesaRect.setStrokeWidth(2);

        // Mesa information
        Text idText = new Text("ID: " + mesa.getId());
        idText.setFill(Color.WHITE);
        idText.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Text mesaNumber = new Text("Mesa " + mesa.getNumero());
        mesaNumber.setFill(Color.WHITE);
        mesaNumber.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Mesa status
        Text statusText = new Text(mesa.isEstadoLivre() ? "Livre" : "Ocupada");
        statusText.setFill(Color.WHITE);
        statusText.setFont(Font.font("System", 14));

        // Stack information on top of rectangle
        StackPane stackedInfo = new StackPane();
        stackedInfo.getChildren().add(mesaRect);

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(idText, mesaNumber, statusText);
        stackedInfo.getChildren().add(infoBox);

        // Button Container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);

        // Action buttons
        Button liberarButton = new Button("Liberar");
        liberarButton.getStyleClass().add("green-button");
        liberarButton.setDisable(mesa.isEstadoLivre());

        Button ocuparButton = new Button("Ocupar");
        ocuparButton.getStyleClass().add("red-button");
        ocuparButton.setDisable(!mesa.isEstadoLivre());

        buttonContainer.getChildren().addAll(liberarButton, ocuparButton);

        // Button actions
        liberarButton.setOnAction(e -> {
            showConfirmDialog("Liberar Mesa", "Tem certeza que deseja liberar a Mesa " + mesa.getNumero() + "?", () -> {
                liberarMesa(mesa.getId());
            });
        });

        ocuparButton.setOnAction(e -> {
            showConfirmDialog("Ocupar Mesa",
                    "Tem certeza que deseja marcar a Mesa " + mesa.getNumero() + " como ocupada?", () -> {
                        ocuparMesa(mesa.getId());
                    });
        });

        // Arrange elements vertically
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(stackedInfo, buttonContainer);

        mesaBox.getChildren().add(layout);
        return mesaBox;
    }

    /**
     * Exibe uma caixa de diálogo de confirmação
     * 
     * @param title     Título da caixa de diálogo
     * @param message   Mensagem a ser exibida
     * @param onConfirm Ação a ser executada ao confirmar
     */
    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(false);

        // Create main container
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        container.setPrefWidth(400);
        container.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setAlignment(Pos.CENTER);

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        messageLabel.setAlignment(Pos.CENTER);

        // Buttons
        Button confirmButton = new Button("Confirmar");
        confirmButton.getStyleClass().add("login-button");
        confirmButton.setOnAction(e -> {
            popup.hide();
            onConfirm.run();
        });

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("login-button");
        cancelButton.setOnAction(e -> popup.hide());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        container.getChildren().addAll(titleLabel, messageLabel, buttonBox);
        popup.getContent().add(container);

        // Show popup centered
        double centerX = contentArea.getScene().getWindow().getX() + contentArea.getScene().getWindow().getWidth() / 2;
        double centerY = contentArea.getScene().getWindow().getY() + contentArea.getScene().getWindow().getHeight() / 2;
        popup.show(contentArea.getScene().getWindow(), centerX - 200, centerY - 100);
    }

    /**
     * Envia requisição para liberar uma mesa
     * 
     * @param mesaId ID da mesa a ser liberada
     */
    private void liberarMesa(int mesaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/liberar?id=" + mesaId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        throw new RuntimeException("Falha ao liberar mesa: " + response.statusCode());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload mesas after action
                        show();
                        showAlert("Mesa liberada com sucesso!", Alert.AlertType.INFORMATION);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Erro ao liberar mesa: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Envia requisição para ocupar uma mesa
     * 
     * @param mesaId ID da mesa a ser ocupada
     */
    private void ocuparMesa(int mesaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/ocupar?id=" + mesaId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        throw new RuntimeException("Falha ao ocupar mesa: " + response.statusCode());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload mesas after action
                        show();
                        showAlert("Mesa ocupada com sucesso!", Alert.AlertType.INFORMATION);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Erro ao ocupar mesa: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Exibe uma caixa de alerta
     * 
     * @param message   Mensagem a ser exibida
     * @param alertType Tipo do alerta
     */
    private void showAlert(String message, Alert.AlertType alertType) {
        String title = alertType == Alert.AlertType.ERROR ? "Erro" : "Sucesso";
        PopUp.showPopupDialog(alertType, title, "", message);
    }

    /**
     * Exibe o diálogo para adicionar uma nova mesa
     */
    private void showAddMesaDialog() {
        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(false);

        // Create main container
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        container.setPrefWidth(400);
        container.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("Adicionar Nova Mesa");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setAlignment(Pos.CENTER);

        // Form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // Add the numero field
        TextField numeroField = new TextField();
        numeroField.setPromptText("Número da Mesa");
        grid.add(new Label("Número:"), 0, 0);
        grid.add(numeroField, 1, 0);

        // Buttons
        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("login-button");
        saveButton.setDisable(true);

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("login-button");
        cancelButton.setOnAction(e -> popup.hide());

        // Validate input - only numbers allowed
        numeroField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = false;
            try {
                if (!newValue.isEmpty()) {
                    int numero = Integer.parseInt(newValue);
                    isValid = numero > 0;
                }
            } catch (NumberFormatException e) {
                isValid = false;
            }
            saveButton.setDisable(!isValid);
        });

        saveButton.setOnAction(e -> {
            try {
                int numero = Integer.parseInt(numeroField.getText());
                popup.hide();
                createMesa(numero);
            } catch (NumberFormatException ex) {
                // Should not happen due to validation
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        container.getChildren().addAll(titleLabel, grid, buttonBox);
        popup.getContent().add(container);

        // Show popup centered
        double centerX = contentArea.getScene().getWindow().getX() + contentArea.getScene().getWindow().getWidth() / 2;
        double centerY = contentArea.getScene().getWindow().getY() + contentArea.getScene().getWindow().getHeight() / 2;
        popup.show(contentArea.getScene().getWindow(), centerX - 200, centerY - 100);

        // Request focus on the numero field
        Platform.runLater(numeroField::requestFocus);
    }

    /**
     * Envia requisição para criar uma nova mesa
     * 
     * @param numero Número da nova mesa
     */
    private void createMesa(int numero) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/create?numero=" + numero)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status: " + response.statusCode());
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        return true;
                    } else {
                        throw new RuntimeException("Falha ao criar mesa: " + response.statusCode());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload mesas after action
                        show();
                        showAlert("Mesa criada com sucesso!", Alert.AlertType.INFORMATION);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Erro ao criar mesa: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Método para limpar recursos quando a view não está mais visível
     * Este método deve ser chamado quando a view for fechada ou substituída
     */
    public void dispose() {
        // Verificar se a view já está inativa para evitar operações duplicadas
        if (isViewActive) {
            System.out.println("Desativando atualização automática das mesas");
            isViewActive = false;
            stopAutoUpdateTimer();
        }
    }
}
