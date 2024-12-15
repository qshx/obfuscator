package com.example.obfuscator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Класс графического интерфейса для взаимодействия с пользователем.
 * Позволяет выбрать исходный файл .java, отобразить его и обфусцировать код.
 * Также позволяет сохранить обфусцированный код.
 */
public class GuiFrame extends JFrame {
    private static final Logger logger = LogManager.getLogger(GuiFrame.class);

    private JTextField filePathField;
    private JTextArea originalCodeArea;
    private JTextArea obfuscatedCodeArea;
    private ObfuscationService obfuscationService;

    /**
     * Конструктор GUI.
     * Инициализирует компоненты и их расположение.
     */
    public GuiFrame() {
        super("Java Obfuscator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        obfuscationService = new ObfuscationService();
        initUI();
    }

    /**
     * Инициализация пользовательского интерфейса.
     */
    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Файл"));

        filePathField = new JTextField();
        JButton loadBtn = new JButton("Загрузить");
        loadBtn.addActionListener(e -> loadFile());
        topPanel.add(filePathField, BorderLayout.CENTER);
        topPanel.add(loadBtn, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Код"));

        originalCodeArea = new JTextArea();
        originalCodeArea.setEditable(false);
        obfuscatedCodeArea = new JTextArea();
        obfuscatedCodeArea.setEditable(false);

        JScrollPane originalScroll = new JScrollPane(originalCodeArea);
        originalScroll.setBorder(BorderFactory.createTitledBorder("Исходный код"));
        JScrollPane obfScroll = new JScrollPane(obfuscatedCodeArea);
        obfScroll.setBorder(BorderFactory.createTitledBorder("Обфусцированный код"));

        centerPanel.add(originalScroll);
        centerPanel.add(obfScroll);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton obfuscateBtn = new JButton("Обфусцировать");
        obfuscateBtn.addActionListener(e -> obfuscateCode());
        JButton saveBtn = new JButton("Сохранить результат");
        saveBtn.addActionListener(e -> saveObfuscated());
        bottomPanel.add(obfuscateBtn);
        bottomPanel.add(saveBtn);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Загрузка исходного файла.
     */
    private void loadFile() {
        String path = filePathField.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Укажите путь к файлу!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String code = FileUtils.readFile(path);
            originalCodeArea.setText(code);
            obfuscatedCodeArea.setText("");
            logger.info("Файл загружен: {}", path);
        } catch (IOException ex) {
            logger.error("Ошибка загрузки файла: {}", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Ошибка загрузки файла: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Обфусцирование кода.
     */
    private void obfuscateCode() {
        String originalCode = originalCodeArea.getText();
        if (originalCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите исходный код!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String obfCode = obfuscationService.obfuscate(originalCode);
        obfuscatedCodeArea.setText(obfCode);
        logger.info("Код обфусцирован.");
    }

    /**
     * Сохранение обфусцированного кода.
     */
    private void saveObfuscated() {
        String obfCode = obfuscatedCodeArea.getText();
        if (obfCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет обфусцированного кода для сохранения!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить обфусцированный код");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String savePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                FileUtils.writeFile(obfCode, savePath);
                logger.info("Обфусцированный файл сохранён: {}", savePath);
                JOptionPane.showMessageDialog(this, "Файл успешно сохранён!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                logger.error("Ошибка сохранения файла: {}", ex.getMessage());
                JOptionPane.showMessageDialog(this, "Ошибка сохранения файла: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
