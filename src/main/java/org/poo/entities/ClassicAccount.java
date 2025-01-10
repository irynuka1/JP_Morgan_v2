package org.poo.entities;

public class ClassicAccount extends Account {
    public ClassicAccount(final String userEmail, final String currency) {
        super(userEmail, currency);
        this.setType("classic");
    }
}
