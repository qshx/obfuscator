package com.example.obfuscator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный класс приложения.
 * Запускает графический интерфейс для выбора файла,
 * просмотра исходного кода и обфусцированной версии.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Точка входа в программу.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        LoggingConfig.initLogging();
        logger.info("Приложение запущено.");
        java.awt.EventQueue.invokeLater(() -> {
            GuiFrame frame = new GuiFrame();
            frame.setVisible(true);
        });
    }
}
