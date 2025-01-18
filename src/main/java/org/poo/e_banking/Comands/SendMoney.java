package org.poo.e_banking.Comands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Comission;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class SendMoney implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;

    public SendMoney(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        ArrayList<User> users = appLogic.getUsers();
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();

        Account senderAccount = null;
        Account receiverAccount = null;
        User sender = null;
        User receiver = null;

        for (User user : users) {
            if (user.getAccountByIban(commandInput.getAccount()) != null) {
                senderAccount = user.getAccountByIban(commandInput.getAccount());
                sender = user;
            } else if (user.getAccountByAlias(commandInput.getAlias()) != null) {
                senderAccount = user.getAccountByAlias(commandInput.getAlias());
                sender = user;
            }

            if (user.getAccountByIban(commandInput.getReceiver()) != null) {
                receiverAccount = user.getAccountByIban(commandInput.getReceiver());
                receiver = user;
            } else if (user.getAccountByAlias(commandInput.getAlias()) != null) {
                receiverAccount = user.getAccountByAlias(commandInput.getAlias());
                receiver = user;
            }
        }

        processTransaction(senderAccount, receiverAccount, sender, receiver, exchangeManager);
    }

    /**
     * Processes the transaction between two accounts.
     * @param senderAccount The account that sends the money.
     * @param receiverAccount The account that receives the money.
     * @param sender The user that sends the money.
     * @param receiver The user that receives the money.
     */
    public void processTransaction(final Account senderAccount, final Account receiverAccount,
                                   final User sender, final User receiver,
                                   final ExchangeRateManager exchangeManager) {
        if (sender == null || receiver == null) {
            ObjectNode wrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
            wrapper.put("command", commandInput.getCommand());
            ObjectNode outputNode = wrapper.putObject("output");
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", commandInput.getTimestamp());
            wrapper.put("timestamp", commandInput.getTimestamp());
            output.add(wrapper);
            return;
        }

        if (senderAccount == null || receiverAccount == null) {
            return;
        }

        double exchangeToRON = exchangeManager.getExchangeRate(senderAccount.getCurrency(), "RON");
        double amountInRON = commandInput.getAmount() * exchangeToRON;
        double commission = Comission.getComission(sender, amountInRON);

        if (senderAccount.withdrawFunds(commandInput.getAmount() + commandInput.getAmount() * commission)) {
            double exchangeRate = exchangeManager.getExchangeRate(senderAccount.getCurrency(),
                    receiverAccount.getCurrency());
            double amountInReceiverCurrency = commandInput.getAmount() * exchangeRate;
            receiverAccount.addFunds(amountInReceiverCurrency);

            logTransaction(sender, senderAccount, successOutput("sent", senderAccount,
                    receiverAccount,
                    commandInput.getAmount() + " " + senderAccount.getCurrency()));
            logTransaction(receiver, receiverAccount, successOutput("received", senderAccount,
                    receiverAccount,
                    amountInReceiverCurrency + " " + receiverAccount.getCurrency()));
        } else {
            logTransaction(sender, senderAccount, failedOutput());
        }
    }

    /**
     * Creates a node with the details of a successful transaction.
     * @param type The type of the transaction.
     * @param senderAccount The account that sends the money.
     * @param receiverAccount The account that receives the money.
     * @param amount The amount of money.
     * @return The created node.
     */
    public ObjectNode successOutput(final String type, final Account senderAccount,
                                    final Account receiverAccount, final String amount) {
        ObjectNode sendMoneyWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        sendMoneyWrapper.put("timestamp", commandInput.getTimestamp());
        sendMoneyWrapper.put("description", commandInput.getDescription());
        sendMoneyWrapper.put("senderIBAN", senderAccount.getIban());
        sendMoneyWrapper.put("receiverIBAN", receiverAccount.getIban());
        sendMoneyWrapper.put("amount", amount);
        sendMoneyWrapper.put("transferType", type);
        return sendMoneyWrapper;
    }

    /**
     * Creates a node with the details in case of insufficient funds.
     * @return The created node.
     */
    public ObjectNode failedOutput() {
        ObjectNode sendMoneyWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        sendMoneyWrapper.put("timestamp", commandInput.getTimestamp());
        sendMoneyWrapper.put("description", "Insufficient funds");
        return sendMoneyWrapper;
    }

    /**
     * Adds the transaction in the user's and account's transaction history.
     * @param user The user that initiated the transaction.
     * @param account The account that initiated the transaction.
     * @param transaction The transaction to be logged.
     */
    public void logTransaction(final User user, final Account account,
                               final ObjectNode transaction) {
        user.getTransactionsNode().add(transaction);
        account.getTransactionsNode().add(transaction);
    }
}
