package com.daniel.stcmessages;

import java.io.Serializable;
import java.util.ArrayList;

import com.daniel.server.chathistory.FileData;

public class ChatHistoryMessage implements STCMessage, Serializable {

    private ArrayList<Object> data = new ArrayList<>();

    private final String messageName = "chatHistory";

    public ChatHistoryMessage(ArrayList<String> newData) {
        this.data.addAll(newData);
    }

    public ChatHistoryMessage(FileData fileData, Integer offset) {
        data = fileData.getDataWithOffset(offset);
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
