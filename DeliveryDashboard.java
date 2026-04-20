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
 * DeliveryDashboard - Dashboard for Delivery Personnel.
 */
public class DeliveryDashboard extends Application {

    private DeliveryAgent agent;
    private StackPane contentArea;
    private Stage stage;
    private Button bellBtn;
    private java.util.function.Consumer<Notification> notificationListener;

    public DeliveryDashboard() {}

    public DeliveryDashboard(DeliveryAgent agent) {
        this.agent = agent;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Delivery Dashboard - SuperMart");

        BorderPane root = new BorderPane();

        // 1. TOP area: Header
        HBox header = new HBox();
        header.getStyleClass().add("admin-header");
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label welcome = new Label("Delivery Agent: " + agent.getFullName());
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        bellBtn = NotificationUI.createNotificationBell(Notification.UserType.DELIVERY_GUY, agent.getEmail());
        
        header.getChildren().addAll(welcome, new Region(), bellBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        root.setTop(header);

        // Setup Live Notifications
        setupNotifications();

        // 2. LEFT area: Sidebar
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("admin-sidebar");
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(20, 10, 20, 10));

        String[] navButtons = {
                "Assigned Orders",
                "Same Day Map",
                "Next Day Map",
                "Route Summary",
                "Notifications",
                "Profile",
                "Logout"
        };

        for (String name : navButtons) {
            Button btn = new Button(name);
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setOnAction(e -> {
                if (name.equals("Logout")) {
                    handleLogout();
                } else {
                    switchCenterContent(name);
                }
            });
            sidebar.getChildren().add(btn);
        }
        root.setLeft(sidebar);

        // 3. CENTER area: Content Area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(25));
        switchCenterContent("Assigned Orders");
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("admin_dashboard.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void setupNotifications() {
        notificationListener = n -> {
            if (n.getUserType() == Notification.UserType.DELIVERY_GUY && n.getTargetUser().equalsIgnoreCase(agent.getEmail())) {
                NotificationUI.updateBellBadge(bellBtn, Notification.UserType.DELIVERY_GUY, agent.getEmail());
                NotificationUI.showToast(stage, n.getTitle(), n.getMessage());
            }
        };
        NotificationService.getInstance().subscribe(notificationListener);
    }

    private void switchCenterContent(String panelName) {
        contentArea.getChildren().clear();
        if (panelName.equals("Assigned Orders")) {
            contentArea.getChildren().add(createAssignedOrdersPanel(null));
        } else if (panelName.equals("Same Day Map")) {
            contentArea.getChildren().add(createMapPanel("Same Day"));
        } else if (panelName.equals("Next Day Map")) {
            contentArea.getChildren().add(createMapPanel("Next Day"));
        } else if (panelName.equals("Route Summary")) {
            contentArea.getChildren().add(createRouteSummaryPanel());
        } else if (panelName.equals("Notifications")) {
            // ... already handled by bell but could add a full view here if wanted
        } else if (panelName.equals("Profile")) {
            contentArea.getChildren().add(createProfilePanel());
        }
    }

    private BorderPane createMapPanel(String type) {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(20));
        
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        
        Label title = new Label(type + " Route Viewer");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label info = new Label("Click below to open the " + type + " delivery route in a full-size window.");
        info.setStyle("-fx-text-fill: gray;");
        
        Button launchBtn = new Button("Open " + type + " Map");
        launchBtn.setPrefSize(250, 60);
        launchBtn.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
        
        Button testBtn = new Button("DEBUG: Test Browser (Open Google)");
        testBtn.setOnAction(e -> {
            System.out.println("[DEBUG] Delivery Test Browser button clicked");
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.google.com"));
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        center.getChildren().addAll(title, info, launchBtn, testBtn);
        panel.setCenter(center);

        launchBtn.setOnAction(e -> {
            OrderDAO orderDao = new OrderDAO();
            String activeDate = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), type);
            
            if (activeDate == null) {
                new Alert(Alert.AlertType.INFORMATION, "No undelivered " + type + " assignments found.").show();
                return;
            }

            List<Order> orders = orderDao.getOrdersForRouteBatchByAgent(agent.getAgentId(), type, activeDate);
            
            if (orders.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No orders with valid coordinates in batch " + activeDate).show();
                return;
            }
            
            RouteMapLauncher.openRouteMapInBrowser(orders, "Delivery Agent: " + agent.getFullName(), type + " Batch: " + activeDate);
        });

        return panel;
    }

    private BorderPane createRouteSummaryPanel() {
        BorderPane panel = new BorderPane();
        VBox root = new VBox(20);
        root.setPadding(new Insets(10));
        
        Label title = new Label("Route & Stop Summary");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        DeliveryAssignmentDAO dao = new DeliveryAssignmentDAO();
        List<DeliveryAssignment> assignments = dao.getAssignmentsByAgentId(agent.getAgentId());
        
        VBox summary = new VBox(15);
        OrderDAO orderDao = new OrderDAO();
        
        for (String type : new String[]{"Same Day", "Next Day"}) {
            String activeDate = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), type);
            String displayDate = (activeDate != null) ? activeDate : "None";
            
            Label typeTitle = new Label(type + " Deliveries (Active Batch: " + displayDate + ")");
            typeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3182ce;");
            summary.getChildren().add(typeTitle);
            
            if (activeDate != null) {
                List<Order> orders = orderDao.getOrdersForRouteBatchByAgent(agent.getAgentId(), type, activeDate);
                if (orders.isEmpty()) {
                    summary.getChildren().add(new Label("No orders found in this batch."));
                } else {
                    for (int i = 0; i < orders.size(); i++) {
                        Order o = orders.get(i);
                        HBox row = new HBox(15);
                        row.setStyle("-fx-background-color: #edf2f7; -fx-padding: 10; -fx-background-radius: 5;");
                        Label stop = new Label("Stop " + (i + 1));
                        stop.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
                        Label details = new Label(o.getOrderId() + " | " + o.getCustomerName() + " | " + o.getStatus());
                        row.getChildren().addAll(stop, details);
                        summary.getChildren().add(row);
                    }
                }
            } else {
                summary.getChildren().add(new Label("No " + type + " assignments pending."));
            }
            summary.getChildren().add(new Separator());
        }
        
        root.getChildren().addAll(title, summary);
        panel.setCenter(new ScrollPane(root));
        return panel;
    }

    private BorderPane createAssignedOrdersPanel(String filterType) {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(10));

        OrderDAO orderDao = new OrderDAO();
        String activeSameDay = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), "Same Day");
        String activeNextDay = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), "Next Day");
        
        Label title = new Label("Active Delivery Batches");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subTitle = new Label(String.format("Same Day: %s | Next Day: %s", 
            (activeSameDay != null ? activeSameDay : "None"), 
            (activeNextDay != null ? activeNextDay : "None")));
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        panel.setTop(new VBox(10, title, subTitle));

        TableView<DeliveryAssignment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DeliveryAssignment, String> colOrderId = new TableColumn<>("Order ID");
        colOrderId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));

        TableColumn<DeliveryAssignment, String> colCust = new TableColumn<>("Customer");
        colCust.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerName"));

        TableColumn<DeliveryAssignment, String> colAddr = new TableColumn<>("Address");
        colAddr.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerAddress"));

        TableColumn<DeliveryAssignment, Integer> colEta = new TableColumn<>("ETA (min)");
        colEta.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("etaMinutes"));

        TableColumn<DeliveryAssignment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        TableColumn<DeliveryAssignment, Void> colActions = new TableColumn<>("Action");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Mark Delivered");
            {
                btn.setStyle("-fx-background-color: #38a169; -fx-text-fill: white;");
                btn.setOnAction(e -> {
                    DeliveryAssignment a = getTableView().getItems().get(getIndex());
                    handleMarkDelivered(a, table);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    DeliveryAssignment a = getTableView().getItems().get(getIndex());
                    if ("Delivered".equals(a.getStatus())) {
                        setGraphic(new Label("✅ Delivered"));
                    } else {
                        setGraphic(btn);
                    }
                }
            }
        });

        table.getColumns().addAll(colOrderId, colCust, colAddr, colEta, colStatus, colActions);

        refreshTableData(table, filterType);
        panel.setCenter(table);

        return panel;
    }

    private void refreshTableData(TableView<DeliveryAssignment> table, String filterType) {
        DeliveryAssignmentDAO dao = new DeliveryAssignmentDAO();
        OrderDAO orderDao = new OrderDAO();
        List<DeliveryAssignment> all = dao.getAssignmentsByAgentId(agent.getAgentId());
        List<DeliveryAssignment> filtered = new java.util.ArrayList<>();
        
        for (DeliveryAssignment da : all) {
            String activeBatch = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), da.getDeliveryType());
            Order o = orderDao.getOrderById(da.getOrderId());
            if (o != null && activeBatch != null && activeBatch.equals(o.getScheduledDeliveryDate())) {
                filtered.add(da);
            }
        }
        
        if (filterType != null) {
            filtered = filtered.stream().filter(a -> a.getDeliveryType().equals(filterType)).collect(Collectors.toList());
        }
        table.setItems(javafx.collections.FXCollections.observableArrayList(filtered));
    }

    private void handleMarkDelivered(DeliveryAssignment a, TableView<DeliveryAssignment> table) {
        DeliveryAssignmentDAO dao = new DeliveryAssignmentDAO();
        if (dao.updateAssignmentStatus(a.getAssignmentId(), a.getOrderId(), "Delivered")) {
            // Notifications
            NotificationService.getInstance().sendToAdmins(
                "Order Delivered",
                "Order " + a.getOrderId() + " delivered by " + agent.getFullName(),
                a.getOrderId()
            );
            
            // Get customer email reliably
            String customerEmail = new OrderDAO().getCustomerEmailByOrderId(a.getOrderId());

            if (customerEmail != null) {
                NotificationService.getInstance().sendToCustomer(
                    customerEmail,
                    "Order Delivered",
                    "Your order " + a.getOrderId() + " has been delivered.",
                    a.getOrderId()
                );
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order " + a.getOrderId() + " marked as delivered.");
            refreshTableData(table, a.getDeliveryType());
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
        }
    }

    private VBox createProfilePanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        Label title = new Label("Agent Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        
        grid.add(new Label("Full Name:"), 0, 0); grid.add(new Label(agent.getFullName()), 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(new Label(agent.getEmail()), 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(new Label(agent.getPhone()), 1, 2);
        grid.add(new Label("Vehicle:"), 0, 3); grid.add(new Label(agent.getVehicleType()), 1, 3);
        grid.add(new Label("Area:"), 0, 4); grid.add(new Label(agent.getAssignedArea()), 1, 4);

        panel.getChildren().addAll(title, grid);
        return panel;
    }

    private void handleLogout() {
        stage.close();
        Platform.runLater(() -> {
            try {
                new SuperMartMain().start(new Stage());
            } catch (Exception ex) {}
        });
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.show();
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
}
