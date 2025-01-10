package org.poo.e_banking.CardAdd_Del;

import org.poo.entities.Account;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class CreateCard extends CardBase {
    private final String cardType;

    public CreateCard(final CommandInput commandInput, final String cardType,
                      final Map<String, User> userMap) {
        super(commandInput, userMap);
        this.cardType = cardType;
    }

    @Override
    protected void processCardCommand(final User user) {
        Account account = user.getAccountByIban(commandInput.getAccount());
        if (account == null) {
            return;
        }
        user.addCardToAccount(commandInput.getAccount(), cardType);

        user.getTransactionsNode().add(toJson(user,
                "New card created",
                account.getCards().getLast().getCardNumber(),
                commandInput.getAccount()));
        account.getTransactionsNode().add(toJson(user,
                "New card created",
                account.getCards().getLast().getCardNumber(),
                commandInput.getAccount()));
    }
}
