package org.poo.e_banking.CardAdd_Del;

import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.Map;

public final class DeleteCard extends CardBase {
    public DeleteCard(final CommandInput commandInput, final Map<String, User> userMap) {
        super(commandInput, userMap);
    }

    /**
     * Process the command to delete a card
     * @param user the user that wants to delete a card
     */
    @Override
    protected void processCardCommand(final User user) {
        Card card = user.getCardByNumber(commandInput.getCardNumber());
        if (card != null) {
            Account account = user.getAccountByIban(card.getAssociatedIban());
            account.removeCard(commandInput.getCardNumber());

            user.getTransactionsNode().add(toJson(user,
                    "The card has been destroyed",
                    commandInput.getCardNumber(),
                    account.getIban()));
        }
    }
}
