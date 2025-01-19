package org.poo.e_banking.helpers;

import org.poo.entities.User;

public final class Commission {
    public static final double STANDARD_COMMISSION = 0.002;
    public static final double SILVER_COMMISSION = 0.001;
    public static final int MAX_SUM = 500;

    private Commission() {
    }

    /**
     * Get the commission for a user
     * @param user the user
     * @param cost the cost of the transaction
     * @return the commission
     */
    public static double getCommission(final User user, final double cost) {
        switch (user.getPlan()) {
            case "standard" -> {
                return STANDARD_COMMISSION;
            }
            case "silver" -> {
                if (cost < MAX_SUM) {
                    return 0;
                } else {
                    return SILVER_COMMISSION;
                }
            }
            default -> {
                return 0;
            }
        }
    }
}
