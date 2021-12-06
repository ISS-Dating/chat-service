package com.knu.service.chat.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {

    private final Properties properties;

    PropertiesManager(String fileName) throws IOException {
        InputStream input = PropertiesManager.class.getClassLoader().getResourceAsStream(fileName);

        properties = new Properties();

        properties.load(input);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
