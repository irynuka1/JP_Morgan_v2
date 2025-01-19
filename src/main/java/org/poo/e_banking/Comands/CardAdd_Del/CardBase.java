package org.poo.e_banking.Comands.CardAdd_Del;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.AppLogic;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

public abstract class CardBase implements Executable {
    protected final CommandInput commandInput;

    public CardBase(final CommandInput commandInput) {
        this.commandInput = commandInput;
    }

    /**
     * Creates a node with the created card information
     *
     * @param user        the user that is adding the card
     * @param description the description of the card
     * @param cardNumber  the card number
     * @param iban        the iban of the account
     * @return the created node
     */
    public ObjectNode toJson(final User user, final String description, final String cardNumber,
                             final String iban) {
        ObjectNode cardWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        cardWrapper.put("timestamp", commandInput.getTimestamp());
        cardWrapper.put("description", description);
        cardWrapper.put("card", cardNumber);
        cardWrapper.put("cardHolder", user.getEmail());
        cardWrapper.put("account", iban);
        return cardWrapper;
    }

    /**
     * If the user is found in the userMap, the command is executed
     */
    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        User user = appLogic.getUserMap().get(commandInput.getEmail());

        if (user == null) {
            return;
        }

        processCardCommand(user);
    }

    protected abstract void processCardCommand(User user);
}
