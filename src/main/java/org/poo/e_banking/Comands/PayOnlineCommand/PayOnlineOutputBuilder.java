package org.poo.e_banking.Comands.PayOnlineCommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.User;


public final class PayOnlineOutputBuilder {
    private PayOnlineOutputBuilder() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Adds a card not found message to the output.
     *
     * @param output    the output
     * @param command   the input command
     * @param timestamp the timestamp
     */
    public static void cardNotFound(final ArrayNode output, final String command,
                                    final long timestamp) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("command", command);

        ObjectNode outputNode = node.putObject("output");
        outputNode.put("timestamp", timestamp);
        outputNode.put("description", "Card not found");

        node.put("timestamp", timestamp);
        output.add(node);
    }

    /**
     * Creates a node for the frozen card output.
     *
     * @param timestamp the timestamp
     * @return the created node
     */
    public static ObjectNode frozenCard(final long timestamp) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", "The card is frozen");
        return node;
    }

    /**
     * Creates a node for the insufficient funds output.
     *
     * @param timestamp the timestamp
     * @return the created node
     */
    public static ObjectNode insufficientFunds(final long timestamp) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Insufficient funds");
        return node;
    }

    /**
     * Creates a node for the success output.
     *
     * @param timestamp the timestamp
     * @param amount    the amount that was paid
     * @param commerciant the commerciant that received the payment
     * @return the created node
     */
    public static ObjectNode success(final long timestamp, final Double amount,
                                     final String commerciant) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Card payment");
        node.put("amount", amount);
        node.put("commerciant", commerciant);
        return node;
    }

    /**
     * Creates an output node for destroying a card.
     *
     * @param account   the account that the card belongs to
     * @param user      the user that destroyed the card
     * @param cardNumber the card number
     * @param timestamp the timestamp
     * @return the created node
     */
    public static ObjectNode destroyCard(final Account account, final User user,
                                         final String cardNumber, final long timestamp) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("account", account.getIban());
        node.put("card", cardNumber);
        node.put("cardHolder", user.getEmail());
        node.put("description", "The card has been destroyed");
        node.put("timestamp", timestamp);
        return node;
    }

    /**
     * Creates an output node for creating a card.
     *
     * @param account   the account that the card belongs to
     * @param card      the card
     * @param user      the user that created the card
     * @param timestamp the timestamp
     * @return the created node
     */
    public static ObjectNode createCard(final Account account, final Card card, final User user,
                                        final long timestamp) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("account", account.getIban());
        node.put("card", card.getCardNumber());
        node.put("cardHolder", user.getEmail());
        node.put("description", "New card created");
        node.put("timestamp", timestamp);
        return node;
    }
}
