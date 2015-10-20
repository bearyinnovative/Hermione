package hermione;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by zjh on 15/10/20.
 */
public class ConfigManager {
    public static Config config;

    static {
        config = ConfigFactory.load("hermione");
    }

    public static String getConfiguration(String key) {
        return config.getString(String.format("hermione.%s", key));
    }
}
