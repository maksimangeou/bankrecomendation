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
public class Invest500Rule implements RecommendationRuleSet {

    private static final Logger log = LoggerFactory.getLogger(Invest500Rule.class);

    private static final UUID PRODUCT_ID = UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");
    private static final String NAME = "Invest 500";
    private static final String TEXT = "Откройте свой путь к успеху с индивидуальным инвестиционным счетом (ИИС) от нашего банка!";

    @Override
    public Optional<RecommendationDto> applyRuleSet(UUID userId, UserFinancials metrics) {
        boolean hasDebit = metrics.getCntDebitProducts() > 0;
        boolean noInvest = metrics.getCntInvestProducts() == 0;
        boolean savingGt1000 = metrics.getSumSavingDeposits() > 1000.0;

        log.trace("Invest500Rule for user {}: hasDebit={}, noInvest={}, savingGt1000={}",
                userId, hasDebit, noInvest, savingGt1000);

        if (hasDebit && noInvest && savingGt1000) {
            RecommendationDto dto = new RecommendationDto(PRODUCT_ID, NAME, TEXT);
            log.info("Invest500Rule matched for user {} -> {}", userId, dto);
            return Optional.of(dto);
        }
        return Optional.empty();
    }
}
