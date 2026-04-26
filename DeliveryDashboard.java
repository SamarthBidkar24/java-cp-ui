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
        header.getStyleClass().add("header-panel");
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(2);
        Label welcome = new Label("SuperMart Delivery");
        welcome.getStyleClass().add("header-title");
        Label agentLabel = new Label("Agent: " + agent.getFullName());
        agentLabel.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(welcome, agentLabel);
        
        bellBtn = NotificationUI.createNotificationBell(Notification.UserType.DELIVERY_GUY, agent.getEmail());
        
        header.getChildren().addAll(titleBox, new Region(), bellBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        root.setTop(header);

        // Setup Live Notifications
        setupNotifications();

        // 2. LEFT area: Sidebar
        VBox sidebar = new VBox(5);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);

        Label navLabel = new Label("LOGISTICS NAV");
        navLabel.setStyle("-fx-text-fill: #37B7C3; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 10 20 10 20;");
        sidebar.getChildren().add(navLabel);

        String[] navButtons = {
                "Assigned Orders",
                "Same Day Map",
                "Next Day Map",
                "Route Summary",
                "Profile",
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
                    sidebar.getChildren().forEach(node -> node.getStyleClass().remove("active"));
                    btn.getStyleClass().add("active");
                }
            });
            sidebar.getChildren().add(btn);
        }
        root.setLeft(sidebar);

        // 3. CENTER area: Content Area
        contentArea = new StackPane();
        switchCenterContent("Assigned Orders");
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1200, 850);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
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

    private ScrollPane createMapPanel(String type) {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(40));
        mainContainer.getStyleClass().add("card-panel");
        mainContainer.setAlignment(Pos.CENTER);

        Label title = new Label(type + " Route Logistics");
        title.getStyleClass().add("section-title");
        
        Label info = new Label("Visualize your " + type + " delivery sequence on the interactive map.");
        info.getStyleClass().add("header-subtitle");
        
        Button launchBtn = new Button("Launch Interactive Map");
        launchBtn.setPrefSize(300, 50);
        
        mainContainer.getChildren().addAll(title, info, launchBtn);

        launchBtn.setOnAction(e -> {
            new Thread(() -> {
                OrderDAO orderDao = new OrderDAO();
                String activeDate = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), type);
                
                if (activeDate == null) {
                    javafx.application.Platform.runLater(() -> 
                        new Alert(Alert.AlertType.INFORMATION, "No undelivered " + type + " assignments found.").show());
                    return;
                }

                List<Order> orders = orderDao.getOrdersForRouteBatchByAgent(agent.getAgentId(), type, activeDate);
                
                if (orders.isEmpty()) {
                    javafx.application.Platform.runLater(() -> 
                        new Alert(Alert.AlertType.INFORMATION, "No orders with valid coordinates in batch " + activeDate).show());
                    return;
                }
                
                RouteMapLauncher.openRouteMapInBrowser(orders, "Delivery Agent: " + agent.getFullName(), type + " Batch: " + activeDate);
            }).start();
        });

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
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

    private ScrollPane createAssignedOrdersPanel(String filterType) {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.getStyleClass().add("card-panel");

        OrderDAO orderDao = new OrderDAO();
        String activeSameDay = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), "Same Day");
        String activeNextDay = orderDao.getEarliestPendingDeliveryDateForAgent(agent.getAgentId(), "Next Day");
        
        Label title = new Label("Active Delivery Assignments");
        title.getStyleClass().add("section-title");
        
        Label subTitle = new Label(String.format("Batch Status: Same Day (%s) | Next Day (%s)", 
            (activeSameDay != null ? activeSameDay : "None"), 
            (activeNextDay != null ? activeNextDay : "None")));
        subTitle.getStyleClass().add("header-subtitle");
        subTitle.setStyle("-fx-text-fill: #718096;");

        mainContainer.getChildren().addAll(title, subTitle);

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
        mainContainer.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
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
