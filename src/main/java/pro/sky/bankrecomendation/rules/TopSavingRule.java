package pro.sky.bankrecomendation.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.RecommendationRuleSet;

import java.util.Optional;
import java.util.UUID;

@Component
public class TopSavingRule implements RecommendationRuleSet {

    private static final Logger logger = LoggerFactory.getLogger(TopSavingRule.class);

    // UUID продукта "Накопительный счет"
    private static final UUID PRODUCT_ID = UUID.fromString("f84e92c7-7ef0-40ac-8a9e-0e13b991c21d");

    @Override
    public Optional<RecommendationDto> applyRuleSet(UUID userId, UserFinancials metrics) {
        logger.info("Применение правила TopSavingRule для пользователя {}", userId);

        if (metrics.getSumSavingDeposits() == 0 && metrics.getSumDebitDeposits() >= 50_000.0) {

            logger.info("Рекомендован сберегательный продукт: Накопительный счет для пользователя {}", userId);

            return Optional.of(new RecommendationDto(
                    PRODUCT_ID,
                    "Накопительный счёт",
                    "Начните копить с повышенной процентной ставкой и гибкими условиями!"
            ));
        }

        logger.info("Правило TopSavingRule не сработало для пользователя {}", userId);
        return Optional.empty();
    }
}
