package fi.tuni.prog3.weatherapp;

import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * Interface for extracting data from the OpenWeatherMap API.
 */
public interface iAPI {

    /**
     * Returns coordinates for a location.
     * @param loc Name of the location for which coordinates should be fetched.
     * @return Location as coordinates
     */
    double[] lookUpLocation(String loc) throws IOException;

    /**
     * Returns the current weather for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return JsonObject with current weather information.
     */
    JsonObject getCurrentWeather(double lat, double lon)throws IOException;

    /**
     * Returns a forecast for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return String.
     */
    String getForecast(double lat, double lon) throws IOException;
}
