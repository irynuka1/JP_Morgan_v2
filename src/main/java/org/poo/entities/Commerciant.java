package org.poo.entities;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter@Setter
public class Commerciant {
    private final CommerciantInput commerciantInput;
    private int nrOfTransactions;

    public Commerciant(final CommerciantInput commerciant) {
        this.commerciantInput = commerciant;
        this.nrOfTransactions = 0;
    }

    public String getCashbackStrategy() {
        return commerciantInput.getCashbackStrategy();
    }

    public void getCashBack(final double sum, final Account account, final User user,
                            final double amountInAccountCurrency) {
        account.setTotalSum(account.getTotalSum() + sum);
        double cashBackRate = calculateCashBackRate(user.getPlan(), account.getTotalSum());
        account.addFunds(amountInAccountCurrency * cashBackRate);
    }

    private double calculateCashBackRate(String plan, double totalSum) {
        if (totalSum >= 500) {
            return switch (plan) {
                case "silver" -> 0.005;
                case "gold" -> 0.007;
                default -> 0.0025;
            };
        } else if (totalSum >= 300) {
            return switch (plan) {
                case "silver" -> 0.004;
                case "gold" -> 0.0055;
                default -> 0.002;
            };
        } else if (totalSum >= 100) {
            return switch (plan) {
                case "silver" -> 0.003;
                case "gold" -> 0.005;
                default -> 0.001;
            };
        }

        return 0;
    }

    public void getCashBack(final Account account, final double amountInAccountCurrency) {
        if (isEligibleForCashBack("Food", 2, account.isCanGetFoodCashBack())) {
            applyCashBack(account, amountInAccountCurrency, 0.02);
            account.setCanGetFoodCashBack(false);
        } else if (isEligibleForCashBack("Clothes", 5, account.isCanGetClothesCashBack())) {
            applyCashBack(account, amountInAccountCurrency, 0.05);
            account.setCanGetClothesCashBack(false);
        } else if (isEligibleForCashBack("Tech", 10, account.isCanGetTechCashBack())) {
            applyCashBack(account, amountInAccountCurrency, 0.1);
            account.setCanGetTechCashBack(false);
        }

        nrOfTransactions++;
    }

    private boolean isEligibleForCashBack(String type, int requiredTransactions, boolean canGetCashBack) {
        return nrOfTransactions == requiredTransactions && commerciantInput.getType().equals(type) && canGetCashBack;
    }

    private void applyCashBack(Account account, double amountInAccountCurrency, double rate) {
        account.addFunds(amountInAccountCurrency * rate);
    }
}
