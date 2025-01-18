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
                    User user = getUserWithError(userMap, objectInput, i);
                    if (user == null) return -1;

                    if (isUserInvolved(user, accounts)) {
                        addRejectionError(objectInput, i);
                        return -1;
                    }
                    return -1;
                }

                if (isAcceptCommand(objectInput, i)) {
                    User user = getUserWithError(userMap, objectInput, i);
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

    private User getUserWithError(Map<String, User> userMap, ObjectInput objectInput, int index) {
        User user = userMap.get(objectInput.getCommands()[index].getEmail());
        if (user == null) {
            ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
            objectNode.put("error", "User not found");
            objectNode.put("timestamp", objectInput.getCommands()[index].getTimestamp());
            output.add(objectNode);
        }
        return user;
    }

    private void addRejectionError(ObjectInput objectInput, int index) {
        ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        objectNode.put("error", "One user rejected the split payment");
        objectNode.put("timestamp", objectInput.getCommands()[index].getTimestamp());
        output.add(objectNode);
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
