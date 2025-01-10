package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class PrintTransactions implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;
    private final Map<String, User> userMap;

    public PrintTransactions(final CommandInput commandInput, final ArrayNode output,
                             final Map<String, User> userMap) {
        this.commandInput = commandInput;
        this.output = output;
        this.userMap = userMap;
    }

    @Override
    public void execute() {
        User user = userMap.get(commandInput.getEmail());

        if (user != null) {
            createOutputNode(user);
        }
    }

    /**
     * Creates the output node for the command.
     *
     * @param user the user for which the output node is created
     */
    public void createOutputNode(final User user) {
        ObjectNode wrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        wrapper.put("command", commandInput.getCommand());
        wrapper.putArray("output").addAll(user.getTransactionsNode());
        wrapper.put("timestamp", commandInput.getTimestamp());
        output.add(wrapper);
    }
}
