package com.daniel.stcmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class LoginStatus implements STCMessage, Serializable {

    private ArrayList<Object> data = new ArrayList<>();

    private final String messageName = "LoginStatus";

    public LoginStatus(String reason, Boolean isSuccessful) {
        data.add(isSuccessful);
        data.add(reason);
    }

    @Override
    public String getName() {
        return messageName;
    }

    @Override
    public ArrayList<Object> getData() {
        return data;
    }
}
