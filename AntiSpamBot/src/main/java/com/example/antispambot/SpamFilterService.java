package com.example.antispambot;

import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Сервис для проверки сообщений на признаки спама.
 * Реализована нормализация текста, фуззи‑сравнение и учёт спам-сообщений.
 */
public class SpamFilterService {
    // Хранение запрещённых слов в нормализованном виде
    private final Set<String> bannedWords = new HashSet<>();
    // Хранение времени последнего сообщения для каждого пользователя (userId -> timestamp)
    private final Map<Long, Long> lastMessageTimestamps = new HashMap<>();
    // Учёт количества спам-сообщений для каждого пользователя (userId -> count)
    private final Map<Long, Integer> userSpamCount = new HashMap<>();
    // Запоминание последнего сообщения (нормализованного) для выявления повторов
    private final Map<Long, String> lastUserMessage = new HashMap<>();

    // Статистика
    private int messagesChecked = 0;
    private int messagesDeleted = 0;

    // Порог спам-сообщений (для отправки предупреждения)
    public static final int SPAM_THRESHOLD = 3;

    public SpamFilterService() {
        // Изначально добавляем несколько запрещённых слов (нормализованных)
        bannedWords.add(normalizeWord("spamword1"));
        bannedWords.add(normalizeWord("spamword2"));
    }

    /**
     * Нормализует слово: приводит к нижнему регистру, удаляет все символы, кроме букв и цифр,
     * и заменяет распространённые символы-замены (например, @ на a, 0 на o и т.д.).
     *
     * @param word исходное слово
     * @return нормализованное слово
     */
    public static String normalizeWord(String word) {
        String normalized = word.toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9]", "");
        normalized = normalized.replace("0", "o")
                .replace("1", "l")
                .replace("@", "a")
                .replace("3", "e")
                .replace("!", "i");
        return normalized;
    }

    /**
     * Вычисляет расстояние Левенштейна между строками.
     *
     * @param a первая строка
     * @param b вторая строка
     * @return расстояние Левенштейна
     */
    private int computeLevenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    /**
     * Проверяет, является ли сообщение спамом.
     * Критерии:
     * - Сообщения отправляются слишком часто (интервал менее 2 секунд).
     * - Повторяющиеся сообщения.
     * - Наличие запрещённых или похожих (фуззи‑сравнение) слов.
     *
     * @param update объект Update с сообщением.
     * @return true, если сообщение определяется как спам.
     */
    public boolean isSpam(Update update) {
        messagesChecked++;
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        long userId = update.getMessage().getFrom().getId();
        long currentTime = System.currentTimeMillis();

        // Проверка частоты отправки сообщений
        if (lastMessageTimestamps.containsKey(userId)) {
            long lastTime = lastMessageTimestamps.get(userId);
            if ((currentTime - lastTime) < 2000) { // менее 2 секунд
                messagesDeleted++;
                incrementSpamCount(userId);
                lastMessageTimestamps.put(userId, currentTime);
                return true;
            }
        }
        lastMessageTimestamps.put(userId, currentTime);

        String text = update.getMessage().getText();
        String normalizedMessage = normalizeWord(text);

        // Детектирование повторяющихся сообщений
        if (lastUserMessage.containsKey(userId) && lastUserMessage.get(userId).equals(normalizedMessage)) {
            messagesDeleted++;
            incrementSpamCount(userId);
            lastUserMessage.put(userId, normalizedMessage);
            return true;
        }
        lastUserMessage.put(userId, normalizedMessage);

        // Разбиваем сообщение на слова и проверяем каждое
        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            String normalizedToken = normalizeWord(token);
            for (String banned : bannedWords) {
                // Если токен содержит запрещённое слово как подстроку
                if (normalizedToken.contains(banned)) {
                    messagesDeleted++;
                    incrementSpamCount(userId);
                    return true;
                } else {
                    // Фуззи‑сравнение: если расстояние Левенштейна достаточно мало, считаем слово похожим
                    int distance = computeLevenshteinDistance(normalizedToken, banned);
                    int threshold = normalizedToken.length() <= 4 ? 0 : (normalizedToken.length() <= 7 ? 1 : 2);
                    if (distance <= threshold) {
                        messagesDeleted++;
                        incrementSpamCount(userId);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void incrementSpamCount(long userId) {
        int count = userSpamCount.getOrDefault(userId, 0) + 1;
        userSpamCount.put(userId, count);
    }

    public int getSpamCount(long userId) {
        return userSpamCount.getOrDefault(userId, 0);
    }

    public String getStatus() {
        return "Проверено сообщений: " + messagesChecked + "\nУдалено сообщений: " + messagesDeleted;
    }

    // Добавление запрещённого слова (нормализуется перед добавлением)
    public void addBannedWord(String word) {
        bannedWords.add(normalizeWord(word));
    }

    // Удаление запрещённого слова
    public boolean removeBannedWord(String word) {
        return bannedWords.remove(normalizeWord(word));
    }

    // Возвращает текущий список запрещённых слов
    public Set<String> getBannedWords() {
        return bannedWords;
    }
}