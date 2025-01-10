package org.poo.e_banking.InterestRate;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.SavingsAccount;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class ChangeInterestRate extends InterestRateBase {
    public ChangeInterestRate(final ArrayList<User> users, final CommandInput commandInput,
                              final ArrayNode output) {
        super(users, commandInput, output);
    }

    /**
     * Process the savings account of the user by changing the interest rate.
     *
     * @param account The savings account of the user.
     * @param user    The user whose savings account is being processed.
     */
    @Override
    protected void processSavingsAccount(final SavingsAccount account, final User user) {
        account.setInterestRate(commandInput.getInterestRate());

        ObjectNode successMessage = successOutput();
        user.getTransactionsNode().add(successMessage);
        account.getTransactionsNode().add(successMessage);
    }

    /**
     * Creates the success output node.
     *
     * @return the output node
     */
    private ObjectNode successOutput() {
        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description",
                "Interest rate of the account changed to " + commandInput.getInterestRate());
        outputNode.put("timestamp", commandInput.getTimestamp());
        return outputNode;
    }
}
