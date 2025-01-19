package org.poo.entities;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter@Setter
public final class Commerciant {
    private final CommerciantInput commerciantInput;
    private int nrOfTransactions;

    public Commerciant(final CommerciantInput commerciant) {
        this.commerciantInput = commerciant;
        this.nrOfTransactions = 0;
    }

    public String getCashbackStrategy() {
        return commerciantInput.getCashbackStrategy();
    }

    /**
     * Gets the cash back for a spendingThreshold commerciant
     *
     * @param amount the spent amount
     * @param account the account to which the sum will be added
     * @param plan the plan of the account
     * @param amountInAccountCurrency the amount in the account currency
     */
    public void getCashBack(final double amount, final Account account, final String plan,
                            final double amountInAccountCurrency) {
        account.setTotalSum(account.getTotalSum() + amount);
        double cashBackRate = calculateCashBackRate(plan, account.getTotalSum());
        account.addFunds(amountInAccountCurrency * cashBackRate);
    }

    private double calculateCashBackRate(final String plan, final double totalSum) {
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

    /**
     * Gets the cash back for a nrOfTransactions commerciant
     *
     * @param account the account to which the sum will be added
     * @param amountInAccountCurrency the amount in the account currency
     */
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

    private boolean isEligibleForCashBack(final String type, final int requiredTransactions,
                                          final boolean canGetCashBack) {
        return nrOfTransactions == requiredTransactions && commerciantInput.getType().equals(type)
                && canGetCashBack;
    }

    private void applyCashBack(final Account account, final double amountInAccountCurrency,
                               final double rate) {
        account.addFunds(amountInAccountCurrency * rate);
    }
}
