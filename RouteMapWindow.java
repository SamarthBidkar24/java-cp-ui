import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.util.List;

/**
 * Utility to open the Route Map in a dedicated separate window.
 */
public class RouteMapWindow {

    private static double shopLat = 18.5204;
    private static double shopLon = 73.8567;

    public static void show(String title, List<Order> orders, String routeGeoJson) {
        Stage stage = new Stage();
        stage.setTitle(title + " - SuperMart Delivery Route");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        WebView webView = new WebView();
        VBox.setVgrow(webView, Priority.ALWAYS);

        Label statusLabel = new Label("Preparing map stops...");
        statusLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerTitle = new Label(title);
        headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button refreshBtn = new Button("Refresh Map");
        Button browserBtn = new Button("Open in Browser");
        
        header.getChildren().addAll(headerTitle, statusLabel, new Region(), refreshBtn, browserBtn);
        HBox.setHgrow(header.getChildren().get(2), Priority.ALWAYS);

        root.getChildren().addAll(header, webView);

        Scene scene = new Scene(root, 1100, 800);
        stage.setScene(scene);
        stage.show();

        // Load content
        loadData(webView, statusLabel, orders, routeGeoJson, title);

        refreshBtn.setOnAction(e -> loadData(webView, statusLabel, orders, routeGeoJson, title));
        browserBtn.setOnAction(e -> MapGenerator.openMap(orders, shopLat, shopLon, routeGeoJson, title));
    }

    private static void loadData(WebView webView, Label statusLabel, List<Order> orders, String routeGeoJson, String title) {
        if (orders == null || orders.isEmpty()) {
            webView.getEngine().loadContent("<html><body style='font-family:sans-serif; text-align:center; padding-top:200px;'><h3>No orders available for this route.</h3></body></html>");
            statusLabel.setText("No data.");
            return;
        }

        statusLabel.setText("Optimizing stops...");
        new Thread(() -> {
            // Locate shop if possible
            double[] loc = GeocodingService.getIPLocation();
            if (loc != null) {
                shopLat = loc[0]; shopLon = loc[1];
            }

            List<Order> optimized = new RoutePlanner().optimizeSequence(orders, shopLat, shopLon);
            
            String finalGeoJson = routeGeoJson;
            if (finalGeoJson == null) {
                java.util.List<double[]> waypoints = new java.util.ArrayList<>();
                waypoints.add(new double[]{shopLon, shopLat});
                for(Order o : optimized) waypoints.add(new double[]{o.getLongitude(), o.getLatitude()});
                
                DirectionsService.RouteResponse res = DirectionsService.getRoute(waypoints);
                if (res.success) finalGeoJson = res.geoJson;
            }

            final String html = MapGenerator.generateMapHtml(optimized, shopLat, shopLon, finalGeoJson, title);
            Platform.runLater(() -> {
                webView.getEngine().loadContent(html);
                statusLabel.setText("✅ Map loaded with " + optimized.size() + " stops.");
            });
        }).start();
    }
}
