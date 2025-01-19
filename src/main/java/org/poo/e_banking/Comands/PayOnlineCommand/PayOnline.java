package org.poo.e_banking.Comands.PayOnlineCommand;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.Helpers.Commission;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.Commerciant;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class PayOnline implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;

    public PayOnline(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();

        Map<String, User> userMap = appLogic.getUserMap();
        User user = userMap.get(commandInput.getEmail());
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();

        if (user == null || commandInput.getAmount() == 0) {
            return;
        }

        Card card = user.getCardByNumber(commandInput.getCardNumber());
        processTransaction(card, user, exchangeManager);
    }

    /**
     * Processes the transaction.
     * @param card the card used for the transaction
     * @param user the user that made the transaction
     */
    public void processTransaction(final Card card, final User user,
                                   final ExchangeRateManager exchangeManager) {
        if (card == null) {
            PayOnlineOutputBuilder.cardNotFound(output,
                    commandInput.getCommand(),
                    commandInput.getTimestamp());
            return;
        }

        if (card.getStatus().equals("frozen")) {
            ObjectNode node = PayOnlineOutputBuilder.frozenCard(commandInput.getTimestamp());
            user.getTransactionsNode().add(node);
            return;
        }

        Account account = user.getAccountByIban(card.getAssociatedIban());
        double exchangeRate = exchangeManager.getExchangeRate(commandInput.getCurrency(),
                account.getCurrency());
        double amountInAccountCurrency = commandInput.getAmount() * exchangeRate;
        double exchangeToRON = exchangeManager.getExchangeRate(commandInput.getCurrency(), "RON");
        double amountInRON = commandInput.getAmount() * exchangeToRON;
        double commission = Commission.getCommission(user, amountInRON);

        if (!account.payByCard(card, amountInAccountCurrency
                + amountInAccountCurrency * commission)) {
            logTransactions(user, account,
                    PayOnlineOutputBuilder.insufficientFunds(commandInput.getTimestamp()));
        } else {
            applyCashback(account, user, amountInAccountCurrency, exchangeManager);

            logTransactions(user, account,
                    PayOnlineOutputBuilder.success(commandInput.getTimestamp(),
                            amountInAccountCurrency, commandInput.getCommerciant()));

            if (card.getType().equals("OneTime")) {
                logTransactions(user, account,
                        PayOnlineOutputBuilder.destroyCard(account, user,
                                commandInput.getCardNumber(),
                                commandInput.getTimestamp()));
                logTransactions(user, account,
                        PayOnlineOutputBuilder.createCard(account, card, user,
                                commandInput.getTimestamp()));
            }
        }
    }

    /**
     * Applies the cashback to the account.
     * @param account the account that made the transaction
     * @param user the user that made the transaction
     * @param amountInAccountCurrency the amount in the account's currency
     * @param exchangeManager the exchange rate manager
     */
    public void applyCashback(final Account account, final User user,
                              final double amountInAccountCurrency,
                              final ExchangeRateManager exchangeManager) {
        Commerciant commerciant = account.getCommerciant(commandInput.getCommerciant());

        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            commerciant.getCashBack(account, amountInAccountCurrency);
        } else {
            double exchangeToRON =
                    exchangeManager.getExchangeRate(commandInput.getCurrency(), "RON");
            double amountInRON = commandInput.getAmount() * exchangeToRON;
            commerciant.getCashBack(amountInRON, account, user.getPlan(), amountInAccountCurrency);
        }
    }

    /**
     * Adds the transaction in the user's and account's transaction history.
     * @param user the user that made the transaction
     * @param account the account that made the transaction
     * @param node the transaction node
     */
    public void logTransactions(final User user, final Account account, final ObjectNode node) {
        user.getTransactionsNode().add(node);
        account.getTransactionsNode().add(node);
    }
}
