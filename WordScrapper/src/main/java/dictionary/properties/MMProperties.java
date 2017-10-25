package dictionary.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Mateusz on 31.03.2017.
 */
public class MMProperties {
    private static final Properties prop;
    public static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";

    static {
        prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(CONFIG_PROPERTIES_FILE_NAME);
            prop.load(input);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getString(String key) {
        return prop.getProperty(key);
    }

    public static int getInteger(String key) {
        return Integer.parseInt(prop.getProperty(key));
    }

    public static boolean getBoolean(String key){
        return Boolean.parseBoolean(prop.getProperty(key));
    }
}
