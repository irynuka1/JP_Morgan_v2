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
        totalSum += sum;
        if (totalSum >= 100 && totalSum < 300) {
            if (account.getPlan().equals("standard") || account.getPlan().equals("student")) {
                account.addFunds(amountInAccountCurrency * 0.001);
            } else if (account.getPlan().equals("silver")) {
                account.addFunds(amountInAccountCurrency * 0.003);
            } else {
                account.addFunds(amountInAccountCurrency * 0.005);
            }
        } else if (totalSum >= 300 && totalSum < 500) {
            if (account.getPlan().equals("standard") || account.getPlan().equals("student")) {
                account.addFunds(amountInAccountCurrency * 0.002);
            } else if (account.getPlan().equals("silver")) {
                account.addFunds(amountInAccountCurrency * 0.004);
            } else {
                account.addFunds(amountInAccountCurrency * 0.0055);
            }
        } else if (totalSum >= 500) {
            if (account.getPlan().equals("standard") || account.getPlan().equals("student")) {
                account.addFunds(amountInAccountCurrency * 0.0025);
            } else if (account.getPlan().equals("silver")) {
                account.addFunds(amountInAccountCurrency * 0.005);
            } else {
                account.addFunds(amountInAccountCurrency * 0.007);
            }
        }
    }

    public void getCashBack(final Account account, final double amountInAccountCurrency) {
        if (canGetCashBack) {
            if (nrOfTransactions == 2 && commerciantInput.getType().equals("Food")) {
                account.addFunds(amountInAccountCurrency * 0.02);
                canGetCashBack = false;
                return;
            }

            if (nrOfTransactions == 5 && commerciantInput.getType().equals("Clothes")) {
                account.addFunds(amountInAccountCurrency * 0.05);
                canGetCashBack = false;
                return;
            }

            if (nrOfTransactions == 10 && commerciantInput.getType().equals("Tech")) {
                account.addFunds(amountInAccountCurrency * 0.1);
                canGetCashBack = false;
                return;
            }

            nrOfTransactions++;
        }
    }
}
