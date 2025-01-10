package org.poo.e_banking;

import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class SetMinBalance implements Executable {
    private final ArrayList<User> users;
    private final CommandInput commandInput;

    public SetMinBalance(final ArrayList<User> users, final CommandInput commandInput) {
        this.users = users;
        this.commandInput = commandInput;
    }

    @Override
    public void execute() {
        String iban = commandInput.getAccount();
        for (User user : users) {
            if (user.getAccountByIban(iban) != null) {
                user.getAccountByIban(iban).setMinBalance(commandInput.getAmount());
                break;
            }
        }
    }
}
