package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class SendMoney implements Executable {
    private final CommandInput commandInput;

    public SendMoney(final CommandInput commandInput) {
        this.commandInput = commandInput;
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
        if (senderAccount == null || receiverAccount == null) {
            return;
        }

        if (senderAccount.withdrawFunds(commandInput.getAmount())) {
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
