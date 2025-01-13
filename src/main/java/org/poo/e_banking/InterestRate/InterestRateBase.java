package org.poo.e_banking.InterestRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.SavingsAccount;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public abstract class InterestRateBase implements Executable {
    protected final CommandInput commandInput;
    protected final ArrayNode output;
    protected final ObjectMapper mapper = new ObjectMapper();

    public InterestRateBase(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    /**
     * Finds the savings account and processes it
     */
    @Override
    public void execute() {
        ArrayList<User> users = AppLogic.getInstance().getUsers();

        for (User user : users) {
            Account account = user.getAccountByIban(commandInput.getAccount());
            if (account != null) {
                if (account.getType().equals("savings")) {
                    processSavingsAccount((SavingsAccount) account, user);
                } else {
                    errorOutput();
                }
                return;
            }
        }
    }

    /**
     * Outputs an error message when the account is not a savings account
     */
    protected void errorOutput() {
        ObjectNode wrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        wrapper.put("command", commandInput.getCommand());
        ObjectNode outputNode = wrapper.putObject("output");
        outputNode.put("timestamp", commandInput.getTimestamp());
        outputNode.put("description", "This is not a savings account");
        wrapper.put("timestamp", commandInput.getTimestamp());

        output.add(wrapper);
    }

    /**
     * Processes a savings account
     *
     * @param account The savings account to process
     * @param user    The user that owns the account
     */
    protected abstract void processSavingsAccount(SavingsAccount account, User user);
}
