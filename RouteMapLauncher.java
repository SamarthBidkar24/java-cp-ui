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
            double shopLat = 18.5204;
            double shopLon = 73.8567;
            
            // Try to get dynamic shop location
            double[] loc = GeocodingService.getIPLocation();
            if (loc != null) {
                shopLat = loc[0];
                shopLon = loc[1];
            }

            // Try to get real road route
            String finalGeoJson = null;
            java.util.List<double[]> waypoints = new java.util.ArrayList<>();
            waypoints.add(new double[]{shopLon, shopLat});
            for(Order o : orders) waypoints.add(new double[]{o.getLongitude(), o.getLatitude()});
            
            DirectionsService.RouteResponse res = DirectionsService.getRoute(waypoints);
            if (res.success) finalGeoJson = res.geoJson;

            // Build HTML using the existing generator logic
            String html = MapGenerator.generateMapHtml(orders, shopLat, shopLon, finalGeoJson, routeType);
            
            // Save to temp file
            File tempFile = Files.createTempFile("supermart_route_" + routeType.toLowerCase().replace(" ", "_") + "_", ".html").toFile();
            tempFile.deleteOnExit();

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8")))) {
                out.println(html);
            }
            
            System.out.println("[DEBUG] Saved route file to: " + tempFile.getAbsolutePath());
            System.out.println("[DEBUG] Opening browser URI: " + tempFile.toURI());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
            } else {
                System.err.println("[ERROR] Desktop is not supported on this system. Cannot open browser.");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to open route map in browser: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
