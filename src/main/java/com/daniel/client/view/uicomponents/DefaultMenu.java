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
import com.daniel.client.view.ClientGUI;
import com.daniel.exceptions.NoActiveSocetException;

public class DefaultMenu implements ActionListener {

    private Button backButton;
    private Button refreshButton;
    
    private ClientGUI clientGUI;
    private Client client;

    private Box sideBar;

    public DefaultMenu(){
        sideBar = Box.createVerticalBox();

        Color defaultColor = Color.black;
        Font defaultFont = new Font("Arial", Font.BOLD, 20);
        Dimension minSize = new Dimension(300, 100);
        Dimension prefSize = new Dimension(300, 100);
        Dimension maxSize = new Dimension(300, 100);

        backButton = new Button("Return", defaultColor, prefSize, minSize, maxSize, defaultFont, this);
        refreshButton = new Button("Refresh", defaultColor, prefSize, minSize, maxSize, defaultFont, this);

        sideBar.add(refreshButton.getButton());

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dm = toolkit.getScreenSize();
        int fillerHeight = (dm.height - 400 - 25) / 3;
        Dimension fillerDim = new Dimension(5, fillerHeight);
        Box.Filler filler_1 = new Box.Filler(fillerDim, fillerDim, fillerDim);

        sideBar.add(filler_1);
        sideBar.add(backButton.getButton());

        sideBar.setVisible(false);

    }
    
    public void showMenu(){
        sideBar.setVisible(true);
    }

    public void hideMenu(){
        sideBar.setVisible(false);
    }

    public Box getMenu(){
        return this.sideBar;
    }

    public void setGUI(ClientGUI gui) {
        this.clientGUI = gui;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton.getButton()){
            clientGUI.returnToPrevView();
        }
        if (e.getSource() == refreshButton.getButton()){
            try {
                client.getParticipantsTable();
            } catch (NoActiveSocetException | IOException e1) {
                client.processError(e1.getMessage());
                e1.printStackTrace();
            }
        }
    }
}