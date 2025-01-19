package org.poo.e_banking.Comands.SplitPayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CustomSplitPayment implements SplitPaymentStrategy {
    @Override
    public void execute(final CommandInput commandInput, final AppLogic appLogic) {
        Map<String, User> userMap = appLogic.getUserMap();
        ArrayList<User> users = appLogic.getUsers();
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();

        List<Account> accounts = getParticipatingAccounts(users, commandInput.getAccounts());
        List<Double> amounts = commandInput.getAmountForUsers();

        Account insufficientFundsAcc = checkAccountsBalance(accounts, amounts, exchangeManager,
                commandInput.getCurrency());

        if (insufficientFundsAcc == null) {
            processSuccessfulPayment(accounts, amounts, exchangeManager, userMap, commandInput);
        } else {
            processFailedPayment(accounts, amounts, insufficientFundsAcc, userMap, commandInput);
        }
    }

    @Override
    public ObjectNode successOutput(final CommandInput commandInput, final List<Double> amounts) {
        ObjectNode splitPaymentWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        splitPaymentWrapper.put("timestamp", commandInput.getTimestamp());
        splitPaymentWrapper.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        splitPaymentWrapper.put("splitPaymentType", commandInput.getSplitPaymentType());
        splitPaymentWrapper.put("currency", commandInput.getCurrency());
        ArrayNode amountPerParticipant = splitPaymentWrapper.putArray("amountForUsers");
        for (double amount : amounts) {
            amountPerParticipant.add(amount);
        }
        splitPaymentWrapper.set("amountForUsers", amountPerParticipant); //?
        addInvolvedAccounts(splitPaymentWrapper, commandInput);

        return splitPaymentWrapper;
    }

    @Override
    public ObjectNode failedOutput(final CommandInput commandInput, final List<Double> amounts,
                                   final Account insufficientFundsAcc) {
        ObjectNode splitPaymentWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        ArrayNode amountPerParticipant = splitPaymentWrapper.putArray("amountForUsers");
        for (double amount : amounts) {
            amountPerParticipant.add(amount);
        }
        splitPaymentWrapper.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        splitPaymentWrapper.put("splitPaymentType", commandInput.getSplitPaymentType());
        splitPaymentWrapper.put("currency", commandInput.getCurrency());
        splitPaymentWrapper.put("error", "Account " + insufficientFundsAcc.getIban()
                + " has insufficient funds for a split payment.");
        addInvolvedAccounts(splitPaymentWrapper, commandInput);
        splitPaymentWrapper.put("timestamp", commandInput.getTimestamp());

        return splitPaymentWrapper;
    }
}



