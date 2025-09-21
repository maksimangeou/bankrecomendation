package pro.sky.bankrecomendation.service.rules;

import org.springframework.stereotype.Component;
import pro.sky.bankrecomendation.dto.RecommendationDto;
import pro.sky.bankrecomendation.model.UserFinancials;
import pro.sky.bankrecomendation.service.RecommendationRuleSet;

import java.util.Optional;
import java.util.UUID;

@Component
public class TopSavingRule implements RecommendationRuleSet {


    private static final UUID PRODUCT_ID = UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");
    private static final String NAME = "Top Saving";
    private static final String TEXT = "Откройте свою собственную «Копилку" + " с нашим банком!";


    @Override
    public Optional<RecommendationDto> apply(UUID userId, UserFinancials m) {
// Правила (все через AND между правилами):
// - пользователь использует как минимум один продукт типа DEBIT
// - (сумма пополнений по всем DEBIT >= 50000 OR сумма пополнений по SAVING >= 50000)
// - сумма пополнений по всем DEBIT > сумма трат по всем DEBIT


        boolean first = m.getCntDebitProducts() > 0;
        boolean second = (m.getSumDebitDeposits() >= 50_000.0) || (m.getSumSavingDeposits() >= 50_000.0);
        boolean third = m.getSumDebitDeposits() > m.getSumDebitSpent();


        if (first && second && third) {
            return Optional.of(new RecommendationDto(PRODUCT_ID, NAME, "Откройте свою собственную «Копилку» с нашим банком!"));
        }
        return Optional.empty();
    }
}
