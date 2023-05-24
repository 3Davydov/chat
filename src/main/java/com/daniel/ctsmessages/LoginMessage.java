package com.daniel.ctsmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class LoginMessage implements Serializable, CTSMessage {

    private ArrayList<Object> data = new ArrayList<>();
    private final String messageName = "login";

    public LoginMessage(String username) {
        data.add(username);
    }

    public LoginMessage() {}

    @Override
    public String getName() {
        return messageName;
    }

    @Override
    public ArrayList<Object> getData() {
        return data;
    }
}
