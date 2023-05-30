package com.daniel.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

import com.daniel.exceptions.DuplicateNameException;
import com.daniel.exceptions.IllegalRequestException;
import com.daniel.properties.PropertiesReader;
import com.daniel.server.chathistory.ChatHistory;
import com.daniel.server.chathistory.FileData;
import com.daniel.server.connectmanager.ConnectionsManager;
import com.daniel.server.participants.ParticipantsList;
import com.daniel.stcmessages.ErrorMessage;
import com.daniel.stcmessages.FilledListMessage;
import com.daniel.stcmessages.LoginStatus;

public class ServerMain {

    private ServerSocket serverSocket;
    private ConnectionsManager connectionsManager;
    private ParticipantsList participantsList;
    private ChatHistory chatHistory;

    private HashMap<Integer, Integer> offsets;
    private Integer currentChatPointer = 0;

    private int avaliableRecentMessagesCount = 0;
    private String protocol;

    private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ServerMain.class.getName());

    public ServerMain() throws IOException {
        participantsList = new ParticipantsList();
        PropertiesReader propertiesReader = new PropertiesReader();
        propertiesReader.getAllProperties("/serverConfig.properties");
        protocol = propertiesReader.getProtocol();
        serverSocket = new ServerSocket(propertiesReader.getPort());
        avaliableRecentMessagesCount = propertiesReader.getRecentMessagesCount();
        if (propertiesReader.getLogFlag() == true) {
            LogManager.getLogManager().reset();
            java.util.logging.Logger globalLogger = java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
            globalLogger.setLevel(Level.FINEST);

            FileHandler fh = new FileHandler("Server.log", 6000, 1, false);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.FINEST);
        }
        logger.info("Server have been started");
        connectionsManager = new ConnectionsManager(serverSocket, this, protocol);
        connectionsManager.start();
        chatHistory = new ChatHistory("serverHistory.csv");
        offsets = new HashMap<>();
    }

    public void registrateUser(Integer sessionID, String username) throws IOException, IllegalRequestException {
        try {
            username = username.replace("\n", "");
            username = username.replace(" ", "");
            participantsList.addNewParcipiant(username, sessionID);
            offsets.put(sessionID, Integer.valueOf(currentChatPointer.intValue()));
        } catch (DuplicateNameException e) {
            connectionsManager.sendMessage(sessionID, new ErrorMessage("user whith this name already exists"));
            return;
        }
        logger.info("New user whith name " + username + " and ID " + String.valueOf(sessionID) + " had connected to server");
        LoginStatus reply = new LoginStatus("succes", true);
        connectionsManager.sendMessage(sessionID, reply);
        currentChatPointer++;
        chatHistory.addSystemMessage("New user whith name " + username + " and ID " + String.valueOf(sessionID) + " had connected to server");
        refreshChat();
    }

    public void deleteUser(Integer sessionID) throws IllegalRequestException {
        try {
            logger.info("New user whith name " + participantsList.getNameByID(sessionID) + " and ID " + String.valueOf(sessionID) + " had disconnected from server");
            currentChatPointer++;
            chatHistory.addSystemMessage("User whith name " + participantsList.getNameByID(sessionID) + " and ID " + String.valueOf(sessionID) + " had disconnected from server");
            participantsList.removeParticipant(sessionID);
            offsets.remove(sessionID);
            refreshChat();
        } catch (DuplicateNameException e) {
            throw new IllegalRequestException(Integer.toString(sessionID));
        }
    }

    public void sendParticipantsTable(Integer sessionID) throws IllegalRequestException {
        try {
            logger.info("Send participants list to " + participantsList.getNameByID(sessionID));
        } catch (DuplicateNameException e) {
            e.printStackTrace();
        }
        FilledListMessage message = new FilledListMessage(participantsList.getPrintableParticipantsNames());
        connectionsManager.sendMessage(sessionID, message);
    }

    public void addMessageToChatHistory(String message, Integer sessionID) {
        message = message.replace("\n", "");
        // message = message.replace(" ", "");
        try {
            logger.info("User " + participantsList.getNameByID(sessionID) + " sent message");
            currentChatPointer++;
            chatHistory.addMessageFromUser(participantsList.getNameByID(sessionID), message);
            refreshChat();
        } catch (DuplicateNameException e) {
            e.printStackTrace();
        }
    }

    private void refreshChat() {
        FileData fileData = chatHistory.getHistory();
        if (fileData != null) connectionsManager.broadcastMessage(offsets, fileData, avaliableRecentMessagesCount, chatHistory.getStartLen());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unused")
        ServerMain serverMain = new ServerMain();
    }
}