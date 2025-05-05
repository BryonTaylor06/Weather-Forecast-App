import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import com.google.gson.*;

public class WeatherForecast {

    /**
     * This method by default displays the 7-day temps for Bloomington Indiana
     * or the user can change it by using "--latitude" , "--longitude", "--unit"
     * and they can choose either Fahrenheit or Celsius
     *
     * @param args terminal arguments ex. "--latitude" , "--longitude", "--unit"
     */
    public static void main(String[] args) {

        double X = 39.168804;
        double Y = -86.536659;
        String Z = "temperature_2m";
        String W = "fahrenheit";
        String V = "EST";

        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--latitude")) {
                X = Double.parseDouble(args[j + 1]);
                j++;
            } else if (args[j].equals("--longitude")) {
                Y = Double.parseDouble(args[j + 1]);
            } else if (args[j].equals("--unit")) {
                if (args[j + 1].toLowerCase().equals("f")) {
                    W = "fahrenheit";
                } else {
                    W = "celsius";
                }
                j++;
            }
        }


        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?" + "latitude=" + X + "&longitude=" + Y + "&hourly=" + Z + "&temperature_unit=" + W + "&timezone=" + V);

            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.setRequestMethod("GET");

            if (connect.getResponseCode() != 200) {
                throw new IOException();
            }

            BufferedReader read = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            StringBuilder answer = new StringBuilder();

            String info;
            while ((info = read.readLine()) != null) {
                answer.append(info);
            }
            read.close();

            JsonElement elements = JsonParser.parseString(answer.toString());
            JsonObject hourRaw = elements.getAsJsonObject().getAsJsonObject("hourly");
            JsonArray datesAndTimes = hourRaw.getAsJsonArray("time");
            JsonArray temps = hourRaw.getAsJsonArray("temperature_2m");


            if (W.equals("fahrenheit")) { //checks if user used fahrenheit or Celsius
                System.out.println("7-Day Forecast in Fahrenheit:");
            } else {
                System.out.println("7-Day Forecast in Celsius:");
            }

            String currDate = "";
            int totalHoursInAWeek = 7 * 24; // total time in a week

            int i = 0;
            while (i < totalHoursInAWeek && i < datesAndTimes.size()) {
                String datesAndTimesRaw = datesAndTimes.get(i).toString().replace("\"", "");
                double temperature = temps.get(i).getAsDouble();

                String[] datesAndTimesArr = datesAndTimesRaw.split("T");
                String time = datesAndTimesArr[1];
                String date = datesAndTimesArr[0];

                if (date.equals(currDate) == false) {
                    currDate = date;
                    System.out.println("Forecast for " + date + ":");
                }
                if (W.equals("fahrenheit")) { //checks if user used fahrenheit or Celsius
                    System.out.println("    " + time + ": " + temperature + "°F");
                } else {
                    System.out.println("    " + time + ": " + temperature + "°C");
                }
                i += 3; //Update time every 3 hours
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
