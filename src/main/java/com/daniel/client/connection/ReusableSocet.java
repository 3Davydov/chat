package com.daniel.client.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.daniel.XMLConverter.ConverterFactory;
import com.daniel.XMLConverter.ClientToXML.ClientMessageConvFactory;
import com.daniel.XMLConverter.ServerToXML.ServerMessageConvFactory;
import com.daniel.client.Client;
import com.daniel.ctsmessages.CTSMessage;
import com.daniel.ctsmessages.ClientKeepAlive;
import com.daniel.exceptions.ConvertionException;
import com.daniel.exceptions.NoActiveSocetException;
import com.daniel.exceptions.SocetStillOpenedException;
import com.daniel.stcmessages.STCMessage;

public class ReusableSocet extends Thread {

    private Client client;
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    private Map<String, Runnable> reactions = new HashMap<>();
    private ArrayList<Object> serverMessageData;

    private String protocol;

    public ReusableSocet(Client client, String protocol) {
        setName("Socket");
        this.protocol = protocol;
        this.client = client;
        initReactions();
    }

    private void initReactions() {
        reactions.put("LoginStatus", new Runnable() {
            @Override
            public void run() {
                client.setRegistrationStatus(true);
            }
        });
        reactions.put("filledList", new Runnable() {
            @Override
            public void run() {
                client.showParticipantsTable(serverMessageData);     
            }  
        });
        reactions.put("chatHistory", new Runnable() {
            @Override
            public void run() {
                client.refreshChatView(serverMessageData);
            }
        });

        reactions.put("KeepAlive", new Runnable() {
            @Override
            public void run() {
                try {
                    sendMessage(new ClientKeepAlive());
                } catch (IOException | NoActiveSocetException e) {
                    e.printStackTrace();
                }
            }
        });
        reactions.put("error", new Runnable() {
            @Override
            public void run() {
                String error = (String) serverMessageData.get(0);
                if (error.equals("user whith this name already exists") || error.equals("userwhiththisnamealreadyexists")) {
                    socket = null;
                    client.setRegistrationStatus(false);
                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                else client.processError((String) serverMessageData.get(0));
            }
        });
    }

    public synchronized void initNewConnection(String host, int port) throws UnknownHostException, IOException, SocetStillOpenedException {
        if (socket!= null) {
            throw new SocetStillOpenedException("There is already a connection");
        }
        socket = new Socket(host, port);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        notify();
    }

    public void closeConnection() throws IOException, NoActiveSocetException {
        if (socket == null) {
            throw new NoActiveSocetException("There is no active connection");
        }
        socket.close();
        objectOutputStream.close();
        objectInputStream.close();
        objectInputStream = null;
        objectOutputStream = null;
        socket = null;
    }

    public boolean isConnected() {
        if (socket == null) return false;
        else return true;
    }

    public void sendMessage(CTSMessage message) throws IOException, NoActiveSocetException {
        if (socket == null) {
            throw new NoActiveSocetException("There is no active connection");
        }
        if (protocol.equals("Basic")) objectOutputStream.writeObject(message);
        if (protocol.equals("XML")) {
            ConverterFactory converterFactory = new ClientMessageConvFactory();
            String StrMessage = null;
            try {
                StrMessage = converterFactory.convertToSerializableXML(message.getName(), message.getData());
            } catch (ConvertionException e) {
                client.processError(e.getMessage());
            }
            objectOutputStream.writeObject(StrMessage);
        }
    }

    private STCMessage getMessage() throws ClassNotFoundException, IOException {
        STCMessage serverMessage = null;
        if (protocol.equals("Basic")) {
            serverMessage = (STCMessage) objectInputStream.readObject();
        }
        if (protocol.equals("XML")) {
            String xmlMessage = (String) objectInputStream.readObject();
            ConverterFactory converterFactory = new ServerMessageConvFactory();
            try {
                serverMessage = converterFactory.convertFromSerializableXMLtoSM(xmlMessage);
            } catch (ConvertionException e) {
                client.processError(e.getMessage());
            }
        }
        return serverMessage;
    }

    @Override
    public synchronized void run() {
        while (! this.isInterrupted()) {
            try {
                STCMessage serverMessage = getMessage();
                serverMessageData = serverMessage.getData();
                reactions.get(serverMessage.getName()).run();
            } catch (ClassNotFoundException | IOException e) {
                client.processError(e.getMessage());
            } catch (NullPointerException e) {
                try {
                    wait();
                } catch (InterruptedException err) {
                    this.interrupt();
                }
            }
        }
        System.out.println("Client socket is interrupted");
    }
}
