package com.daniel.stcmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class ErrorMessage implements STCMessage, Serializable {

    private ArrayList<Object> data = new ArrayList<>();

    private final String messageName = "error";

    public ErrorMessage(String error) {
        this.data.add(error);
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
