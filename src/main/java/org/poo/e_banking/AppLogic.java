package org.poo.e_banking;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.e_banking.Helpers.CommandSelector;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class AppLogic {
    private static ArrayList<User> users = null;
    private static ArrayNode output = null;
    private static Map<String, User> userMap = new HashMap<>();
    private static ExchangeRateManager exchangeRateManager;

    private AppLogic() {
    }

    /**
     * Initializes the application with the object input.
     *
     * @param objectInput The object input to be used.
     */
    public static void initApp(final ObjectInput objectInput) {
        users = new ArrayList<>();
        exchangeRateManager = new ExchangeRateManager();

        for (var user : objectInput.getUsers()) {
            users.add(new User(user.getFirstName(), user.getLastName(), user.getEmail()));
            userMap.put(user.getEmail(), users.getLast());
        }

        for (var exchangeRate : objectInput.getExchangeRates()) {
            exchangeRateManager.addExchangeRate(exchangeRate.getFrom(), exchangeRate.getTo(),
                                                exchangeRate.getRate());
        }
    }

    /**
     * Selects the command to be executed based on the command input.
     *
     * @param commandInput The command input to be processed.
     */
    public static void processCommands(final CommandInput commandInput) {
        CommandSelector.selectCommand(commandInput, userMap, output, users, exchangeRateManager);
    }

    /**
     * Iterates through the input and processes the commands.
     *
     * @param objectInput The object input to be used.
     */
    public static void startSession(final ObjectInput objectInput) {
        for (int i = 0; i < objectInput.getCommands().length; i++) {
            processCommands(objectInput.getCommands()[i]);
        }
    }

    /**
     * Sets the output object to be used by the AppLogic class.
     *
     * @param outputNode The output object to be used.
     */
    public static void setOutputObject(final ArrayNode outputNode) {
        AppLogic.output = outputNode;
    }
}
