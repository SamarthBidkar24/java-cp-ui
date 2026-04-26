import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import java.util.List;

/**
 * Reusable RouteMapComponent using WebView and Leaflet.
 */
public class RouteMapComponent extends VBox {

    private final WebView webView = new WebView();
    private final Label statusLabel = new Label("Ready to load map...");
    private double shopLat = Config.SHOP_LAT;
    private double shopLon = Config.SHOP_LON;

    public RouteMapComponent() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        VBox.setVgrow(webView, Priority.ALWAYS);

        statusLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        
        HBox controls = new HBox(15);
        controls.getChildren().add(statusLabel);
        
        webView.setMinHeight(400);
        this.getChildren().addAll(controls, webView);
        
        // Auto-locate shop
        // Removed IP override to strictly use Config.SHOP_LAT/LON
    }

    public void loadRoute(List<Order> orders, String routeGeoJson) {
        if (orders.isEmpty()) {
            Platform.runLater(() -> {
                webView.getEngine().loadContent("<html><body><h3 style='font-family:sans-serif; text-align:center; padding-top:100px; color:#a0aec0;'>No assigned orders for this map.</h3></body></html>");
                statusLabel.setText("No orders to display.");
            });
            return;
        }

        statusLabel.setText("Optimizing " + orders.size() + " stops...");
        
        new Thread(() -> {
            List<Order> optimized = new RoutePlanner().optimizeSequence(orders, shopLat, shopLon);
            
            String finalGeoJson = routeGeoJson;
            if (finalGeoJson == null) {
                java.util.List<double[]> waypoints = new java.util.ArrayList<>();
                waypoints.add(new double[]{shopLon, shopLat});
                for(Order o : optimized) waypoints.add(new double[]{o.getLongitude(), o.getLatitude()});
                
                DirectionsService.RouteResponse res = DirectionsService.getRoute(waypoints);
                if (res.success) finalGeoJson = res.geoJson;
            }

            final String html = MapGenerator.generateMapHtml(optimized, shopLat, shopLon, finalGeoJson, "Delivery Route");
            Platform.runLater(() -> {
                System.out.println("[DEBUG_MAP] Loading HTML for " + optimized.size() + " orders into WebView.");
                webView.getEngine().loadContent(html);
                statusLabel.setText("✅ Map loaded with " + optimized.size() + " stops.");
            });
        }).start();
    }
}
