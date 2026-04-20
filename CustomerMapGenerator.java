import java.io.*;
import java.awt.Desktop;
import java.nio.file.Files;

/**
 * Generates the external HTML Map Picker for the customer.
 */
public class CustomerMapGenerator {

    public static void openPicker() {
        try {
            File tempFile = Files.createTempFile("supermart_picker_", ".html").toFile();
            tempFile.deleteOnExit();

            String html = "<!DOCTYPE html><html><head>" +
                "<title>Select Delivery Location</title>" +
                "<meta charset='utf-8' />" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "  body { margin: 0; display: flex; flex-direction: column; height: 100vh; font-family: sans-serif; }" +
                "  #header { background: #2d3748; color: white; padding: 15px 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                "  #map { flex: 1; cursor: crosshair; }" +
                "  #footer { background: #f7fafc; padding: 15px 20px; border-top: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }" +
                "  .btn { padding: 10px 20px; border-radius: 6px; border: none; font-weight: bold; cursor: hand; }" +
                "  .btn-primary { background: #3182ce; color: white; }" +
                "  .btn-primary:disabled { background: #a0aec0; cursor: not-allowed; }" +
                "  #status { font-size: 14px; color: #4a5568; max-width: 60%; }" +
                "</style></head><body>" +
                "<div id='header'><strong>📍 SuperMart:</strong> Click on the map to mark your exact delivery point</div>" +
                "<div id='map'></div>" +
                "<div id='footer'>" +
                "  <div id='status'>Please click on the map...</div>" +
                "  <button id='confirmBtn' class='btn btn-primary' disabled onclick='confirmLocation()'>Confirm This Point</button>" +
                "</div>" +
                "<script>" +
                "var map = L.map('map').setView([18.5204, 73.8567], 13);" +
                "L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                "var marker; var selectedLat, selectedLon, selectedAddr;" +
                "var apiKey = '" + Config.ORS_API_KEY + "';" +

                "map.on('click', function(e) {" +
                "  selectedLat = e.latlng.lat; selectedLon = e.latlng.lng;" +
                "  if(marker) map.removeLayer(marker); marker = L.marker(e.latlng).addTo(map);" +
                "  document.getElementById('status').innerText = 'Fetching address...';" +
                "  document.getElementById('confirmBtn').disabled = true;" +
                
                "  // Reverse Geocoding via ORS\n" +
                "  fetch('https://api.openrouteservice.org/geocode/reverse?api_key=' + apiKey + '&point.lon=' + selectedLon + '&point.lat=' + selectedLat + '&size=1')" +
                "    .then(r => r.json()).then(data => {" +
                "      selectedAddr = (data.features && data.features.length > 0) ? data.features[0].properties.label : 'Coordinates: ' + selectedLat.toFixed(5) + ', ' + selectedLon.toFixed(5);" +
                "      document.getElementById('status').innerText = 'Selected: ' + selectedAddr;" +
                "      document.getElementById('confirmBtn').disabled = false;" +
                "    }).catch(err => { " +
                "      selectedAddr = 'Point selected at ' + selectedLat.toFixed(5) + ', ' + selectedLon.toFixed(5);" +
                "      document.getElementById('status').innerText = selectedAddr;" +
                "      document.getElementById('confirmBtn').disabled = false;" +
                "    });" +
                "});" +

                "function confirmLocation() {" +
                "  var url = 'http://localhost:8080/confirm?lat=' + selectedLat + '&lon=' + selectedLon + '&addr=' + encodeURIComponent(selectedAddr);" +
                "  window.location.href = url;" +
                "}" +
                "</script></body></html>";

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8")))) {
                out.println(html);
            }

            Desktop.getDesktop().browse(tempFile.toURI());
        } catch (Exception e) { e.printStackTrace(); }
    }
}
