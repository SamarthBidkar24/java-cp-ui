import java.io.*;
import java.awt.Desktop;
import java.nio.file.Files;
import java.util.List;

/**
 * MapGenerator - Generates a production-grade Leaflet HTML map and opens it in the system browser.
 */
public class MapGenerator {

    public static void openMap(List<Order> orders, double shopLat, double shopLon, String routeGeoJson, String subtitle) {
        try {
            String html = generateMapHtml(orders, shopLat, shopLon, routeGeoJson, subtitle);
            File tempFile = Files.createTempFile("supermart_route_", ".html").toFile();
            tempFile.deleteOnExit();

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8")))) {
                out.println(html);
            }

            Desktop.getDesktop().browse(tempFile.toURI());
            System.out.println("[INFO] Map export successful: " + tempFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate map export: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String generateMapHtml(List<Order> orders, double shopLat, double shopLon, String routeGeoJson, String subtitle) {
        // 1. Build Side Panel Stops with interaction
        StringBuilder sidePanelHtml = new StringBuilder();
        sidePanelHtml.append("<div class='stop-item shop' onclick='flyToShop()'>")
                     .append("<div class='stop-num'>0</div>")
                     .append("<div class='stop-info'><strong>SUPERMART WAREHOUSE</strong><br><span class='meta'>Base Location</span></div></div>");
        
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            String escAddr = (o.getAddress() != null ? o.getAddress() : "No Address").replace("'", "\\'");
            String escName = (o.getCustomerName() != null ? o.getCustomerName() : "Unknown").replace("'", "\\'");
            String pin = o.getPincode() != null ? o.getPincode() : "N/A";
            String status = o.getStatus() != null ? o.getStatus() : "Pending";
            
            String sDate = o.getScheduledDeliveryDate() != null ? o.getScheduledDeliveryDate() : "N/A";
            
            sidePanelHtml.append("<div class='stop-item' onclick='flyToStop(").append(i).append(")'>")
                .append("<div class='stop-num'>").append(i + 1).append("</div>")
                .append("<div class='stop-info'>")
                .append("<strong>").append(escName).append("</strong><br>")
                .append("<span class='meta'>ID: ").append(o.getOrderId()).append(" | Pin: ").append(pin).append("</span><br>")
                .append("<span class='meta'>📅 Delivery: ").append(sDate).append("</span><br>")
                .append("<span class='meta'>📞 ").append(o.getCustomerPhone()).append("</span><br>")
                .append("<span class='status-badge'>").append(status).append("</span>")
                .append("<span class='addr'>").append(escAddr).append("</span>")
                .append("</div></div>");
        }

        // 2. Build Markers and Path JS
        StringBuilder jsLogic = new StringBuilder();
        jsLogic.append("var markers = [];\n");
        jsLogic.append("var shopMarker = L.marker([").append(shopLat).append(", ").append(shopLon).append("], {icon: greenIcon})")
               .append(".addTo(map).bindPopup('<b>SuperMart Warehouse</b>');\n");
        
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            String popup = "<b>Stop " + (i + 1) + "</b><br>" +
                           "<b>Order:</b> " + o.getOrderId() + "<br>" +
                           "<b>Cust:</b> " + (o.getCustomerName() != null ? o.getCustomerName().replace("'", "\\'") : "Unknown") + "<br>" +
                           "<b>Addr:</b> " + (o.getAddress() != null ? o.getAddress().replace("'", "\\'") : "N/A");
            jsLogic.append("var m").append(i).append(" = L.marker([").append(o.getLatitude()).append(", ").append(o.getLongitude()).append("])")
                   .append(".addTo(map).bindPopup('").append(popup).append("');\n")
                   .append("markers.push(m").append(i).append(");\n");
        }

        // 3. Complete HTML Template
        return "<!DOCTYPE html><html><head>" +
            "<title>Delivery Route Planner</title>" +
            "<meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
            "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap' rel='stylesheet'>" +
            "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
            "<style>" +
            "  :root { --sidebar-bg: #1a202c; --item-bg: #2d3748; --accent: #4299e1; --shop-accent: #48bb78; }" +
            "  body { margin: 0; display: flex; height: 100vh; font-family: 'Inter', sans-serif; background: #f7fafc; overflow: hidden; }" +
            "  #sidebar { width: 380px; background: var(--sidebar-bg); color: white; display: flex; flex-direction: column; box-shadow: 4px 0 15px rgba(0,0,0,0.3); z-index: 1000; }" +
            "  .sidebar-header { padding: 24px; background: #111; border-bottom: 1px solid #333; }" +
            "  .sidebar-header h2 { margin: 0; font-size: 1.5rem; color: var(--accent); letter-spacing: -0.5px; }" +
            "  .stop-list { flex: 1; overflow-y: auto; padding: 20px; scrollbar-width: thin; scrollbar-color: #4a5568 transparent; }" +
            "  .stop-item { display: flex; align-items: flex-start; gap: 14px; background: var(--item-bg); margin-bottom: 15px; padding: 18px; border-radius: 12px; cursor: pointer; transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); border-left: 5px solid var(--accent); }" +
            "  .stop-item:hover { transform: scale(1.02); background: #3a475e; box-shadow: 0 4px 12px rgba(0,0,0,0.2); }" +
            "  .stop-item.shop { border-left-color: var(--shop-accent); background: #22543d; }" +
            "  .stop-num { background: #000; color: white; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem; flex-shrink: 0; border: 2px solid var(--accent); }" +
            "  .stop-item.shop .stop-num { border-color: var(--shop-accent); }" +
            "  .stop-info { font-size: 0.95rem; line-height: 1.5; }" +
            "  .meta { color: #a0aec0; font-size: 0.8rem; font-weight: 500; }" +
            "  .status-badge { display: inline-block; padding: 2px 8px; background: var(--accent); color: white; border-radius: 4px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; margin: 4px 0; }" +
            "  .addr { display: block; margin-top: 6px; color: #cbd5e0; font-size: 0.85rem; font-style: italic; }" +
            "  #map { flex: 1; }" +
            "  .leaflet-popup-content-wrapper { border-radius: 12px; font-family: inherit; padding: 5px; }" +
            "  .leaflet-popup-content { font-size: 0.9rem; }" +
            "  .stop-list::-webkit-scrollbar { width: 6px; }" +
            "  .stop-list::-webkit-scrollbar-thumb { background: #4a5568; border-radius: 10px; }" +
            "</style></head><body>" +
            "<div id='sidebar'>" +
            "  <div class='sidebar-header'><h2>" + (subtitle != null ? subtitle : "Delivery Route") + "</h2><div style='color:#718096; font-size:0.8rem;'>SuperMart Logistics Dashboard</div></div>" +
            "  <div class='stop-list'>" + sidePanelHtml.toString() + "</div>" +
            "</div>" +
            "<div id='map'></div>" +
            "<script>" +
            "var map = L.map('map').setView([" + shopLat + ", " + shopLon + "], 13);" +
            "L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '&copy; OpenStreetMap' }).addTo(map);" +
            "var greenIcon = new L.Icon({iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png', iconSize: [25, 41], iconAnchor: [12, 41]});" +
            jsLogic.toString() +
            "function flyToShop() { map.flyTo([" + shopLat + ", " + shopLon + "], 16); shopMarker.openPopup(); }" +
            "function flyToStop(idx) { var m = markers[idx]; map.flyTo(m.getLatLng(), 16); m.openPopup(); }" +
            (routeGeoJson != null && !routeGeoJson.isEmpty() ? 
                "var routeData = " + routeGeoJson + "; var layer = L.geoJSON(routeData, {style: {color: '#3182ce', weight: 6, opacity: 0.8}}).addTo(map); map.fitBounds(layer.getBounds(), {padding: [50, 50]});" :
                "var coords = [[" + shopLat + "," + shopLon + "]," + 
                orders.stream().map(o -> "[" + o.getLatitude() + "," + o.getLongitude() + "]").collect(java.util.stream.Collectors.joining(",")) + 
                "]; var poly = L.polyline(coords, {color: '#3182ce', weight: 5, dashArray: '10, 10', opacity: 0.6}).addTo(map); map.fitBounds(poly.getBounds(), {padding: [50, 50]});") +
            "</script></body></html>";
    }
}
