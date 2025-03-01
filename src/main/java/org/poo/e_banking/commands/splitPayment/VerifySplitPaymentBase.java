package org.poo.e_banking.commands.splitPayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class VerifySplitPaymentBase implements Executable {
    protected final CommandInput commandInput;
    protected final ArrayNode output;
    protected final String splitType;

    protected VerifySplitPaymentBase(final CommandInput commandInput, final ArrayNode output,
                                     final String splitType) {
        this.commandInput = commandInput;
        this.output = output;
        this.splitType = splitType;
    }

    /**
     * Checks if the split payment was accepted or rejected.
     */
    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        ArrayList<User> users = appLogic.getUsers();
        Map<String, User> userMap = appLogic.getUserMap();
        List<Account> accounts = getParticipatingAccounts(users);
        ObjectInput objectInput = appLogic.getObjectInput();
        ArrayList<Integer> verifiedTimestamps = appLogic.getVerifiedTimestamps();

        int timestamp = checkResponse(objectInput, accounts, userMap, verifiedTimestamps);

        if (timestamp == -1) {
            return;
        }

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getPendingTransactions().add(new PendingTransaction(commandInput, timestamp));
        }
    }

    /**
     * Checks the response of the users involved in the split payment.
     *
     * @param objectInput The object input to be used.
     * @param accounts The accounts that are involved in the split payment.
     * @param userMap The map of users.
     * @return The timestamp of the last command that was accepted.
     */
    protected int checkResponse(final ObjectInput objectInput, final List<Account> accounts,
                                final Map<String, User> userMap,
                                final ArrayList<Integer> verifiedTimestamps) {
        int verifiedAccounts = 0;
        int acceptResponses = 0;
        int lastTimestamp = -1;

        for (int i = 0; i < objectInput.getCommands().length; i++) {
            if (objectInput.getCommands()[i].getTimestamp() > commandInput.getTimestamp()
                    && !verifiedTimestamps.contains(objectInput.getCommands()[i].getTimestamp())) {

                if (isRejectCommand(objectInput, i)) {
                    verifiedTimestamps.add(objectInput.getCommands()[i].getTimestamp());
                    verifiedAccounts++;

                    User user = getUserWithError(userMap, objectInput.getCommands()[i]);
                    if (user != null) {
                        if (isUserInvolved(user, accounts)) {
                            addRejectionError(accounts, userMap);
                            continue;
                        }
                    }
                }

                if (isAcceptCommand(objectInput, i)) {
                    verifiedTimestamps.add(objectInput.getCommands()[i].getTimestamp());
                    verifiedAccounts++;

                    User user = getUserWithError(userMap, objectInput.getCommands()[i]);
                    if (user != null) {
                        if (isUserInvolved(user, accounts)) {
                            acceptResponses++;
                            lastTimestamp = objectInput.getCommands()[i].getTimestamp();
                        }
                    }
                }

                if (verifiedAccounts == accounts.size()) {
                    break;
                }
            }

            if (acceptResponses == accounts.size()) {
                return lastTimestamp;
            }
        }

        return acceptResponses == accounts.size() ? lastTimestamp : -1;
    }

    private User getUserWithError(final Map<String, User> userMap,
                                  final CommandInput currentInput) {
        User user = userMap.get(currentInput.getEmail());
        if (user == null) {
            ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            objectNode.put("command", currentInput.getCommand());
            ObjectNode errorNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            objectNode.set("output", errorNode);
            errorNode.put("description", "User not found");
            errorNode.put("timestamp", currentInput.getTimestamp());
            objectNode.put("timestamp", currentInput.getTimestamp());
            AppLogic.getInstance().getUserNotFounds().add(objectNode);
        }

        return user;
    }

    private void addRejectionError(final List<Account> accounts, final Map<String, User> userMap) {
        ObjectNode objectNode;

        if (splitType.equals("custom")) {
            objectNode = customPaymentError();
        } else {
            objectNode = equalPaymentError();
        }

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getTransactionsNode().add(objectNode);
        }
    }

    private boolean isUserInvolved(final User user, final List<Account> accounts) {
        return user.getAccounts().stream().anyMatch(accounts::contains);
    }

    private boolean isRejectCommand(final ObjectInput objectInput, final int index) {
        return objectInput.getCommands()[index].getCommand().equals("rejectSplitPayment")
                && objectInput.getCommands()[index].getSplitPaymentType().equals(splitType);
    }

    private boolean isAcceptCommand(final ObjectInput objectInput, final int index) {
        return objectInput.getCommands()[index].getCommand().equals("acceptSplitPayment")
                && objectInput.getCommands()[index].getSplitPaymentType().equals(splitType);
    }

    /**
     * Returns the accounts that are involved in the split payment.
     *
     * @param users The list of users to search for the accounts.
     * @return The list of accounts.
     */
    protected List<Account> getParticipatingAccounts(final ArrayList<User> users) {
        return commandInput.getAccounts().stream()
                .flatMap(iban -> users.stream()
                        .map(user -> user.getAccountByIban(iban))
                        .filter(account -> account != null)
                        .limit(1))
                .collect(Collectors.toList());
    }

    private ObjectNode customPaymentError() {
        ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        ArrayNode amountPerParticipant = objectNode.putArray("amountForUsers");
        for (int i = 0; i < commandInput.getAccounts().size(); i++) {
            amountPerParticipant.add(commandInput.getAmountForUsers().get(i));
        }
        objectNode.put("currency", commandInput.getCurrency());
        objectNode.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        objectNode.put("error", "One user rejected the payment.");
        ArrayNode accountsNode = objectNode.putArray("involvedAccounts");
        for (String account : commandInput.getAccounts()) {
            accountsNode.add(account);
        }
        objectNode.put("splitPaymentType", commandInput.getSplitPaymentType());
        objectNode.put("timestamp", commandInput.getTimestamp());
        return objectNode;
    }

    private ObjectNode equalPaymentError() {
        ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("description", "Split payment of "
                + String.format("%.2f", commandInput.getAmount()) + " "
                + commandInput.getCurrency());
        objectNode.put("currency", commandInput.getCurrency());
        objectNode.put("amount", commandInput.getAmount());
        ArrayNode accountsNode = objectNode.putArray("involvedAccounts");
        for (String account : commandInput.getAccounts()) {
            accountsNode.add(account);
        }
        objectNode.put("error", "One user rejected the payment");
        return objectNode;
    }
}
