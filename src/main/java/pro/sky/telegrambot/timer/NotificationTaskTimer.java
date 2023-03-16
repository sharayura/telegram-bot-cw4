package pro.sky.telegrambot.timer;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.repositories.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;

@Component
public class NotificationTaskTimer {
    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;

    public NotificationTaskTimer(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void findNotificationTasks() {
        notificationTaskService.findNotificationNow().forEach(notificationTask -> {
            telegramBot.execute(new SendMessage(notificationTask.getChatId(),
                    "Напоминаю: " + notificationTask.getMessage()));
        });
    }
}
