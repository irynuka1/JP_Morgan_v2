package org.poo.e_banking.commands;

import org.poo.e_banking.AppLogic;
import org.poo.e_banking.helpers.Executable;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class AddFunds implements Executable {
    private final CommandInput commandInput;

    public AddFunds(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    @Override
    public void execute() {
        ArrayList<User> users = AppLogic.getInstance().getUsers();

        for (User user : users) {
            if (user.addFundsToAccount(commandInput.getAccount(), commandInput.getAmount())) {
                break;
            }
        }
    }
}
