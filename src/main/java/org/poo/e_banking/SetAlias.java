package org.poo.e_banking;

import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class SetAlias implements Executable {
    private final CommandInput commandInput;
    private final Map<String, User> userMap;

    public SetAlias(final CommandInput commandInput, final Map<String, User> userMap) {
        this.commandInput = commandInput;
        this.userMap = userMap;
    }

    @Override
    public void execute() {
        User user = userMap.get(commandInput.getEmail());

        if (user != null) {
            Account account = user.getAccountByIban(commandInput.getAccount());
            if (account != null) {
                account.setAlias(commandInput.getAlias());
            }
        }
    }
}
