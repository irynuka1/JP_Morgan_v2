package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Map;

public final class UpgradePlan implements Executable {
    private final CommandInput commandInput;

    public UpgradePlan(final CommandInput commandInput) {
        this.commandInput = commandInput;
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

        if (user != null) {
            String key = user.getPlan() + " -> " + commandInput.getNewPlanType();
            if (planTaxMap.containsKey(key)) {
                double exchangeRate = exchangeManager.getExchangeRate("RON",
                        account.getCurrency());
                double amountInAccountCurrency = planTaxMap.get(key) * exchangeRate;

                if (account.withdrawFunds(amountInAccountCurrency)) {
                    user.setPlan(commandInput.getNewPlanType());
                    logTransaction(user, account, successOutput());
                }
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
