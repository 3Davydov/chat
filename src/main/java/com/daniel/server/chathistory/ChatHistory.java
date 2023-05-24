package com.daniel.server.chathistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class ChatHistory {

    private String fileName;
    private int startBufferLen;

    public ChatHistory(String historyFileName) {
        this.fileName = historyFileName;
        startBufferLen = setStartLen();
    }

    private int setStartLen() {
        File file = new File(fileName);
        byte[] buffer = null;
        try {
            FileInputStream in = new FileInputStream(file);
            int fileLen = (int) file.length();
            buffer = new byte[fileLen];
            in.read(buffer);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        String s = new String(buffer);
        return (s.length() - s.replace("\n", "").length());
    }

    public int getStartLen() {
        return startBufferLen;
    }

    public void addMessageFromUser(String sender, String message) {
        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.append(sender + ":," + message + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSystemMessage(String message) {
        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.append("system:," + message + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileData getHistory() {
        File file = new File(fileName);
        byte[] buffer = null;
        try {
            FileInputStream in = new FileInputStream(file);
            int fileLen = (int) file.length();
            buffer = new byte[fileLen];
            in.read(buffer);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new FileData(buffer);
    }
}
