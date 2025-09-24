package pro.sky.bankrecomendation.service.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.RecommendationRuleSet;

import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleCreditRule implements RecommendationRuleSet {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCreditRule.class);

    // UUID продукта "Простой кредит"
    private static final UUID PRODUCT_ID = UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");

    @Override
    public Optional<RecommendationDto> applyRuleSet(UUID userId, UserFinancials metrics) {
        logger.info("Применение правила SimpleCreditRule для пользователя {}", userId);

        if (metrics.getCntCreditProducts() == 0 &&
            metrics.getSumDebitDeposits() > 0) {

            logger.info("Рекомендован кредитный продукт: Простой кредит для пользователя {}", userId);

            return Optional.of(new RecommendationDto(
                    PRODUCT_ID,
                    "Простой кредит",
                    "Откройте мир выгодных кредитов с нами!"
            ));
        }

        logger.info("Правило SimpleCreditRule не сработало для пользователя {}", userId);
        return Optional.empty();
    }
}
