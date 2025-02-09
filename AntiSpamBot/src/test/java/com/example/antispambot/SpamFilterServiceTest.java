package com.example.antispambot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.telegram.telegrambots.meta.api.objects.*;

public class SpamFilterServiceTest {

    private SpamFilterService spamFilterService;

    @Before
    public void setUp() {
        spamFilterService = new SpamFilterService();
    }

    @Test
    public void testNonSpamMessage() {
        Update update = createUpdate("Привет, как дела?", 1L);
        boolean isSpam = spamFilterService.isSpam(update);
        Assert.assertFalse("Сообщение не должно определяться как спам", isSpam);
    }

    @Test
    public void testSpamMessageDueToBannedWord() {
        Update update = createUpdate("Это сообщение содержит spamword1", 2L);
        boolean isSpam = spamFilterService.isSpam(update);
        Assert.assertTrue("Сообщение должно определяться как спам из-за запрещённого слова", isSpam);
    }

    @Test
    public void testSpamMessageDueToRapidSending() {
        Update update1 = createUpdate("Первое сообщение", 3L);
        boolean isSpam1 = spamFilterService.isSpam(update1);
        Assert.assertFalse("Первое сообщение не должно быть спамом", isSpam1);

        Update update2 = createUpdate("Второе сообщение", 3L);
        boolean isSpam2 = spamFilterService.isSpam(update2);
        Assert.assertTrue("Второе сообщение должно определяться как спам из-за быстроты отправки", isSpam2);
    }

    private Update createUpdate(String text, Long userId) {
        Update update = new Update();

        User user = new User();
        user.setId(userId);
        user.setUserName("testUser" + userId);

        Chat chat = new Chat();
        chat.setId(100L);

        Message message = new Message();
        message.setMessageId(1);
        message.setText(text);
        message.setChat(chat);
        message.setFrom(user);

        update.setMessage(message);
        return update;
    }
}