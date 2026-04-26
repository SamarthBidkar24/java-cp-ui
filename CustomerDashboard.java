import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CustomerDashboard extends Application {

    private Customer customer;
    private StackPane contentArea;
    private Label cartLabel;
    private java.util.List<OrderItem> cartItems = new java.util.ArrayList<>();
    private int cartCount = 0;
    private Stage stage;
    private double validatedLat = 0, validatedLon = 0;
    private String validatedDisplayName = "";
    private String geocodeStatus = "NOT_VALIDATED";
    
    // Payment State
    private boolean isPaymentConfirmed = false;
    private String currentOrderIdForUpi = "";
    private javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView();
    private Button bellBtn;
    private java.util.function.Consumer<Notification> notificationListener;

    public CustomerDashboard() {
    }

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("Customer Dashboard - SuperMart");

        BorderPane root = new BorderPane();

        // 1. TOP area: Header
        HBox header = new HBox();
        header.getStyleClass().add("header-panel");
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(2);
        Label welcome = new Label("SuperMart Customer");
        welcome.getStyleClass().add("header-title");
        Label emailLabel = new Label("Logged in as: " + (customer != null ? customer.getEmail() : "Guest"));
        emailLabel.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(welcome, emailLabel);
        
        bellBtn = NotificationUI.createNotificationBell(Notification.UserType.CUSTOMER, customer.getEmail());
        
        header.getChildren().addAll(titleBox, new Region(), bellBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        
        root.setTop(header);
        
        // Setup Live Notifications
        setupNotifications();

        // 2. LEFT area: Sidebar
        VBox sidebar = new VBox(5);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);

        Label navLabel = new Label("STORE NAVIGATION");
        navLabel.setStyle("-fx-text-fill: #37B7C3; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 10 20 10 20;");
        sidebar.getChildren().add(navLabel);

        String[] navItems = { "Home", "Cart", "Orders", "Profile", "Logout" };

        for (String name : navItems) {
            Button btn = new Button(name);
            btn.getStyleClass().add("sidebar-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            
            if (name.equals("Cart")) {
                cartLabel = new Label("(" + cartCount + ")");
                cartLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                HBox cartBox = new HBox(5, new Label("Cart"), cartLabel);
                cartBox.setAlignment(Pos.CENTER_LEFT);
                btn.setGraphic(cartBox);
                btn.setText("");
            }

            btn.setOnAction(e -> {
                if (name.equals("Logout")) {
                    handleLogout();
                } else {
                    switchPanel(name);
                    sidebar.getChildren().forEach(node -> node.getStyleClass().remove("active"));
                    btn.getStyleClass().add("active");
                }
            });
            sidebar.getChildren().add(btn);
        }
        root.setLeft(sidebar);

        // 3. CENTER area: Content Area
        contentArea = new StackPane();
        switchPanel("Home");
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1200, 850);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void setupNotifications() {
        notificationListener = n -> {
            if (n.getUserType() == Notification.UserType.CUSTOMER && n.getTargetUser().equalsIgnoreCase(customer.getEmail())) {
                NotificationUI.updateBellBadge(bellBtn, Notification.UserType.CUSTOMER, customer.getEmail());
                NotificationUI.showToast(stage, n.getTitle(), n.getMessage());
            }
        };
        NotificationService.getInstance().subscribe(notificationListener);
    }

    private void switchPanel(String name) {
        contentArea.getChildren().clear();
        if (name.equals("Home")) {
            contentArea.getChildren().add(createHomePanel());
        } else if (name.equals("Cart")) {
            contentArea.getChildren().add(createCartPanel());
        } else if (name.equals("Orders")) {
            contentArea.getChildren().add(createOrdersHistoryPanel());
        } else if (name.equals("Profile")) {
            contentArea.getChildren().add(createProfilePanel());
        } else {
            VBox panel = new VBox(20);
            panel.setAlignment(Pos.CENTER);
            Label title = new Label(name + " Screen");
            title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
            Label status = new Label("Functionality for [" + name + "] is preserved.");
            status.setStyle("-fx-text-fill: #718096; -fx-font-style: italic;");
            panel.getChildren().addAll(title, status);
            contentArea.getChildren().add(panel);
        }
    }

    // --- Customer Home Migration ---
    private ScrollPane createHomePanel() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.getStyleClass().add("card-panel");

        Label titleLabel = new Label("Product Catalog");
        titleLabel.getStyleClass().add("section-title");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Search for groceries, electronics, etc...");
        searchField.setPrefWidth(400);

        Button clearBtn = new Button("Clear Search");
        clearBtn.getStyleClass().add("button-secondary");
        clearBtn.setOnAction(e -> searchField.clear());
        
        topBar.getChildren().addAll(new Label("Quick Find:"), searchField, clearBtn);
        mainContainer.getChildren().addAll(titleLabel, topBar);

        FlowPane grid = new FlowPane(20, 20);
        grid.setAlignment(Pos.TOP_LEFT);
        
        mainContainer.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);

        ProductDAO dao = new ProductDAO();
        java.util.List<Product> allProducts = dao.getAllProducts();

        java.util.List<Product> inStockProducts = allProducts.stream()
                .filter(p -> p.getQuantity() > 0)
                .collect(java.util.stream.Collectors.toList());

        renderProducts(grid, inStockProducts);

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            java.util.List<Product> filtered = inStockProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(newV.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            renderProducts(grid, filtered);
        });

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        return scrollPane;
    }

    private void renderProducts(FlowPane grid, java.util.List<Product> products) {
        grid.getChildren().clear();
        for (Product p : products)
            grid.getChildren().add(createProductCard(p));
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-panel");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setMinWidth(220);

        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        imgView.setFitHeight(100);
        imgView.setFitWidth(100);
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            try {
                imgView.setImage(new javafx.scene.image.Image("file:" + p.getImagePath(), 100, 100, true, true));
            } catch (Exception e) {
            }
        }

        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #071952;");
        Label priceLbl = new Label("₹" + p.getPrice());
        priceLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #088395; -fx-font-weight: bold;");
        Label stockLbl = new Label("Available: " + p.getQuantity());
        stockLbl.getStyleClass().add("header-subtitle");

        Button addBtn = new Button("Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            // Group by unique Product ID
            OrderItem existing = cartItems.stream()
                    .filter(oi -> oi.getProduct().getId() == p.getId())
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.qty++;
            } else {
                cartItems.add(new OrderItem(p, 1));
            }

            cartCount++;
            if (cartLabel != null)
                cartLabel.setText("Cart (" + cartCount + ")");
            new Alert(Alert.AlertType.INFORMATION, p.getName() + " added to cart!").show();
        });

        card.getChildren().addAll(imgView, nameLbl, priceLbl, stockLbl, addBtn);
        return card;
    }

    // --- Customer Cart Migration ---
    private BorderPane createCartPanel() {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(10));
        if (cartItems.isEmpty()) {
            panel.setCenter(new Label("Your cart is empty."));
            return panel;
        }

        TableView<OrderItem> table = new TableView<>(javafx.collections.FXCollections.observableArrayList(cartItems));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<OrderItem, String> colName = new TableColumn<>("Product");
        colName.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getName()));

        TableColumn<OrderItem, String> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty("₹" + data.getValue().getProduct().getPrice()));

        TableColumn<OrderItem, Void> colQty = new TableColumn<>("Quantity");
        colQty.setCellFactory(param -> new TableCell<>() {
            private final Button plus = new Button("+");
            private final Button minus = new Button("-");
            private final Label qtyLbl = new Label();
            private final HBox box = new HBox(10, minus, qtyLbl, plus);
            {
                box.setAlignment(Pos.CENTER);
                plus.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    item.qty++;
                    cartCount++;
                    cartLabel.setText("Cart (" + cartCount + ")");
                    getTableView().refresh();
                    updateCartUI(panel, table);
                });
                minus.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    if (item.qty > 1) {
                        item.qty--;
                        cartCount--;
                        cartLabel.setText("Cart (" + cartCount + ")");
                        getTableView().refresh();
                        updateCartUI(panel, table);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    qtyLbl.setText(String.valueOf(getTableView().getItems().get(getIndex()).qty));
                    setGraphic(box);
                }
            }
        });

        TableColumn<OrderItem, Void> colRemove = new TableColumn<>("Action");
        colRemove.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Remove");
            {
                btn.getStyleClass().add("btn-remove");
                btn.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    cartCount -= item.qty;
                    cartLabel.setText("Cart (" + cartCount + ")");
                    updateCartUI(panel, table);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btn);
            }
        });

        table.getColumns().addAll(colName, colPrice, colQty, colRemove);
        panel.setCenter(table);
        updateCartUI(panel, table);
        return panel;
    }

    private void updateCartUI(BorderPane panel, TableView<OrderItem> table) {
        if (cartItems.isEmpty()) {
            panel.setCenter(new Label("Your cart is empty."));
            panel.setRight(null);
            return;
        }
        table.setItems(javafx.collections.FXCollections.observableArrayList(cartItems));

        VBox summary = new VBox(15);
        summary.setPadding(new Insets(20));
        summary.setPrefWidth(350);
        summary.getStyleClass().add("card");

        double subtotal = cartItems.stream().mapToDouble(oi -> oi.getProduct().getPrice() * oi.qty).sum();
        Label subLbl = new Label("Subtotal: ₹" + String.format("%.2f", subtotal));
        ComboBox<String> deliveryType = new ComboBox<>(
                javafx.collections.FXCollections.observableArrayList("Same Day (+₹50)", "Next Day (+₹30)"));
        deliveryType.getSelectionModel().select(0);

        Label totalLbl = new Label();
        
        // Dynamic Order ID for UPI Note
        if (currentOrderIdForUpi.isEmpty()) {
            currentOrderIdForUpi = "ORD" + (System.currentTimeMillis() % 100000);
        }

        Runnable calcTotal = () -> {
            double charge = deliveryType.getSelectionModel().getSelectedIndex() == 0 ? 50 : 30;
            double grandTotal = subtotal + charge;
            totalLbl.setText("Grand Total: ₹" + String.format("%.2f", grandTotal));
            totalLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Reset payment if total changes
            isPaymentConfirmed = false;
            updateUpiPanel(grandTotal);
        };
        deliveryType.setOnAction(e -> calcTotal.run());
        calcTotal.run();

        Label addrTitle = new Label("1. Manual Address Details");
        addrTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        GridPane addrGrid = new GridPane();
        addrGrid.setHgap(8); addrGrid.setVgap(8);

        TextField houseField = new TextField(); houseField.setPromptText("House/Flat No *");
        TextField streetField = new TextField(); streetField.setPromptText("Street/Area *");
        TextField landmarkField = new TextField(); landmarkField.setPromptText("Landmark (Optional)");
        TextField cityField = new TextField(); cityField.setPromptText("City *");
        TextField stateField = new TextField(); stateField.setPromptText("State *");
        TextField pinField = new TextField(); pinField.setPromptText("Pincode *");

        addrGrid.add(new Label("House:"), 0, 0); addrGrid.add(houseField, 1, 0);
        addrGrid.add(new Label("Street:"), 0, 1); addrGrid.add(streetField, 1, 1);
        addrGrid.add(new Label("Landmark:"), 0, 2); addrGrid.add(landmarkField, 1, 2);
        addrGrid.add(new Label("City:"), 0, 3); addrGrid.add(cityField, 1, 3);
        addrGrid.add(new Label("State:"), 0, 4); addrGrid.add(stateField, 1, 4);
        addrGrid.add(new Label("Pincode:"), 0, 5); addrGrid.add(pinField, 1, 5);

        Label mapTitle = new Label("2. Exact Map Location");
        mapTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-padding: 10 0 0 0;");
        
        Button openMapBtn = new Button("📍 Choose Delivery Point on Map");
        openMapBtn.setMaxWidth(Double.MAX_VALUE);
        openMapBtn.setStyle("-fx-background-color: #edf2f7; -fx-border-color: #cbd5e0; -fx-cursor: hand;");

        Label mapStatus = new Label("No point selected on map");
        mapStatus.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
        mapStatus.setWrapText(true);

        openMapBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    LocationPickerServer.start(res -> {
                        Platform.runLater(() -> {
                            validatedLat = res.lat;
                            validatedLon = res.lon;
                            validatedDisplayName = res.address;
                            geocodeStatus = "SUCCESS";
                            mapStatus.setText("✅ Confirmed: " + res.address);
                            mapStatus.setStyle("-fx-text-fill: #38a169; -fx-font-weight: bold;");
                        });
                    });
                    CustomerMapGenerator.openPicker();
                } catch (Exception ex) {
                    Platform.runLater(() -> 
                        new Alert(Alert.AlertType.ERROR, "Failed to launch map: " + ex.getMessage()).show());
                }
            }).start();
        });

        // Reset logic: if any field changes, we might want to re-validate map point 
        // but for now we just show a warning
        Runnable fieldChangeHandler = () -> {
            if ("SUCCESS".equals(geocodeStatus)) {
                mapStatus.setText("⚠️ Address modified - ensure map point still matches!");
                mapStatus.setStyle("-fx-text-fill: #dd6b20;");
            }
        };

        houseField.textProperty().addListener((o, old, n) -> fieldChangeHandler.run());
        streetField.textProperty().addListener((o, old, n) -> fieldChangeHandler.run());
        cityField.textProperty().addListener((o, old, n) -> fieldChangeHandler.run());

        Button placeOrderBtn = new Button("Confirm & Place Order");
        placeOrderBtn.getStyleClass().add("btn-place-order");
        placeOrderBtn.setMaxWidth(Double.MAX_VALUE);
        placeOrderBtn.setStyle("-fx-font-size: 16px; -fx-padding: 12; -fx-background-color: #3182ce; -fx-text-fill: white;");

        // UPI Payment Section
        VBox upiSection = createUpiSection(subtotal + (deliveryType.getSelectionModel().getSelectedIndex() == 0 ? 50 : 30));

        placeOrderBtn.setOnAction(e -> {
            // Validate Manual Fields
            if (houseField.getText().isEmpty() || streetField.getText().isEmpty() || cityField.getText().isEmpty() || pinField.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all mandatory address fields (*)").show();
                return;
            }
            if (!pinField.getText().matches("\\d{6}")) {
                new Alert(Alert.AlertType.WARNING, "Please enter a valid 6-digit Pincode").show();
                return;
            }
            // Validate Map Confirmation
            if (!"SUCCESS".equals(geocodeStatus)) {
                new Alert(Alert.AlertType.WARNING, "Please confirm your exact delivery point on the map.").show();
                return;
            }
            
            // NEW: Validate Payment
            if (!isPaymentConfirmed) {
                new Alert(Alert.AlertType.WARNING, "Please complete the UPI payment and click 'I Have Paid' before placing the order.").show();
                return;
            }

            String fullAddr = houseField.getText() + ", " + streetField.getText() + ", " + landmarkField.getText() + ", " + cityField.getText() + ", " + stateField.getText() + " - " + pinField.getText();
            String orderId = "ORD-" + System.currentTimeMillis();
            double charge = deliveryType.getSelectionModel().getSelectedIndex() == 0 ? 50 : 30;
            String delType = deliveryType.getValue().contains("Same") ? "Same Day" : "Next Day";

            Order newOrder = new Order(customer.getId(), orderId, java.time.LocalDate.now().toString(), "Pending",
                    subtotal + charge, delType, fullAddr, "UPI");
            newOrder.setCustomerEmail(customer.getEmail());
            newOrder.setHouseFlatNo(houseField.getText());
            newOrder.setStreetArea(streetField.getText());
            newOrder.setLandmark(landmarkField.getText());
            newOrder.setCity(cityField.getText());
            newOrder.setState(stateField.getText());
            newOrder.setPincode(pinField.getText());
            newOrder.setFullAddress(fullAddr);
            newOrder.setLatitude(validatedLat);
            newOrder.setLongitude(validatedLon);
            newOrder.setGeocodedDisplayName(validatedDisplayName);
            newOrder.setGeocodeStatus("SUCCESS");

            OrderDAO dao = new OrderDAO();
            if (dao.placeOrder(newOrder, new java.util.ArrayList<>(cartItems))) {
                new Alert(Alert.AlertType.INFORMATION, "Order Placed Successfully!").show();
                cartItems.clear();
                cartCount = 0;
                cartLabel.setText("Cart (0)");
                isPaymentConfirmed = false;
                placeOrderBtn.setDisable(true);
                switchPanel("Orders");
            } else {
                String error = OrderDAO.getLastErrorMessage();
                new Alert(Alert.AlertType.ERROR, "Order Placement Failed\n\nReason: " + error).show();
            }
        });

        summary.getChildren().addAll(new Label("Order Summary"), subLbl, new Label("Delivery:"), deliveryType, totalLbl,
                new Separator(), addrTitle, addrGrid, new Separator(), mapTitle, openMapBtn, mapStatus, new Separator(), upiSection, placeOrderBtn);
        
        ScrollPane summaryScroll = new ScrollPane(summary);
        summaryScroll.setFitToWidth(true);
        summaryScroll.setPrefWidth(370); // Slightly wider than summary to prevent horizontal scroll
        panel.setRight(summaryScroll);
    }

    private Label upiStatusLabel;
    private void updateUpiPanel(double total) {
        if (upiStatusLabel != null) {
            upiStatusLabel.setText("🕒 Waiting for Payment...");
            upiStatusLabel.setStyle("-fx-text-fill: #718096;");
        }
        String upiLink = String.format("upi://pay?pa=%s&pn=%s&am=%.2f&cu=INR&tn=Order_%s", 
            Config.MERCHANT_UPI_ID, Config.MERCHANT_NAME.replace(" ", "%20"), total, currentOrderIdForUpi);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + upiLink;
        qrView.setImage(new javafx.scene.image.Image(qrUrl, true));
    }

    private VBox createUpiSection(double total) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        box.setAlignment(Pos.CENTER);

        Label title = new Label("UPI Payment (ONLY)");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label merchant = new Label(Config.MERCHANT_NAME);
        merchant.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3182ce;");

        Label upiId = new Label(Config.MERCHANT_UPI_ID);
        upiId.setStyle("-fx-font-family: 'Consolas'; -fx-text-fill: #4a5568;");

        qrView.setFitWidth(140);
        qrView.setFitHeight(140);
        
        upiStatusLabel = new Label("🕒 Waiting for Payment...");
        upiStatusLabel.setStyle("-fx-text-fill: #718096; -fx-font-style: italic;");

        Button confirmPayBtn = new Button("I Have Paid");
        confirmPayBtn.setMaxWidth(Double.MAX_VALUE);
        confirmPayBtn.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        confirmPayBtn.setOnAction(e -> {
            isPaymentConfirmed = true;
            upiStatusLabel.setText("✅ Payment Confirmed");
            upiStatusLabel.setStyle("-fx-text-fill: #2f855a; -fx-font-weight: bold;");
            new Alert(Alert.AlertType.INFORMATION, "Payment simulation successful! You can now place the order.").show();
        });

        box.getChildren().addAll(title, merchant, upiId, qrView, upiStatusLabel, confirmPayBtn);
        updateUpiPanel(total);
        return box;
    }

    private void loadInteractiveMap(WebEngine engine, TextField addressField, Label statusLabel) {
        String html = "<!DOCTYPE html><html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "  html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; overflow: hidden; }" +
                "  #map { cursor: crosshair; background: #f8f9fa; }" +
                "</style></head><body>" +
                "<div id='map'></div><script>" +
                "var map = L.map('map', {zoomControl: true}).setView([" + Config.SHOP_LAT + ", " + Config.SHOP_LON + "], 12);" +
                "L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                "var marker;" +
                "function updatePick(lat, lng) {" +
                "  if(marker) map.removeLayer(marker);" +
                "  marker = L.marker([lat, lng]).addTo(map);" +
                "  alert('MAP_PICK:' + lat + ',' + lng);" +
                "}" +
                "map.on('click', function(e) { updatePick(e.latlng.lat, e.latlng.lng); });" +
                "// Critical fix for WebView distortion: call invalidateSize multiple times\n" +
                "window.onload = function() {" +
                "  setTimeout(function(){ map.invalidateSize(); }, 200);" +
                "  setTimeout(function(){ map.invalidateSize(); }, 1000);" +
                "};" +
                "// Try to get user location\n" +
                "if (navigator.geolocation) {" +
                "  navigator.geolocation.getCurrentPosition(function(position) {" +
                "    var pos = [position.coords.latitude, position.coords.longitude];" +
                "    map.setView(pos, 15);" +
                "    updatePick(pos[0], pos[1]);" +
                "  });" +
                "}" +
                "</script></body></html>";

        engine.setOnAlert(event -> {
            String data = event.getData();
            if (data.startsWith("MAP_PICK:")) {
                String[] coords = data.substring(9).split(",");
                validatedLat = Double.parseDouble(coords[0]);
                validatedLon = Double.parseDouble(coords[1]);
                Platform.runLater(() -> {
                    addressField.setText("Lat: " + String.format("%.4f", validatedLat) + ", Lon: "
                            + String.format("%.4f", validatedLon));
                    statusLabel.setText("✅ Location Picked from Map");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            }
        });

        engine.loadContent(html);
    }

    // --- Customer Orders History Migration ---
    private BorderPane createOrdersHistoryPanel() {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(15));
        HBox header = new HBox(15);
        Label title = new Label("Order History");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button allBtn = new Button("All History");
        Button todayBtn = new Button("Today Only");

        header.getChildren().addAll(title, new Region(), allBtn, todayBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        panel.setTop(header);

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Order, String> colId = new TableColumn<>("Order ID");
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
        TableColumn<Order, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));
        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        TableColumn<Order, Double> colTotal = new TableColumn<>("Total ₹");
        colTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));

        table.getColumns().addAll(colId, colDate, colStatus, colTotal);

        OrderDAO dao = new OrderDAO();
        table.setItems(javafx.collections.FXCollections
                .observableArrayList(dao.getOrdersByCustomerEmail(customer.getEmail())));

        allBtn.setOnAction(e -> table.setItems(javafx.collections.FXCollections
                .observableArrayList(dao.getOrdersByCustomerEmail(customer.getEmail()))));
        todayBtn.setOnAction(e -> table.setItems(javafx.collections.FXCollections
                .observableArrayList(dao.getTodaysOrdersForCustomer(customer.getEmail()))));

        table.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()))
                    showOrderDetails(row.getItem());
            });
            return row;
        });

        panel.setCenter(table);
        return panel;
    }

    private void showOrderDetails(Order o) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Reference: " + o.getOrderId());
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setPrefWidth(500);

        Label title = new Label("Order Information");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        grid.add(title, 0, 0, 2, 1);

        grid.add(new Label("Order ID:"), 0, 1);
        Label idVal = new Label(o.getOrderId());
        idVal.setStyle("-fx-font-weight: bold;");
        grid.add(idVal, 1, 1);

        grid.add(new Label("Date:"), 0, 2);
        grid.add(new Label(o.getDate()), 1, 2);

        Label itemsLbl = new Label("Items Purchased:");
        grid.add(itemsLbl, 0, 3);

        // Requirements: Load actual DB items for the dialog
        java.util.List<OrderItem> liveItems = new OrderDAO().getOrderItemsByOrderId(o.getOrderId());
        StringBuilder liveSummary = new StringBuilder();
        for (OrderItem oi : liveItems) {
            if (liveSummary.length() > 0)
                liveSummary.append("\n");
            liveSummary.append("• ").append(oi.getProduct().getName()).append(" x").append(oi.qty);
        }

        Label itemsVal = new Label(liveSummary.length() > 0 ? liveSummary.toString() : "No items found");
        itemsVal.setWrapText(true);
        itemsVal.setMaxWidth(300);
        itemsVal.setStyle(
                "-fx-background-color: #f7fafc; -fx-padding: 10; -fx-border-color: #edf2f7; -fx-font-family: 'Consolas';");
        grid.add(itemsVal, 1, 3);

        grid.add(new Label("Shipping Address:"), 0, 4);
        Label addrVal = new Label(o.getAddress());
        addrVal.setWrapText(true);
        addrVal.setMaxWidth(300);
        grid.add(addrVal, 1, 4);

        grid.add(new Label("Method:"), 0, 5);
        grid.add(new Label(o.getPaymentMethod() + " (UPI)"), 1, 5);

        grid.add(new Label("Order Status:"), 0, 6);
        Label statusVal = new Label(o.getStatus());
        statusVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b6cb0;");
        grid.add(statusVal, 1, 6);

        // NEW: Show ETA if assigned
        DeliveryAssignmentDAO assignmentDao = new DeliveryAssignmentDAO();
        DeliveryAssignment assignment = assignmentDao.getAssignmentByOrderId(o.getOrderId());
        if (assignment != null) {
            grid.add(new Label("Delivery ETA:"), 0, 7);
            Label etaVal = new Label(assignment.getEtaMinutes() + " mins from dispatch");
            etaVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #38a169;");
            grid.add(etaVal, 1, 7);
            
            grid.add(new Label("Delivery Status:"), 0, 8);
            grid.add(new Label(assignment.getStatus()), 1, 8);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    // --- Customer Profile Migration ---
    private VBox createProfilePanel() {
        VBox panel = new VBox(25);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setMaxWidth(500);

        Label title = new Label("Account Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        CustomerDAO dao = new CustomerDAO();
        Customer c = dao.getCustomerById(customer.getId());

        // 1. Details Section
        GridPane details = new GridPane();
        details.setHgap(15);
        details.setVgap(15);
        details.setPadding(new Insets(20));
        details.getStyleClass().add("profile-card");

        details.add(new Label("Full Name:"), 0, 0);
        Label nameVal = new Label(c != null ? c.getName() : "N/A");
        nameVal.setStyle("-fx-font-weight: bold;");
        details.add(nameVal, 1, 0);

        details.add(new Label("Email Address:"), 0, 1);
        Label emailVal = new Label(c != null ? c.getEmail() : "N/A");
        emailVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #718096;");
        details.add(emailVal, 1, 1);

        details.add(new Label("Phone Number:"), 0, 2);
        Label phoneVal = new Label(c != null ? c.getPhone() : "N/A");
        phoneVal.setStyle("-fx-font-weight: bold;");
        details.add(phoneVal, 1, 2);

        // 2. Password Section
        VBox passBox = new VBox(15);
        passBox.setPadding(new Insets(20));
        passBox.getStyleClass().add("profile-card");

        Label passTitle = new Label("Change Password");
        passTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Current Password");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");

        Button updateBtn = new Button("Update Password");
        updateBtn.getStyleClass().add("btn-update-pass");
        updateBtn.setMaxWidth(Double.MAX_VALUE);

        updateBtn.setOnAction(e -> {
            String cur = currentPass.getText();
            String n1 = newPass.getText();
            String n2 = confirmPass.getText();

            // Refresh customer data to verify latest password
            Customer currentDbData = dao.getCustomerById(customer.getId());

            if (currentDbData == null) {
                new Alert(Alert.AlertType.ERROR, "Error loading account data.").show();
            } else if (!cur.equals(currentDbData.getPassword())) {
                new Alert(Alert.AlertType.ERROR, "Incorrect current password!").show();
            } else if (n1.isEmpty() || n1.length() < 4) {
                new Alert(Alert.AlertType.ERROR, "New password must be at least 4 characters.").show();
            } else if (!n1.equals(n2)) {
                new Alert(Alert.AlertType.ERROR, "New passwords do not match!").show();
            } else {
                if (dao.updatePassword(customer.getId(), cur, n1)) {
                    new Alert(Alert.AlertType.INFORMATION, "Password updated successfully!").show();
                    currentPass.clear();
                    newPass.clear();
                    confirmPass.clear();
                }
            }
        });

        passBox.getChildren().addAll(passTitle, currentPass, newPass, confirmPass, updateBtn);

        panel.getChildren().addAll(title, details, passBox);
        return panel;
    }

    private void handleLogout() {
        stage.close();
        Platform.runLater(() -> {
            try {
                new SuperMartMain().start(new Stage());
            } catch (Exception ex) {
            }
        });
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
