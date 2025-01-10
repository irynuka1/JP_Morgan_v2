package org.poo.e_banking.InterestRate;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.SavingsAccount;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class AddInterestRate extends InterestRateBase {
    public AddInterestRate(final ArrayList<User> users, final CommandInput commandInput,
                           final ArrayNode output) {
        super(users, commandInput, output);
    }

    /**
     * Process the savings account of the user by adding the interest rate to the balance.
     *
     * @param account The savings account of the user.
     * @param user    The user whose savings account is being processed.
     */
    @Override
    protected void processSavingsAccount(final SavingsAccount account, final User user) {
        double interestRate = account.getInterestRate();
        double newBalance = account.getBalance() + (account.getBalance() * interestRate);
        account.setBalance(newBalance);
    }
}
