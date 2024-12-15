package com.example.obfuscator;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Утилитный класс для работы с файлами.
 */
public class FileUtils {

    /**
     * Читает содержимое текстового файла.
     *
     * @param path путь к файлу
     * @return содержимое файла в виде строки
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static String readFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Записывает строку в файл.
     *
     * @param content содержимое для записи
     * @param path путь к файлу
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void writeFile(String content, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), StandardCharsets.UTF_8))) {
            bw.write(content);
        }
    }
}
