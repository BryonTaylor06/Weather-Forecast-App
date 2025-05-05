import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class WeatherForecastApp extends JFrame {

    private final JTextField city;
    private final JTextField state;
    private final JTextField latitude;
    private final JTextField longitude;
    private final JComboBox<String> unitCombo;
    private final JTextArea outputArea;

    public WeatherForecastApp() {
        setTitle("Weather Forecast");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(230, 240, 255));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(230, 240, 255));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(new Color(200, 220, 255));
        inputPanel.setOpaque(true);

        inputPanel.add(new JLabel("City:"));
        city = new JTextField();
        city.setBackground(Color.WHITE);
        inputPanel.add(city);

        inputPanel.add(new JLabel("State:"));
        state = new JTextField();
        state.setBackground(Color.WHITE);
        inputPanel.add(state);

        inputPanel.add(new JLabel("Latitude (optional):"));
        latitude = new JTextField("39.168804");
        latitude.setBackground(Color.WHITE);
        inputPanel.add(latitude);

        inputPanel.add(new JLabel("Longitude (optional):"));
        longitude = new JTextField("-86.536659");
        longitude.setBackground(Color.WHITE);
        inputPanel.add(longitude);

        inputPanel.add(new JLabel("Unit:"));
        unitCombo = new JComboBox<>(new String[]{"Fahrenheit", "Celsius"});
        unitCombo.setBackground(Color.WHITE);
        inputPanel.add(unitCombo);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(new Color(240, 248, 255));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(230, 240, 255));
        JButton fetchButton = new JButton("Get Forecast");
        fetchButton.setBackground(new Color(70, 130, 180));
        fetchButton.setForeground(Color.black);
        fetchButton.setFocusPainted(false);
        fetchButton.setPreferredSize(new Dimension(150, 30));
        fetchButton.addActionListener(this::onFetch);
        buttonPanel.add(fetchButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void onFetch(ActionEvent e) {
        outputArea.setText("Fetching forecast...");

        double latitude;
        double longitude;

        String cityInput = city.getText().trim();
        String stateInput = state.getText().trim();

        if (!cityInput.isEmpty() && !stateInput.isEmpty()) {
            double[] coords = GeocodingService.getCoordinatesFromCityAndState(cityInput, stateInput);
            if (coords == null) {
                outputArea.setText("Could not find location: " + cityInput + ", " + stateInput);
                return;
            }
            latitude = coords[0];
            longitude = coords[1];
            this.latitude.setText(String.valueOf(latitude));
            this.longitude.setText(String.valueOf(longitude));
        } else {
            try {
                latitude = Double.parseDouble(this.latitude.getText());
                longitude = Double.parseDouble(this.longitude.getText());
            } catch (NumberFormatException ex) {
                outputArea.setText("Please enter valid latitude and longitude.");
                return;
            }
        }

        String unitArg;
        if (unitCombo.getSelectedItem().toString().equals("Fahrenheit")) {
            unitArg = "f";
        } else {
            unitArg = "c";
        }

        String[] args = {
                "--latitude", String.valueOf(latitude),
                "--longitude", String.valueOf(longitude),
                "--unit", unitArg
        };

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream oldOut = System.out;
            System.setOut(new PrintStream(buffer));

            WeatherForecast.main(args);

            System.out.flush();
            System.setOut(oldOut);
            outputArea.setText(buffer.toString());
        } catch (Exception ex) {
            outputArea.setText("Unexpected error");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherForecastApp app = new WeatherForecastApp();
            app.setLocationRelativeTo(null);
        });
    }
}