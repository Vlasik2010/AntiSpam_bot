package com.example.antispambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс для загрузки конфигурационных параметров из файла config.properties.
 */
public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);
    private static BotConfig instance;
    private Properties properties;

    private BotConfig() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Файл config.properties не найден!");
                throw new RuntimeException("Файл config.properties не найден!");
            }
            properties.load(input);
            logger.info("Конфигурация успешно загружена.");
        } catch (IOException ex) {
            logger.error("Ошибка загрузки конфигурации: ", ex);
            throw new RuntimeException("Ошибка загрузки конфигурации", ex);
        }
    }

    public static synchronized BotConfig getInstance() {
        if (instance == null) {
            instance = new BotConfig();
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}