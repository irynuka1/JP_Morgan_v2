package org.poo.e_banking.Comands.AccountAdd_Del;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

public final class DeleteAccount extends AccountBase {
    private final ArrayNode output;

    public DeleteAccount(final CommandInput commandInput, final ArrayNode output) {
        super(commandInput);
        this.output = output;
    }

    @Override
    public void execute() {
        User user = getUserFromMap();
        if (user == null) {
            return;
        }

        boolean removed = user.removeAccount(commandInput.getAccount());
        if (removed) {
            toJson(successOutput());
        } else {
            Account account = user.getAccountByIban(commandInput.getAccount());
            if (account != null && account.getBalance() != 0) {
                logTransaction(user, account, noBalanceOutput());
            }

            toJson(noAccOutput());
        }
    }

    private ObjectNode successOutput() {
        ObjectNode successNode = createBaseNode();
        successNode.put("success", "Account deleted");
        return successNode;
    }

    private ObjectNode noAccOutput() {
        ObjectNode errorNode = createBaseNode();
        errorNode.put("error",
                      "Account couldn't be deleted - see org.poo.transactions for details");
        return errorNode;
    }

    private ObjectNode noBalanceOutput() {
        ObjectNode outputNode = createBaseNode();
        outputNode.put("description",
                       "Account couldn't be deleted - there are funds remaining");
        return outputNode;
    }

    private void toJson(final ObjectNode node) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.put("command", commandInput.getCommand());
        wrapper.set("output", node);
        wrapper.put("timestamp", commandInput.getTimestamp());
        output.add(wrapper);
    }
}
