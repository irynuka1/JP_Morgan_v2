package org.poo.e_banking.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.commands.splitPayment.SplitPaymentStrategy;
import org.poo.e_banking.helpers.Executable;
import org.poo.e_banking.commands.splitPayment.CustomSplitPayment;
import org.poo.e_banking.commands.splitPayment.EqualSplitPayment;
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
            executePendingTransactions(user);
            sortTransactions(user);
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

    /**
     * Sorts the transactions of the user by the timestamp.
     *
     * @param user the user for which the transactions are sorted
     */
    public void sortTransactions(final User user) {
        int n = user.getTransactionsNode().size();
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                int timestamp1 = user.getTransactionsNode().get(j).get("timestamp").asInt();
                int timestamp2 = user.getTransactionsNode().get(j + 1).get("timestamp").asInt();

                if (timestamp1 > timestamp2) {
                    ObjectNode temp = (ObjectNode) user.getTransactionsNode().get(j);
                    user.getTransactionsNode().set(j, user.getTransactionsNode().get(j + 1));
                    user.getTransactionsNode().set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * Executes the pending transactions for the user.
     *
     * @param user the user for which the pending transactions are executed
     */
    public void executePendingTransactions(final User user) {
        if (!user.getPendingTransactions().isEmpty()) {
            user.getPendingTransactions().forEach(pendingTransaction -> {
                if (pendingTransaction.getTimestamp() < commandInput.getTimestamp()
                        && !pendingTransaction.isVerified()) {

                    SplitPaymentStrategy strategy;
                    String type = pendingTransaction.getCommandInput().getSplitPaymentType();
                    if (type.equals("equal")) {
                        strategy = new EqualSplitPayment();
                    } else {
                        strategy = new CustomSplitPayment();
                    }

                    strategy.execute(pendingTransaction.getCommandInput(), AppLogic.getInstance());
                    pendingTransaction.verify(pendingTransaction.getTimestamp());
                }
            });
        }
    }
}
