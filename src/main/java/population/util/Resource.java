package population.util;

import java.util.ResourceBundle;

/**
 * Class for retrieving i18n ResourceBundles
 */
public class Resource {
    /**
     * root directory for resources
     */
    protected static final String BASE_NAME = "population/resource/strings";

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
