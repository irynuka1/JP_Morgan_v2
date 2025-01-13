package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SplitPayment implements Executable {
    private final CommandInput commandInput;

    public SplitPayment(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        Map<String, User> userMap = appLogic.getUserMap();
        ArrayList<User> users = appLogic.getUsers();
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();

        int numberOfParticipants = commandInput.getAccounts().size();
        double amountPerParticipant = commandInput.getAmount() / numberOfParticipants;
        List<Account> accounts = getParticipatingAccounts(users);
        Account insufficientFundsAcc = checkAccountsBalance(accounts, amountPerParticipant, exchangeManager);

        if (insufficientFundsAcc == null) {
            processSuccessfulPayment(accounts, amountPerParticipant, exchangeManager, userMap);
        } else {
            processFailedPayment(accounts, amountPerParticipant, insufficientFundsAcc, userMap);
        }
    }

    /**
     * Gets the accounts involved in the split payment.
     * @return The accounts involved in the split payment.
     */
    public List<Account> getParticipatingAccounts(final ArrayList<User> users) {
        List<Account> accounts = new ArrayList<>();

        for (String iban : commandInput.getAccounts()) {
            for (User user : users) {
                Account account = user.getAccountByIban(iban);
                if (account != null) {
                    accounts.add(account);
                    break;
                }
            }
        }

        return accounts;
    }

    /**
     * Checks if the accounts involved in the split payment have enough funds to pay the amount.
     * @param accounts The accounts involved in the split payment.
     * @param amountPerParticipant The amount each participant was supposed to pay.
     * @return The last account that had insufficient funds, or null if all accounts had
     * enough funds.
     */
    public Account checkAccountsBalance(final List<Account> accounts,
                                        final double amountPerParticipant,
                                        final ExchangeRateManager exchangeManager) {
        Account insufficientFundsAcc = null;
        for (Account account : accounts) {
            double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(),
                    account.getCurrency());
            double amountInAccCurrency = amountPerParticipant * exchangeRate;

            if (account.getBalance() - amountInAccCurrency < 0) {
                insufficientFundsAcc = account;
            }
        }
        return insufficientFundsAcc;
    }

    /**
     * Processes a successful payment by withdrawing the amount from each participant's account
     * and adding the successful transaction to the transactions node of each participant.
     * @param accounts The accounts involved in the split payment.
     * @param amountPerParticipant The amount each participant was supposed to pay.
     */
    private void processSuccessfulPayment(final List<Account> accounts,
                                          final double amountPerParticipant,
                                          final ExchangeRateManager exchangeManager,
                                          final Map<String, User> userMap) {
        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(),
                    account.getCurrency());
            double amountInAccountCurrency = amountPerParticipant * exchangeRate;
            account.withdrawFunds(amountInAccountCurrency);
            user.getTransactionsNode().add(successOutput(amountPerParticipant));
        }
    }

    /**
     * Processes a failed payment by adding the failed transaction to the transactions node
     * of each participant.
     * @param accounts The accounts involved in the split payment.
     * @param amountPerParticipant The amount each participant was supposed to pay.
     * @param insufficientFundsAcc The account that had insufficient funds.
     */
    private void processFailedPayment(final List<Account> accounts,
                                      final double amountPerParticipant,
                                      final Account insufficientFundsAcc,
                                      final Map<String, User> userMap) {
        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getTransactionsNode().add(failedOutput(amountPerParticipant,
                    insufficientFundsAcc));
            account.getTransactionsNode().add(failedOutput(amountPerParticipant,
                    insufficientFundsAcc));
        }
    }

    /**
     * Adds the accounts involved in the split payment to the split payment node.
     * @param splitPaymentWrapper The node containing the split payment information.
     */
    public void addInvolvedAccounts(final ObjectNode splitPaymentWrapper) {
        ArrayNode accountsNode = splitPaymentWrapper.putArray("involvedAccounts");
        for (String account : commandInput.getAccounts()) {
            accountsNode.add(account);
        }
    }

    /**
     * Creates a node containing the information of a successful split payment.
     * @param amountPerParticipant The amount each participant was supposed to pay.
     * @return The created node.
     */
    public ObjectNode successOutput(final double amountPerParticipant) {
        ObjectNode splitPaymentWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        splitPaymentWrapper.put("timestamp", commandInput.getTimestamp());
        splitPaymentWrapper.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        splitPaymentWrapper.put("currency", commandInput.getCurrency());
        splitPaymentWrapper.put("amount", amountPerParticipant);
        addInvolvedAccounts(splitPaymentWrapper);

        return splitPaymentWrapper;
    }

    /**
     * Creates a node containing the information of a failed split payment.
     * @param amountPerParticipant The amount each participant was supposed to pay.
     * @param insufficientFundsAcc The account that had insufficient funds.
     * @return The created node.
     */
    public ObjectNode failedOutput(final double amountPerParticipant,
                                   final Account insufficientFundsAcc) {
        ObjectNode splitPaymentWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        splitPaymentWrapper.put("amount", amountPerParticipant);
        splitPaymentWrapper.put("currency", commandInput.getCurrency());
        splitPaymentWrapper.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        splitPaymentWrapper.put("error", "Account " + insufficientFundsAcc.getIban()
                + " has insufficient funds for a split payment.");
        addInvolvedAccounts(splitPaymentWrapper);
        splitPaymentWrapper.put("timestamp", commandInput.getTimestamp());

        return splitPaymentWrapper;
    }
}
