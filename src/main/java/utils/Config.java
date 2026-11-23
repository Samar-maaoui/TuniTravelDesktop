package utils;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();
    static {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) { e.printStackTrace(); }
    }
    public static String get(String key) { return props.getProperty(key); }
}
