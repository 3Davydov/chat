package com.daniel.server.connectmanager;

import com.daniel.stcmessages.STCMessage;

public interface STCPassingInterface {
    public void sendMessage(Integer sessionID, STCMessage message);
}
