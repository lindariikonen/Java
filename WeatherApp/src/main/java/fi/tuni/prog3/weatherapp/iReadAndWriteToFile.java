package fi.tuni.prog3.weatherapp;

/**
 * Interface with methods to read from a file and write to a file.
 */
public interface iReadAndWriteToFile {

    /**
     * Reads the saved city name from the given file.
     * @param fileName Name of the file to read from.
     * @return The name of the saved city
     * @throws Exception if the method e.g, cannot find the file.
     */
    String readFromFile(String fileName) throws Exception;

    /**
     * Save the last viewed city to a JSON file.
     * @param fileName Name of the file to write to.
     * @param city City name to be saved.
     * @return True if writing was successful, otherwise false.
     * @throws Exception if the method e.g., cannot write to a file.
     */
    boolean writeToFile(String fileName, String city) throws Exception;
}
