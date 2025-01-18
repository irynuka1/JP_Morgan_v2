package org.poo.e_banking.Comands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Comission;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class WithdrawSavings implements Executable {
    private final CommandInput commandInput;

    public WithdrawSavings(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    @Override
    public void execute() {
        final int minAge = 21;
        User user = null;
        Account account = null;
        AppLogic appLogic = AppLogic.getInstance();
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();
        ArrayList<User> users = appLogic.getUsers();

        for (User u : users) {
            if (u.getAccountByIban(commandInput.getAccount()) != null) {
                user = u;
                account = u.getAccountByIban(commandInput.getAccount());
                break;
            }
        }

        if (user == null || account == null) {
            ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
            output.put("description", "Account not found");
            output.put("timestamp", commandInput.getTimestamp());
            return;
        }

        if (!account.getType().equals("savings")) {
            wrongAccountTypeOutput(user);
            return;
        }

        if (user.getUserAge(user.getBirthdate()) < minAge) {
            notOldEnoughOutput(user);
            return;
        }

        double exchangeRate = exchangeManager.getExchangeRate(account.getCurrency(),
                commandInput.getCurrency());
        double amountToWithdraw = commandInput.getAmount() * exchangeRate;
        double exchangeToRON = exchangeManager.getExchangeRate(commandInput.getCurrency(), "RON");
        double amountRON = commandInput.getAmount() * exchangeToRON;
        double commission = Comission.getComission(user, amountRON);

        if (account.getBalance() < amountToWithdraw + amountToWithdraw * commission) {
            insufficientFundsOutput(user);
            return;
        }

        for (Account accountToDeposit : user.getAccounts()) {
            if (accountToDeposit.getType().equals("classic")
                    && accountToDeposit.getCurrency().equals(commandInput.getCurrency())) {
                accountToDeposit.addFunds(commandInput.getAmount());
                account.setBalance(account.getBalance()
                        - (amountToWithdraw + amountToWithdraw * commission));

                successOutput(user);
                return;
            }
        }

        noClassicAccountOutput(user);
    }

    /**
     * Creates a success transaction output
     * @param user the user that made the withdrawal
     */
    public void successOutput(final User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "Savings withdrawal");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    /**
     * Creates a transaction output for the case when the user does not have a classic account
     * @param user the user that made the withdrawal
     */
    public void noClassicAccountOutput(final User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "You do not have a classic account.");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    /**
     * Creates a transaction output for the case when the user does not have enough funds
     * @param user the user that made the withdrawal
     */
    public void insufficientFundsOutput(final User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "Insufficient funds");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    /**
     * Creates a transaction output for the case when the user is under 21
     * @param user the user that made the withdrawal
     */
    public void notOldEnoughOutput(final User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "You don't have the minimum age required.");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    /**
     * Creates a transaction output for the case when the account is not of type savings
     * @param user the user that made the withdrawal
     */
    public void wrongAccountTypeOutput(final User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "Account is not of type savings.");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }
}
