package pro.sky.bankrecomendation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import pro.sky.bankrecomendation.telegram.BankRecommendationBot;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.name}")
    private String botName;

    @Bean
    public TelegramBotsApi telegramBotsApi(BankRecommendationBot bot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            return botsApi;
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to register Telegram bot", e);
        }
    }

    @Bean
    public BankRecommendationBankRecommendationBot bankRecommendationBot() {
        return new BankRecommendationBot(botToken, botName);
    }
}