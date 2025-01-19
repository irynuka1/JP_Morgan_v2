package org.poo.e_banking.Comands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Map;

public final class UpgradePlan implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;

    public UpgradePlan(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        Map<String, Integer> planTaxMap = appLogic.getPlanTaxMap();
        ArrayList<User> users = appLogic.getUsers();
        ExchangeRateManager exchangeManager = appLogic.getExchangeRateManager();

        User user = null;
        Account account = null;

        for (User currentUser : users) {
            if (currentUser.getAccountByIban(commandInput.getAccount()) != null) {
                user = currentUser;
                account = user.getAccountByIban(commandInput.getAccount());
                break;
            }
        }

        if (user == null) {
            ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            outputNode.put("command", "upgradePlan");
            ObjectNode errorNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            outputNode.set("output", errorNode);
            errorNode.put("description", "Account not found");
            errorNode.put("timestamp", commandInput.getTimestamp());
            outputNode.put("timestamp", commandInput.getTimestamp());
            output.add(outputNode);
            return;
        }

        String key = user.getPlan() + " -> " + commandInput.getNewPlanType();
        if (planTaxMap.containsKey(key)) {
            double exchangeRate = exchangeManager.getExchangeRate("RON",
                    account.getCurrency());
            double amountInAccountCurrency = planTaxMap.get(key) * exchangeRate;

            if (account.withdrawFunds(amountInAccountCurrency)) {
                user.setPlan(commandInput.getNewPlanType());
                logTransaction(user, account, successOutput());
            } else {
                ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
                outputNode.put("description", "Insufficient funds");
                outputNode.put("timestamp", commandInput.getTimestamp());
                logTransaction(user, account, outputNode);
            }
        }
    }

    /**
     * Creates the output node for a successful transaction
     * @return the output node
     */
    public ObjectNode successOutput() {
        ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        outputNode.put("description", "Upgrade plan");
        outputNode.put("accountIBAN", commandInput.getAccount());
        outputNode.put("newPlanType", commandInput.getNewPlanType());
        outputNode.put("timestamp", commandInput.getTimestamp());
        return outputNode;
    }

    /**
     * Logs a transaction in the user and account transaction nodes
     * @param user the user
     * @param account the account
     * @param outputNode the output node
     */
    public void logTransaction(final User user, final Account account,
                               final ObjectNode outputNode) {
        user.getTransactionsNode().add(outputNode);
        account.getTransactionsNode().add(outputNode);
    }
}
