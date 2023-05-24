package com.daniel.client.view.uicomponents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;

import com.daniel.client.Client;
import com.daniel.exceptions.NoActiveSocetException;

public class ChatMenu implements ActionListener {
    
    private Button showParticipantsButton;
    private Button exitButton;
    
    private Client client = null;

    private Box sideBar;

    public ChatMenu() {
        sideBar = Box.createVerticalBox();

        Color defaultColor = Color.black;
        Font defaultFont = new Font("Arial", Font.BOLD, 20);
        Dimension minSize = new Dimension(300, 100);
        Dimension prefSize = new Dimension(300, 100);
        Dimension maxSize = new Dimension(300, 100);

        showParticipantsButton = new Button("All Participants", defaultColor, prefSize, minSize, maxSize, defaultFont, this);
        exitButton = new Button("Exit", defaultColor, prefSize, minSize, maxSize, defaultFont, this);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dm = toolkit.getScreenSize();
        int fillerHeight = (dm.height - 400 - 25) / 3;

        Dimension fillerDim = new Dimension(5, fillerHeight);
        Box.Filler filler_1 = new Box.Filler(fillerDim, fillerDim, fillerDim);

        sideBar.add(showParticipantsButton.getButton());
        sideBar.add(filler_1);
        sideBar.add(exitButton.getButton());

        sideBar.setVisible(false);

    }

    public void showMenu() {
        sideBar.setVisible(true);
    }

    public void hideMenu() {
        sideBar.setVisible(false);
    }

    public Box getMenu() {
        return this.sideBar;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showParticipantsButton.getButton()) {
            try {
                client.getParticipantsTable();
            } catch (NoActiveSocetException | IOException e1) {
                client.processError(e1.getMessage());
            }
        }

        if (e.getSource() == exitButton.getButton()) {
            try {
                client.disconnect();
            } catch (IOException e1) {
                client.processError(e1.getMessage());
            } catch (NoActiveSocetException e1) {
                client.processError(e1.getMessage());
            }
        }
    }
}
