package org.poo.e_banking.Comands.Reports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.fileio.CommandInput;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public final class SpendingsReport extends BaseReport {
    public SpendingsReport(final CommandInput commandInput, final ArrayNode output) {
        super(commandInput, output);
    }

    @Override
    public void handleAccount(final Account account) {
        if (account.getType().equals("savings")) {
            savingsAccountOutput();
            return;
        }

        int startTimestamp = commandInput.getStartTimestamp();
        int endTimestamp = commandInput.getEndTimestamp();

        Map<String, Double> sortedMap = new TreeMap<>();
        ArrayNode transactionsNodeCopy = account.getTransactionsNode().deepCopy();
        filterTransactions(transactionsNodeCopy, startTimestamp, endTimestamp, sortedMap);

        successOutput(account, transactionsNodeCopy, sortedMap);
    }

    /**
     * Filters the transactions that are not card payments or are not in the specified
     * time interval.
     * Also, it sums the amounts for each commerciant.
     *
     * @param transactionsNodeCopy the transactions node copy
     * @param startTimestamp       the start timestamp
     * @param endTimestamp         the end timestamp
     * @param sortedMap            the map that contains the commerciant and the total amount
     */
    public void filterTransactions(final ArrayNode transactionsNodeCopy, final int startTimestamp,
                                   final int endTimestamp, final Map<String, Double> sortedMap) {
        Iterator<JsonNode> iterator = transactionsNodeCopy.iterator();
        while (iterator.hasNext()) {
            JsonNode transaction = iterator.next();
            int timestamp = transaction.get("timestamp").asInt();
            String description = transaction.get("description").asText();

            if (timestamp < startTimestamp
                    || timestamp > endTimestamp || !description.equals("Card payment")) {
                iterator.remove();
            } else {
                String commerciant = transaction.get("commerciant").asText();
                double amount = transaction.get("amount").asDouble();

                sortedMap.merge(commerciant, amount, Double::sum);
            }
        }
    }

    /**
     * Creates the output for a successful spendings report.
     *
     * @param account      the account
     * @param transactions the transactions
     * @param sortedMap    the sorted map
     */
    private void successOutput(final Account account, final ArrayNode transactions,
                               final Map<String, Double> sortedMap) {
        ObjectNode spendingsNode = mapper.createObjectNode();
        spendingsNode.put("command", commandInput.getCommand());

        ObjectNode outputNode = spendingsNode.putObject("output");
        outputNode.put("IBAN", account.getIban());
        outputNode.put("balance", account.getBalance());
        outputNode.put("currency", account.getCurrency());
        outputNode.set("transactions", transactions);

        ArrayNode commerciantsArray = outputNode.putArray("commerciants");
        populateSpendingsArray(commerciantsArray, sortedMap);

        spendingsNode.put("timestamp", commandInput.getTimestamp());
        output.add(spendingsNode);
    }

    /**
     * Populates the spendings array with the commerciant and the total amount spent.
     *
     * @param spendingsArray the spendings array
     * @param sortedMap      the sorted map
     */
    private void populateSpendingsArray(final ArrayNode spendingsArray,
                                        final Map<String, Double> sortedMap) {
        for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
            ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", entry.getKey());
            commerciantNode.put("total", entry.getValue());
            spendingsArray.add(commerciantNode);
        }
    }

    /**
     * Outputs an error message for a savings account.
     */
    private void savingsAccountOutput() {
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.put("command", commandInput.getCommand());

        ObjectNode outputNode = wrapper.putObject("output");
        outputNode.put("error",
                "This kind of report is not supported for a saving account");

        wrapper.put("timestamp", commandInput.getTimestamp());
        output.add(wrapper);
    }
}
