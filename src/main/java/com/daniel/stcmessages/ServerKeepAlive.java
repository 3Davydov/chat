package com.daniel.stcmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerKeepAlive implements STCMessage, Serializable {

    private final String messageName = "KeepAlive";

    public ServerKeepAlive() {}

    @Override
    public String getName() {
        return messageName;
    }

    @Override
    public ArrayList<Object> getData() {
        return null;
    }
}
