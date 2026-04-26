import java.awt.Desktop;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * RouteMapLauncher - Handles generating HTML and opening the route map in the system browser.
 */
public class RouteMapLauncher {

    public static void openRouteMapInBrowser(List<Order> orders, String title, String routeType) {
        System.out.println("[DEBUG] Generating route HTML for: " + title + " (" + routeType + ")");
        try {
            double shopLat = Config.SHOP_LAT;
            double shopLon = Config.SHOP_LON;
            
            System.out.println("[DEBUG_MAP] Using Base Shop Location: Lat=" + shopLat + ", Lon=" + shopLon);

            // Try to get real road route
            String finalGeoJson = null;
            java.util.List<double[]> waypoints = new java.util.ArrayList<>();
            waypoints.add(new double[]{shopLon, shopLat});
            for(Order o : orders) waypoints.add(new double[]{o.getLongitude(), o.getLatitude()});
            
            System.out.println("[DEBUG_MAP] Requesting road-aware route for " + orders.size() + " orders...");
            DirectionsService.RouteResponse res = DirectionsService.getRoute(waypoints);
            if (res.success) {
                finalGeoJson = res.geoJson;
                System.out.println("[DEBUG_MAP] Road-aware route generated successfully (GeoJSON size: " + finalGeoJson.length() + ")");
            } else {
                System.err.println("[DEBUG_MAP] Routing failed: " + res.errorMsg + ". Falling back to direct lines.");
            }

            // Build HTML using the existing generator logic
            String html = MapGenerator.generateMapHtml(orders, shopLat, shopLon, finalGeoJson, routeType);
            
            // Save to temp file - SANITIZE filename prefix (remove colons, spaces etc)
            String safePrefix = ("supermart_route_" + routeType.toLowerCase())
                                .replaceAll("[^a-zA-Z0-9]", "_");
            if (safePrefix.length() > 50) safePrefix = safePrefix.substring(0, 50);

            File tempFile = Files.createTempFile(safePrefix + "_", ".html").toFile();
            tempFile.deleteOnExit();

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8")))) {
                out.println(html);
            }
            
            System.out.println("[DEBUG] Saved route file to: " + tempFile.getAbsolutePath());
            System.out.println("[DEBUG] Opening browser URI: " + tempFile.toURI());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
            } else {
                String err = "Desktop is not supported on this system. Cannot open browser automatically.";
                System.err.println("[ERROR] " + err);
                javafx.application.Platform.runLater(() -> {
                    new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, err + "\nFile: " + tempFile.getAbsolutePath()).show();
                });
            }

        } catch (Exception e) {
            String msg = "Failed to open route map: " + e.getMessage();
            System.err.println("[ERROR] " + msg);
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, msg).show();
            });
        }
    }
}
