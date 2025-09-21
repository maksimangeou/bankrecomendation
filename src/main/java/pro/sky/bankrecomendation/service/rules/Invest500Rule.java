package pro.sky.bankrecomendation.service.rules;

import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.RecommendationRuleSet;


import java.util.Optional;
import java.util.UUID;


@Component
public class Invest500Rule implements RecommendationRuleSet {


    private static final UUID PRODUCT_ID = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");
    private static final String NAME = "Invest 500";
    private static final String TEXT = "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка!";


    @Override
    public Optional<RecommendationDto> apply(UUID userId, UserFinancials m) {
// Правила:
// 1) пользователь использует как минимум один продукт типа DEBIT
// 2) пользователь не использует продукты типа INVEST
// 3) сумма пополнений по продуктам типа SAVING > 1000 ₽
        if (m.getCntDebitProducts() > 0 && m.getCntInvestProducts() == 0 && m.getSumSavingDeposits() > 1000.0) {
            return Optional.of(new RecommendationDto(PRODUCT_ID, NAME, TEXT));
        }
        return Optional.empty();
    }
}

