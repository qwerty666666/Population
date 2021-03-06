package population.util.Resources;

import java.util.ResourceBundle;

/**
 * Class for retrieving application properties
 */
public class AppResource {
    /**
     * property files
     */
    protected static final String BASE_NAME = "population/resource/application";

    /**
     * @return {@link ResourceBundle#getBundle(String)}
     */
    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BASE_NAME);
    }

    /**
     * @param key string key
     * @return {@link ResourceBundle#getString(String)}
     */
    public static String getString(String key) {
        return getBundle().getString(key);
    }
}
