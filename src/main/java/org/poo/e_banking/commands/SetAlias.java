package org.poo.e_banking.commands;

import org.poo.e_banking.AppLogic;
import org.poo.e_banking.helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class SetAlias implements Executable {
    private final CommandInput commandInput;

    public SetAlias(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    @Override
    public void execute() {
        Map<String, User> userMap = AppLogic.getInstance().getUserMap();
        User user = userMap.get(commandInput.getEmail());

        if (user != null) {
            Account account = user.getAccountByIban(commandInput.getAccount());
            if (account != null) {
                account.setAlias(commandInput.getAlias());
            }
        }
    }
}
