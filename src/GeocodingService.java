import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Service to validate and geocode addresses using OpenRouteService.
 */
public class GeocodingService {

    private static final String GEOCODE_URL = "https://api.openrouteservice.org/geocode/search/structured";

    public static class GeocodeResponse {
        public double lat, lon;
        public String label;
        public boolean success = false;
        public String errorMsg = "";
    }

    public static GeocodeResponse validateAddress(String house, String street, String city, String state, String pincode) {
        GeocodeResponse response = new GeocodeResponse();
        try {
            StringBuilder query = new StringBuilder(GEOCODE_URL + "?api_key=" + Config.ORS_API_KEY);
            if (street != null && !street.isEmpty()) query.append("&address=").append(URLEncoder.encode(street, "UTF-8"));
            if (city != null && !city.isEmpty()) query.append("&locality=").append(URLEncoder.encode(city, "UTF-8"));
            if (state != null && !state.isEmpty()) query.append("&region=").append(URLEncoder.encode(state, "UTF-8"));
            if (pincode != null && !pincode.isEmpty()) query.append("&postalcode=").append(URLEncoder.encode(pincode, "UTF-8"));
            query.append("&country=India&size=1");

            URL url = new URL(query.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) content.append(line);
                in.close();

                String json = content.toString();
                if (json.contains("\"coordinates\":[")) {
                    int cIdx = json.indexOf("\"coordinates\":[") + 15;
                    String coordsStr = json.substring(cIdx, json.indexOf("]", cIdx));
                    String[] parts = coordsStr.split(",");
                    response.lon = Double.parseDouble(parts[0]);
                    response.lat = Double.parseDouble(parts[1]);
                    
                    int lIdx = json.indexOf("\"label\":\"") + 9;
                    response.label = json.substring(lIdx, json.indexOf("\"", lIdx));
                    response.success = true;
                } else {
                    response.errorMsg = "No matching location found.";
                }
            } else if (code == 401 || code == 403) {
                response.errorMsg = "Invalid API Key.";
            } else {
                response.errorMsg = "API Error (Code: " + code + ")";
            }
        } catch (SocketTimeoutException e) {
            response.errorMsg = "Connection Timeout.";
        } catch (Exception e) {
            response.errorMsg = "Network Error: " + e.getMessage();
        }
        return response;
    }

    public static String reverseGeocode(double lat, double lon) {
        try {
            // ORS expects [longitude, latitude]
            String query = "https://api.openrouteservice.org/geocode/reverse?api_key=" + Config.ORS_API_KEY +
                           "&point.lon=" + lon + "&point.lat=" + lat + "&size=1";
            
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) content.append(line);
                in.close();

                String json = content.toString();
                if (json.contains("\"label\":\"")) {
                    int idx = json.indexOf("\"label\":\"") + 9;
                    return json.substring(idx, json.indexOf("\"", idx));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }

    /**
     * Helper to detect shop location based on external IP.
     */
    public static double[] getIPLocation() {
        try {
            URL url = new URL("http://ip-api.com/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json = in.readLine();
            in.close();
            if (json.contains("\"lat\":")) {
                double lat = Double.parseDouble(json.substring(json.indexOf("\"lat\":") + 6, json.indexOf(",", json.indexOf("\"lat\":"))));
                double lon = Double.parseDouble(json.substring(json.indexOf("\"lon\":") + 6, json.indexOf(",", json.indexOf("\"lon\":"))));
                return new double[]{lat, lon};
            }
        } catch (Exception e) {}
        return null;
    }
}
