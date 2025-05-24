package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.AsyncOperationManager;
import com.EatEaseFrontend.LoadingStateManager;
import com.EatEaseFrontend.Mesa;
import com.EatEaseFrontend.JsonParser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;

/**
 * View para gerenciar e exibir QR Codes para mesas
 */
public class QRCodesView {

    private final StackPane contentArea;
    private final HttpClient httpClient;
    private final AsyncOperationManager asyncManager;
    private final LoadingStateManager loadingManager;
    private List<Mesa> mesas;

    /**
     * Construtor da view de QR Codes
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public QRCodesView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
        this.asyncManager = new AsyncOperationManager();
        this.loadingManager = new LoadingStateManager();
    }

    /**
     * Carrega e exibe a página de geração de QR Codes
     */
    public void show() {
        System.out.println("Carregando página de QR Codes...");

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando mesas...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Carregar lista de mesas primeiro
        loadMesas();
    }

    /**
     * Carrega a lista de mesas do servidor (CORRIGIDO)
     */
    private void loadMesas() {
        System.out.println("[QRCODES] Iniciando carregamento de mesas...");
        
        if (!loadingManager.startLoading("loadMesas")) {
            System.out.println("[QRCODES] Carregamento de mesas já em execução");
            return;
        }

        // Criar requisição
        HttpRequest getMesasReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/getAll")))
                .GET()
                .build();

        // Usar AsyncOperationManager para controlar a operação
        asyncManager.executeOperation(
                () -> {
                    try {
                        System.out.println("[QRCODES] Enviando requisição síncrona para carregar mesas...");
                        HttpResponse<String> response = httpClient.send(getMesasReq, HttpResponse.BodyHandlers.ofString());
                        
                        if (response.statusCode() == 200) {
                            System.out.println("[QRCODES] Mesas recebidas com sucesso");
                            return JsonParser.parseMesas(response.body());
                        } else {
                            System.err.println("[QRCODES] Erro HTTP ao carregar mesas. Status: " + response.statusCode());
                            throw new RuntimeException("Erro HTTP: " + response.statusCode());
                        }
                    } catch (Exception e) {
                        System.err.println("[QRCODES] Exceção ao carregar mesas: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    // onSuccess - executado na UI thread
                    System.out.println("[QRCODES] Mesas carregadas com sucesso, atualizando UI");
                    this.displayQRCodeInterface();
                    loadingManager.stopLoading("loadMesas");
                },
                () -> {
                    // onError - executado na UI thread
                    System.err.println("[QRCODES] Erro ao carregar mesas");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Falha ao carregar mesas");
                    alert.setContentText("Não foi possível carregar a lista de mesas. Tente novamente.");
                    alert.showAndWait();
                    loadingManager.stopLoading("loadMesas");
                }
        ).thenAccept(mesasCarregadas -> {
            if (mesasCarregadas != null) {
                this.mesas = mesasCarregadas;
            }
        });
    }

    /**
     * Exibe a interface para geração e visualização de QR Codes
     */
    private void displayQRCodeInterface() {
        contentArea.getChildren().clear();

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Text title = new Text("Geração de QR Codes para Mesas");
        title.getStyleClass().add("welcome-text");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        header.getChildren().add(title);

        // Container principal
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));

        // Área de seleção de mesa
        VBox selectionArea = new VBox(15);
        selectionArea.setPadding(new Insets(20));
        selectionArea.getStyleClass().add("dashboard-card");

        Label selectLabel = new Label("Selecione uma mesa para gerar o QR Code:");
        selectLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        ComboBox<String> mesaComboBox = new ComboBox<>();

        // Ordenar mesas pelo número
        mesas.sort(Comparator.comparing(Mesa::getNumero));

        // Adicionar mesas ao combobox
        for (Mesa mesa : mesas) {
            mesaComboBox.getItems().add("Mesa #" + mesa.getNumero());
        }

        // Selecionar a primeira mesa por padrão se existir
        if (!mesas.isEmpty()) {
            mesaComboBox.getSelectionModel().selectFirst();
        }

        // Botão para gerar QR Code
        Button generateButton = new Button("Gerar QR Code");
        generateButton.getStyleClass().add("login-button");

        // Container para o QR Code gerado
        VBox qrCodeContainer = new VBox(15);
        qrCodeContainer.setAlignment(Pos.CENTER);
        qrCodeContainer.setPadding(new Insets(15));
        qrCodeContainer.getStyleClass().add("dashboard-card");

        Text qrTitle = new Text("QR Code");
        qrTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Placeholder para a imagem do QR Code
        ImageView qrImageView = new ImageView();
        qrImageView.setSmooth(false); // desativa a interpolação
        qrImageView.setCache(false); // força-o a redesenhar sempre com pixels “puros”
        qrImageView.setFitHeight(250);
        qrImageView.setFitWidth(250);
        qrImageView.setPreserveRatio(true);

        // Label para informações da mesa
        Label mesaInfoLabel = new Label("Selecione uma mesa e gere o QR Code");

        qrCodeContainer.getChildren().addAll(qrTitle, qrImageView, mesaInfoLabel);

        // Adicionar ação ao botão para gerar QR (CORRIGIDO)
        generateButton.setOnAction(e -> {
            int selectedIndex = mesaComboBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < mesas.size()) {
                Mesa selectedMesa = mesas.get(selectedIndex);
                generateQRCode(selectedMesa.getId(), qrImageView, mesaInfoLabel, generateButton);
            }
        });

        // Adicionar componentes à área de seleção
        selectionArea.getChildren().addAll(selectLabel, mesaComboBox, generateButton);

        // Adicionar componentes ao container principal
        mainContainer.getChildren().addAll(selectionArea, qrCodeContainer);

        // ScrollPane para facilitar visualização em telas pequenas
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Layout principal
        VBox rootLayout = new VBox(20);
        rootLayout.setPadding(new Insets(20));
        rootLayout.getChildren().addAll(header, scrollPane);

        contentArea.getChildren().add(rootLayout);
    }

    /**
     * Gera e exibe o QR Code para uma mesa específica (CORRIGIDO)
     * 
     * @param mesaId    ID da mesa
     * @param imageView ImageView onde exibir o QR Code
     * @param infoLabel Label para exibir informações da mesa
     * @param button    Botão a ser desabilitado durante a operação
     */
    private void generateQRCode(int mesaId, ImageView imageView, Label infoLabel, Button button) {
        System.out.println("[QRCODES] Iniciando geração de QR Code para mesa ID: " + mesaId);
        
        String operationId = "generateQR_" + mesaId;
        if (!loadingManager.startLoading(operationId, button)) {
            System.out.println("[QRCODES] Geração de QR Code já em execução para mesa: " + mesaId);
            return;
        }

        // Atualizar o status
        infoLabel.setText("Gerando QR Code para Mesa #" + mesaId + "...");

        // Limpar imagem atual
        imageView.setImage(null);

        // Criar requisição
        HttpRequest getQRRequest = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/qr/generateQR?mesaID=" + mesaId)))
                .GET()
                .build();

        // Usar AsyncOperationManager para controlar a operação
        asyncManager.executeOperation(
                () -> {
                    try {
                        System.out.println("[QRCODES] Enviando requisição síncrona para gerar QR Code...");
                        HttpResponse<byte[]> response = httpClient.send(getQRRequest, HttpResponse.BodyHandlers.ofByteArray());
                        
                        if (response.statusCode() == 200) {
                            System.out.println("[QRCODES] QR Code gerado com sucesso");
                            return response.body();
                        } else {
                            System.err.println("[QRCODES] Erro HTTP ao gerar QR Code. Status: " + response.statusCode());
                            throw new RuntimeException("Erro HTTP: " + response.statusCode());
                        }
                    } catch (Exception e) {
                        System.err.println("[QRCODES] Exceção ao gerar QR Code: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    // onSuccess - executado na UI thread
                    System.out.println("[QRCODES] QR Code processado com sucesso");
                    loadingManager.stopLoading(operationId);
                },
                () -> {
                    // onError - executado na UI thread
                    System.err.println("[QRCODES] Erro ao gerar QR Code");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Falha ao gerar QR Code");
                    alert.setContentText("Não foi possível gerar o QR Code. Tente novamente.");
                    alert.showAndWait();
                    infoLabel.setText("Erro ao gerar QR Code para Mesa #" + mesaId);
                    loadingManager.stopLoading(operationId);
                }
        ).thenAccept(bytes -> {
            if (bytes != null) {
                Platform.runLater(() -> {
                    try {
                        // Criar imagem a partir dos bytes retornados
                        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                        double w = imageView.getFitWidth();
                        double h = imageView.getFitHeight();
                        Image qrImage = new Image(bis, w, h, true, false);

                        imageView.setImage(qrImage);
                        imageView.setSmooth(false);
                        imageView.setCache(false);

                        // Atualizar label com informações da mesa
                        Mesa mesaSelecionada = findMesaById(mesaId);
                        if (mesaSelecionada != null) {
                            infoLabel.setText("QR Code para Mesa #" + mesaSelecionada.getNumero());
                        } else {
                            infoLabel.setText("QR Code para Mesa ID: " + mesaId);
                        }
                    } catch (Exception e) {
                        System.err.println("[QRCODES] Erro ao processar imagem: " + e.getMessage());
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao processar imagem do QR Code");
                        alert.setContentText("Erro: " + e.getMessage());
                        alert.showAndWait();
                        infoLabel.setText("Erro ao processar QR Code");
                    }
                });
            }
        });
    }

    /**
     * Encontra uma mesa pelo ID
     * 
     * @param mesaId ID da mesa a encontrar
     * @return O objeto Mesa correspondente ou null se não encontrado
     */
    private Mesa findMesaById(int mesaId) {
        for (Mesa mesa : mesas) {
            if (mesa.getId() == mesaId) {
                return mesa;
            }
        }
        return null;
    }

    /**
     * Método para limpar recursos quando a view não está mais visível
     */
    public void dispose() {
        // Nada a limpar por enquanto
    }
}
