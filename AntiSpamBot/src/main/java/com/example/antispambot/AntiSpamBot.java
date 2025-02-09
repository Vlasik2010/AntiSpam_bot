package com.example.antispambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;

/**
 * Основной класс бота.
 * Реализованы обработка команд, inline‑клавиатура, редактирование списка запрещённых слов,
 * а также фуззи‑сравнение для обнаружения завуалированных сообщений.
 */
public class AntiSpamBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(AntiSpamBot.class);
    private final BotConfig config = BotConfig.getInstance();
    private final SpamFilterService spamService = new SpamFilterService();

    // Состояния редактирования списка запрещённых слов для каждого пользователя
    private enum EditState { NONE, WAITING_FOR_ADD, WAITING_FOR_REMOVE }
    private final Map<Long, EditState> editingStates = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String messageText = update.getMessage().getText();
            logger.info("Получено сообщение: '{}' от пользователя: {}", messageText, userId);

            // Если пользователь находится в режиме редактирования запрещённых слов, обрабатываем ввод
            if (editingStates.getOrDefault(userId, EditState.NONE) != EditState.NONE) {
                processEditingInput(update);
                return;
            }

            if (messageText.startsWith("/")) {
                processCommand(update);
            } else {
                if (spamService.isSpam(update)) {
                    deleteSpamMessage(update);
                    int spamCount = spamService.getSpamCount(userId);
                  //  sendTextMessage(chatId, "Пожалуйста, не спамьте! (" + spamCount + " предупреждение)");
                }
            }
        }
    }

    private void processCommand(Update update) {
        String command = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        switch (command) {
            case "/start":
                sendTextMessage(chatId, "Привет! Я анти-спам бот. Используйте /help для получения списка команд.");
                break;
            case "/help":
                sendHelpMessage(chatId);
                break;
            case "/status":
                sendTextMessage(chatId, spamService.getStatus());
                break;
            case "/editbanned":
                sendEditBannedMenu(chatId);
                break;
            default:
                sendTextMessage(chatId, "Неизвестная команда. Используйте /help для получения списка команд.");
        }
    }

    // Обработка callback-запросов от inline-кнопок
    private void processCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        if (data.equals("show_commands")) {
            sendTextMessage(chatId, "Команды:\n" +
                    "/start - Приветствие\n" +
                    "/help - Список команд\n" +
                    "/status - Статистика\n" +
                    "/editbanned - Редактировать запрещённые слова");
        } else if (data.equals("show_settings")) {
            sendSettings(chatId);
        } else if (data.equals("edit_add")) {
            editingStates.put(userId, EditState.WAITING_FOR_ADD);
            sendTextMessage(chatId, "Введите слово, которое хотите добавить в список запрещённых:");
        } else if (data.equals("edit_remove")) {
            editingStates.put(userId, EditState.WAITING_FOR_REMOVE);
            sendTextMessage(chatId, "Введите слово, которое хотите удалить из списка запрещённых:");
        }

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQuery.getId());
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке ответа на callback: ", e);
        }
    }

    // Обработка ввода пользователя в режиме редактирования списка запрещённых слов
    private void processEditingInput(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String input = update.getMessage().getText().trim();
        EditState state = editingStates.getOrDefault(userId, EditState.NONE);
        if (state == EditState.WAITING_FOR_ADD) {
            spamService.addBannedWord(input);
            sendTextMessage(chatId, "Слово '" + input + "' добавлено в список запрещённых.");
        } else if (state == EditState.WAITING_FOR_REMOVE) {
            boolean removed = spamService.removeBannedWord(input);
            if (removed) {
                sendTextMessage(chatId, "Слово '" + input + "' удалено из списка запрещённых.");
            } else {
                sendTextMessage(chatId, "Слово '" + input + "' не найдено в списке запрещённых.");
            }
        }
        editingStates.put(userId, EditState.NONE);
    }

    private void sendHelpMessage(long chatId) {
        String helpText = "Доступные команды:\n" +
                "/start - Приветственное сообщение\n" +
                "/help - Список команд\n" +
                "/status - Статистика работы бота\n" +
                "/editbanned - Редактировать список запрещённых слов";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton commandsBtn = new InlineKeyboardButton();
        commandsBtn.setText("Команды");
        commandsBtn.setCallbackData("show_commands");

        InlineKeyboardButton settingsBtn = new InlineKeyboardButton();
        settingsBtn.setText("Настройки");
        settingsBtn.setCallbackData("show_settings");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(commandsBtn);
        row.add(settingsBtn);
        keyboard.add(row);
        markup.setKeyboard(keyboard);

        sendTextMessage(chatId, helpText, markup);
    }

    private void sendSettings(long chatId) {
        String settingsText = "Настройки анти-спам фильтра:\n" +
                "Порог предупреждений: " + SpamFilterService.SPAM_THRESHOLD + "\n" +
                "Запрещённые слова: " + spamService.getBannedWords() + "\n" +
                "Время между сообщениями: 2000 мс";
        sendTextMessage(chatId, settingsText);
    }

    private void sendEditBannedMenu(long chatId) {
        String text = "Редактирование списка запрещённых слов. Выберите действие:";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton addBtn = new InlineKeyboardButton();
        addBtn.setText("Добавить слово");
        addBtn.setCallbackData("edit_add");

        InlineKeyboardButton removeBtn = new InlineKeyboardButton();
        removeBtn.setText("Удалить слово");
        removeBtn.setCallbackData("edit_remove");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addBtn);
        row.add(removeBtn);
        keyboard.add(row);
        markup.setKeyboard(keyboard);

        sendTextMessage(chatId, text, markup);
    }

    private void sendTextMessage(long chatId, String text) {
        sendTextMessage(chatId, text, null);
    }

    private void sendTextMessage(long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (markup != null) {
            message.setReplyMarkup(markup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка отправки сообщения: ", e);
        }
    }

    private void deleteSpamMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
            logger.info("Сообщение с id {} удалено из чата {}", messageId, chatId);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при удалении сообщения с id {}: ", messageId, e);
        }
    }

    @Override
    public String getBotUsername() {
        String botUsername = config.getProperty("bot.username");
        if (botUsername == null || botUsername.trim().isEmpty()) {
            throw new RuntimeException("bot.username не задан в конфигурации.");
        }
        return botUsername;
    }

    @Override
    public String getBotToken() {
        String token = config.getProperty("bot.token");
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("bot.token не задан в конфигурации.");
        }
        return token;
    }
}