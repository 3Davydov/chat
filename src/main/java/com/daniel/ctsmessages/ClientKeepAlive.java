package com.daniel.ctsmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class ClientKeepAlive implements CTSMessage, Serializable {

    private final String messageName = "KeepAlive";

    public ClientKeepAlive() {}

    @Override
    public String getName() {
        return messageName;
    }

    @Override
    public ArrayList<Object> getData() {
        return null;
    }
}
