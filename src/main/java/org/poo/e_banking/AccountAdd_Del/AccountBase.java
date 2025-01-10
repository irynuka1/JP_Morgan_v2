package org.poo.e_banking.AccountAdd_Del;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public abstract class AccountBase implements Executable {
    protected final CommandInput commandInput;
    protected final Map<String, User> userMap;
    protected final ObjectMapper objectMapper;

    protected AccountBase(final CommandInput commandInput, final Map<String, User> userMap) {
        this.commandInput = commandInput;
        this.userMap = userMap;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves the user from the userMap based on the email address.
     *
     * @return the user if it exists, null otherwise
     */
    protected User getUserFromMap() {
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
