package com.daniel.server.connectmanager;

import com.daniel.exceptions.ConnectionError;
import com.daniel.exceptions.IllegalRequestException;
import com.daniel.server.ChatServerThread;
import com.daniel.server.ServerMain;
import com.daniel.server.chathistory.FileData;
import com.daniel.stcmessages.ChatHistoryMessage;
import com.daniel.stcmessages.ErrorMessage;
import com.daniel.stcmessages.STCMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ConnectionsManager extends Thread implements CTSPassingInterface, STCPassingInterface {

    private ServerMain server;
    private ServerSocket serverSocket;
    private final Map<Integer, ChatServerThread> connections = new HashMap<>();

    private Integer nextID = 0;
    private String protocol;

    public ConnectionsManager(ServerSocket serverSock, ServerMain server, String protocol) {
        this.protocol = protocol;
        this.server = server;
        this.serverSocket = serverSock;
        setName("Manager");
    }

    @Override
    public void run() {
        while (! this.isInterrupted()) {
            try {
                Socket newClient = serverSocket.accept();
                try {
                    ChatServerThread newConnection = new ChatServerThread(this, newClient, nextID, protocol);
                    connections.put(nextID, newConnection);
                    nextID++;
                    newConnection.start();
                } catch (ConnectionError e) {
                    new ObjectOutputStream(newClient.getOutputStream()).writeObject(new ErrorMessage("Cannot create connection"));
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void connectUser(String username, Integer sessionID) {
        try {
            server.registrateUser(sessionID, username);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void disconnectUser(Integer sessionID) {
        try {
            server.deleteUser(sessionID);
        } catch (IllegalRequestException e) {}
        if (connections.get(sessionID) != null) connections.get(sessionID).interrupt();
        connections.remove(sessionID);
    }

    public synchronized void disconnectUser(Integer sessionID, String reason) {
        try {
            server.deleteUser(sessionID, reason);
        } catch (IllegalRequestException e) {}
        connections.get(sessionID).interrupt();
        connections.remove(sessionID);
    }

    @Override
    public synchronized void requestForParticipantsList(Integer sessionID) {
        try {
            server.sendParticipantsTable(sessionID);
        } catch (IllegalRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void sendMessage(Integer sessionID, STCMessage message) {
        connections.get(sessionID).sendMessage(message);
    }

    @Override
    public synchronized void chatMessageNotification(String message, Integer sessionID) {
        server.addMessageToChatHistory(message, sessionID);
    }

    public void broadcastMessage(HashMap<Integer, Integer> offsets, FileData message, int recentMessagesCount, int startOffset) {
        try {
            for (Integer i : connections.keySet()) {
                if (offsets.get(i) != null) sendMessage(i, new ChatHistoryMessage(message, offsets.get(i) - recentMessagesCount + startOffset));
            }
        } catch (NullPointerException e) {}
    }
}
