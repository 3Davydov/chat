package com.daniel.server.connectmanager;

public interface CTSPassingInterface {
    public void connectUser(String username, Integer sessionID);
    public void disconnectUser(Integer sessionID);
    public void requestForParticipantsList(Integer sessionID);
    public void chatMessageNotification(String message, Integer sessionID);
}
