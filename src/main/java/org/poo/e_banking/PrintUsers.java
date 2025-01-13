package org.poo.e_banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.e_banking.Helpers.Executable;
import org.poo.entities.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public final class PrintUsers implements Executable {
    private final CommandInput commandInput;
    private final ArrayNode output;

    public PrintUsers(final CommandInput commandInput, final ArrayNode output) {
        this.commandInput = commandInput;
        this.output = output;
    }

    @Override
    public void execute() {
        AppLogic appLogic = AppLogic.getInstance();
        ArrayList<User> users = appLogic.getUsers();

        ObjectNode usersWrapper = new ObjectNode(new ObjectMapper().getNodeFactory());
        usersWrapper.put("command", commandInput.getCommand());

        ArrayNode usersNode = usersWrapper.putArray("output");
        for (User user : users) {
            usersNode.add(user.toJson());
        }

        usersWrapper.put("timestamp", commandInput.getTimestamp());
        output.add(usersWrapper);
    }
}
