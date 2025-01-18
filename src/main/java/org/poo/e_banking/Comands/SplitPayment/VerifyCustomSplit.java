package org.poo.e_banking.Comands.SplitPayment;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

public class VerifyCustomSplit extends VerifySplitPaymentBase {
    public VerifyCustomSplit(CommandInput commandInput, ArrayNode output) {
        super(commandInput, output, "custom");
    }
}
