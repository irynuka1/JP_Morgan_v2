package org.poo.e_banking.commands.splitPayment;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

public class VerifyCustomSplit extends VerifySplitPaymentBase {
    public VerifyCustomSplit(final CommandInput commandInput, final ArrayNode output) {
        super(commandInput, output, "custom");
    }
}
