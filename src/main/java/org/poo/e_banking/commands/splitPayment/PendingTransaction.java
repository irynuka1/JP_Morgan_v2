package org.poo.e_banking.commands.splitPayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.e_banking.AppLogic;
import org.poo.fileio.CommandInput;

@Getter@Setter
public final class PendingTransaction {
    private final CommandInput commandInput;
    private final int timestamp;
    private boolean isVerified = false;

    public PendingTransaction(final CommandInput commandInput, final int timestamp) {
        this.commandInput = commandInput;
        this.timestamp = timestamp;
    }

    /**
     * Verifies the pending transaction with the given timestamp.
     */
    public void verify(final Integer currentTimestamp) {
        commandInput.getAccounts().stream()
                .flatMap(iban -> AppLogic.getInstance().getUsers().stream()
                        .filter(user -> user.getAccountByIban(iban) != null))
                .forEach(user -> user.getPendingTransactions().stream()
                        .filter(pendingTransaction ->
                                pendingTransaction.getTimestamp() == currentTimestamp)
                        .findFirst()
                        .ifPresent(pendingTransaction -> pendingTransaction.setVerified(true)));
    }
}
