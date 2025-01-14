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
        ExchangeRateManager exchangeRateManager = appLogic.getExchangeRateManager();

        if (user == null) {
            ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", commandInput.getTimestamp());
            output.add(outputNode);
            return;
        }

        Card card = user.getCardByNumber(commandInput.getCardNumber());
        if (card == null) {
            ObjectNode wrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
            wrapper.put("command", "cashWithdrawal");

            ObjectNode outputNode = wrapper.putObject("output");
            outputNode.put("description", "Card not found");
            outputNode.put("timestamp", commandInput.getTimestamp());

            wrapper.put("timestamp", commandInput.getTimestamp());
            output.add(wrapper);
            return;
        }

        if (card.getStatus().equals("frozen")) {
            ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            outputNode.put("description", "The card is frozen");
            outputNode.put("timestamp", commandInput.getTimestamp());
            user.getTransactionsNode().add(outputNode);
            return;
        }

        Account account = user.getAccountByIban(card.getAssociatedIban());
        double exchangeToRON = exchangeRateManager.getExchangeRate(account.getCurrency(), "RON");
        double amountRON = account.getBalance() * exchangeToRON;
        double commission = Comission.getComission(user, commandInput.getAmount());

        if (amountRON < commandInput.getAmount() + commandInput.getAmount() * commission) {
            ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            outputNode.put("description", "Insufficient funds");
            outputNode.put("timestamp", commandInput.getTimestamp());
            user.getTransactionsNode().add(outputNode);
            return;
        }

        double exchangeRate = exchangeRateManager.getExchangeRate("RON", account.getCurrency());
        double amountInAccountCurrency = commandInput.getAmount() * exchangeRate;
        commission = Comission.getComission(user, amountInAccountCurrency);
        if (account.payByCard(card, amountInAccountCurrency + amountInAccountCurrency * commission)) {
            ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            outputNode.put("description", "Cash withdrawal of " + commandInput.getAmount());
            outputNode.put("timestamp", commandInput.getTimestamp());
            user.getTransactionsNode().add(outputNode);
        }

        ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        outputNode.put("description", "Cannot perform payment due to a minimum balance being set");
        outputNode.put("timestamp", commandInput.getTimestamp());
        user.getTransactionsNode().add(outputNode);
    }
}
