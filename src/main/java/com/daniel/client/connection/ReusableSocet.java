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
import com.daniel.stcmessages.STCMessage;

public class ReusableSocet extends Thread {

    private Client client;
    private Socket socket = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    private Map<String, Runnable> reactions = new HashMap<>();
    private ArrayList<Object> serverMessageData;

    private String protocol;
    private ScheduledExecutorService executorService;

    private boolean suspicionOnZombie = false;

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
                // try {
                //     sendMessage(new ClientKeepAlive());
                // } catch (IOException | NoActiveSocetException e) {
                //     e.printStackTrace();
                // }
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
        
        socket.setSoTimeout(5000); // TODO add to properties
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                sendMessage(new ClientKeepAlive());
            } catch (IOException | NoActiveSocetException e) {
                e.printStackTrace();
            }
        }, 0, 2500, TimeUnit.MILLISECONDS); // TODO add to properties
        notify();
    }

    public void closeConnection() throws IOException, NoActiveSocetException {
        if (socket == null) {
            throw new NoActiveSocetException("There is no active connection");
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS); // ќжидание завершени€ выполнени€ задач
        } catch (InterruptedException e) {
            e.printStackTrace();
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
                for (int i = 0; i < 4; i++) {
                    while (true) {
                        try {
                            sendMessage(new ClientKeepAlive());
                            serverMessage = (STCMessage) objectInputStream.readObject();
                        } catch (SocketTimeoutException err) {
                            if (i == 3) closeConnection();
                            else break;
                        } 
                        catch (ClassNotFoundException | IOException err) {
                            err.printStackTrace();
                        }
                        if (serverMessage.getName().equals("KeepAlive")) {
                            suspicionOnZombie = false;
                            break;
                        }
                        else {
                            // clientArchive.put(Long.valueOf(System.currentTimeMillis()), message);
                        }
                    }
                    if (suspicionOnZombie == false) break;
                }
                reactions.get(serverMessage.getName()).run();
            }
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
                // if (serverMessage == null) continue; // TODO improve
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
