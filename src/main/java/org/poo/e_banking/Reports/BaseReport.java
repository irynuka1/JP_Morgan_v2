package org.poo.e_banking.Reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public abstract class BaseReport implements Executable {
    protected final CommandInput commandInput;
    protected final ArrayList<User> users;
    protected final ArrayNode output;
    protected final ObjectMapper mapper = new ObjectMapper();

    public BaseReport(final CommandInput commandInput, final ArrayList<User> users,
                      final ArrayNode output) {
        this.commandInput = commandInput;
        this.users = users;
        this.output = output;
    }

    /**
     * If the account is found, it is passed to the handleAccount method.
     */
    @Override
    public void execute() {
        Account account = findAccount(commandInput.getAccount());
        if (account == null) {
            errorOutput("Account not found");
            return;
        }

        handleAccount(account);
    }

    /**
     * This method handles the specific report logic for each subclass.
     *
     * @param account the account
     */
    public abstract void handleAccount(Account account);

    /**
     * Finds the account with the specified IBAN.
     *
     * @param iban the IBAN
     * @return the account
     */
    public Account findAccount(final String iban) {
        for (User user : users) {
            Account account = user.getAccountByIban(iban);
            if (account != null) {
                return account;
            }
        }
        return null;
    }

    /**
     * Outputs a node containing error details.
     *
     * @param description the description
     */
    public void errorOutput(final String description) {
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.put("command", commandInput.getCommand());

        ObjectNode outputNode = wrapper.putObject("output");
        outputNode.put("timestamp", commandInput.getTimestamp());
        outputNode.put("description", description);

        wrapper.put("timestamp", commandInput.getTimestamp());
        output.add(wrapper);
    }
}

