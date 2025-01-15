package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.e_banking.Helpers.Comission;

public class CashWithdrawal implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;

    public CashWithdrawal(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        User user = appLogic.getUserMap().get(commandInput.getEmail());
        if (user == null) {
            generateOutput("User not found", null);
            return;
        }

        Card card = user.getCardByNumber(commandInput.getCardNumber());
        if (card == null) {
            generateOutput("Card not found", "cashWithdrawal");
            return;
        }

        if ("frozen".equals(card.getStatus())) {
            frozenCardOutput(user, "The card is frozen");
            return;
        }

        processWithdrawal(user, card);
    }

    private void processWithdrawal(User user, Card card) {
        AppLogic appLogic = AppLogic.getInstance();
        ExchangeRateManager exchangeRateManager = appLogic.getExchangeRateManager();
        Account account = user.getAccountByIban(card.getAssociatedIban());

        double exchangeToRON = exchangeRateManager.getExchangeRate(account.getCurrency(), "RON");
        double amountRON = account.getBalance() * exchangeToRON;
        double commission = Comission.getComission(user, commandInput.getAmount());
        double totalWithdrawal = commandInput.getAmount() + commandInput.getAmount() * commission;

        if (amountRON < totalWithdrawal) {
            insufficientFundsOutput(user, account);
            return;
        }

        double exchangeRate = exchangeRateManager.getExchangeRate("RON", account.getCurrency());
        double amountInAccountCurrency = totalWithdrawal * exchangeRate;

        if (account.withdrawCash(amountInAccountCurrency)) {
            successOutput(user, account);
        } else {
            failedOutput(user, account);
        }
    }

    private void generateOutput(String description, String command) {
        ObjectNode outputNode = new ObjectMapper().createObjectNode();
        outputNode.put("description", description);
        outputNode.put("timestamp", commandInput.getTimestamp());

        if (command != null) {
            ObjectNode wrapper = new ObjectMapper().createObjectNode();
            wrapper.put("command", command);
            wrapper.set("output", outputNode);
            wrapper.put("timestamp", commandInput.getTimestamp());
            output.add(wrapper);
        } else {
            output.add(outputNode);
        }
    }

    private void frozenCardOutput(User user, String description) {
        ObjectNode outputNode = new ObjectMapper().createObjectNode();
        outputNode.put("description", description);
        outputNode.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(outputNode);
    }

    private void insufficientFundsOutput(User user, Account account) {
        ObjectNode outputNode = createTransactionNode("Insufficient funds");
        user.getTransactionsNode().add(outputNode);
        account.getTransactionsNode().add(outputNode);
    }

    private void successOutput(User user, Account account) {
        ObjectNode outputNode = createTransactionNode("Cash withdrawal of " + commandInput.getAmount());
        outputNode.put("amount", commandInput.getAmount());
        user.getTransactionsNode().add(outputNode);
        account.getTransactionsNode().add(outputNode);
    }

    private void failedOutput(User user, Account account) {
        ObjectNode outputNode = createTransactionNode("Cannot perform payment due to a minimum balance being set");
        user.getTransactionsNode().add(outputNode);
        account.getTransactionsNode().add(outputNode);
    }

    private ObjectNode createTransactionNode(String description) {
        ObjectNode outputNode = new ObjectMapper().createObjectNode();
        outputNode.put("description", description);
        outputNode.put("timestamp", commandInput.getTimestamp());
        return outputNode;
    }
}
