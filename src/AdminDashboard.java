import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminDashboard - JavaFX Shell
 * Migrated from Java Swing.
 * 
 * Hierarchy:
 * Root: BorderPane
 * - Top: Header with Admin name
 * - Left: Sidebar (VBox) with buttons
 * - Center: Content Container (StackPane)
 * - Bottom: Status bar with stats and refresh
 */
public class AdminDashboard extends Application {

    private String adminEmail;
    private StackPane contentArea;
    private Label statsLabel;
    private Stage stage;
    private Button bellBtn;
    private java.util.function.Consumer<Notification> notificationListener;
    private TableView<OrderRoute> routeTable; // Use a field to avoid scope issues

    // Constructors
    public AdminDashboard() {
    }

    public AdminDashboard(String email) {
        this.adminEmail = email;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Admin Dashboard - SuperMart");

        BorderPane root = new BorderPane();

        // 1. TOP area: Header
        HBox header = new HBox();
        header.getStyleClass().add("header-panel");
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(2);
        Label welcome = new Label("SuperMart Admin");
        welcome.getStyleClass().add("header-title");
        Label emailLabel = new Label("Logged in as: " + (adminEmail != null ? adminEmail : "Admin"));
        emailLabel.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(welcome, emailLabel);
        
        bellBtn = NotificationUI.createNotificationBell(Notification.UserType.ADMIN, "admin");
        
        header.getChildren().addAll(titleBox, new Region(), bellBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        
        root.setTop(header);
        
        // Setup Live Notifications
        setupNotifications();

        // 2. LEFT area: Sidebar
        VBox sidebar = new VBox(5);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);

        Label navLabel = new Label("NAVIGATION");
        navLabel.setStyle("-fx-text-fill: #37B7C3; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 10 20 10 20;");
        sidebar.getChildren().add(navLabel);

        String[] navButtons = {
                "Inventory",
                "Same Day Deliveries",
                "Next Day Deliveries",
                "Route Planner",
                "Delivery Management",
                "Daily Reports",
                "Logout"
        };

        for (String name : navButtons) {
            Button btn = new Button(name);
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> {
                if (name.equals("Logout")) {
                    handleLogout();
                } else {
                    switchCenterContent(name);
                    // Update active state
                    sidebar.getChildren().forEach(node -> node.getStyleClass().remove("active"));
                    btn.getStyleClass().add("active");
                }
            });
            sidebar.getChildren().add(btn);
        }
        root.setLeft(sidebar);

        // 3. CENTER area: Content Container
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(25));
        // Default initial view
        switchCenterContent("Admin Home");
        root.setCenter(contentArea);

        // 4. BOTTOM area: Status/Info Bar
        HBox statusBar = new HBox(20);
        statusBar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0 transparent transparent transparent; -fx-padding: 10 25 10 25;");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statsLabel = new Label("Total Orders Today: 0 | Revenue: ₹0");
        statsLabel.setStyle("-fx-text-fill: #4A5568; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Data");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> {
            refreshDashboardStats();
        });

        statusBar.getChildren().addAll(statsLabel, spacer, refreshBtn);
        refreshDashboardStats();

        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 850);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void setupNotifications() {
        notificationListener = n -> {
            if (n.getUserType() == Notification.UserType.ADMIN) {
                NotificationUI.updateBellBadge(bellBtn, Notification.UserType.ADMIN, "admin");
                NotificationUI.showToast(stage, n.getTitle(), n.getMessage());
            }
        };
        NotificationService.getInstance().subscribe(notificationListener);
    }

    private void switchCenterContent(String panelName) {
        contentArea.getChildren().clear();
        if (panelName.equals("Admin Home")) {
            contentArea.getChildren().add(createAdminHomePanel());
        } else if (panelName.equals("Inventory")) {
            contentArea.getChildren().add(createInventoryPanel());
        } else if (panelName.equals("Same Day Deliveries")) {
            contentArea.getChildren().add(createOrdersPanel("Same Day"));
        } else if (panelName.equals("Next Day Deliveries")) {
            contentArea.getChildren().add(createOrdersPanel("Next Day"));
        } else if (panelName.equals("Daily Reports")) {
            contentArea.getChildren().add(createReportsPanel());
        } else if (panelName.equals("Route Planner")) {
            contentArea.getChildren().add(createRoutePlannerPanel());
        } else if (panelName.equals("Delivery Management")) {
            contentArea.getChildren().add(createDeliveryManagementPanel());
        } else {
            VBox panel = new VBox(20);
            panel.setAlignment(Pos.TOP_LEFT);
            Label title = new Label(panelName);
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            panel.getChildren().add(title);
            Label placeholder = new Label("Logic for [" + panelName + "] Screen - Panel migration pending.");
            placeholder.setStyle("-fx-text-fill: #666666; -fx-italic: true;");
            panel.getChildren().add(placeholder);
            contentArea.getChildren().add(panel);
        }
    }

    private ScrollPane createAdminHomePanel() {
        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(40));
        mainContainer.getStyleClass().add("card-panel");

        // Welcome Section
        VBox welcomeBox = new VBox(5);
        Label welcomeTitle = new Label("Welcome back, Administrator");
        welcomeTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #071952;");
        Label welcomeSub = new Label("Here is what's happening in SuperMart today.");
        welcomeSub.getStyleClass().add("header-subtitle");
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSub);

        // Stats Row
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        ReportDAO reportDao = new ReportDAO();
        java.util.Map<String, Object> metrics = reportDao.getMetricsForDate(java.time.LocalDate.now().toString());
        
        int totalOrders = (int) metrics.getOrDefault("total_orders", 0);
        double totalRevenue = (double) metrics.getOrDefault("total_revenue", 0.0);
        int pendingOrders = (int) metrics.getOrDefault("pending_count", 0);
        
        int totalProducts = new ProductDAO().getAllProducts().size();
        int totalAgents = new DeliveryDAO().getAllAgents().size();

        statsRow.getChildren().addAll(
            createStatChip("Today's Orders", String.valueOf(totalOrders)),
            createStatChip("Today's Revenue", String.format("₹%.0f", totalRevenue)),
            createStatChip("Pending Deliveries", String.valueOf(pendingOrders)),
            createStatChip("Active Agents", String.valueOf(totalAgents))
        );

        // Quick Actions Section
        Label actionTitle = new Label("Quick Management");
        actionTitle.getStyleClass().add("section-title");
        actionTitle.setPadding(new Insets(20, 0, 0, 0));

        FlowPane actions = new FlowPane(20, 20);
        String[][] quickActions = {
            {"Inventory", "Manage products and stock levels"},
            {"Route Planner", "Optimize delivery assignments"},
            {"Delivery Management", "Register and track agents"},
            {"Daily Reports", "Analyze sales performance"}
        };

        for (String[] action : quickActions) {
            VBox actionCard = new VBox(10);
            actionCard.getStyleClass().add("stat-chip");
            actionCard.setPrefSize(220, 120);
            actionCard.setStyle(actionCard.getStyle() + "; -fx-cursor: hand;");
            
            Label title = new Label(action[0]);
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #088395;");
            Label desc = new Label(action[1]);
            desc.setWrapText(true);
            desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            
            actionCard.getChildren().addAll(title, desc);
            actionCard.setOnMouseClicked(e -> switchCenterContent(action[0]));
            actions.getChildren().add(actionCard);
        }

        mainContainer.getChildren().addAll(welcomeBox, statsRow, actionTitle, actions);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }

    private VBox createStatChip(String label, String value) {
        VBox chip = new VBox(5);
        chip.getStyleClass().add("stat-chip");
        chip.setMinWidth(220);
        chip.setPadding(new Insets(20));
        
        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");
        
        Label labLbl = new Label(label);
        labLbl.getStyleClass().add("stat-label");
        
        chip.getChildren().addAll(valLbl, labLbl);
        return chip;
    }

    // --- Inventory Panel Migration (Strict BorderPane implementation) ---
    private ScrollPane createInventoryPanel() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.getStyleClass().add("card-panel");

        // Header
        Label titleLabel = new Label("Inventory Management");
        titleLabel.getStyleClass().add("section-title");

        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        controlBox.setPadding(new Insets(0, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add Product");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");

        controlBox.getChildren().addAll(new Label("Filter:"), searchField, spacer, addBtn, deleteBtn, refreshBtn);
        mainContainer.getChildren().addAll(titleLabel, controlBox);

        // 2. CENTER area: TableView
        TableView<Product> inventoryTable = new TableView<>();
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Product, String> colImg = new TableColumn<>("Image");
        colImg.setPrefWidth(60);
        colImg.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("imagePath"));
        colImg.setCellFactory(param -> new TableCell<Product, String>() {
            private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        javafx.scene.image.Image img = new javafx.scene.image.Image("file:" + path, 40, 40, true, true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null); // Fallback to no image
                    }
                }
            }
        });

        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> colPrice = new TableColumn<>("Price(₹)");
        colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));

        TableColumn<Product, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("category"));

        inventoryTable.getColumns().addAll(colId, colImg, colName, colQty, colPrice, colCat);
        inventoryTable.setFixedCellSize(50);
        mainContainer.getChildren().add(inventoryTable);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        // Logic & Filtering
        ProductDAO dao = new ProductDAO();
        javafx.collections.ObservableList<Product> masterData = javafx.collections.FXCollections
                .observableArrayList(dao.getAllProducts());
        javafx.collections.transformation.FilteredList<Product> filteredData = new javafx.collections.transformation.FilteredList<>(
                masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                return product.getName().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        inventoryTable.setItems(filteredData);

        // Actions
        addBtn.setOnAction(e -> showAddProductDialog(inventoryTable, masterData));

        deleteBtn.setOnAction(e -> {
            Product selected = inventoryTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (dao.deleteProduct(selected.getId())) {
                    masterData.setAll(dao.getAllProducts());
                }
            }
        });

        refreshBtn.setOnAction(e -> {
            masterData.setAll(dao.getAllProducts());
        });

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }

    private void showAddProductDialog(TableView<Product> table, javafx.collections.ObservableList<Product> dataList) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Add New Inventory Item");

        ButtonType saveButton = new ButtonType("Add Product", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nameIn = new TextField();
        TextField priceIn = new TextField();
        TextField qtyIn = new TextField();
        ComboBox<String> catIn = new ComboBox<>(
                javafx.collections.FXCollections.observableArrayList("Fruits", "Vegetables", "Dairy", "Other"));
        catIn.getSelectionModel().selectFirst();

        TextField pathIn = new TextField();
        pathIn.setEditable(false);
        pathIn.setPromptText("No image selected");
        Button browseBtn = new Button("Browse...");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameIn, 1, 0);
        grid.add(new Label("Price(₹):"), 0, 1);
        grid.add(priceIn, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(qtyIn, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(catIn, 1, 3);
        grid.add(new Label("Image Path:"), 0, 4);
        HBox pathBox = new HBox(5, pathIn, browseBtn);
        grid.add(pathBox, 1, 4);

        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            java.io.File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                pathIn.setText(selectedFile.getAbsolutePath());
            }
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                try {
                    String name = nameIn.getText();
                    double price = Double.parseDouble(priceIn.getText());
                    int qty = Integer.parseInt(qtyIn.getText());
                    String cat = catIn.getValue();
                    String imgPath = pathIn.getText();

                    if (name.isEmpty())
                        throw new Exception("Product name is required.");

                    ProductDAO dao = new ProductDAO();
                    if (dao.addProduct(new Product(0, name, qty, price, cat, imgPath))) {
                        dataList.setAll(dao.getAllProducts());
                    }
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Validation Error: " + ex.getMessage()).show();
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // --- Orders Panels Migration ---
    private ScrollPane createOrdersPanel(String temporalType) {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.getStyleClass().add("card-panel");

        OrderDAO dao = new OrderDAO();
        String activeDate = dao.getEarliestPendingDeliveryDate(temporalType);
        String displayDate = (activeDate != null) ? activeDate : "No Pending Orders";
        
        Label titleLabel = new Label(temporalType + " Orders - Batch: " + displayDate);
        titleLabel.getStyleClass().add("section-title");
        
        Label subLabel = new Label("Showing the earliest batch with undelivered orders as per business rules.");
        subLabel.getStyleClass().add("header-subtitle");
        subLabel.setStyle("-fx-text-fill: #718096;");
        
        mainContainer.getChildren().addAll(titleLabel, subLabel);

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Order, String> colId = new TableColumn<>("Order ID");
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));

        TableColumn<Order, String> colCust = new TableColumn<>("Customer Email");
        colCust.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerEmail"));

        TableColumn<Order, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("address"));

        TableColumn<Order, String> colTime = new TableColumn<>("Order Date");
        colTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));

        TableColumn<Order, String> colScheduled = new TableColumn<>("Scheduled Date");
        colScheduled.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("scheduledDeliveryDate"));

        TableColumn<Order, Double> colTotal = new TableColumn<>("Total ₹");
        colTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));

        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        TableColumn<Order, Void> colActions = new TableColumn<>("Update Status");
        colActions.setCellFactory(param -> new TableCell<Order, Void>() {
            private final ComboBox<String> statusCombo = new ComboBox<>(
                    javafx.collections.FXCollections.observableArrayList("Pending", "Processing", "Delivered"));
            private final Button updateBtn = new Button("Update");
            private final HBox container = new HBox(10, statusCombo, updateBtn);

            {
                statusCombo.setPrefWidth(120);
                updateBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    String newStatus = statusCombo.getValue();
                    if (newStatus != null) {
                        OrderDAO dao = new OrderDAO();
                        // 1. Update DB
                        if (dao.updateOrderStatus(order.getOrderId(), newStatus)) {
                            new Alert(Alert.AlertType.INFORMATION, "Status updated! Reloading data...").show();
                            // 2. Refresh Table from DB to ensure local state matches DB
                            if (temporalType.equalsIgnoreCase("Today")) {
                                table.setItems(javafx.collections.FXCollections
                                        .observableArrayList(dao.getSameDayOrdersForToday()));
                            } else {
                                table.setItems(javafx.collections.FXCollections
                                        .observableArrayList(dao.getNextDayOrdersForTomorrow()));
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(order.getStatus());
                    setGraphic(container);
                }
            }
        });

        table.getColumns().addAll(colId, colCust, colAddress, colTime, colScheduled, colTotal, colStatus, colActions);

        // Data & Temporal Filtering
        javafx.collections.ObservableList<Order> data;
        if (activeDate != null) {
            data = javafx.collections.FXCollections.observableArrayList(dao.getOrdersForRouteBatch(temporalType, activeDate));
        } else {
            data = javafx.collections.FXCollections.observableArrayList();
        }
        table.setItems(data);

        // NEW: Double-click to open details
        table.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Order rowData = row.getItem();
                    showOrderDetailsPopup(rowData);
                }
            });
            return row;
        });

        mainContainer.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }

    // --- Daily Reports Panel Migration ---
    private BorderPane createReportsPanel() {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(20));

        // 1. TOP area: Date Selection
        HBox topArea = new HBox(15);
        topArea.setAlignment(Pos.CENTER_LEFT);
        topArea.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("Analytics & Reports");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        Button generateBtn = new Button("Generate Detailed Report");
        generateBtn.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");

        topArea.getChildren().addAll(titleLabel, new Region(), new Label("Select Date:"), datePicker, generateBtn);
        HBox.setHgrow(topArea.getChildren().get(1), Priority.ALWAYS);
        panel.setTop(topArea);

        // 2. CENTER area: Scrollable Content
        VBox mainContent = new VBox(30);
        mainContent.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        panel.setCenter(scrollPane);

        // Section A: Summary Metrics (Cards)
        FlowPane metricsPane = new FlowPane(20, 20);
        metricsPane.setAlignment(Pos.TOP_LEFT);

        // Section B: Top Products Table
        VBox topProductsSection = new VBox(10);
        Label topTitle = new Label("Top Selling Products");
        topTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        TableView<ReportDAO.TopProduct> topTable = new TableView<>();
        topTable.setPrefHeight(250);
        topTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<ReportDAO.TopProduct, Integer> colRank = new TableColumn<>("Rank");
        colRank.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("rank"));
        TableColumn<ReportDAO.TopProduct, String> colProdName = new TableColumn<>("Product Name");
        colProdName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        TableColumn<ReportDAO.TopProduct, Integer> colQtySold = new TableColumn<>("Qty Sold");
        colQtySold.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        TableColumn<ReportDAO.TopProduct, Double> colRev = new TableColumn<>("Revenue (₹)");
        colRev.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("revenue"));
        
        topTable.getColumns().addAll(colRank, colProdName, colQtySold, colRev);
        topProductsSection.getChildren().addAll(topTitle, topTable);

        // Section C: Detailed Orders
        VBox detailedOrdersSection = new VBox(10);
        Label detailedTitle = new Label("All Orders for Selected Date");
        detailedTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        TableView<Order> ordersTable = new TableView<>();
        ordersTable.setPrefHeight(350);
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Order, String> colOrderId = new TableColumn<>("Order ID");
        colOrderId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
        TableColumn<Order, String> colCust = new TableColumn<>("Customer");
        colCust.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerName"));
        TableColumn<Order, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerPhone"));
        TableColumn<Order, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deliveryType"));
        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        TableColumn<Order, Double> colAmt = new TableColumn<>("Amount (₹)");
        colAmt.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));

        ordersTable.getColumns().addAll(colOrderId, colCust, colPhone, colType, colStatus, colAmt);
        detailedOrdersSection.getChildren().addAll(detailedTitle, ordersTable);

        mainContent.getChildren().addAll(new Label("Performance Metrics Overview"), metricsPane, topProductsSection, detailedOrdersSection);

        // Logic
        generateBtn.setOnAction(e -> {
            java.time.LocalDate date = datePicker.getValue();
            if (date == null) return;
            String dateStr = date.toString();
            ReportDAO dao = new ReportDAO();
            
            // 1. Update Metrics
            java.util.Map<String, Object> m = dao.getMetricsForDate(dateStr);
            metricsPane.getChildren().clear();
            metricsPane.getChildren().addAll(
                createMetricCard("Total Orders", m.getOrDefault("total_orders", 0).toString(), "#3182ce"),
                createMetricCard("Total Revenue", "₹" + String.format("%.2f", m.getOrDefault("total_revenue", 0.0)), "#38a169"),
                createMetricCard("Same Day", m.getOrDefault("same_day_count", 0).toString(), "#805ad5"),
                createMetricCard("Next Day", m.getOrDefault("next_day_count", 0).toString(), "#d69e2e"),
                createMetricCard("Delivered", m.getOrDefault("delivered_count", 0).toString(), "#2f855a"),
                createMetricCard("Processing", m.getOrDefault("processing_count", 0).toString(), "#3182ce"),
                createMetricCard("Pending", m.getOrDefault("pending_count", 0).toString(), "#e53e3e"),
                createMetricCard("Avg Value", "₹" + String.format("%.2f", m.getOrDefault("avg_order_value", 0.0)), "#4a5568")
            );

            // 2. Update Top Products Table
            topTable.setItems(javafx.collections.FXCollections.observableArrayList(dao.getTopSellingProducts(dateStr)));
            
            // 3. Update Detailed Orders Table
            ordersTable.setItems(javafx.collections.FXCollections.observableArrayList());
            ordersTable.getItems().addAll(new OrderDAO().getOrdersByCriteria("Same Day", "'" + dateStr + "'"));
            ordersTable.getItems().addAll(new OrderDAO().getOrdersByCriteria("Next Day", "'" + dateStr + "'"));
            
            new Alert(Alert.AlertType.INFORMATION, "Report generated for " + dateStr).show();
        });

        // Initial load
        generateBtn.fire();

        return panel;
    }

    private VBox createMetricCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-border-radius: 4;");
        
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(t, v);
        return card;
    }

    private void refreshDashboardStats() {
        ReportDAO reportDao = new ReportDAO();
        java.util.Map<String, Object> metrics = reportDao.getMetricsForDate(java.time.LocalDate.now().toString());
        int orders = (int) metrics.getOrDefault("total_orders", 0);
        double revenue = (double) metrics.getOrDefault("total_revenue", 0.0);
        statsLabel.setText(String.format("Total Orders Today: %d | Revenue: ₹%.2f", orders, revenue));
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Logging out from Admin session...");
        alert.showAndWait();
        stage.close();
        Platform.runLater(() -> {
            try {
                new SuperMartMain().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // Bridge for Swing/Manual launch
    private double shopLat = Config.SHOP_LAT;
    private double shopLon = Config.SHOP_LON;

    private ScrollPane createRoutePlannerPanel() {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.getStyleClass().add("card-panel");

        Label title = new Label("Logistics & Route Optimization");
        title.getStyleClass().add("section-title");
        
        Label shopStatus = new Label("Identifying shop location...");
        shopStatus.getStyleClass().add("header-subtitle");

        Button generateBtn = new Button("Optimize & Launch Map");
        generateBtn.getStyleClass().add("btn-primary");
        generateBtn.setPrefHeight(40);
        
        // Shop location is strictly taken from Config.java
        shopStatus.setText("📍 Warehouse: Laxmi Road / Shaniwar Peth");

        ComboBox<String> typeCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList("Same Day", "Next Day"));
        typeCombo.getSelectionModel().select(1);
        
        Label batchLabel = new Label("Active Batch: Detecting...");
        batchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #088395;");

        HBox controls = new HBox(15, new VBox(5, title, shopStatus, batchLabel), new Region(), new Label("Delivery Type:"), typeCombo, generateBtn);
        HBox.setHgrow(controls.getChildren().get(1), Priority.ALWAYS);
        controls.setAlignment(Pos.CENTER_LEFT);

        OrderDAO orderDao = new OrderDAO();
        java.util.function.Consumer<String> updateBatchLabel = (type) -> {
            String date = orderDao.getEarliestPendingDeliveryDate(type);
            batchLabel.setText("Active Batch: " + (date != null ? date : "No Pending Orders"));
        };
        
        typeCombo.setOnAction(e -> updateBatchLabel.accept(typeCombo.getValue()));
        updateBatchLabel.accept(typeCombo.getValue());

        routeTable = new TableView<>();
        routeTable.setPrefHeight(400);
        routeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<OrderRoute, Integer> colStop = new TableColumn<>("Stop");
        colStop.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stop"));
        TableColumn<OrderRoute, String> colCustName = new TableColumn<>("Customer");
        colCustName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        TableColumn<OrderRoute, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("phone"));
        TableColumn<OrderRoute, String> colAddr = new TableColumn<>("Address");
        colAddr.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("address"));
        routeTable.getColumns().addAll(colStop, colCustName, colPhone, colAddr);

        VBox content = new VBox(20);
        content.getChildren().addAll(controls, new Separator(), routeTable);
        VBox.setVgrow(routeTable, Priority.ALWAYS);

        generateBtn.setOnAction(e -> {
            String selectedType = typeCombo.getValue();
            String activeDate = orderDao.getEarliestPendingDeliveryDate(selectedType);
            
            if (activeDate == null) {
                new Alert(Alert.AlertType.INFORMATION, "No undelivered orders for " + selectedType).show();
                return;
            }

            System.out.println("[DEBUG] Admin Route button clicked for " + selectedType + " batch: " + activeDate);
            List<Order> orders = orderDao.getOrdersForRouteBatch(selectedType, activeDate).stream()
                .filter(o -> o.getLatitude() != 0 && o.getLongitude() != 0)
                .collect(java.util.stream.Collectors.toList());

            if (orders.isEmpty()) { 
                new Alert(Alert.AlertType.INFORMATION, "No orders with valid coordinates in this batch.").show(); 
                return; 
            }
            
            new Thread(() -> {
                List<Order> optimized = new RoutePlanner().optimizeSequence(orders, shopLat, shopLon);
                
                // Update UI Table (Must be on UI Thread)
                Platform.runLater(() -> {
                    routeTable.getItems().setAll(optimized.stream()
                        .map(o -> new OrderRoute(optimized.indexOf(o)+1, o.getCustomerName(), o.getCustomerPhone(), o.getAddress()))
                        .collect(java.util.stream.Collectors.toList()));
                });
                
                // Launch Map (Heavy/Network - Keep on Background Thread)
                RouteMapLauncher.openRouteMapInBrowser(optimized, "Admin Dashboard", selectedType + " Batch: " + activeDate);
            }).start();
        });

        mainContainer.getChildren().add(content);
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }


    public static class OrderRoute {
        private final int stop;
        private final String name;
        private final String phone;
        private final String address;

        public OrderRoute(int stop, String name, String phone, String address) {
            this.stop = stop;
            this.name = name;
            this.phone = phone;
            this.address = address;
        }

        public int getStop() { return stop; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
    }

    private void showOrderDetailsPopup(Order order) {
        Stage popupStage = new Stage();
        popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popupStage.setTitle("Order Details - " + order.getOrderId());

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("ORDER SUMMARY & PACKING SLIP");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // 1. Header Information
        GridPane header = new GridPane();
        header.setHgap(30);
        header.setVgap(10);
        header.add(new Label("Order ID:"), 0, 0);
        Label oid = new Label(order.getOrderId()); oid.setStyle("-fx-font-weight: bold;");
        header.add(oid, 1, 0);
        
        header.add(new Label("Customer:"), 0, 1);
        header.add(new Label(order.getCustomerName() + " (" + order.getCustomerEmail() + ")"), 1, 1);
        
        header.add(new Label("Phone:"), 0, 2);
        Label ph = new Label(order.getCustomerPhone()); ph.setStyle("-fx-text-fill: #3182ce; -fx-font-weight: bold;");
        header.add(ph, 1, 2);
        
        header.add(new Label("Status:"), 2, 0);
        header.add(new Label(order.getStatus()), 3, 0);
        
        header.add(new Label("Type:"), 2, 1);
        header.add(new Label(order.getDeliveryType()), 3, 1);
        
        header.add(new Label("Date:"), 2, 2);
        header.add(new Label(order.getDate()), 3, 2);
        
        Label addrLabel = new Label("Delivery Address: " + order.getAddress());
        addrLabel.setWrapText(true);
        addrLabel.setStyle("-fx-background-color: #f7fafc; -fx-padding: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 4;");

        // 2. Items Table
        TableView<OrderItem> itemsTable = new TableView<>();
        itemsTable.setPlaceholder(new Label("No items found for this order. Check order_items table for OrderID: " + order.getOrderId()));
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<OrderItem, String> colProd = new TableColumn<>("Product");
        colProd.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getName()));
        
        TableColumn<OrderItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQty()).asObject());
        
        TableColumn<OrderItem, Double> colPrice = new TableColumn<>("Unit Price");
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPriceAtOrder()).asObject());
        
        TableColumn<OrderItem, Double> colTotal = new TableColumn<>("Line Total");
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getQty() * data.getValue().getPriceAtOrder()).asObject());

        itemsTable.getColumns().addAll(colProd, colQty, colPrice, colTotal);
        
        // Load Data from DAO with Debugging
        System.out.println("[DEBUG_UI] Opening details for: " + order.getOrderId() + " | Cust: " + order.getCustomerName());
        List<OrderItem> items = new OrderDAO().getOrderItemsByOrderId(order.getOrderId());
        System.out.println("[DEBUG_UI] Received " + items.size() + " items for table.");
        
        itemsTable.setItems(javafx.collections.FXCollections.observableArrayList(items));

        // 3. Footer Summary
        int totalQty = items.stream().mapToInt(OrderItem::getQty).sum();
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label summary = new Label("Distinct Items: " + items.size() + " | Total Qty: " + totalQty + " | Grand Total: ₹" + String.format("%.2f", order.getTotal()));
        summary.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        footer.getChildren().add(summary);

        Button closeBtn = new Button("Close Details");
        closeBtn.setOnAction(e -> popupStage.close());
        closeBtn.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white; -fx-padding: 8 20;");

        root.getChildren().addAll(title, header, addrLabel, new Separator(), new Label("ITEMS TO PACK"), itemsTable, footer, closeBtn);

        Scene scene = new Scene(root, 750, 650);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private BorderPane createDeliveryManagementPanel() {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(20));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Agents List & Registration
        VBox agentBox = new VBox(20);
        agentBox.setPadding(new Insets(20));
        
        Label agentTitle = new Label("Delivery Agents");
        agentTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        TableView<DeliveryAgent> agentTable = new TableView<>();
        agentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<DeliveryAgent, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("agentId"));
        TableColumn<DeliveryAgent, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fullName"));
        TableColumn<DeliveryAgent, String> colArea = new TableColumn<>("Area");
        colArea.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("assignedArea"));
        TableColumn<DeliveryAgent, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("approvalStatus"));
        
        agentTable.getColumns().addAll(colId, colName, colArea, colStatus);
        
        DeliveryDAO agentDao = new DeliveryDAO();
        agentTable.setItems(javafx.collections.FXCollections.observableArrayList(agentDao.getAllAgents()));

        Button addAgentBtn = new Button("Register New Agent");
        addAgentBtn.setOnAction(e -> showAddAgentDialog(agentTable));

        agentBox.getChildren().addAll(agentTitle, agentTable, addAgentBtn);
        tabs.getTabs().add(new Tab("Agents", agentBox));

        // Tab 2: Assign Orders
        VBox assignBox = new VBox(20);
        assignBox.setPadding(new Insets(20));
        
        Label assignTitle = new Label("Assign Pending Orders (Active Batches)");
        assignTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label batchInfo = new Label("Showing earliest pending batches for both types.");
        batchInfo.setStyle("-fx-text-fill: #718096;");
        
        TableView<Order> pendingTable = new TableView<>();
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Order, String> colOid = new TableColumn<>("Order ID");
        colOid.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
        TableColumn<Order, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deliveryType"));
        pendingTable.getColumns().addAll(colOid, colType);
        
        OrderDAO orderDao = new OrderDAO();
        javafx.collections.ObservableList<Order> pendingOrders = javafx.collections.FXCollections.observableArrayList();
        
        for (String type : new String[]{"Same Day", "Next Day"}) {
            String activeDate = orderDao.getEarliestPendingDeliveryDate(type);
            if (activeDate != null) {
                pendingOrders.addAll(orderDao.getOrdersForRouteBatch(type, activeDate).stream()
                    .filter(o -> "Pending".equals(o.getStatus()) || "Processing".equals(o.getStatus()))
                    .collect(java.util.stream.Collectors.toList()));
            }
        }
        pendingTable.setItems(pendingOrders);

        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<DeliveryAgent> agentCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(agentDao.getAllAgents()));
        agentCombo.setPromptText("Select Agent");
        Button assignBtn = new Button("Assign Selected Order");
        
        assignBtn.setOnAction(e -> {
            Order selectedOrder = pendingTable.getSelectionModel().getSelectedItem();
            DeliveryAgent selectedAgent = agentCombo.getValue();
            if (selectedOrder != null && selectedAgent != null) {
                handleAssignOrder(selectedOrder, selectedAgent, pendingTable);
            } else {
                new Alert(Alert.AlertType.WARNING, "Select both an order and an agent.").show();
            }
        });

        controlBox.getChildren().addAll(new Label("Assign to:"), agentCombo, assignBtn);
        assignBox.getChildren().addAll(assignTitle, batchInfo, pendingTable, controlBox);
        tabs.getTabs().add(new Tab("Assignments", assignBox));

        panel.setCenter(tabs);
        return panel;
    }

    private void handleAssignOrder(Order o, DeliveryAgent a, TableView<Order> table) {
        DeliveryAssignmentDAO dao = new DeliveryAssignmentDAO();
        int seq = dao.getNextSequenceForAgent(a.getAgentId(), o.getDeliveryType());
        
        // Simple ETA: 15 (dispatch) + (seq * 12 travel) + (seq * 5 service)
        int eta = 15 + (seq * 12) + (seq * 5);
        
        if (dao.assignOrder(o.getOrderId(), a.getAgentId(), o.getDeliveryType(), seq, eta)) {
            // Update order status to Processing if it was Pending
            if ("Pending".equals(o.getStatus())) {
                new OrderDAO().updateOrderStatus(o.getOrderId(), "Processing");
            }
            
            // Notification to Delivery Guy
            NotificationService.getInstance().sendToDeliveryGuy(
                a.getEmail(),
                "New Assignment",
                "Order " + o.getOrderId() + " assigned to you for " + o.getDeliveryType() + " delivery. ETA: " + eta + " mins.",
                o.getOrderId()
            );

            new Alert(Alert.AlertType.INFORMATION, "Order assigned successfully!").show();
            table.getItems().remove(o);
        }
    }

    private void showAddAgentDialog(TableView<DeliveryAgent> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register Delivery Agent");
        ButtonType saveButton = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameIn = new TextField();
        TextField emailIn = new TextField();
        TextField phoneIn = new TextField();
        TextField pwdIn = new TextField();
        TextField areaIn = new TextField();
        ComboBox<String> vehicleIn = new ComboBox<>(javafx.collections.FXCollections.observableArrayList("Bike", "Van", "Bicycle"));
        vehicleIn.getSelectionModel().select(0);

        grid.add(new Label("Full Name:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailIn, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneIn, 1, 2);
        grid.add(new Label("Password:"), 0, 3); grid.add(pwdIn, 1, 3);
        grid.add(new Label("Area:"), 0, 4); grid.add(areaIn, 1, 4);
        grid.add(new Label("Vehicle:"), 0, 5); grid.add(vehicleIn, 1, 5);

        dialog.getDialogPane().setContent(grid);

        java.util.Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            String name = nameIn.getText().trim();
            String email = emailIn.getText().trim();
            String phone = phoneIn.getText().trim();
            String pwd = pwdIn.getText().trim();
            String area = areaIn.getText().trim();
            String vehicle = vehicleIn.getValue();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pwd.isEmpty() || area.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Validation Error: All fields are required.").show();
                return;
            }

            DeliveryDAO dao = new DeliveryDAO();
            if (dao.emailExists(email)) {
                new Alert(Alert.AlertType.ERROR, "Validation Error: Email already exists.").show();
                return;
            }

            if (dao.registerAgent(name, email, phone, pwd, vehicle, area, "APPROVED")) {
                new Alert(Alert.AlertType.INFORMATION, "Success: Delivery Agent registered successfully.").show();
                table.setItems(javafx.collections.FXCollections.observableArrayList(dao.getAllAgents()));
            } else {
                new Alert(Alert.AlertType.ERROR, "Error: Failed to register agent in database.").show();
            }
        }
    }

    public void startApp() {
        Platform.runLater(() -> {
            try {
                start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
