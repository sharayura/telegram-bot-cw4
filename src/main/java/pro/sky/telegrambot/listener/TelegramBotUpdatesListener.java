package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern NOTIFICATION_PATTERN = Pattern.compile(
            "([\\d\\\\.:\\s]{16})(\\s)([A-zА-я\\s\\d,.!?:]+)");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskService notificationTaskService;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                String text = update.message().text();
                if ("/start".equals(text)) {
                    SendMessage sendMessage = new SendMessage(update.message().chat().id(), "Привет!");
                    sendMessage.parseMode(ParseMode.Markdown);
                    SendResponse response = telegramBot.execute(sendMessage);
                } else if (text != null) {
                    Matcher matcher = NOTIFICATION_PATTERN.matcher(text);
                    LocalDateTime dateTime;
                    Long chatId = update.message().chat().id();
                    if (matcher.matches() && (dateTime = parse(matcher.group(1))) != null) {
                        String textMessage = matcher.group(3);
                        notificationTaskService.addNotificationTask(dateTime,textMessage,chatId);
                        telegramBot.execute(new SendMessage(chatId,"Задача принята!"));
                    }else telegramBot.execute(new SendMessage(chatId,
                            "Неверный формат даты или времени, пример: 18.01.2023 16:30 Важнейшее совещание"));
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
