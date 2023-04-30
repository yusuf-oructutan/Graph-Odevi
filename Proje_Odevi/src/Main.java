import java.util.*;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class DeliveryPlan {
    // Your Google Maps API key
    private static final String API_KEY = "YOUR_API_KEY";

    // İhtiyaç noktalarının koordinatlarını ve stoklarını tutmak için bir sınıf oluşturun
    static class Location {
        double lat;
        double lng;
        int priority;
        int stock;

        Location(double lat, double lng, int priority, int stock) {
            this.lat = lat;
            this.lng = lng;
            this.priority = priority;
            this.stock = stock;
        }
    }

    // Google Maps API kullanarak iki nokta arasındaki gerçek mesafeyi hesaplayın
    public static double getRealDistance(Location origin, Location destination) throws Exception {
        GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).build();

        LatLng[] origins = {new LatLng(origin.lat, origin.lng)};
        LatLng[] destinations = {new LatLng(destination.lat, destination.lng)};

        DistanceMatrix distanceMatrix = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations)
                .mode(TravelMode.DRIVING) // Harita üzerindeki karayolu rotasını kullanarak mesafeyi hesaplar
                .await();

        return distanceMatrix.rows[0].elements[0].distance.inMeters;
    }

    // Dijkstra algoritması kullanarak en kısa yol bulma
    public static List<Location> findShortestPath(Location start, List<Location> locations) throws Exception {
        Map<Location, Double> distances = new HashMap<>();
        Map<Location, Location> previous = new HashMap<>();
        PriorityQueue<Location> queue = new PriorityQueue<>((a, b) -> Double.compare(distances.get(a), distances.get(b)));

        // Başlangıç noktasına mesafeyi sıfırlayın ve kuyruğa ekleyin
        distances.put(start, 0.0);
        queue.offer(start);

        // Düğümleri ziyaret edin ve mesafeleri güncelleyin
        while (!queue.isEmpty()) {
            Location current = queue.poll();
            for (Location neighbor : locations) {
                if (current.equals(neighbor)) continue;

                // İki nokta arasındaki mesafeyi hesaplayın
                double distance = getRealDistance(current, neighbor);

                // Öncelik ve stok dikkate alınarak ihtiyaç noktalarının önceliği hesaplanır
                int priority = neighbor.priority;
                if (neighbor.stock == 0) priority += 10;
                else if (neighbor.stock < priority) priority = neighbor.stock;

                // Yeni mesafeyi hesaplayın
                double newDistance = distances.get(current) + distance * priority;

                if (!distances.containsKey(neighbor) || newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        // En kısa yolun listesini oluşturun
        List<Location> path = new ArrayList<>();
        Location current = locations.get(0);
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) throws Exception {
// İhtiyaç noktalarını ve stoklarını belirleyin
        Location loc1 = new Location(37.0033, 35.3289, 1, 100);
        Location loc2 = new Location(37.0037, 35.3254, 2, 100);
        Location loc3 = new Location(37.0014, 35.3294, 3, 70);
        Location loc4 = new Location(37.0016, 35.3267, 4, 70);
        Location loc5 = new Location(36.9972, 35.3343, 5, 60);
        Location loc6 = new Location(36.9965, 35.3305, 6, 50);
        Location loc7 = new Location(36.9948, 35.3262, 7, 40);
        Location loc8 = new Location(36.9943, 35.3299, 8, 30);
        Location loc9 = new Location(36.9918, 35.3306, 9, 20);
        Location loc10 = new Location(36.9906, 35.3276, 10, 10);
// İhtiyaç noktalarını bir liste olarak kaydedin
        List<Location> locations = new ArrayList<>();
        locations.add(loc1);
        locations.add(loc2);
        locations.add(loc3);
        locations.add(loc4);
        locations.add(loc5);
        locations.add(loc6);
        locations.add(loc7);
        locations.add(loc8);
        locations.add(loc9);
        locations.add(loc10);

// Başlangıç noktasını belirleyin
        Location start = new Location(37.0012, 35.3213, 0, 0);

// En kısa yolu bulun
        List<Location> path = findShortestPath(start, locations);

// Yolu ekrana yazdırın
        for (Location location : path) {
            System.out.println(location.lat + "," + location.lng);
        }
    }
}