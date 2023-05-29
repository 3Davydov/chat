package com.daniel.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.daniel.XMLConverter.ConverterFactory;
import com.daniel.XMLConverter.ClientToXML.ClientMessageConvFactory;
import com.daniel.XMLConverter.ServerToXML.ServerMessageConvFactory;
import com.daniel.ctsmessages.CTSMessage;
import com.daniel.ctsmessages.LogoutMessage;
import com.daniel.exceptions.ConnectionError;
import com.daniel.exceptions.ConvertionException;
import com.daniel.properties.PropertiesReader;
import com.daniel.server.connectmanager.ConnectionsManager;
import com.daniel.stcmessages.STCMessage;
import com.daniel.stcmessages.ServerKeepAlive;

public class ChatServerThread extends Thread {

    private ConnectionsManager connectionsManager;
    private Integer sessionID;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    private Map<String, Runnable> reactions = new HashMap<>();
    private ArrayList<Object> clientMessageData;

    private String protocol;
    private boolean suspicionOnZombie = false;

    private ArrayList<STCMessage> archive;

    public ChatServerThread(ConnectionsManager connectionsManager, Socket client, Integer sessionID, String protocol) throws ConnectionError {
        setName("chatThread" + sessionID.toString());
        this.sessionID = sessionID;
        this.protocol = protocol;
        this.connectionsManager = connectionsManager;
        PropertiesReader propertiesReader = new PropertiesReader();
        propertiesReader.getAllProperties("/serverConfig.properties");
        try {
            client.setSoTimeout(propertiesReader.getTimeout());
        } catch (SocketException e) {
            e.printStackTrace();
        }
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
        reactions.put("KeepAlive", new Runnable() {
            @Override
            public void run() {
                suspicionOnZombie = false;
                int archiveLen = archive.size();
                if (archiveLen > 0) {
                    for (int i = 0; i < archiveLen; i++) {
                        sendMessage(archive.remove(i));
                    }
                }
            }
        });
    }

    private CTSMessage readClientMessage() throws Exception {
        CTSMessage message = null;
        if (protocol.equals("Basic")) {
            while (true) {
                try {
                    message = (CTSMessage) objectInputStream.readObject();
                    break;
                } catch (SocketTimeoutException e) {
                    if (suspicionOnZombie == false) {
                        suspicionOnZombie = true;
                        sendMessage(new ServerKeepAlive());
                    }
                    else {
                        return new LogoutMessage();
                    }
                }
                catch (ClassNotFoundException | IOException | NullPointerException e) {
                    throw e;
                }
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
                e.printStackTrace();
                connectionsManager.disconnectUser(sessionID);
            }
        }
        System.out.println("connection " + String.valueOf(sessionID) + " interrupted");
    }

    public void sendMessage(STCMessage message) {
        if (suspicionOnZombie == true) {
            archive.add(message);
            return;
        }
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
