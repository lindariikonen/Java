package fi.tuni.prog3.weatherapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Application;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Get weather from OpenWeather Map using iAPI interface
 */
class fetchWeather implements iAPI {
    /**
     * Returns coordinates for a location.
     * @param loc Name of the location for which coordinates should be fetched.
     * @return Location as coordinates
     */
    @Override
    public double[] lookUpLocation(String loc) throws IOException {
        String base_address = "http://api.openweathermap.org/geo/1.0/direct?";
        String city = "q=".concat(loc);
        String limit = "&limit=1";
        String api_key = "&appid=a63f509610c179ead1659a2db7e1aab6";
        String url = base_address.concat(city).concat(limit).concat(api_key);
        URL website = new URL(url);
        BufferedReader br = new BufferedReader(new InputStreamReader(
                website.openConnection().getInputStream()));

        try {
            List<String> dataList = br.lines().collect(Collectors.toList());

            String[] data = dataList.get(0).split("}");
            String[] dt = data[1].split(",");
            String[] lat_string = dt[1].split(":");
            double lat = Double.parseDouble(lat_string[1]);
            String[] lon_string = dt[2].split(":");
            double lon = Double.parseDouble(lon_string[1]);

            double[] location = new double[2];
            location[0] = lat;
            location[1] = lon;

            return location;
        }
        catch (ArrayIndexOutOfBoundsException e){
            double[] location = new double[2];
            location[0] = 0;
            location[1] = 0;

            return location;
        }
    }

    /**
     * Returns the current weather for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return JsonObject with current weather information.
     */
    @Override
    public JsonObject getCurrentWeather(double lat, double lon)
            throws IOException {
        String base_address = "http://pro.openweathermap.org/data/2.5/weather?";
        String coordinates = "lat=" + lat + "&lon=" + lon;
        String units = "&units=metric";
        String api_key = "&appid=a63f509610c179ead1659a2db7e1aab6";
        String url = base_address.concat(coordinates).concat(units)
                .concat(api_key);
        URL website = new URL(url);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                website.openConnection().getInputStream()))) {
            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseReader(br).getAsJsonObject();

            // Extract required weather information
            JsonObject main = jsonObject.getAsJsonObject("main");
            double temperature = Math.round(main.get("temp").getAsDouble());

            JsonObject wind = jsonObject.getAsJsonObject("wind");
            double windSpeed = wind.get("speed").getAsDouble();
            
            double pressure = 0.0;
            if (main.has("pressure")) {
                pressure = main.get("pressure").getAsDouble();
            }

            JsonArray weatherArray = jsonObject.getAsJsonArray("weather");
            JsonObject weatherObject = weatherArray.get(0).getAsJsonObject();
            String weatherMain = weatherObject.get("main").getAsString();
            String weatherIcon = weatherObject.get("icon").getAsString();
            
             

            JsonObject weatherJson = new JsonObject();
            weatherJson.addProperty("Temperature", temperature);
            weatherJson.addProperty("WindSpeed", windSpeed);
            weatherJson.addProperty("Weather", weatherMain);
            weatherJson.addProperty("WeatherIcon", weatherIcon);
            weatherJson.addProperty("Pressure", pressure);

            return weatherJson;
        }
    }

    /**
     * Returns a forecast for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return String with the forecast information.
     */
    @Override
    public String getForecast(double lat, double lon) throws IOException {
        String base_address = "http://pro.openweathermap.org/data/2.5/forecast?";
        String coordinates = "lat=" + lat + "&lon=" + lon;
        String units = "&units=metric";
        String api_key = "&appid=a63f509610c179ead1659a2db7e1aab6";
        String url = base_address.concat(coordinates).concat(units)
                .concat(api_key);
        URL website = new URL(url);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                website.openConnection().getInputStream()))) {
            JsonObject jsonObject = JsonParser.parseReader(br).getAsJsonObject();

            // Extract forecast data
            JsonArray forecastList = jsonObject.getAsJsonArray("list");

            StringBuilder forecastStringBuilder = new StringBuilder();
            int count = 0;

            String previousDay = "";

            for (int i = 0; i < forecastList.size(); i++) {
                JsonObject forecastItem = forecastList.get(i).getAsJsonObject();
                String date = forecastItem.get("dt_txt").getAsString()
                        .split(" ")[0]; // Otetaan vain päivämäärä
                JsonObject main = forecastItem.getAsJsonObject("main");
                double temperature = Math.round(main.get("temp").getAsDouble());
                double minTemperature = Math.round(main.get("temp_min")
                        .getAsDouble());
                double maxTemperature = Math.round(main.get("temp_max")
                        .getAsDouble());

                // Check if the date has changed
                if (!date.equals(previousDay)) {
                    // Extract weather information including the icon code
                    JsonArray weatherArray = forecastItem
                            .getAsJsonArray("weather");
                    JsonObject weatherObject = weatherArray.get(0)
                            .getAsJsonObject();
                    String weatherMain = weatherObject.get("main")
                            .getAsString();
                    String weatherIcon = weatherObject.get("icon")
                            .getAsString();

                    // Add forecast information to StringBuilder
                    forecastStringBuilder.append("Date: ").append(date)
                            .append(", ").append("Temperature: ")
                            .append(temperature).append("°C, ")
                            .append("Min Temperature: ").append(minTemperature)
                            .append("°C, ").append("Max Temperature: ")
                            .append(maxTemperature).append("°C, ")
                            .append("Weather: ").append(weatherMain)
                            .append(" (Icon: ").append(weatherIcon)
                            .append(")\n");

                    count++;

                    // End loop after five days
                    if (count == 5)
                        break;

                    // Update previous day
                    previousDay = date;
                }
            }
            return forecastStringBuilder.toString();
        }
    }
}

/**
 * Read from and write to JSON file
 */
class readwriteJSON implements iReadAndWriteToFile{
    /**
     * Reads the saved city name from the given file.
     * @param fileName Name of the file to read from.
     * @return The name of the saved city.
     * @throws FileNotFoundException if the method e.g, cannot find the file.
     */
    @Override
    public String readFromFile(String fileName) throws FileNotFoundException {
        try {
            Object o = new JsonParser().parse(new FileReader("saveFile.json"));
            JsonObject jsonObject = (JsonObject) o;
            String city = jsonObject.get("City").toString();
            return city;
        }

        catch (FileNotFoundException e){
            String city = "Tampere";
            return city;
        }
    }

    /**
     * Save the last viewed city to a JSON file.
     * @param fileName Name of the file to write to.
     * @param city City name to be saved.
     * @return True if writing was successful, otherwise false.
     */
    @Override
    public boolean writeToFile(String fileName, String city) {
        try{
            FileWriter file = new FileWriter(fileName);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("City", city);
            file.write(jsonObject.toString());
            file.close();
            return true;
        }
        catch (IOException e){
            System.out.println("No such file!");
            return false;
        }
    }
}

/**
 * JavaFX Weather Application.
 */
public class WeatherApp extends Application {
    // Text field for entering the city name
    private TextField cityTextField;
    // Label to display warning if searched city is not found
    private Label warningLabel;

    /**
     * Main function for the JavaFX application.
     * @param stage The stage for the UI
     * @throws Exception if readFromFile -function throws an exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        String city = new readwriteJSON().readFromFile("saveFile.json");
        city = city.substring(1, city.length()-1);

        //Creating a new BorderPane.
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));

        //Adding button to the BorderPane and aligning it to the right.
        var quitButton = getQuitButton();
        BorderPane.setMargin(quitButton, new Insets(10, 10, 0, 10));
        root.setBottom(quitButton);
        BorderPane.setAlignment(quitButton, Pos.TOP_RIGHT);
        
        // Adding search functionality
        cityTextField = new TextField();
        cityTextField.setPromptText("Enter city name");
        Button searchButton = new Button("Search");
        // Add warning label properties
        warningLabel = new Label("City not found!");
        warningLabel.setVisible(false);
        warningLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        warningLabel.setTextFill(Color.RED);
        warningLabel.setPadding(new Insets(0,0,0,10));

        searchButton.setOnAction(this::handleSearch);
        HBox searchBox = new HBox(cityTextField, searchButton, warningLabel);
        searchBox.setAlignment(Pos.TOP_LEFT);
        root.setTop(searchBox);

        //Adding HBox to the center of the BorderPane.
        root.setCenter(getCenterVBox(city));

        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.setTitle("WeatherApp");
        stage.show();
    }

    /**
     * The main function of the program. Starts the JavaFX application.
     * @param args Unused.
     */
    public static void main(String[] args) {
        // Start JavaFX application
        launch();
    }

    /**
     * Create a box in the center of the UI and call for functions to create
     * two boxes inside it.
     * @param city The name of the city that was searched.
     * @return CenterHBox.
     * @throws IOException if the lookUpLocation -function throws an exception.
     */
    private VBox getCenterVBox(String city) throws IOException {
        //Creating an HBox.
        VBox centerHBox = new VBox(5);

        //Adding two VBox to the HBox.
        centerHBox.getChildren().addAll(getTopHBox(city), getBottomHBox(city));

        return centerHBox;
    }

    /**
     * Create upper part of the central box and display current weather for
     * the searched city in it.
     * @param city The name of the city that was searched.
     * @return LeftHBox.
     * @throws IOException if the lookUpLocation -function throws an exception.
     */
    private HBox getTopHBox(String city) throws IOException {
        //Creating a VBox for the left side.
        HBox leftHBox = new HBox();
        leftHBox.setPrefHeight(200);
        leftHBox.setStyle("-fx-background-color: #8fc6fd;");
        leftHBox.setAlignment(Pos.TOP_CENTER);

        VBox verticalBox = new VBox();
        verticalBox.setAlignment(Pos.TOP_CENTER);

        // Fetch location coordinates
        double[] location = new fetchWeather().lookUpLocation(city);
        if (location[0] == 0 && location[1] == 0){
            warningLabel.setVisible(true);
            return leftHBox;
        }
        else {
            warningLabel.setVisible(false);
            // Fetch current weather
            JsonObject currentWeather = new fetchWeather()
                    .getCurrentWeather(location[0], location[1]);

            // Extract icon code from current weather string
            String iconCode = currentWeather.get("WeatherIcon").toString();
            iconCode = iconCode.substring(1, iconCode.length() - 1);

            // Fetch weather icon image from OpenWeatherMap
            Image iconImage = new Image("http://openweathermap.org/img/wn/"
                    + iconCode + ".png");
            ImageView iconImageView = new ImageView(iconImage);
            iconImageView.setFitHeight(80);
            iconImageView.setPreserveRatio(true);

            Label cityName = new Label(city);
            cityName.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
            Label current = new Label("Current weather");
            current.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
            Label currentWeatherLabel = new Label(currentWeather
                    .get("Temperature").toString() + " °C");
            currentWeatherLabel.setGraphic(iconImageView);
            currentWeatherLabel.setFont(Font.font("Tahoma", 15));
            String wDescription = currentWeather.get("Weather").toString();
            double pressure= currentWeather.get("Pressure").getAsDouble();
            wDescription = wDescription.substring(1, wDescription.length() - 1);
            Label weatherDescription = new Label(wDescription +
                    ", Wind speed " + currentWeather.get("WindSpeed")
                    .toString() + " m/s"+", Pressure " + pressure + " hPa");
       
            
          

            verticalBox.getChildren().addAll(cityName, current,
                    currentWeatherLabel, weatherDescription);

            leftHBox.getChildren().add(verticalBox);

            return leftHBox;
        }
    }

    /**
     * Create bottom part of the central box and display weather forecast for
     * the searched city in it.
     * @param city The name of the city that was searched.
     * @return RightHBox.
     */
    private HBox getBottomHBox(String city) {
        // Creating a VBox for the right side.
        HBox rightHBox = new HBox();
        rightHBox.setPrefHeight(300);
        rightHBox.setStyle("-fx-background-color: #b1c2d4;");
        rightHBox.setAlignment(Pos.TOP_CENTER);

        VBox verticalBox = new VBox();
        verticalBox.setAlignment(Pos.TOP_CENTER);
        // Add border style to the VBox
        verticalBox.setStyle("-fx-border-color: white; -fx-border-width: 2px;" +
                " -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label forecastLabel = new Label("5 Day Forecast");
        forecastLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        // Add margin around the forecastLabel
        VBox.setMargin(forecastLabel, new Insets(3));

        try {
            // Fetch location coordinates
            double[] location = new fetchWeather().lookUpLocation(city);

            // Display warning if city location is (0,0)
            if (location[0] == 0 && location[1] == 0){
                warningLabel.setVisible(true);
                return rightHBox;
            }
            else{
                // Set warning not visible if searched city is found
                warningLabel.setVisible(false);

                // Fetch forecast
                String forecastData = new fetchWeather()
                        .getForecast(location[0], location[1]);

                // Create labels for each forecast entry
                String[] forecasts = forecastData.split("\n");
                // Create a DateTimeFormatter for the date format,
                // adjusted to match the date format
                DateTimeFormatter formatter = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd");

                for (String forecastEntry : forecasts) {
                    // Extract the date from the forecast string
                    String dateString = forecastEntry.substring(
                            forecastEntry.indexOf("Date:") + 6,
                            forecastEntry.indexOf(","));
                    // Parse the date string into a LocalDate object
                    LocalDate date = LocalDate.parse(dateString.trim(),
                            formatter);

                    // Get the day abbreviation from the LocalDate object
                    String dayOfWeek = date.getDayOfWeek().getDisplayName(
                            TextStyle.SHORT, Locale.ENGLISH);

                    // Fetch icon code
                    String iconCode = forecastEntry.substring(
                            forecastEntry.lastIndexOf("(Icon: ") + 7,
                            forecastEntry.lastIndexOf(")"));

                    // Fetch weather icon image from OpenWeatherMap
                    Image iconImage = new Image("http://openweathermap.org/img/wn/"
                            + iconCode + ".png");
                    ImageView iconImageView = new ImageView(iconImage);
                    iconImageView.setFitHeight(32);
                    iconImageView.setPreserveRatio(true);

                    // Extract min and max temperature from the forecast entry
                    int minTemperatureIndex = forecastEntry
                            .indexOf("Min Temperature:") + 17;
                    int maxTemperatureIndex = forecastEntry
                            .indexOf("Max Temperature:") + 17;
                    int minTemperatureEndIndex = forecastEntry
                            .indexOf("°C", minTemperatureIndex);
                    int maxTemperatureEndIndex = forecastEntry
                            .indexOf("°C", maxTemperatureIndex);
                    String minTemperature = forecastEntry.substring(
                            minTemperatureIndex, minTemperatureEndIndex);
                    String maxTemperature = forecastEntry.substring(
                            maxTemperatureIndex, maxTemperatureEndIndex);

                    // Construct a label for min and max temperature
                    Label temperatureLabel = new Label(minTemperature
                            + "°C / " + maxTemperature + "°C");
                    temperatureLabel.setFont(Font.font("Tahoma", 12));

                    Label entryLabel = new Label(dayOfWeek);
                    entryLabel.setFont(Font.font("Tahoma", 12));
                    entryLabel.setGraphic(iconImageView);

                    // Add a separator line below each entry
                    Separator separator = new Separator();

                    // Add the entry label, temperature label,
                    // and separator to the vertical box
                    verticalBox.getChildren().addAll(entryLabel,
                            temperatureLabel, separator);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        VBox containerVBox = new VBox(forecastLabel, verticalBox);
        // Center the forecast label and entries vertically
        containerVBox.setAlignment(Pos.CENTER);

        rightHBox.getChildren().add(containerVBox);

        return rightHBox;
    }

    /**
     * Update UI after user searches for weather data for a new city
     * @param event The button click which triggers this function
     */
    private void handleSearch(ActionEvent event) {
        String newCity = cityTextField.getText();

        // Change first letter to upper case
        newCity = newCity.substring(0, 1).toUpperCase() + newCity.substring(1);
        try {
            // Create a new VBox and add a CenterVBox
            VBox cityVBox = new VBox();
            cityVBox.getChildren().add(getCenterVBox(newCity));

            // Update the UI with the new VBox
            BorderPane root = (BorderPane) ((Button) event.getSource())
                    .getParent().getParent();
            root.setCenter(cityVBox);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Quit -button in the UI
     * @return Button.
     */
    private Button getQuitButton() {
        //Creating a button.
        Button button = new Button("Quit");

        //Adding an event to the button to terminate the application.
        button.setOnAction((ActionEvent event) -> {
            // Save latest searched city to JSON file
            try {
                boolean fileWriter = new readwriteJSON().writeToFile(
                        "saveFile.json", cityTextField.getText());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            Platform.exit();
        });
        return button;
    }
}