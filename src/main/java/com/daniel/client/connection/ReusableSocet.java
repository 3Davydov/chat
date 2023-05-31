package com.daniel.client.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.daniel.XMLConverter.ConverterFactory;
import com.daniel.XMLConverter.ClientToXML.ClientMessageConvFactory;
import com.daniel.XMLConverter.ServerToXML.ServerMessageConvFactory;
import com.daniel.client.Client;
import com.daniel.ctsmessages.CTSMessage;
import com.daniel.ctsmessages.ClientKeepAlive;
import com.daniel.exceptions.ConvertionException;
import com.daniel.exceptions.NoActiveSocetException;
import com.daniel.exceptions.SocetStillOpenedException;
import com.daniel.properties.PropertiesReader;
import com.daniel.stcmessages.STCMessage;

public class ReusableSocet extends Thread {

    private Client client;
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    private Map<String, Runnable> reactions = new HashMap<>();
    private ArrayList<Object> serverMessageData;

    private String protocol;
    private ScheduledExecutorService executorService = null;

    private boolean suspicionOnZombie = false;
    private ArrayList<STCMessage> archive = new ArrayList<>();

    private int pingCount = 0;
    private int timeout = 0;

    public ReusableSocet(Client client, String protocol) {
        setName("Socket");
        this.protocol = protocol;
        this.client = client;
        PropertiesReader propertiesReader = new PropertiesReader();
        propertiesReader.getAllProperties("/clientConfig.properties");
        this.pingCount = propertiesReader.getPingCount();
        this.timeout = propertiesReader.getTimeout();
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

        reactions.put("ServerKeepAlive", new Runnable() {
            @Override
            public void run() {
                if (archive.size() > 0) {
                    for (int i = 0; i < archive.size(); i++) {
                        STCMessage s = archive.remove(i);
                        serverMessageData = s.getData();
                        reactions.get(s.getName()).run();
                    }
                }
            }
        });
        reactions.put("error", new Runnable() {
            @Override
            public void run() {
                String error = (String) serverMessageData.get(0);
                if (error.equals("user whith this name already exists")) {
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
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        
        socket.setSoTimeout(timeout);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                sendMessage(new ClientKeepAlive());
            } catch (IOException | NoActiveSocetException e) {
                e.printStackTrace();
            }
        }, 0, timeout / 2, TimeUnit.MILLISECONDS);
        notify();
    }

    public void closeConnection() throws IOException, NoActiveSocetException {
        if (socket == null) {
            throw new NoActiveSocetException("There is no active connection");
        }
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        socket.close();
        if (objectOutputStream != null) objectOutputStream.close();
        if (objectInputStream != null) objectInputStream.close();
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
        try {
            if (protocol.equals("Basic")) {
                synchronized (objectOutputStream) {
                    objectOutputStream.writeObject(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (protocol.equals("XML")) {
            ConverterFactory converterFactory = new ClientMessageConvFactory();
            String StrMessage = null;
            try {
                StrMessage = converterFactory.convertToSerializableXML(message.getName(), message.getData());
            } catch (ConvertionException e) {
                e.printStackTrace();
                client.processError(e.getMessage());
            }
            objectOutputStream.writeObject(StrMessage);
        }
    }

    private STCMessage getMessage() throws ClassNotFoundException, IOException, NoActiveSocetException {
        STCMessage serverMessage = null;
        if (protocol.equals("Basic")) {
            try {
                serverMessage = (STCMessage) objectInputStream.readObject();
            } catch (SocketTimeoutException e) {
                suspicionOnZombie = true;
                client.processError("Connection problem");
                for (int i = 0; i < pingCount; i++) {
                    while (true) {
                        try {
                            sendMessage(new ClientKeepAlive());
                            serverMessage = (STCMessage) objectInputStream.readObject();
                        } catch (SocketTimeoutException err) {
                            if (i == pingCount - 1) {
                                client.processError("Connection closed");
                                client.disconnect();
                            }
                            else break;
                        } 
                        catch (ClassNotFoundException | IOException err) {
                            err.printStackTrace();
                        }
                        if (serverMessage.getName().equals("ServerKeepAlive")) {
                            suspicionOnZombie = false;
                            break;
                        }
                        else {
                            archive.add(serverMessage);
                        }
                    }
                    if (suspicionOnZombie == false) {
                        client.processError("reconnected");
                        break;
                    }
                }
                serverMessageData = serverMessage.getData();
                reactions.get(serverMessage.getName()).run();
            }
        }
        String xmlMessage = null;
        if (protocol.equals("XML")) {
            try {
                xmlMessage = (String) objectInputStream.readObject();
                ConverterFactory converterFactory = new ServerMessageConvFactory();
                try {
                    serverMessage = converterFactory.convertFromSerializableXMLtoSM(xmlMessage);
                } catch (ConvertionException err) {
                    client.processError(err.getMessage());
                }
            } catch (SocketTimeoutException e) {
                suspicionOnZombie = true;
                client.processError("Connection problem");
                for (int i = 0; i < pingCount; i++) {
                    while (true) {
                        try {
                            sendMessage(new ClientKeepAlive());
                            xmlMessage = (String) objectInputStream.readObject();
                            ConverterFactory converterFactory = new ServerMessageConvFactory();
                            try {
                                serverMessage = converterFactory.convertFromSerializableXMLtoSM(xmlMessage);
                            } catch (ConvertionException err) {
                                client.processError(err.getMessage());
                            }
                        } catch (SocketTimeoutException err) {
                            if (i == pingCount - 1) {
                                client.processError("Connection closed");
                                client.disconnect();
                            }
                            else break;
                        } 
                        catch (ClassNotFoundException | IOException err) {
                            err.printStackTrace();
                        }
                        if (serverMessage.getName().equals("ServerKeepAlive")) {
                            suspicionOnZombie = false;
                            break;
                        }
                        else {
                            archive.add(serverMessage);
                        }
                    }
                    if (suspicionOnZombie == false) {
                        client.processError("reconnected");
                        break;
                    }
                }
                serverMessageData = serverMessage.getData();
                reactions.get(serverMessage.getName()).run();
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
            } catch (NoActiveSocetException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client socket is interrupted");
    }
}
