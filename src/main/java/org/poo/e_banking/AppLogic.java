package org.poo.e_banking;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.e_banking.Helpers.CommandSelector;
import org.poo.e_banking.Helpers.ExchangeRateManager;
import org.poo.entities.Commerciant;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class AppLogic {
    private static AppLogic instance = null;

    private static ArrayList<User> users;
    private static ArrayNode output;
    private static Map<String, User> userMap;
    private static ExchangeRateManager exchangeRateManager;

    private AppLogic() {
        this.users = new ArrayList<>();
        this.userMap = new HashMap<>();
        this.exchangeRateManager = new ExchangeRateManager();
    }

    public static AppLogic getInstance() {
        if (instance == null) {
            synchronized (AppLogic.class) {
                if (instance == null) {
                    instance = new AppLogic();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the application with the object input.
     *
     * @param objectInput The object input to be used.
     */
    public void initApp(final ObjectInput objectInput) {
        users.clear();
        userMap.clear();

        for (var user : objectInput.getUsers()) {
            users.add(new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getBirthDate(),
                               user.getOccupation()));
            userMap.put(user.getEmail(), users.getLast());
        }

        for (var commerciant : objectInput.getCommerciants()) {
            for (var user : users) {
                user.getComerciants().add(new Commerciant(commerciant));
            }
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
    public void processCommands(final CommandInput commandInput) {
        CommandSelector.selectCommand(commandInput, output);
    }

    /**
     * Iterates through the input and processes the commands.
     *
     * @param objectInput The object input to be used.
     */
    public void startSession(final ObjectInput objectInput) {
        for (int i = 0; i < objectInput.getCommands().length; i++) {
            processCommands(objectInput.getCommands()[i]);
        }
    }

    /**
     * Sets the output object to be used by the AppLogic class.
     *
     * @param outputNode The output object to be used.
     */
    public void setOutputObject(final ArrayNode outputNode) {
        AppLogic.output = outputNode;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public ExchangeRateManager getExchangeRateManager() {
        return exchangeRateManager;
    }
}
