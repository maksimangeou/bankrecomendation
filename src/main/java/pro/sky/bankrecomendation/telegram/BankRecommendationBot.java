package pro.sky.bankrecomendation.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.dto.RecommendationResponse;
import pro.sky.bankrecomendation.service.RecommendationService;

import java.util.List;
import java.util.UUID;

@Component
public class BankRecommendationBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(BankRecommendationBot.class);

    private final String botToken;
    private final String botName;
    private final RecommendationService recommendationService;
    private final JdbcTemplate jdbcTemplate;

    public BankRecommendationBot(String botToken, String botName,
                                 RecommendationService recommendationService,
                                 JdbcTemplate jdbcTemplate) {
        super(botToken);
        this.botToken = botToken;
        this.botName = botName;
        this.recommendationService = recommendationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();
            Long chatId = message.getChatId();

            log.debug("Received message from {}: {}", chatId, text);

            if (text.equals("/start")) {
                sendHelpMessage(chatId);
            } else if (text.startsWith("/recommend ")) {
                handleRecommendCommand(chatId, text);
            } else {
                sendUnknownCommandMessage(chatId);
            }
        }
    }

    private void handleRecommendCommand(Long chatId, String text) {
        try {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                sendMessage(chatId, "Пожалуйста, укажите имя пользователя: /recommend username");
                return;
            }

            String username = parts[1].trim();
            List<UUID> userIds = findUserIdsByUsername(username);

            if (userIds.isEmpty()) {
                sendMessage(chatId, "Пользователь не найден");
            } else if (userIds.size() > 1) {
                sendMessage(chatId, "Найдено несколько пользователей. Уточните запрос.");
            } else {
                UUID userId = userIds.get(0);
                String userName = getUserName(userId);
                RecommendationResponse response = recommendationService.getRecommendations(userId);

                String message = formatRecommendations(userName, response.getRecommendations());
                sendMessage(chatId, message);
            }

        } catch (Exception e) {
            log.error("Error processing recommend command", e);
            sendMessage(chatId, "Произошла ошибка при обработке запроса");
        }
    }

    private List<UUID> findUserIdsByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username ILIKE ?";
        return jdbcTemplate.queryForList(sql, UUID.class, username);
    }

    private String getUserName(UUID userId) {
        String sql = "SELECT first_name, last_name FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                return (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
            }
            return "Пользователь";
        }, userId.toString());
    }

    private String formatRecommendations(String userName, List<RecommendationDto> recommendations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Здравствуйте, ").append(userName).append("!\n\n");

        if (recommendations.isEmpty()) {
            sb.append("На данный момент у нас нет специальных предложений для вас.");
        } else {
            sb.append("Новые продукты для вас:\n");
            for (int i = 0; i < recommendations.size(); i++) {
                RecommendationDto rec = recommendations.get(i);
                sb.append(i + 1).append(". ").append(rec.getName()).append("\n");
                sb.append("   ").append(rec.getText()).append("\n\n");
            }
        }

        return sb.toString();
    }

    private void sendHelpMessage(Long chatId) {
        String helpText = "Добро пожаловать в банк рекомендаций!\n\n" +
                          "Доступные команды:\n" +
                          "/recommend username - получить рекомендации для пользователя\n" +
                          "/start - показать эту справку";
        sendMessage(chatId, helpText);
    }

    private void sendUnknownCommandMessage(Long chatId) {
        sendMessage(chatId, "Неизвестная команда. Используйте /start для просмотра справки.");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
            log.debug("Message sent to {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}", chatId, e);
        }
    }
}