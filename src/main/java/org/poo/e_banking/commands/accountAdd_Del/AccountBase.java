package org.poo.e_banking.commands.accountAdd_Del;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public abstract class AccountBase implements Executable {
    protected final CommandInput commandInput;
    protected final ObjectMapper objectMapper;

    protected AccountBase(final CommandInput commandInput) {
        this.commandInput = commandInput;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves the user from the userMap based on the email address.
     *
     * @return the user if it exists, null otherwise
     */
    protected User getUserFromMap() {
        Map<String, User> userMap = AppLogic.getInstance().getUserMap();
        return userMap.get(commandInput.getEmail());
    }

    /**
     * Creates a base node with the timestamp.
     *
     * @return the base node
     */
    protected ObjectNode createBaseNode() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("timestamp", commandInput.getTimestamp());
        return node;
    }

    /**
     * Logs a transaction in the user and account transactions nodes.
     *
     * @param user            the user
     * @param account         the account
     * @param transactionNode the transaction to log
     */
    protected void logTransaction(final User user, final Account account,
                                  final ObjectNode transactionNode) {
        user.getTransactionsNode().add(transactionNode);
        account.getTransactionsNode().add(transactionNode);
    }
}
