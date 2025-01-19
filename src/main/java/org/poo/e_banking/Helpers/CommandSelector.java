package org.poo.e_banking.Helpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.e_banking.Comands.*;
import org.poo.e_banking.Comands.AccountAdd_Del.AddAccount;
import org.poo.e_banking.Comands.AccountAdd_Del.DeleteAccount;
import org.poo.e_banking.Comands.CardAdd_Del.CreateCard;
import org.poo.e_banking.Comands.CardAdd_Del.DeleteCard;
import org.poo.e_banking.Comands.InterestRate.AddInterestRate;
import org.poo.e_banking.Comands.InterestRate.ChangeInterestRate;
import org.poo.e_banking.Comands.PayOnlineCommand.PayOnline;
import org.poo.e_banking.Comands.Reports.Report;
import org.poo.e_banking.Comands.Reports.SpendingsReport;
import org.poo.e_banking.Comands.SplitPayment.VerifyCustomSplit;
import org.poo.e_banking.Comands.SplitPayment.VerifyEqualSplit;
import org.poo.fileio.CommandInput;

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
                command = new SendMoney(commandInput, output);
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
                if (commandInput.getSplitPaymentType().equals("equal")) {
                    command = new VerifyEqualSplit(commandInput, output);
                } else {
                    command = new VerifyCustomSplit(commandInput, output);
                }
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
            case "upgradePlan":
                command = new UpgradePlan(commandInput, output);
                break;
            case "cashWithdrawal":
                command = new CashWithdrawal(commandInput, output);
                break;
            default:
                break;
        }

        if (command != null) {
            command.execute();
        }
    }
}
