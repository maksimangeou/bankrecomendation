package pro.sky.bankrecomendation.service.rules;

import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.RecommendationRuleSet;

import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleCreditRule implements RecommendationRuleSet {


    private static final UUID PRODUCT_ID = UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");
    private static final String NAME = "Простой кредит";
    private static final String TEXT = "Откройте мир выгодных кредитов с нами!";


    @Override
    public Optional<RecommendationDto> apply(UUID userId, UserFinancials m) {
// Правила:
// - пользователь не использует продукты типа CREDIT
// - сумма пополнений по всем DEBIT > сумма трат по всем DEBIT
// - сумма трат по всем DEBIT > 100_000


        if (m.getCntCreditProducts() == 0 && m.getSumDebitDeposits() > m.getSumDebitSpent() && m.getSumDebitSpent() > 100_000.0) {
            return Optional.of(new RecommendationDto(PRODUCT_ID, NAME, TEXT));
        }
        return Optional.empty();
    }
}
