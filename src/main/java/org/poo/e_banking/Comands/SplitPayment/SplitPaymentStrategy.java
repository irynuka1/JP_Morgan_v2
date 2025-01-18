package org.poo.e_banking.Comands.SplitPayment;

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
import java.util.stream.Collectors;

public interface SplitPaymentStrategy {
    void execute(CommandInput commandInput, AppLogic appLogic);
    ObjectNode successOutput(CommandInput commandInput, List<Double> amounts);
    ObjectNode failedOutput(CommandInput commandInput, List<Double> amounts, Account insufficientFundsAcc);

    default List<Account> getParticipatingAccounts(ArrayList<User> users, List<String> acounts) {
        return acounts.stream()
                .flatMap(iban -> users.stream()
                        .map(user -> user.getAccountByIban(iban))
                        .filter(account -> account != null)
                        .limit(1))
                .collect(Collectors.toList());
    }

    default Account checkAccountsBalance(final List<Account> accounts,
                                        final List<Double> amounts,
                                        final ExchangeRateManager exchangeManager,
                                        final String currency) {
        int counter = 0;

        for (Account account : accounts) {
            double exchangeRate = exchangeManager.getExchangeRate(currency, account.getCurrency());
            double amountInAccCurrency = amounts.get(counter) * exchangeRate;

            if (account.getBalance() - amountInAccCurrency < 0) {
                return account;
            }
            counter++;
        }

        return null;
    }

    default void processSuccessfulPayment(final List<Account> accounts,
                                          final List<Double> amounts,
                                          final ExchangeRateManager exchangeManager,
                                          final Map<String, User> userMap,
                                          final CommandInput commandInput) {
        int counter = 0;

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(), account.getCurrency());
            double amountInAccountCurrency = amounts.get(counter) * exchangeRate;
            account.withdrawFunds(amountInAccountCurrency);
            user.getTransactionsNode().add(successOutput(commandInput, amounts));

            counter++;
        }
    }

    default void processFailedPayment(final List<Account> accounts,
                                      final List<Double> amounts,
                                      final Account insufficientFundsAcc,
                                      final Map<String, User> userMap,
                                      final CommandInput commandInput) {
        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getTransactionsNode().add(failedOutput(commandInput, amounts,
                    insufficientFundsAcc));
            account.getTransactionsNode().add(failedOutput(commandInput, amounts,
                    insufficientFundsAcc));
        }
    }

    /**
     * Adds the accounts involved in the split payment to the split payment node.
     * @param splitPaymentWrapper The node containing the split payment information.
     */
    default void addInvolvedAccounts(final ObjectNode splitPaymentWrapper, final CommandInput commandInput) {
        ArrayNode accountsNode = splitPaymentWrapper.putArray("involvedAccounts");
        for (String account : commandInput.getAccounts()) {
            accountsNode.add(account);
        }
    }
}
