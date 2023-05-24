package com.daniel.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.daniel.client.connection.ReusableSocet;
import com.daniel.client.view.ClientGUI;
import com.daniel.ctsmessages.ListMessage;
import com.daniel.ctsmessages.LoginMessage;
import com.daniel.ctsmessages.LogoutMessage;
import com.daniel.ctsmessages.TextMessage;
import com.daniel.exceptions.ConnectionError;
import com.daniel.exceptions.NoActiveSocetException;
import com.daniel.exceptions.SocetStillOpenedException;
import com.daniel.properties.PropertiesReader;

public class Client {

    private LoginMessage userInfo = null;
    private String host = null;
    private int port = -1;
    private boolean isConnected = false;

    private final ReusableSocet socet;

    private ClientGUI clientGUI;
    private String protocol;

    public Client() {
        Thread.currentThread().setName("Client");
        PropertiesReader propertiesReader = new PropertiesReader();
        propertiesReader.getAllProperties("/clientConfig.properties");
        protocol = propertiesReader.getProtocol();
        this.socet = new ReusableSocet(this, protocol);
        this.socet.start();
        clientGUI = new ClientGUI(this);
    }

    public synchronized void connect() throws ConnectionError {
        try {
            if (! socet.isConnected()) {
                socet.initNewConnection(host, port);
                socet.sendMessage(userInfo);
            }
        }
        catch (SocetStillOpenedException e) {
            throw new ConnectionError("Client has active connection");
        } catch (IOException e) {
            throw new ConnectionError("Unknown host or port");
        } catch (NoActiveSocetException e) {}

        try {
            wait();
        } catch (InterruptedException e) {
            closeClient();
            Thread.currentThread().interrupt();
            return;
        }
        if (isConnected) openChat();
        else processError("user whith this name already exists");
    }

    public synchronized void setRegistrationStatus(Boolean status) {
        isConnected = status;
        notify();
    }

    public void processError(String err) {
        if (err.equals("Connection reset")) {
            System.out.println("reset");
            try {
                clientGUI.closeChat();
                socet.closeConnection();
            } catch (IOException | NoActiveSocetException e) {
                processError(err);
            }
        }
        clientGUI.displayError(err);
    }

    private void openChat() {
        clientGUI.openChat();
    }

    public void disconnect() throws IOException, NoActiveSocetException {
        if (socet.isConnected()) {
            clientGUI.closeChat();
            socet.sendMessage(new LogoutMessage());
            socet.closeConnection();
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUserInfo(LoginMessage userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserName() {
        return (String) userInfo.getData().get(0);
    }

    public void closeClient() {
        if (socet.isConnected())
            try {
                socet.closeConnection();
            } catch (IOException | NoActiveSocetException e) {
                clientGUI.displayError(e.getMessage());
            }
        socet.interrupt();
    }

    public void getParticipantsTable() throws IOException, NoActiveSocetException {
        socet.sendMessage(new ListMessage());
    }

    public void showParticipantsTable(ArrayList<Object> participants) {
        clientGUI.showParticipants(participants);
    }

    public void refreshChatView(ArrayList<Object> messages) {
        clientGUI.displayMessages(messages);
    }

    public void sendChatMessageToServer(String message) {
        try {
            socet.sendMessage(new TextMessage(message));
        } catch (IOException | NoActiveSocetException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws UnknownHostException, IOException, ConnectionError {
        @SuppressWarnings("unused")
        Client client = new Client();
    }
}
