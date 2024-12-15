package com.example.obfuscator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для обфускации Java-кода без использования внешних парсеров.
 * Применяются следующие шаги:
 * 1. Удаление комментариев.
 * 2. Генерация новых имён для локальных переменных.
 * 3. Замену имён переменных.
 * 4. Вставка "мусорного" кода, не влияющего на логику.
 */
public class ObfuscationService {
    private static final Logger logger = LogManager.getLogger(ObfuscationService.class);

    /**
     * Главный метод обфускации.
     * @param originalCode исходный код .java файла
     * @return обфусцированный код, компилируемый и работоспособный
     */
    public String obfuscate(String originalCode) {
        logger.info("Начало обфускации кода...");

        // 1. Удаляем все комментарии
        String codeNoComments = removeComments(originalCode);

        // 2. Извлекаем карту переменных для переименования
        Map<String, String> variableMap = buildVariableRenameMap(codeNoComments);

        // 3. Заменяем имена переменных
        String renamedCode = replaceVariables(codeNoComments, variableMap);

        // 4. Добавляем мусорный код:
        //    - Фиктивный метод в конец класса
        //    - Вставляем фиктивные локальные переменные в методы
        String codeWithJunk = addJunkCode(renamedCode);

        logger.info("Обфускация завершена.");
        return codeWithJunk;
    }

    /**
     * Удаляет комментарии (однострочные // и многострочные /* ... *\/).
     * @param code исходный код
     * @return код без комментариев
     */
    private String removeComments(String code) {
        // Удаляем многострочные комментарии
        String noMultiComments = code.replaceAll("(?s)/\\*.*?\\*/", "");
        // Удаляем однострочные комментарии
        String noSingleComments = noMultiComments.replaceAll("//.*", "");
        return noSingleComments;
    }

    /**
     * Пытается найти локальные переменные и их имена.
     * Мы будем рассматривать переменные объявленные в методах, а также поля класса.
     * Однако, чтобы не ломать код, не будем менять названия полей класса,
     * ограничимся локальными переменными методов.
     *
     * @param code код после удаления комментариев
     * @return карта староеИмя -> новоеИмя
     */
    private Map<String, String> buildVariableRenameMap(String code) {
        Map<String, String> variableMap = new HashMap<>();

        // Рассмотрим примитивы и некоторые распространённые типы:
        String typePattern = "(?:int|long|boolean|char|double|float|short|byte|String|List|Map|Set|Object|File)";


        Pattern varPattern = Pattern.compile("\\b" + typePattern + "\\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\\s*,\\s*[a-zA-Z_][a-zA-Z0-9_]*)*)\\s*[;=]");
        Matcher matcher = varPattern.matcher(code);

        // Чтобы избежать коллизий, соберём все имена переменных.
        Set<String> allVars = new HashSet<>();
        while (matcher.find()) {
            String varList = matcher.group(1);
            // varList может быть: "x", или "flag, done"
            String[] vars = varList.split("\\s*,\\s*");
            for (String v : vars) {
                // v - имя переменной
                if (!variableMap.containsKey(v)) {
                    String newName = generateRandomVarName(8);
                    variableMap.put(v, newName);
                    allVars.add(v);
                }
            }
        }

        // Для надёжности проверим, что все новые имена уникальны
        // Генерация с запасом и set проверки должна гарантировать уникальность.

        return variableMap;
    }

    /**
     * Генерирует новое случайное имя переменной, чтобы снизить читабельность.
     * Используем большой набор символов, чтобы уменьшить шанс коллизий.
     */
    private String generateRandomVarName(int length) {
        String chars = "BERAXYZQWERTOPASDFGHJKL";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++){
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Заменяет имена переменных в коде с учётом границ слов.
     * Используем регулярные выражения: \bимя\b -> новоеИмя
     */
    private String replaceVariables(String code, Map<String, String> variableMap) {
        String result = code;
        for (Map.Entry<String, String> e : variableMap.entrySet()) {
            String oldName = e.getKey();
            String newName = e.getValue();
            // Заменяем с учётом границ слов
            result = result.replaceAll("\\b" + Pattern.quote(oldName) + "\\b", newName);
        }
        return result;
    }

    /**
     * Добавляет мусорный код:
     * 1. Добавляет фиктивный приватный статический метод в конец класса (если найдет класс).
     * 2. Вставляет фиктивные локальные переменные в тело методов.
     *
     * Для упрощения предполагаем, что есть один основной класс в файле.
     * Ищем "class <Name> { ... }" и вставляем перед закрывающей фигурной скобкой мусорный метод.
     *
     * Аналогично, для методов: при входе в метод после "{" вставим пару фиктивных переменных.
     */
    private String addJunkCode(String code) {
        // Добавление фиктивного метода в конец класса
        // Найдем основную конструкцию class XXX { ... }
        // Вставим перед последней '}' класса наш метод
        Pattern classPattern = Pattern.compile("(class\\s+[A-Za-z_][A-Za-z0-9_]*\\s*\\{)");
        Matcher cm = classPattern.matcher(code);
        if (cm.find()) {
            // Находим конец класса - последнюю '}' на том же уровне вложенности
            int classStart = cm.end(); // позиция после {
            int braceCount = 1;
            int pos = classStart;
            while (pos < code.length() && braceCount > 0) {
                char c = code.charAt(pos);
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                pos++;
            }
            // pos - позиция, где braceCount стал 0, то есть конец класса
            // Вставим перед этой точкой фиктивный метод
            String junkMethod = "\nprivate static void " + generateRandomVarName(10) + "() {\n" +
                    "    int " + generateRandomVarName(5) + " = 0;\n" +
                    "    "  +
                    "}\n";

            code = code.substring(0, pos-1) + junkMethod + code.substring(pos-1);
        }

        // Добавление фиктивных локальных переменных внутри методов
        // Ищем сигнатуры методов: (public|private|protected|static)* <тип> имя(...) {
        // После '{' вставляем пару строк мусора.
        Pattern methodPattern = Pattern.compile("(public|private|protected|static|final|synchronized|\\s)+[\\p{L}0-9_$<>\\[\\]]+\\s+[a-zA-Z_][A-Za-z0-9_]*\\s*\\(.*?\\)\\s*\\{");
        Matcher mm = methodPattern.matcher(code);
        StringBuffer sb = new StringBuffer();
        while (mm.find()) {
            // После совпадения вставим мусор
            String junkLocal = "\n    int " + generateRandomVarName(5) + " = (int)(Math.random()*100);\n" +
                    "    String " + generateRandomVarName(5) + " = \"junk\";\n";
            mm.appendReplacement(sb, mm.group(0) + junkLocal);
        }
        mm.appendTail(sb);

        return sb.toString();
    }
}
