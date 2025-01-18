package org.poo.e_banking.Comands.SplitPayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Comands.AppLogic;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomSplitPayment {
    private final CommandInput commandInput;

    public CustomSplitPayment(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        Map<String, User> userMap = appLogic.getUserMap();
        ArrayList<User> users = appLogic.getUsers();
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();

        int numberOfParticipants = commandInput.getAccounts().size();
        List<Double> amountPerParticipant = commandInput.getAmountForUsers();
        List<Account> accounts = getParticipatingAccounts(users);
        Account insufficientFundsAcc = checkAccountsBalance(accounts, amountPerParticipant, exchangeManager);

        if (insufficientFundsAcc == null) {
            processSuccessfulPayment(accounts, amountPerParticipant, exchangeManager, userMap);
        } else {
            processFailedPayment(accounts, insufficientFundsAcc, userMap, amountPerParticipant);
        }
    }

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

    public Account checkAccountsBalance(final List<Account> accounts,
                                        final List<Double> amountPerParticipant,
                                        final ExchangeRateManager exchangeManager) {
        Account insufficientFundsAcc = null;
        int counter = 0;

        for (Account account : accounts) {
            double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(),
                    account.getCurrency());
            double amountInAccCurrency = amountPerParticipant.get(counter) * exchangeRate;

            if (account.getBalance() - amountInAccCurrency < 0) {
                insufficientFundsAcc = account;
                break;
            }

            counter++;
        }

        return insufficientFundsAcc;
    }

    private void processSuccessfulPayment(final List<Account> accounts,
                                          final List<Double> amountPerParticipant,
                                          final ExchangeRateManager exchangeManager,
                                          final Map<String, User> userMap) {
        int counter = 0;

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(),
                    account.getCurrency());
            double amountInAccountCurrency = amountPerParticipant.get(counter) * exchangeRate;
            account.withdrawFunds(amountInAccountCurrency);
            user.getTransactionsNode().add(successOutput(amountPerParticipant));

            counter++;
        }
    }

    private void processFailedPayment(final List<Account> accounts,
                                      final Account insufficientFundsAcc,
                                      final Map<String, User> userMap,
                                      final List<Double> amountPerParticipant) {
        int counter = 0;

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getTransactionsNode().add(failedOutput(amountPerParticipant, insufficientFundsAcc));
            account.getTransactionsNode().add(failedOutput(amountPerParticipant, insufficientFundsAcc));

            counter++;
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

    public ObjectNode successOutput(final List<Double> amountPerParticipant) {
        ObjectNode splitPaymentWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        splitPaymentWrapper.put("timestamp", commandInput.getTimestamp());
        splitPaymentWrapper.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        splitPaymentWrapper.put("splitPaymentType", commandInput.getSplitPaymentType());
        splitPaymentWrapper.put("currency", commandInput.getCurrency());
        ArrayNode amounts = splitPaymentWrapper.putArray("amountForUsers");
        for (double amount : amountPerParticipant) {
            amounts.add(amount);
        }
        splitPaymentWrapper.set("amountForUsers", amounts);
        addInvolvedAccounts(splitPaymentWrapper);

        return splitPaymentWrapper;
    }

    public ObjectNode failedOutput(final List<Double> amountPerParticipant, final Account insufficientFundsAcc) {
        ObjectNode splitPaymentWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        ArrayNode amounts = splitPaymentWrapper.putArray("amountForUsers");
        for (double amount : amountPerParticipant) {
            amounts.add(amount);
        }
        splitPaymentWrapper.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        splitPaymentWrapper.put("splitPaymentType", commandInput.getSplitPaymentType());
        splitPaymentWrapper.put("currency", commandInput.getCurrency());
        splitPaymentWrapper.put("error", "Account " + insufficientFundsAcc.getIban()
                + " has insufficient funds for a split payment.");
        addInvolvedAccounts(splitPaymentWrapper);
        splitPaymentWrapper.put("timestamp", commandInput.getTimestamp());

        return splitPaymentWrapper;
    }
}
