package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class WithdrawSavings implements Executable {
    private final CommandInput commandInput;
    private final ArrayList<User> users;
    private final ExchangeRateManager exchangeRateManager;

    public WithdrawSavings(final CommandInput commandInput, final ArrayList<User> users, final ExchangeRateManager exchangeRateManager) {
        this.commandInput = commandInput;
        this.users = users;
        this.exchangeRateManager = exchangeRateManager;
    }

    @Override
    public void execute() {
        User user = null;
        Account account = null;

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

        if (user.getUserAge(user.getBirthdate()) < 21) {
            notOldEnoughOutput(user);
            return;
        }

        double exchangeRate = exchangeRateManager.getExchangeRate(account.getCurrency(), commandInput.getCurrency());
        double amountToWithdraw = commandInput.getAmount() * exchangeRate;

        if (account.getBalance() < amountToWithdraw) {
            insufficientFundsOutput(user);
            return;
        }

        for (Account accountToDeposit : user.getAccounts()) {
            if (accountToDeposit.getType().equals("classic") && accountToDeposit.getCurrency().equals(commandInput.getCurrency())) {
                accountToDeposit.addFunds(commandInput.getAmount());
                account.setBalance(account.getBalance() - amountToWithdraw);

                successOutput(user);
                return;
            }
        }

        noClassicAccountOutput(user);
    }

    public void successOutput(User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "Savings withdrawal");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    public void noClassicAccountOutput(User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "You do not have a classic account.");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    public void insufficientFundsOutput(User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "Insufficient funds");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    public void notOldEnoughOutput(User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "You don't have the minimum age required.");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }

    public void wrongAccountTypeOutput(User user) {
        ObjectNode output = new ObjectNode(new ObjectMapper().getNodeFactory());
        output.put("description", "Account is not of type savings.");
        output.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(output);
    }
}
