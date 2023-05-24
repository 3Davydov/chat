package com.daniel.stcmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class FilledListMessage implements STCMessage, Serializable {

    private ArrayList<Object> data = new ArrayList<>();

    private final String messageName = "filledList";

    public FilledListMessage(ArrayList<String> newData) {
        this.data.addAll(newData);
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
