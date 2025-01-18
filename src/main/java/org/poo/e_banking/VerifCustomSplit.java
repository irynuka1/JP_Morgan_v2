package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifCustomSplit implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;

    public VerifCustomSplit(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        ArrayList<User> users = appLogic.getUsers();
        Map<String, User> userMap = appLogic.getUserMap();
        List<Account> accounts = getParticipatingAccounts(users);
        ObjectInput objectInput = appLogic.getObjectInput();

        int timestamp = checkFutureCommands(objectInput, accounts, userMap);

        if (timestamp == -1) {
            return;
        }

        for (Account account : accounts) {
            User user = userMap.get(account.getUserEmail());
            user.getPendingTransactions().add(new PendingTransaction(commandInput, timestamp));
        }
    }

    public int checkFutureCommands(final ObjectInput objectInput, List<Account> accounts, Map<String, User> userMap) {
        int verifiedAccounts = 0;
        int lastTimestamp = -1;

        for (int i = 0; i < objectInput.getCommands().length; i++) {
            if (objectInput.getCommands()[i].getTimestamp() > commandInput.getTimestamp()) {
                if (objectInput.getCommands()[i].getCommand().equals("rejectSplitPayment") &&
                        objectInput.getCommands()[i].getSplitPaymentType().equals("custom")) {
                    User user = userMap.get(objectInput.getCommands()[i].getEmail());
                    if (user == null) {
                        ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
                        objectNode.put("error", "User not found");
                        objectNode.put("timestamp", objectInput.getCommands()[i].getTimestamp());
                        output.add(objectNode);
                        return -1;
                    }

                    for (Account account : user.getAccounts()) {
                        if (accounts.contains(account)) {
                            ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
                            objectNode.put("error", "One user rejected the split payment");
                            objectNode.put("timestamp", objectInput.getCommands()[i].getTimestamp());
                            output.add(objectNode);
                            return -1;
                        }
                    }

                    return -1;
                }

                if (objectInput.getCommands()[i].getCommand().equals("acceptSplitPayment") &&
                        objectInput.getCommands()[i].getSplitPaymentType().equals("custom")) {
                    User user = userMap.get(objectInput.getCommands()[i].getEmail());
                    if (user == null) {
                        ObjectNode objectNode = new ObjectNode(new ObjectMapper().getNodeFactory());
                        objectNode.put("error", "User not found");
                        objectNode.put("timestamp", objectInput.getCommands()[i].getTimestamp());
                        output.add(objectNode);
                        return -1;
                    }

                    for (Account account : user.getAccounts()) {
                        if (accounts.contains(account)) {
                            verifiedAccounts++;
                            lastTimestamp = objectInput.getCommands()[i].getTimestamp();
                        }
                    }
                }
            }

            if (verifiedAccounts == accounts.size()) {
                return lastTimestamp;
            }
        }

        if (verifiedAccounts == accounts.size()) {
            return lastTimestamp;
        }

        return -1;
    }

    public List<Account> getParticipatingAccounts(final ArrayList<User> users) {
        List<Account> accounts = new ArrayList<>();

        for (String iban : commandInput.getAccounts()) {
            for (User user : users) {
                Account account = user.getAccountByIban(iban);
                if (account != null) {
                    accounts.add(account);
                    break;
                }
            }
        }

        return accounts;
    }
}
