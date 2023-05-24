package com.daniel.ctsmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class ListMessage implements CTSMessage, Serializable {

    private final String messageName = "list";

    public ListMessage() {}

    @Override
    public String getName() {
        return messageName;
    }

    @Override
    public ArrayList<Object> getData() {
        return null;
    }
}
