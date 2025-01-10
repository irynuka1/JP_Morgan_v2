package org.poo.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter@Setter
public class Account implements ToOutput {
    private String iban;
    private double balance;
    private String currency;
    private String userEmail;
    private double minBalance;
    private List<Card> cards;
    private String type;
    private String alias;
    private String plan;
    private ObjectNode accountNode;
    private ArrayNode transactionsNode;
    private Map<String, Card> cardMap = new HashMap<>();

    public Account(final String userEmail, final String currency) {
        this.iban = Utils.generateIBAN();
        this.balance = 0.0;
        this.currency = currency;
        this.userEmail = userEmail;
        this.minBalance = 0.0;
        this.cards = new ArrayList<>();
        this.alias = "";
        this.plan = "";
        this.accountNode = new ObjectNode(new ObjectMapper().getNodeFactory());
        accountNode.put("command", "report");
        transactionsNode = accountNode.putArray("transactions");
    }

    /**
     * This method is used to add a card to the account.
     * @param cardType The type of the card to add.
     */
    public void addCard(final String cardType) {
        Card newCard = new Card(this.iban, this.userEmail, cardType);
        cards.add(newCard);
        cardMap.put(newCard.getCardNumber(), newCard);
    }

    /**
     * This method is used to remove a card from the account.
     * @param cardNumber The number of the card to remove.
     */
    public void removeCard(final String cardNumber) {
        if (cardMap.containsKey(cardNumber)) {
            cards.remove(cardMap.get(cardNumber));
            cardMap.remove(cardNumber);
        }
    }

    /**
     * This method is used to add funds to the account.
     * @param amount The amount to add.
     */
    public void addFunds(final double amount) {
        this.balance += amount;
    }

    /**
     * This method is used to withdraw funds from the account.
     * @param amount The amount to withdraw.
     * @return Returns true if the withdrawal was successful, false otherwise.
     */
    public boolean withdrawFunds(final double amount) {
        if (this.balance - amount >= 0) {
            this.balance -= amount;
            return true;
        }

        return false;
    }

    /**
     * This method is used to pay by card.
     * @param card The card used for the payment.
     * @param amount The amount to pay.
     * @return Returns true if the payment was successful, false otherwise.
     */
    public boolean payByCard(final Card card, final double amount) {
        if (this.balance - amount >= this.minBalance) {
            this.balance -= amount;

            if (card.getType().equals("OneTime")) {
                String currentNumber = card.getCardNumber();
                cardMap.remove(currentNumber);
                card.resetCard();
                cardMap.put(card.getCardNumber(), card);
            }

            return true;
        }
        return false;
    }

    /**
     * This method creates a JSON object with the account information and the cards.
     * @return Returns the JSON object.
     */
    @Override
    public ObjectNode toJson() {
        ObjectNode account = new ObjectNode(new ObjectMapper().getNodeFactory());
        account.put("IBAN", this.iban);
        account.put("balance", this.balance);
        account.put("currency", this.currency);
        account.put("type", this.type);

        ArrayNode cardsNode = account.putArray("cards");
        for (Card card : cards) {
            cardsNode.add(card.toJson());
        }

        return account;
    }
}
