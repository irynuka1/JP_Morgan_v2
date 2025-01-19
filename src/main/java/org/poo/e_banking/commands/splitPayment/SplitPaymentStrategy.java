package org.poo.e_banking.commands.splitPayment;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.helpers.ExchangeRateManager;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface for the split payment strategies.
 */
public interface SplitPaymentStrategy {
    /**
     * Executes the split payment command.
     * @param commandInput The command input.
     * @param appLogic The application logic.
     */
    void execute(CommandInput commandInput, AppLogic appLogic);

    /**
     * Outputs the success message for the split payment.
     * @param commandInput The command input.
     * @param amounts The amounts to be paid.
     * @return The success message.
     */
    ObjectNode successOutput(CommandInput commandInput, List<Double> amounts);

    /**
     * Outputs the failed message for the split payment.
     * @param commandInput The command input.
     * @param amounts The amounts to be paid.
     * @param insufficientFundsAcc The account with insufficient funds.
     * @return The failed message.
     */
    ObjectNode failedOutput(CommandInput commandInput, List<Double> amounts,
                            Account insufficientFundsAcc);

    /**
     * Gets the accounts that are participating in the split payment.
     * @param users The users.
     * @param accounts The ibans.
     * @return A list with the participating accounts.
     */
    default List<Account> getParticipatingAccounts(final ArrayList<User> users,
                                                   final List<String> accounts) {
        return accounts.stream()
                .flatMap(iban -> users.stream()
                        .map(user -> user.getAccountByIban(iban))
                        .filter(account -> account != null)
                        .limit(1))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the accounts have enough funds for the split payment.
     * @param accounts The accounts.
     * @param amounts The amounts to be paid.
     * @param exchangeManager The exchange rate manager.
     * @param currency The currency of the payment.
     * @return The account with insufficient funds.
     */
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

    /**
     * Processes the successful payment.
     * @param accounts The accounts.
     * @param amounts The amounts to be paid.
     * @param exchangeManager The exchange rate manager.
     * @param userMap The user map.
     * @param commandInput The command input.
     */
    default void processSuccessfulPayment(final List<Account> accounts,
                                          final List<Double> amounts,
                                          final ExchangeRateManager exchangeManager,
                                          final Map<String, User> userMap,
                                          final CommandInput commandInput) {
        int counter = 0;

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(),
                    account.getCurrency());
            double amountInAccountCurrency = amounts.get(counter) * exchangeRate;
            account.withdrawFunds(amountInAccountCurrency);
            user.getTransactionsNode().add(successOutput(commandInput, amounts));

            counter++;
        }
    }

    /**
     * Processes the failed payment.
     * @param accounts The accounts.
     * @param amounts The amounts to be paid.
     * @param insufficientFundsAcc The account with insufficient funds.
     * @param userMap The user map.
     * @param commandInput The command input.
     */
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
    default void addInvolvedAccounts(final ObjectNode splitPaymentWrapper,
                                     final CommandInput commandInput) {
        ArrayNode accountsNode = splitPaymentWrapper.putArray("involvedAccounts");
        for (String account : commandInput.getAccounts()) {
            accountsNode.add(account);
        }
    }
}
