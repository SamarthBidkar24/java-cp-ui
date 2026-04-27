import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.List;

/**
 * Utility class for Notification UI components.
 */
public class NotificationUI {

    public static Button createNotificationBell(Notification.UserType type, String targetUser) {
        Button bellBtn = new Button("🔔");
        bellBtn.getStyleClass().add("bell-btn");
        bellBtn.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand;");

        updateBellBadge(bellBtn, type, targetUser);

        bellBtn.setOnAction(e -> showNotificationPopup(bellBtn, type, targetUser));

        return bellBtn;
    }

    public static void updateBellBadge(Button bellBtn, Notification.UserType type, String targetUser) {
        List<Notification> notifications = NotificationService.getInstance().getNotificationsForUser(type, targetUser);
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        
        Platform.runLater(() -> {
            if (unreadCount > 0) {
                bellBtn.setText("🔔 (" + unreadCount + ")");
                bellBtn.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #e53e3e; -fx-font-weight: bold;");
            } else {
                bellBtn.setText("🔔");
                bellBtn.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #4a5568;");
            }
        });
    }

    public static void showNotificationPopup(Button bellButton, Notification.UserType type, String targetUser) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5);");
        root.setPrefWidth(350);
        root.setPrefHeight(450);

        Label title = new Label("Notifications");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Button markAllRead = new Button("Mark all as read");
        markAllRead.setStyle("-fx-background-color: transparent; -fx-text-fill: #3182ce; -fx-cursor: hand; -fx-font-size: 12px;");
        
        HBox header = new HBox(title, new Region(), markAllRead);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        
        ListView<Notification> listView = new ListView<>();
        listView.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        listView.setCellFactory(param -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox box = new VBox(5);
                    box.setPadding(new Insets(8));
                    
                    Label t = new Label(n.getTitle());
                    t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; " + (n.isRead() ? "-fx-text-fill: #4a5568;" : "-fx-text-fill: #2d3748;"));
                    
                    Label m = new Label(n.getMessage());
                    m.setWrapText(true);
                    m.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
                    
                    Label d = new Label(n.getCreatedAt());
                    d.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");
                    
                    box.getChildren().addAll(t, m, d);
                    
                    if (!n.isRead()) {
                        box.setStyle("-fx-background-color: #ebf8ff; -fx-background-radius: 8;");
                    } else {
                        box.setStyle("-fx-background-color: transparent;");
                    }
                    
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2 0;");
                }
            }
        });

        List<Notification> notifications = NotificationService.getInstance().getNotificationsForUser(type, targetUser);
        listView.getItems().addAll(notifications);
        
        markAllRead.setOnAction(e -> {
            NotificationService.getInstance().markAllAsRead(type, targetUser);
            listView.getItems().forEach(n -> n.setRead(true));
            listView.refresh();
            updateBellBadge(bellButton, type, targetUser);
        });

        listView.setOnMouseClicked(e -> {
            Notification selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead()) {
                NotificationService.getInstance().markAsRead(selected.getNotificationId());
                selected.setRead(true);
                listView.refresh();
                updateBellBadge(bellButton, type, targetUser);
            }
        });

        if (notifications.isEmpty()) {
            VBox emptyBox = new VBox(10, new Label("No notifications yet"), new Label("We'll notify you when something happens."));
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 50 0;");
            emptyBox.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #a0aec0;");
            emptyBox.getChildren().get(1).setStyle("-fx-font-size: 12px; -fx-text-fill: #cbd5e0;");
            root.getChildren().addAll(header, emptyBox);
        } else {
            root.getChildren().addAll(header, listView);
        }
        
        popup.getContent().add(root);
        
        javafx.geometry.Bounds bounds = bellButton.localToScreen(bellButton.getBoundsInLocal());
        popup.show(bellButton, bounds.getMinX() - 310, bounds.getMaxY() + 10);
    }

    public static void showToast(Stage owner, String title, String message) {
        Popup toast = new Popup();
        toast.setAutoHide(true);
        
        VBox root = new VBox(5);
        root.setPadding(new Insets(15, 20, 15, 20));
        root.setStyle("-fx-background-color: #2d3748; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        root.setMinWidth(250);
        root.setMaxWidth(400);

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label m = new Label(message);
        m.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");
        m.setWrapText(true);
        
        root.getChildren().addAll(t, m);
        toast.getContent().add(root);
        
        toast.show(owner);
        
        // Position at bottom right
        double x = owner.getX() + owner.getWidth() - root.getBoundsInParent().getWidth() - 30;
        double y = owner.getY() + owner.getHeight() - root.getBoundsInParent().getHeight() - 50;
        toast.setX(x);
        toast.setY(y);

        // Auto hide after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
            Platform.runLater(toast::hide);
        }).start();
    }
}
