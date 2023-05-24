package com.daniel.server.participants;

import java.util.ArrayList;
import java.util.HashMap;

import com.daniel.exceptions.DuplicateNameException;

public class ParticipantsList {

    private HashMap<Integer, String> participants;

    public ParticipantsList() {
        participants = new HashMap<>();
    }

    public void addNewParcipiant(String participant, Integer participantID) throws DuplicateNameException {
        if ((participants.size() == 0 || ! usernameIsRelevant(participant)) && 
            (participants.size() == 0 || ! idIsRelevant(participantID))) {
            participants.put(participantID, participant);
        }
        else throw new DuplicateNameException("User whith this name is already connected");
    }

    public void removeParticipant(Integer participantID) throws DuplicateNameException {
        if (idIsRelevant(participantID)) {
            participants.remove(participantID);
        }
        else throw new DuplicateNameException("User whith this name doesn't exist");
    }

    public ArrayList<String> getPrintableParticipantsNames() {
        ArrayList<String> printableTable = new ArrayList<String>();
        for (String p : participants.values()) {
            printableTable.add(p);
        }
        return printableTable;
    }

    public String getNameByID(Integer id) throws DuplicateNameException {
        if (! idIsRelevant(id)) throw new DuplicateNameException("User whith this id doesn't exist");
        return participants.get(id);
    }

    private boolean idIsRelevant(Integer id) {
        return participants.containsKey(id);
    }

    private boolean usernameIsRelevant(String username) {
        for (String p : participants.values()) {
            if (p.equals(username)) return true;
        }
        return false;
    }
}
