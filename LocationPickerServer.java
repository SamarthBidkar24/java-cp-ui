import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * A simple internal HTTP server to receive location data from the external browser.
 */
public class LocationPickerServer {

    public static class PickerResult {
        public double lat, lon;
        public String address;
    }

    private HttpServer server;
    private final Consumer<PickerResult> onResult;

    public LocationPickerServer(Consumer<PickerResult> onResult) {
        this.onResult = onResult;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/confirm", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                // Query format: lat=...&lon=...&addr=...
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

                onResult.accept(res);

                String response = "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>" +
                                  "<h2 style='color:green;'>Location Confirmed!</h2>" +
                                  "<p>You can now close this browser tab and return to the SuperMart app.</p>" +
                                  "</body></html>";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                
                // Stop server after a short delay
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (Exception e) {}
                    stop();
                }).start();
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("[INFO] Location Callback Server started on port 8080");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}
