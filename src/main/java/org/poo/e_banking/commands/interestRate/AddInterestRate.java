package org.poo.e_banking.commands.interestRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.SavingsAccount;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

public class AddInterestRate extends InterestRateBase {
    public AddInterestRate(final CommandInput commandInput,
                           final ArrayNode output) {
        super(commandInput, output);
    }

    /**
     * Process the savings account of the user by adding the interest rate to the balance.
     *
     * @param account The savings account of the user.
     * @param user    The user whose savings account is being processed.
     */
    @Override
    protected void processSavingsAccount(final SavingsAccount account, final User user) {
        ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());

        double interestRate = account.getInterestRate();
        outputNode.put("amount", account.getBalance() * interestRate);

        double newBalance = account.getBalance() + (account.getBalance() * interestRate);
        account.setBalance(newBalance);

        outputNode.put("currency", account.getCurrency());
        outputNode.put("description", "Interest rate income");
        outputNode.put("timestamp", commandInput.getTimestamp());

        user.getTransactionsNode().add(outputNode);
        account.getTransactionsNode().add(outputNode);
    }
}
