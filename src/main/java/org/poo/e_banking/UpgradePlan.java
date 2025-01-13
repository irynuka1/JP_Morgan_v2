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
        ExchangeRateManager exchangeRateManager = appLogic.getExchangeRateManager();
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
                double exchangeRate = exchangeRateManager.getExchangeRate("RON", account.getCurrency());
                double amountInAccountCurrency = planTaxMap.get(key) * exchangeRate;

                if (account.withdrawFunds(amountInAccountCurrency)) {
                    user.setPlan(commandInput.getNewPlanType());
                    ObjectNode outputNode = new ObjectNode(new ObjectMapper().getNodeFactory());
                    outputNode.put("description", "Upgrade plan");
                    outputNode.put("accountIBAN", account.getIban());
                    outputNode.put("newPlanType", commandInput.getNewPlanType());
                    outputNode.put("timestamp", commandInput.getTimestamp());

                    user.getTransactionsNode().add(outputNode);
                    account.getTransactionsNode().add(outputNode);
                }
            }
        }
    }
}
