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
     * @param output            The output array.
     */
    public static void selectCommand(final CommandInput commandInput, final ArrayNode output) {
        Executable command = null;

        switch (commandInput.getCommand()) {
            case "printUsers":
                command = new PrintUsers(commandInput, output);
                break;
            case "addAccount":
                command = new AddAccount(commandInput);
                break;
            case "createCard":
                command = new CreateCard(commandInput, "Normal");
                break;
            case "createOneTimeCard":
                command = new CreateCard(commandInput, "OneTime");
                break;
            case "addFunds":
                command = new AddFunds(commandInput);
                break;
            case "deleteAccount":
                command = new DeleteAccount(commandInput, output);
                break;
            case "deleteCard":
                command = new DeleteCard(commandInput);
                break;
            case "setMinimumBalance":
                command = new SetMinBalance(commandInput);
                break;
            case "payOnline":
                command = new PayOnline(commandInput, output);
                break;
            case "sendMoney":
                command = new SendMoney(commandInput);
                break;
            case "printTransactions":
                command = new PrintTransactions(commandInput, output);
                break;
            case "setAlias":
                command = new SetAlias(commandInput);
                break;
            case "checkCardStatus":
                command = new CheckCardStatus(commandInput, output);
                break;
            case "changeInterestRate":
                command = new ChangeInterestRate(commandInput, output);
                break;
            case "splitPayment":
                command = new SplitPayment(commandInput);
                break;
            case "report":
                command = new Report(commandInput, output);
                break;
            case "spendingsReport":
                command = new SpendingsReport(commandInput, output);
                break;
            case "addInterest":
                command = new AddInterestRate(commandInput, output);
                break;
            case "withdrawSavings":
                command = new WithdrawSavings(commandInput);
                break;
            default:
                System.out.println("Invalid command");
        }

        if (command != null) {
            command.execute();
        }
    }
}
