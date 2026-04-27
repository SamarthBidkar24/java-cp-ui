import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoutePlanner {

    public List<Order> optimizeSequence(List<Order> orders, double startLat, double startLon) {
        if (orders == null) return new ArrayList<>();
        
        List<Order> unvisited = new ArrayList<>(orders);
        unvisited.removeIf(Objects::isNull);
        
        // Filter out orders that are too far from the warehouse (> 100km) to prevent bad coordinates from breaking routing
        unvisited.removeIf(o -> calculateHaversine(startLat, startLon, o.getLatitude(), o.getLongitude()) > 100.0);

        List<Order> optimized = new ArrayList<>();
        
        double currentLat = startLat;
        double currentLng = startLon;

        while (!unvisited.isEmpty()) {
            Order nearest = null;
            double minDist = Double.MAX_VALUE;

            for (Order o : unvisited) {
                double d = calculateHaversine(currentLat, currentLng, o.getLatitude(), o.getLongitude());
                if (!Double.isNaN(d) && d < minDist) {
                    minDist = d;
                    nearest = o;
                }
            }

            if (nearest != null) {
                optimized.add(nearest);
                unvisited.remove(nearest);
                currentLat = nearest.getLatitude();
                currentLng = nearest.getLongitude();
            } else {
                // Fallback to prevent infinite loop
                nearest = unvisited.get(0);
                optimized.add(nearest);
                unvisited.remove(0);
                currentLat = nearest.getLatitude();
                currentLng = nearest.getLongitude();
            }
        }
        return optimized;
    }

    public double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        // Clamp 'a' to [0, 1] to avoid NaN from Math.sqrt(1 - a) due to precision issues
        a = Math.min(1.0, Math.max(0.0, a));
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

