package org.poo.entities;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter@Setter
public class Commerciant {
    private final CommerciantInput commerciantInput;
    private int nrOfTransactions;
    private double totalSum;
    private boolean canGetCashBack;

    public Commerciant(final CommerciantInput commerciant) {
        this.commerciantInput = commerciant;
        this.nrOfTransactions = 0;
        this.totalSum = 0.0;
        this.canGetCashBack = true;
    }

    public void getCashBack(final double sum, final Account account, final double amountInAccountCurrency) {
        if (commerciantInput.getCashbackStrategy().equals("spendingThreshold")) {
            totalSum += sum;
            if (totalSum >= 100 && totalSum < 300) {
                account.addFunds(amountInAccountCurrency * 0.001);
            } else if (totalSum >= 300 && totalSum < 500) {
                account.addFunds(amountInAccountCurrency * 0.002);
            } else if (totalSum >= 500) {
                account.addFunds(amountInAccountCurrency * 0.0025);
            }
        }
    }
}
