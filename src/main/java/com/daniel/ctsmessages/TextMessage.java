package com.daniel.ctsmessages;

import java.io.Serializable;
import java.util.ArrayList;

public class TextMessage implements CTSMessage, Serializable{

    private final String messageName = "text";
    private ArrayList<Object> data = new ArrayList<>();

    public TextMessage(String text) {
        data.add(text);
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
