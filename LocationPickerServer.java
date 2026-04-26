import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * A Singleton internal HTTP server to receive location data from the external browser.
 * Prevents "Address already in use" errors by reusing the same server instance.
 */
public class LocationPickerServer {

    public static class PickerResult {
        public double lat, lon;
        public String address;
    }

    private static LocationPickerServer instance;
    private HttpServer server;
    private Consumer<PickerResult> currentCallback;

    private LocationPickerServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/confirm", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery();
                    if (query == null) return;

                    PickerResult res = new PickerResult();
                    String[] params = query.split("&");
                    for (String p : params) {
                        String[] kv = p.split("=");
                        if (kv.length < 2) continue;
                        String key = kv[0];
                        String val = java.net.URLDecoder.decode(kv[1], "UTF-8");
                        if (key.equals("lat")) res.lat = Double.parseDouble(val);
                        else if (key.equals("lon")) res.lon = Double.parseDouble(val);
                        else if (key.equals("addr")) res.address = val;
                    }

                    if (currentCallback != null) {
                        currentCallback.accept(res);
                    }

                    String response = "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>" +
                                      "<h2 style='color:green;'>Location Confirmed!</h2>" +
                                      "<p>You can now close this browser tab and return to the SuperMart app.</p>" +
                                      "</body></html>";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("[INFO] Persistent Location Server started on port 8080");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to start Location Server: " + e.getMessage());
        }
    }

    public static synchronized void start(Consumer<PickerResult> onResult) {
        if (instance == null) {
            instance = new LocationPickerServer();
        }
        instance.currentCallback = onResult;
    }

    public static synchronized void stop() {
        if (instance != null && instance.server != null) {
            instance.server.stop(0);
            instance = null;
        }
    }
}
