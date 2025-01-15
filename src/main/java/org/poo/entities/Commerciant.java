package org.poo.entities;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter@Setter
public class Commerciant {
    private final CommerciantInput commerciantInput;
    private int nrOfTransactions;
    private double totalSum;
    private boolean canGetFoodCashBack;
    private boolean canGetClothesCashBack;
    private boolean canGetTechCashBack;

    public Commerciant(final CommerciantInput commerciant) {
        this.commerciantInput = commerciant;
        this.nrOfTransactions = 0;
        this.totalSum = 0.0;
        this.canGetFoodCashBack = true;
        this.canGetClothesCashBack = true;
        this.canGetTechCashBack = true;
    }

    public void getCashBack(final double sum, final Account account, User user,
                            final double amountInAccountCurrency) {
        totalSum += sum;
        double cashBackRate = calculateCashBackRate(user.getPlan(), totalSum);
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

    public void getCashBack(final Account account, final double amountInAccountCurrency) {
        if (isEligibleForCashBack("Food", 2, canGetFoodCashBack)) {
            applyCashBack(account, amountInAccountCurrency, 0.02);
            canGetFoodCashBack = false;
        } else if (isEligibleForCashBack("Clothes", 5, canGetClothesCashBack)) {
            applyCashBack(account, amountInAccountCurrency, 0.05);
            canGetClothesCashBack = false;
        } else if (isEligibleForCashBack("Tech", 10, canGetTechCashBack)) {
            applyCashBack(account, amountInAccountCurrency, 0.1);
            canGetTechCashBack = false;
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
