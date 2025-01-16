package org.poo.entities;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter@Setter
public class Commerciant {
    private final CommerciantInput commerciantInput;

    public Commerciant(final CommerciantInput commerciant) {
        this.commerciantInput = commerciant;
    }

    public String getCashbackStrategy() {
        return commerciantInput.getCashbackStrategy();
    }

    public void getCashBack(final double sum, final Account account, User user,
                            final double amountInAccountCurrency) {
        user.setTotalSum(user.getTotalSum() + sum);
        double cashBackRate = calculateCashBackRate(user.getPlan(), user.getTotalSum());
        account.addFunds(amountInAccountCurrency * cashBackRate);
    }

    private double calculateCashBackRate(String plan, double totalSum) {
        if (totalSum >= 500) {
            return switch (plan) {
                case "silver" -> 0.005;
                case "gold", "premium" -> 0.007;
                default -> 0.0025;
            };
        } else if (totalSum >= 300) {
            return switch (plan) {
                case "silver" -> 0.004;
                case "gold", "premium" -> 0.0055;
                default -> 0.002;
            };
        } else if (totalSum >= 100) {
            return switch (plan) {
                case "silver" -> 0.003;
                case "gold", "premium" -> 0.005;
                default -> 0.001;
            };
        }

        return 0;
    }

    public void getCashBack(final User user, final Account account, final double amountInAccountCurrency) {
        if (isEligibleForCashBack("Food", 2, user.isCanGetFoodCashBack(), user.getNrOfTransactions())) {
            applyCashBack(account, amountInAccountCurrency, 0.02);
            user.setCanGetFoodCashBack(false);
        } else if (isEligibleForCashBack("Clothes", 5, user.isCanGetClothesCashBack(), user.getNrOfTransactions())) {
            applyCashBack(account, amountInAccountCurrency, 0.05);
            user.setCanGetClothesCashBack(false);
        } else if (isEligibleForCashBack("Tech", 10, user.isCanGetTechCashBack(), user.getNrOfTransactions())) {
            applyCashBack(account, amountInAccountCurrency, 0.1);
            user.setCanGetTechCashBack(false);
        }

        user.setNrOfTransactions(user.getNrOfTransactions() + 1);
    }

    private boolean isEligibleForCashBack(String type, int requiredTransactions, boolean canGetCashBack, int nrOfTransactions) {
        return nrOfTransactions == requiredTransactions && commerciantInput.getType().equals(type) && canGetCashBack;
    }

    private void applyCashBack(Account account, double amountInAccountCurrency, double rate) {
        account.addFunds(amountInAccountCurrency * rate);
    }
}
