package org.poo.e_banking.Comands.SplitPayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Comands.AppLogic;
import org.poo.e_banking.Helpers.Executable;
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

    protected VerifySplitPaymentBase(CommandInput commandInput, ArrayNode output, String splitType) {
        this.commandInput = commandInput;
        this.output = output;
        this.splitType = splitType;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        ArrayList<User> users = appLogic.getUsers();
        Map<String, User> userMap = appLogic.getUserMap();
        List<Account> accounts = getParticipatingAccounts(users);
        ObjectInput objectInput = appLogic.getObjectInput();

        int timestamp = checkResponse(objectInput, accounts, userMap);

        if (timestamp == -1) {
            return;
        }

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getPendingTransactions().add(new PendingTransaction(commandInput, timestamp));
        }
    }

    protected int checkResponse(ObjectInput objectInput, List<Account> accounts, Map<String, User> userMap) {
        int verifiedAccounts = 0;
        int lastTimestamp = -1;

        for (int i = 0; i < objectInput.getCommands().length; i++) {
            if (objectInput.getCommands()[i].getTimestamp() > commandInput.getTimestamp()) {
                if (isRejectCommand(objectInput, i)) {
                    User user = getUserWithError(userMap, objectInput, i, "rejectSplitPayment");
                    if (user == null) return -1;

                    if (isUserInvolved(user, accounts)) {
                        addRejectionError(accounts, userMap);
                        return -1;
                    }
                    return -1;
                }

                if (isAcceptCommand(objectInput, i)) {
                    User user = getUserWithError(userMap, objectInput, i, "acceptSplitPayment");
                    if (user == null) return -1;

                    for (Account account : user.getAccounts()) {
                        if (accounts.contains(account)) {
                            verifiedAccounts++;
                            lastTimestamp = objectInput.getCommands()[i].getTimestamp();
                            break;
                        }
                    }
                }
            }

            if (verifiedAccounts == accounts.size()) {
                return lastTimestamp;
            }
        }

        return verifiedAccounts == accounts.size() ? lastTimestamp : -1;
    }

    private User getUserWithError(Map<String, User> userMap, ObjectInput objectInput, int index, String command) {
        User user = userMap.get(objectInput.getCommands()[index].getEmail());
        if (user == null) {
            ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            objectNode.put("command", command);
            ObjectNode errorNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            objectNode.set("output", errorNode);
            errorNode.put("description", "User not found");
            errorNode.put("timestamp", objectInput.getCommands()[index].getTimestamp());
            objectNode.put("timestamp", objectInput.getCommands()[index].getTimestamp());
            output.add(objectNode);
        }

        return user;
    }

    private void addRejectionError(List<Account> accounts, Map<String, User> userMap) {
        if (splitType.equals("custom")) {
            ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            ArrayNode amountPerParticipant = objectNode.putArray("amountForUsers");
            for (int i = 0; i < accounts.size(); i++) {
                amountPerParticipant.add(commandInput.getAmountForUsers().get(i));
            }
            objectNode.put("currency", commandInput.getCurrency());
            objectNode.put("description", "Split payment of "
                    + String.format("%.2f", commandInput.getAmount()) + " "
                    + commandInput.getCurrency());
            objectNode.put("error", "One user rejected the payment");
            ArrayNode accountsNode = objectNode.putArray("involvedAccounts");
            for (String account : commandInput.getAccounts()) {
                accountsNode.add(account);
            }
            objectNode.put("splitPaymentType", commandInput.getSplitPaymentType());
            objectNode.put("timestamp", commandInput.getTimestamp());

            for (Account account : accounts) {
                User user = userMap.get(account.getUserEmail());
                user.getTransactionsNode().add(objectNode);
            }
        } else {
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

            for (Account account : accounts) {
                User user = userMap.get(account.getUserEmail());
                user.getTransactionsNode().add(objectNode);
            }
        }
    }

    private boolean isUserInvolved(User user, List<Account> accounts) {
        return user.getAccounts().stream().anyMatch(accounts::contains);
    }

    private boolean isRejectCommand(ObjectInput objectInput, int index) {
        return objectInput.getCommands()[index].getCommand().equals("rejectSplitPayment") &&
                objectInput.getCommands()[index].getSplitPaymentType().equals(splitType);
    }

    private boolean isAcceptCommand(ObjectInput objectInput, int index) {
        return objectInput.getCommands()[index].getCommand().equals("acceptSplitPayment") &&
                objectInput.getCommands()[index].getSplitPaymentType().equals(splitType);
    }

    protected List<Account> getParticipatingAccounts(ArrayList<User> users) {
        return commandInput.getAccounts().stream()
                .flatMap(iban -> users.stream()
                        .map(user -> user.getAccountByIban(iban))
                        .filter(account -> account != null)
                        .limit(1))
                .collect(Collectors.toList());
    }
}
