import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.regex.Pattern;

/**
 * SuperMartMain - Authentication Window (JavaFX)
 * Migrated from Java Swing.
 */
public class SuperMartMain extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SuperMart Online Ordering System");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("SuperMart Online Ordering");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 10 0 20 0;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        root.setTop(titleBox);

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab loginTab = new Tab("Login", createLoginPanel());
        Tab registerTab = new Tab("Register", createRegisterPanel());

        tabPane.getTabs().addAll(loginTab, registerTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 600, 500);
        // Load professional CSS
        scene.getStylesheets().add(getClass().getResource("auth.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createLoginPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(250);

        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText("Password");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(pwdField, 1, 1);

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);
        Button adminBtn = new Button("Admin Login");
        Button customerBtn = new Button("Customer Login");
        Button deliveryBtn = new Button("Delivery Login");
        
        btnBox.getChildren().addAll(adminBtn, customerBtn, deliveryBtn);

        panel.getChildren().addAll(grid, btnBox);

        // Logic (Unchanged)
        adminBtn.setOnAction(e -> handleLogin(emailField.getText(), pwdField.getText(), "Admin"));
        customerBtn.setOnAction(e -> handleLogin(emailField.getText(), pwdField.getText(), "Customer"));
        deliveryBtn.setOnAction(e -> handleLogin(emailField.getText(), pwdField.getText(), "Delivery"));

        return panel;
    }

    private ScrollPane createRegisterPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        PasswordField pwdField = new PasswordField();
        PasswordField confirmField = new PasswordField();
        grid.add(new Label("Full Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("Password:"), 0, 3); grid.add(pwdField, 1, 3);
        grid.add(new Label("Confirm Password:"), 0, 4); grid.add(confirmField, 1, 4);

        Button registerBtn = new Button("Register as Customer");
        registerBtn.setPrefWidth(200);
        
        
        panel.getChildren().addAll(grid, registerBtn);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String pwd = pwdField.getText();
            String confirm = confirmField.getText();
            String role = "Customer"; // Forced to Customer for public form

            // Validations
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
                return;
            }
            if (!isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
                return;
            }
            if (!pwd.equals(confirm)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match.");
                return;
            }

            CustomerDAO dao = new CustomerDAO();
            if (dao.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Customer email already exists.");
                return;
            }

            // OTP Simulation
            String otp = SecurityUtils.generateOTP();
            System.out.println("[SIMULATED EMAIL] To: " + email + " | Subject: SuperMart Verification | Code: " + otp);
            
            TextInputDialog otpDialog = new TextInputDialog();
            otpDialog.setTitle("Email Verification");
            otpDialog.setHeaderText("Verification code sent to " + email);
            otpDialog.setContentText("Enter 6-digit code (Check console for simulated email):");
            
            java.util.Optional<String> result = otpDialog.showAndWait();
            if (result.isPresent() && result.get().equals(otp)) {
                if (dao.registerCustomer(name, email, phone, pwd)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful! You can now log in.");
                    nameField.clear(); emailField.clear(); phoneField.clear();
                    pwdField.clear(); confirmField.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Registration failed database write.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Verification Failed", "Incorrect OTP. Registration cancelled.");
            }
        });

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void handleLogin(String email, String pwd, String role) {
        if (email.isEmpty() || pwd.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Email and Password are required.");
            return;
        }

        if ("Admin".equals(role)) {
            AdminDAO dao = new AdminDAO();
            Admin admin = dao.adminLogin(email, pwd);
            if (admin != null) {
                new AdminDashboard(admin.getEmail()).startApp();
                primaryStage.hide();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid admin credentials.");
            }
        } else if ("Delivery".equals(role)) {
            DeliveryDAO dao = new DeliveryDAO();
            DeliveryAgent agent = dao.agentLogin(email, pwd);
            if (agent != null) {
                new DeliveryDashboard(agent).startApp();
                primaryStage.close();
            } else {
                // Check if account is pending
                showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid credentials or Account Pending Approval.");
            }
        } else {
            CustomerDAO dao = new CustomerDAO();
            Customer customer = dao.customerLogin(email, pwd);
            if (customer != null) {
                new CustomerDashboard(customer).startApp();
                primaryStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid customer credentials.");
            }
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.show();
    }

    // Support for Dashboard Logout calling setVisible(true)
    public void setVisible(boolean visible) {
        if (visible) {
            Platform.runLater(() -> {
                if (primaryStage != null) primaryStage.show();
                else start(new Stage());
            });
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
