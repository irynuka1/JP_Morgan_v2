package org.poo.e_banking.AccountAdd_Del;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class AddAccount extends AccountBase {

    public AddAccount(final CommandInput commandInput) {
        super(commandInput);
    }

    private ObjectNode toJson() {
        ObjectNode accountNode = createBaseNode();
        accountNode.put("description", "New account created");
        return accountNode;
    }

    @Override
    public void execute() {
        User user = getUserFromMap();
        if (user == null || commandInput.getAccountType().equals("business")) {
            return;
        }

        user.addAccount(commandInput.getAccountType(),
                commandInput.getCurrency(),
                commandInput.getInterestRate());

        Account account = user.getAccounts().getLast();

        ObjectNode transactionNode = toJson();
        logTransaction(user, account, transactionNode);
    }
}
