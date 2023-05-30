package com.daniel.ctsmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class LogoutMessage implements Serializable, CTSMessage {

    private final String messageName = "logout";
    private ArrayList<Object> data = new ArrayList<>();

    public LogoutMessage() {}

    public LogoutMessage(String reason) {
        data.add(reason);
    }

    @Override
    public String getName() {
        return messageName;
    }

    @Override
    public ArrayList<Object> getData() {
        return null;
    }
}
