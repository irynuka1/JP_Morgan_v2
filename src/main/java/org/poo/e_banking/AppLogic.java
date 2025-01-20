package org.poo.e_banking;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.helpers.CommandSelector;
import org.poo.e_banking.helpers.ExchangeRateManager;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;
import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ObjectInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

public final class AppLogic {
    private static AppLogic instance = null;

    private static final int STANDARD_TAX = 100;
    private static final int SILVER_TAX = 250;
    private static final int GOLD_TAX = 350;

    private static ArrayList<User> users;
    private static ArrayNode output;
    private static Map<String, User> userMap;
    private static Map<String, Integer> planTaxMap;
    private static ExchangeRateManager exchangeRateManager;
    private static List<CommerciantInput> commerciants;
    private static ObjectInput objectInput;
    private static ArrayList<Integer> verificationTimestamps;
    private static ArrayList<ObjectNode> userNotFounds;

    private AppLogic() {
        this.users = new ArrayList<>();
        this.userMap = new HashMap<>();
        this.exchangeRateManager = new ExchangeRateManager();
        this.planTaxMap = new HashMap<>();
        this.commerciants = new ArrayList<>();
        this.verificationTimestamps = new ArrayList<>();
        this.userNotFounds = new ArrayList<>();
    }

    /**
     * Returns the instance of the AppLogic class or creates a new one if it doesn't exist.
     *
     * @return The instance of the AppLogic class.
     */
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
     * @param input The object input to be used.
     */
    public void initApp(final ObjectInput input) {
        users.clear();
        userMap.clear();
        planTaxMap.clear();
        this.objectInput = input;

        for (var user : input.getUsers()) {
            users.add(new User(user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getBirthDate(), user.getOccupation()));
            userMap.put(user.getEmail(), users.getLast());
        }

        commerciants.addAll(Arrays.asList(input.getCommerciants()));

        for (var exchangeRate : input.getExchangeRates()) {
            exchangeRateManager.addExchangeRate(exchangeRate.getFrom(), exchangeRate.getTo(),
                                                exchangeRate.getRate());
        }

        planTaxMap.put("standard -> silver", STANDARD_TAX);
        planTaxMap.put("student -> silver", STANDARD_TAX);
        planTaxMap.put("silver -> gold", SILVER_TAX);
        planTaxMap.put("standard -> gold", GOLD_TAX);
        planTaxMap.put("student -> gold", GOLD_TAX);
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
     * @param input The object input to be used.
     */
    public void startSession(final ObjectInput input) {
        for (int i = 0; i < input.getCommands().length; i++) {
            processCommands(input.getCommands()[i]);
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

    public Map<String, Integer> getPlanTaxMap() {
        return planTaxMap;
    }

    public List<CommerciantInput> getCommerciants() {
        return commerciants;
    }

    public ObjectInput getObjectInput() {
        return objectInput;
    }

    public ArrayList<Integer> getVerificationTimestamps() {
        return verificationTimestamps;
    }

    public ArrayList<ObjectNode> getUserNotFounds() {
        return userNotFounds;
    }

    /**
     * Resets the instance of the AppLogic class.
     */
    public static void resetInstance() {
        instance = null;
    }
}
