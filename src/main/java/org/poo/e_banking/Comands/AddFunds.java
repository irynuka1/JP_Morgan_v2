package org.poo.e_banking.Comands;

import org.poo.e_banking.Helpers.Executable;
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
