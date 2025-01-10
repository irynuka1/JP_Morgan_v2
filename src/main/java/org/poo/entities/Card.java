package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

@Getter@Setter
public final class Card implements ToOutput {
    private String cardNumber;
    private String associatedIban;
    private String userEmail;
    private String status;
    private String type;
    private static final double MIN_BALANCE_WARNING = 30;

    public Card(final String associatedIban, final String userEmail, final String cardType) {
        this.cardNumber = Utils.generateCardNumber();
        this.associatedIban = associatedIban;
        this.userEmail = userEmail;
        this.status = "active";
        this.type = cardType;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode cardNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        cardNode.put("cardNumber", this.cardNumber);
        cardNode.put("status", this.status);

        return cardNode;
    }

    /**
     * Updates the status of the card based on the balance and the minimum balance.
     * @param balance the balance of the card
     * @param minBalance the minimum balance of the card
     */
    public void updateStatus(final double balance, final double minBalance) {
        if (this.getStatus().equals("frozen")) {
            return;
        }

        if (balance <= minBalance) {
            this.status = "frozen";
        } else if (balance - minBalance <= MIN_BALANCE_WARNING) {
            this.status = "warning";
        } else {
            this.status = "active";
        }
    }

    /**
     * Resets the card number and status of the card.
     */
    public void resetCard() {
        this.cardNumber = Utils.generateCardNumber();
        this.status = "active";
    }
}
