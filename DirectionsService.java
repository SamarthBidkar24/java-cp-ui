import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Hardened Service for OpenRouteService Directions API.
 */
public class DirectionsService {

    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

    public static class RouteResponse {
        public String geoJson;
        public boolean success = false;
        public String errorMsg = "";
    }

    public static RouteResponse getRoute(List<double[]> waypoints) {
        RouteResponse response = new RouteResponse();
        
        System.out.println("\n--- [DEBUG] STARTING ROUTING REQUEST ---");
        
        try {
            // 1. Validate Coordinates
            if (waypoints == null || waypoints.size() < 2) {
                response.errorMsg = "At least 2 points (start and end) are required.";
                return response;
            }

            for (double[] p : waypoints) {
                if (p == null || p.length < 2) {
                    response.errorMsg = "Malformed coordinates detected.";
                    return response;
                }
                double lon = p[0];
                double lat = p[1];
                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    response.errorMsg = "Invalid coordinates: Lat(" + lat + "), Lon(" + lon + ")";
                    return response;
                }
            }

            // 2. Prepare Request
            URL url = new URL(DIRECTIONS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            
            String maskedKey = Config.ORS_API_KEY != null && Config.ORS_API_KEY.length() > 8 
                               ? Config.ORS_API_KEY.substring(0, 4) + "..." + Config.ORS_API_KEY.substring(Config.ORS_API_KEY.length()-4) 
                               : "HIDDEN/INVALID";
            
            conn.setRequestProperty("Authorization", Config.ORS_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Construct JSON: {"coordinates": [[lon,lat], ...]}
            StringBuilder body = new StringBuilder("{\"coordinates\":[");
            for (int i = 0; i < waypoints.size(); i++) {
                body.append("[").append(waypoints.get(i)[0]).append(",").append(waypoints.get(i)[1]).append("]");
                if (i < waypoints.size() - 1) body.append(",");
            }
            body.append("]}");

            // LOG REQUEST
            System.out.println("URL: " + DIRECTIONS_URL);
            System.out.println("API Key: " + maskedKey);
            System.out.println("Payload: " + body.toString());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes("utf-8"));
            }

            // 3. Handle Response
            int code = conn.getResponseCode();
            System.out.println("HTTP Status Code: " + code);

            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) content.append(line);
                in.close();
                response.geoJson = content.toString();
                response.success = true;
                System.out.println("Response: SUCCESS (GeoJSON Length: " + response.geoJson.length() + ")");
            } else {
                // Parse Error Stream
                StringBuilder errorContent = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) errorContent.append(line);
                } catch (Exception e) {}
                
                String errorBody = errorContent.toString();
                System.out.println("Error Body: " + errorBody);

                if (code == 401) response.errorMsg = "Invalid API Key";
                else if (code == 403) response.errorMsg = "Access Forbidden / Key Issue";
                else if (code == 404) response.errorMsg = "Route not found (point too far from road?)";
                else if (code == 429) response.errorMsg = "Daily Quota Exceeded";
                else if (code >= 500) response.errorMsg = "ORS Service Unavailable (Server Error)";
                else {
                    // Try to extract ORS message: {"error":{"message":"..."}}
                    if (errorBody.contains("\"message\":\"")) {
                        int mIdx = errorBody.indexOf("\"message\":\"") + 11;
                        response.errorMsg = errorBody.substring(mIdx, errorBody.indexOf("\"", mIdx));
                    } else {
                        response.errorMsg = "API Error Code: " + code;
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            response.errorMsg = "Connection Timeout (ORS is slow)";
        } catch (Exception e) {
            response.errorMsg = "Network Error: " + e.getMessage();
            e.printStackTrace();
        }
        
        System.out.println("Final Result: " + (response.success ? "SUCCESS" : "FAILED - " + response.errorMsg));
        System.out.println("--- [DEBUG] END ROUTING REQUEST ---\n");
        return response;
    }
}
