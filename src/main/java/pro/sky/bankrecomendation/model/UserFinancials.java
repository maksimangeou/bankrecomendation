package pro.sky.bankrecomendation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFinancials {

    private long cntDebitProducts;
    private long cntInvestProducts;
    private long cntCreditProducts;
    private double sumDebitDeposits;
    private double sumDebitSpent;
    private double sumSavingDeposits;
}
