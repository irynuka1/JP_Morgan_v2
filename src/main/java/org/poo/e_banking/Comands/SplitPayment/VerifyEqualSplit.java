package org.poo.e_banking.Comands.SplitPayment;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

public class VerifyEqualSplit extends VerifySplitPaymentBase {
    public VerifyEqualSplit(final CommandInput commandInput, final ArrayNode output) {
        super(commandInput, output, "equal");
    }
}
