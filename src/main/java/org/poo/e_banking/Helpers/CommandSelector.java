package org.poo.e_banking.Helpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.e_banking.AccountAdd_Del.AddAccount;
import org.poo.e_banking.AccountAdd_Del.DeleteAccount;
import org.poo.e_banking.*;
import org.poo.e_banking.CardAdd_Del.CreateCard;
import org.poo.e_banking.CardAdd_Del.DeleteCard;
import org.poo.e_banking.InterestRate.AddInterestRate;
import org.poo.e_banking.InterestRate.ChangeInterestRate;
import org.poo.e_banking.PayOnlineCommand.PayOnline;
import org.poo.e_banking.Reports.Report;
import org.poo.e_banking.Reports.SpendingsReport;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Map;

public final class CommandSelector {
    private CommandSelector() {
    }

    /**
     * Selects the command to be executed based on the input command.
     *
     * @param commandInput      The input.
     * @param userMap           The map of users.
     * @param output            The output array.
     * @param users             The list of users.
     * @param exchangeRateManager The exchange rate manager.
     */
    public static void selectCommand(final CommandInput commandInput,
                                     final Map<String, User> userMap, final ArrayNode output,
                                     final ArrayList<User> users,
                                     final ExchangeRateManager exchangeRateManager) {
        Executable command = null;

        switch (commandInput.getCommand()) {
            case "printUsers":
                command = new PrintUsers(commandInput, output, users);
                break;
            case "addAccount":
                command = new AddAccount(commandInput, userMap);
                break;
            case "createCard":
                command = new CreateCard(commandInput, "Normal", userMap);
                break;
            case "createOneTimeCard":
                command = new CreateCard(commandInput, "OneTime", userMap);
                break;
            case "addFunds":
                command = new AddFunds(users, commandInput);
                break;
            case "deleteAccount":
                command = new DeleteAccount(commandInput, output, userMap);
                break;
            case "deleteCard":
                command = new DeleteCard(commandInput, userMap);
                break;
            case "setMinimumBalance":
                command = new SetMinBalance(users, commandInput);
                break;
            case "payOnline":
                command = new PayOnline(commandInput, output, userMap, exchangeRateManager);
                break;
            case "sendMoney":
                command = new SendMoney(commandInput, users, exchangeRateManager);
                break;
            case "printTransactions":
                command = new PrintTransactions(commandInput, output, userMap);
                break;
            case "setAlias":
                command = new SetAlias(commandInput, userMap);
                break;
            case "checkCardStatus":
                command = new CheckCardStatus(commandInput, users, output);
                break;
            case "changeInterestRate":
                command = new ChangeInterestRate(users, commandInput, output);
                break;
            case "splitPayment":
                command = new SplitPayment(commandInput, users, userMap, exchangeRateManager);
                break;
            case "report":
                command = new Report(commandInput, users, output);
                break;
            case "spendingsReport":
                command = new SpendingsReport(commandInput, users, output);
                break;
            case "addInterest":
                command = new AddInterestRate(users, commandInput, output);
                break;
            default:
                System.out.println("Invalid command");
        }

        if (command != null) {
            command.execute();
        }
    }
}
