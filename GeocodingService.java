import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



public class GeocodingService {

    public static void main(String[] args) {
        double[] coords = getCoordinatesFromCityAndState("New York", "NY");
        if (coords != null) {
            System.out.println("Latitude: " + coords[0] + ", Longitude: " + coords[1]);
        } else {
            System.out.println("Location not found");
        }
    }

    public static double[] getCoordinatesFromCityAndState(String city, String state) {
        try {
            String query = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String apiUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + query + "&count=10";
            HttpURLConnection connect = (HttpURLConnection) new URL(apiUrl).openConnection();
            connect.setRequestMethod("GET");

            if (connect.getResponseCode() != 200) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");

            if (results != null && results.size() > 0) {
                // First try to find exact match with city and state
                for (JsonElement el : results) {
                    JsonObject loc = el.getAsJsonObject();
                    if (loc.has("admin1") && loc.get("admin1").getAsString().equalsIgnoreCase(state)) {
                        double lat = loc.get("latitude").getAsDouble();
                        double lon = loc.get("longitude").getAsDouble();
                        return new double[]{lat, lon};
                    }
                }

                // If no exact match, return the first US result
                for (JsonElement el : results) {
                    JsonObject loc = el.getAsJsonObject();
                    if (loc.has("country_code") && loc.get("country_code").getAsString().equalsIgnoreCase("US")) {
                        double lat = loc.get("latitude").getAsDouble();
                        double lon = loc.get("longitude").getAsDouble();
                        return new double[]{lat, lon};
                    }
                }

                // If no US result, return the first result
                JsonObject first = results.get(0).getAsJsonObject();
                return new double[]{
                        first.get("latitude").getAsDouble(),
                        first.get("longitude").getAsDouble()
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}