package com.example.obfuscator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.net.URI;

/**
 * Класс для инициализации системы логирования.
 */
public class LoggingConfig {
    /**
     * Инициализирует систему логирования log4j2.
     */
    public static void initLogging() {
        try {
            URI configUri = LoggingConfig.class.getResource("/log4j2.xml").toURI();
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.setConfigLocation(configUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
