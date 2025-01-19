package org.poo.e_banking.Comands;

import org.poo.e_banking.AppLogic;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class SetMinBalance implements Executable {
    private final CommandInput commandInput;

    public SetMinBalance(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    @Override
    public void execute() {
        ArrayList<User> users = AppLogic.getInstance().getUsers();
        String iban = commandInput.getAccount();
        for (User user : users) {
            if (user.getAccountByIban(iban) != null) {
                user.getAccountByIban(iban).setMinBalance(commandInput.getAmount());
                break;
            }
        }
    }
}
