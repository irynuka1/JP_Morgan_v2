package org.poo.e_banking.Reports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Iterator;

public final class Report extends BaseReport {
    public Report(final CommandInput commandInput, final ArrayList<User> users,
                  final ArrayNode output) {
        super(commandInput, users, output);
    }

    @Override
    public void handleAccount(final Account account) {
        int startTimestamp = commandInput.getStartTimestamp();
        int endTimestamp = commandInput.getEndTimestamp();
        output.add(createReportNode(account, startTimestamp, endTimestamp));
    }

    /**
     * Creates a node the report details.
     *
     * @param account        the account
     * @param startTimestamp the start timestamp
     * @param endTimestamp   the end timestamp
     * @return the created node
     */
    public ObjectNode createReportNode(final Account account, final int startTimestamp,
                                       final int endTimestamp) {
        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("command", commandInput.getCommand());

        ObjectNode outputNode = reportNode.putObject("output");
        outputNode.put("IBAN", account.getIban());
        outputNode.put("balance", account.getBalance());
        outputNode.put("currency", account.getCurrency());

        ArrayNode transactionsNodeCopy = account.getTransactionsNode().deepCopy();
        filterTransactions(transactionsNodeCopy, startTimestamp, endTimestamp);
        outputNode.putArray("transactions").addAll(transactionsNodeCopy);

        reportNode.put("timestamp", commandInput.getTimestamp());
        return reportNode;
    }

    /**
     * Filters the transactions that are not in the specified time interval.
     *
     * @param transactionsNodeCopy the transactions node copy
     * @param startTimestamp       the start timestamp
     * @param endTimestamp         the end timestamp
     */
    public void filterTransactions(final ArrayNode transactionsNodeCopy,
                                   final int startTimestamp, final int endTimestamp) {
        Iterator<JsonNode> iterator = transactionsNodeCopy.iterator();
        while (iterator.hasNext()) {
            JsonNode transaction = iterator.next();
            int timestamp = transaction.get("timestamp").asInt();
            if (timestamp < startTimestamp || timestamp > endTimestamp) {
                iterator.remove();
            }
        }
    }
}
