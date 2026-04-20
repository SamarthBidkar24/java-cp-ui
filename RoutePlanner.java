import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {

    public List<Order> optimizeSequence(List<Order> orders, double startLat, double startLon) {
        List<Order> unvisited = new ArrayList<>(orders);
        List<Order> optimized = new ArrayList<>();
        
        double currentLat = startLat;
        double currentLng = startLon;

        while (!unvisited.isEmpty()) {
            Order nearest = null;
            double minDist = Double.MAX_VALUE;

            for (Order o : unvisited) {
                double d = calculateHaversine(currentLat, currentLng, o.getLatitude(), o.getLongitude());
                if (d < minDist) {
                    minDist = d;
                    nearest = o;
                }
            }

            if (nearest != null) {
                optimized.add(nearest);
                unvisited.remove(nearest);
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
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
