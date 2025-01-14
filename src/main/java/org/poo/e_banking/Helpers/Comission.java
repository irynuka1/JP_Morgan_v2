package org.poo.e_banking.Helpers;

import org.poo.entities.User;

public class Comission {
    public Comission() {
    }

    public static double getComission(User user, double sum) {
        if (user.getPlan().equals("student") || user.getPlan().equals("gold")) {
            return 0;
        } else if (user.getPlan().equals("standard")) {
            return 0.002;
        } else if (user.getPlan().equals("silver")) {
            if (sum < 500)
                return 0;
            else
                return 0.001;
        }

        return 0;
    }
}
