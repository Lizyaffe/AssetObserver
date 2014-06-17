package com.masterface.nxt.ae;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PropertiesStorage {

    public static final String ASSET_OBSERVER_DEFAULT_PROPERTIES = "asset.observer.default.properties";
    public static final String ASSET_OBSERVER_PROPERTIES = "asset.observer.properties";

    private static final Properties defaultProperties = new Properties();
    private static final Properties properties = new Properties(defaultProperties);

    static {
        AssetObserver.log.info("Initializing asset observer properties");
        try (InputStream is = ClassLoader.getSystemResourceAsStream(ASSET_OBSERVER_DEFAULT_PROPERTIES)) {
            if (is != null) {
                defaultProperties.load(is);
            } else {
                String configFile = System.getProperty(ASSET_OBSERVER_DEFAULT_PROPERTIES);
                if (configFile != null) {
                    try (InputStream fis = new FileInputStream(configFile)) {
                        defaultProperties.load(fis);
                    } catch (IOException e) {
                        throw new RuntimeException("Error loading properties from file " + configFile);
                    }
                } else {
                    throw new RuntimeException(ASSET_OBSERVER_DEFAULT_PROPERTIES + " not in classpath and system property " + ASSET_OBSERVER_DEFAULT_PROPERTIES + " not defined");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading nxt-default.properties", e);
        }
    }

    static {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(ASSET_OBSERVER_PROPERTIES)) {
            if (is != null) {
                properties.load(is);
            } // ignore if missing
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + ASSET_OBSERVER_PROPERTIES, e);
        }
    }

    public static int getIntProperty(String name) {
        try {
            int result = Integer.parseInt(properties.getProperty(name));
            AssetObserver.log.info(name + " = \"" + result + "\"");
            return result;
        } catch (NumberFormatException e) {
            AssetObserver.log.info(name + " not defined, assuming 0");
            return 0;
        }
    }

    public static String getStringProperty(String name) {
        return getStringProperty(name, null);
    }

    public static String getStringProperty(String name, String defaultValue) {
        String value = properties.getProperty(name);
        if (value != null && !"".equals(value)) {
            AssetObserver.log.info(name + " = \"" + value + "\"");
            return value;
        } else {
            AssetObserver.log.info(name + " not defined");
            return defaultValue;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static List<String> getStringListProperty(String name) {
        String value = getStringProperty(name);
        if (value == null || value.length() == 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String s : value.split(";")) {
            s = s.trim();
            if (s.length() > 0) {
                result.add(s);
            }
        }
        return result;
    }

    public static Boolean getBooleanProperty(String name) {
        String value = properties.getProperty(name);
        if (Boolean.TRUE.toString().equals(value)) {
            AssetObserver.log.info(name + " = \"true\"");
            return true;
        } else if (Boolean.FALSE.toString().equals(value)) {
            AssetObserver.log.info(name + " = \"false\"");
            return false;
        }
        AssetObserver.log.info(name + " not defined, assuming false");
        return false;
    }
}
