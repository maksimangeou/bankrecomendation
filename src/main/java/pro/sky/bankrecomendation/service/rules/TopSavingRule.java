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
    public Optional<RecommendationDto> applyRuleSet(UUID userId, UserFinancials userFinancials) {
// Правила (все через AND между правилами):
// - пользователь использует как минимум один продукт типа DEBIT
// - (сумма пополнений по всем DEBIT >= 50000 OR сумма пополнений по SAVING >= 50000)
// - сумма пополнений по всем DEBIT > сумма трат по всем DEBIT


        boolean first = userFinancials.getCntDebitProducts() > 0;
        boolean second = (userFinancials.getSumDebitDeposits() >= 50_000.0) || (userFinancials.getSumSavingDeposits() >= 50_000.0);
        boolean third = userFinancials.getSumDebitDeposits() > userFinancials.getSumDebitSpent();


        if (first && second && third) {
            return Optional.of(new RecommendationDto(PRODUCT_ID, NAME, TEXT));
        }
        return Optional.empty();
    }
}
