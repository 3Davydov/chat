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
import java.util.TreeMap;

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

    private HashMap<Long, STCMessage> serverArchive;
    private HashMap<Long, CTSMessage> clientArchive;

    public ChatServerThread(ConnectionsManager connectionsManager, Socket client, Integer sessionID, String protocol) throws ConnectionError {
        setName("chatThread" + sessionID.toString());
        this.sessionID = sessionID;
        this.protocol = protocol;
        this.connectionsManager = connectionsManager;
        serverArchive = new HashMap<>();
        clientArchive = new HashMap<>();
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
                // try {
                // Map<Long, Object> s = new TreeMap<>();
                // if (serverArchive.size() > 0) s.putAll(serverArchive);
                // if (clientArchive.size() > 0) s.putAll(clientArchive);
                // for (Map.Entry<Long, Object> entry : s.entrySet()) {
                //     Object value = entry.getValue();
                //     if (value instanceof STCMessage) sendMessage((STCMessage) value);
                //     else {
                //         CTSMessage copy = (CTSMessage) value;
                //         clientMessageData = copy.getData();
                //         reactions.get(copy.getName()).run();
                //     }
                // }
                // serverArchive.clear();
                // clientArchive.clear();
                // } catch (Exception e) {
                //     e.printStackTrace();
                // }
                sendMessage(new ServerKeepAlive());
            }
        });
    }

    private CTSMessage pingClient() {
        return null;
        // TODO realize
    }

    private CTSMessage readClientMessage() throws Exception {
        CTSMessage message = null;
        if (protocol.equals("Basic")) {
            try {
                message = (CTSMessage) objectInputStream.readObject();
            } catch (SocketTimeoutException e) {
                // e.printStackTrace();
                suspicionOnZombie = true;
                for (int i = 0; i < 4; i++) {
                    while (true) {
                        try {
                            message = (CTSMessage) objectInputStream.readObject();
                        } catch (SocketTimeoutException err) {
                            if (i == 3) return new LogoutMessage();
                            else break;
                        } 
                        catch (ClassNotFoundException | IOException err) {
                            err.printStackTrace();
                        }
                        if (message.getName().equals("KeepAlive")) {
                            suspicionOnZombie = false;
                            break;
                        }
                        else {
                            clientArchive.put(Long.valueOf(System.currentTimeMillis()), message);
                        }
                    }
                    if (suspicionOnZombie == false) break;
                }
                reactions.get(message.getName()).run();
            } catch (ClassNotFoundException | IOException | NullPointerException e) {
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
                if (message == null) continue; // TODO maybe delete
                clientMessageData = message.getData();
                reactions.get(message.getName()).run();
            } catch (Exception e) {
                connectionsManager.disconnectUser(sessionID);
            }
        }
        System.out.println("connection " + String.valueOf(sessionID) + " interrupted");
    }

    public void sendMessage(STCMessage message) {
        if (suspicionOnZombie == true && ! message.getName().equals("KeepAlive")) {
            serverArchive.put(Long.valueOf(System.currentTimeMillis()), message);
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
