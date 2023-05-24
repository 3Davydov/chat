package com.daniel.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.daniel.XMLConverter.ConverterFactory;
import com.daniel.XMLConverter.ClientToXML.ClientMessageConvFactory;
import com.daniel.XMLConverter.ServerToXML.ServerMessageConvFactory;
import com.daniel.ctsmessages.CTSMessage;
import com.daniel.exceptions.ConnectionError;
import com.daniel.exceptions.ConvertionException;
import com.daniel.server.connectmanager.ConnectionsManager;
import com.daniel.stcmessages.STCMessage;

public class ChatServerThread extends Thread {

    private ConnectionsManager connectionsManager;
    private Integer sessionID;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    private Map<String, Runnable> reactions = new HashMap<>();
    private ArrayList<Object> clientMessageData;

    private String protocol;

    public ChatServerThread(ConnectionsManager connectionsManager, Socket client, Integer sessionID, String protocol) throws ConnectionError {
        setName("chatThread" + sessionID.toString());
        this.sessionID = sessionID;
        this.protocol = protocol;
        this.connectionsManager = connectionsManager;
        try {
            objectInputStream = new ObjectInputStream(client.getInputStream());
            objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            throw new ConnectionError("Cannot connect to new client : " + getName());
        }
        initReactions();
    }

    private void initReactions() {
        reactions.put("login", new Runnable() {
            @Override
            public void run() {
                connectionsManager.connectUser((String) clientMessageData.get(0), sessionID);
            }
        });
        reactions.put("logout", new Runnable() {
            @Override
            public void run() {
                connectionsManager.disconnectUser(sessionID);
            }
        });
        reactions.put("list", new Runnable() {
            @Override
            public void run() {
                connectionsManager.requestForParticipantsList(sessionID);
            }
        });
        reactions.put("text", new Runnable() {
            @Override
            public void run() {
                connectionsManager.chatMessageNotification((String) clientMessageData.get(0), sessionID);
            }
        });
    }

    private CTSMessage readClientMessage() throws Exception {
        CTSMessage message = null;
        if (protocol.equals("Basic")) {
            try {
                message = (CTSMessage) objectInputStream.readObject();
            } catch (SocketException e) {
                e.printStackTrace();
            } 
            catch (ClassNotFoundException | IOException e) {
                // e.printStackTrace();
                throw e;
            } catch (NullPointerException e) {
                // e.printStackTrace();
                throw e;
            }
        }
        if (protocol.equals("XML")) {
            String XMLMessage = (String) objectInputStream.readObject();
            ConverterFactory converterFactory = new ClientMessageConvFactory();
            message = converterFactory.convertFromSerializableXMLtoCM(XMLMessage);
        }
        return message;
    }

    @Override
    public void run() {
        while (! this.isInterrupted()) {
            try {
                CTSMessage message = readClientMessage();
                clientMessageData = message.getData();
                reactions.get(message.getName()).run();
            } catch (Exception e) {
                connectionsManager.disconnectUser(sessionID);
            }
        }
        System.out.println("connection " + String.valueOf(sessionID) + " interrupted");
    }

    public void sendMessage(STCMessage message) {
        if (protocol.equals("Basic")) {
            try {
                objectOutputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (protocol.equals("XML")) {
            ConverterFactory converterFactory = new ServerMessageConvFactory();
            String strMessage = null;
            try {
                strMessage = converterFactory.convertToSerializableXML(message.getName(), message.getData());
            } catch (ConvertionException e) {
                e.printStackTrace();
                return;
            }
            try {
                objectOutputStream.writeObject(strMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
