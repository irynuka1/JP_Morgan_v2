package org.poo.entities;

public final class AccountFactory {
    private AccountFactory() {
    }

    /**
     * This method is used to create a specific account.
     * @param accountType The type of the account.
     * @param userEmail The email of the user.
     * @param currency The currency of the account.
     * @param interestRate The interest rate of the account.
     * @return Returns the created account.
     */
    public static Account createAccount(final String accountType, final String userEmail,
                                        final String currency, final double interestRate) {
        if (accountType.equalsIgnoreCase("classic")) {
            return new ClassicAccount(userEmail, currency);
        } else if (accountType.equalsIgnoreCase("savings")) {
            return new SavingsAccount(userEmail, currency, interestRate);
        }

        return null;
    }
}
