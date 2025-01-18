package org.poo.e_banking.Comands.SplitPayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.e_banking.Comands.AppLogic;
import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

@Getter@Setter
public class PendingTransaction {
    private final CommandInput commandInput;
    private final int timestamp;
    private boolean isVerified = false;

    public PendingTransaction(final CommandInput commandInput, final int timestamp) {
        this.commandInput = commandInput;
        this.timestamp = timestamp;
    }

    public void verify(Integer timestamp) {
        for (String iban : commandInput.getAccounts()) {
            for (User user : AppLogic.getInstance().getUsers()) {
                Account account = user.getAccountByIban(iban);
                if (account != null) {
                    user.getPendingTransactions().stream()
                            .filter(pendingTransaction -> pendingTransaction.getTimestamp() == timestamp)
                            .forEach(pendingTransaction -> pendingTransaction.setVerified(true));
                    break;
                }
            }
        }
    }
}
