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

    public PrintTransactions(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        Map<String, User> userMap = AppLogic.getInstance().getUserMap();
        User user = userMap.get(commandInput.getEmail());

        if (user != null) {
            if (user.getPendingTransactions().size() > 0) {
                user.getPendingTransactions().forEach(pendingTransaction -> {
                    if (pendingTransaction.getTimestamp() < commandInput.getTimestamp() && !pendingTransaction.isVerified()) {
                        if (pendingTransaction.getCommandInput().getSplitPaymentType().equals("equal")) {
                            SplitPayment splitPayment = new SplitPayment(pendingTransaction.getCommandInput());
                            splitPayment.execute();
                        } else {
                            CustomSplitPayment customSplitPayment = new CustomSplitPayment(pendingTransaction.getCommandInput());
                            customSplitPayment.execute();
                        }

                        pendingTransaction.verify(pendingTransaction.getTimestamp());
                    }
                });
            }

            for (int i = 0; i < user.getTransactionsNode().size() - 1; i++) {
                for (int j = i + 1; j < user.getTransactionsNode().size(); j++) {
                    if (user.getTransactionsNode().get(j).get("timestamp").asInt() < user.getTransactionsNode().get(i).get("timestamp").asInt()) {
                        ObjectNode temp = (ObjectNode) user.getTransactionsNode().get(i);
                        user.getTransactionsNode().set(i, user.getTransactionsNode().get(j));
                        user.getTransactionsNode().set(j, temp);
                    }
                }
            }

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
