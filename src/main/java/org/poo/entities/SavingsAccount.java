package org.poo.entities;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String userEmail, final String currency,
                          final double interestRate) {
        super(userEmail, currency);
        this.setType("savings");
        this.interestRate = interestRate;
    }
}
