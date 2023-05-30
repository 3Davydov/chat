package com.daniel.client.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import com.daniel.client.Client;
import com.daniel.client.view.uicomponents.ChatMenu;
import com.daniel.client.view.uicomponents.ChatView;
import com.daniel.client.view.uicomponents.DefaultMenu;
import com.daniel.client.view.uicomponents.MainMenu;
import com.daniel.client.view.uicomponents.ParticipantsView;

import java.awt.*;
import java.util.ArrayList;

public class ClientGUI {

    private JFrame mainFrame;
    private JPanel mainWindow;

    private JPanel actionWindow;
    private JPanel choiseWindow;

    private MainMenu mainMenu;
    private ChatMenu chatMenu;

    private ChatView chatView;
    private DefaultMenu defaultMenu;
    private ParticipantsView participantsView;

    private Client client = null;

    public ClientGUI(Client client) {

        this.client = client;
        mainFrame = getJFrame();
        mainWindow = new JPanel();
        mainFrame.add(mainWindow);
        mainWindow.setLayout(new BorderLayout());

        actionWindow = new JPanel();
        choiseWindow = new JPanel();
        
        mainMenu = new MainMenu(mainFrame);
        mainMenu.setClient(client);
        mainMenu.showMenu();

        chatMenu = new ChatMenu();
        chatMenu.setClient(client);

        actionWindow.setBackground(Color.GRAY);
        actionWindow.setLayout(new BorderLayout());
        
        choiseWindow.setBackground(Color.YELLOW);
        choiseWindow.setLayout(new BorderLayout());
        choiseWindow.add(mainMenu.getMenu(), BorderLayout.CENTER);

        mainWindow.add(actionWindow, BorderLayout.CENTER);
        mainWindow.add(choiseWindow, BorderLayout.WEST);

        chatView = new ChatView(this);
        defaultMenu = new DefaultMenu();
        defaultMenu.setGUI(this);
        defaultMenu.setClient(client);
        participantsView = new ParticipantsView();

        mainWindow.revalidate();
        mainFrame.revalidate();
        actionWindow.revalidate();
        choiseWindow.revalidate();
    }
    
    private JFrame getJFrame() {
        JFrame jframe = new JFrame() {};
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dm = toolkit.getScreenSize();

        jframe.setLocation(0, 0);
        jframe.setSize(dm.width, dm.height);
        jframe.setTitle("CHAT");
        return  jframe;
    }

    public void openChat() {
        choiseWindow.remove(mainMenu.getMenu());
        choiseWindow.add(chatMenu.getMenu());
        chatMenu.showMenu();

        actionWindow.removeAll();
        actionWindow.add(chatView.getTable());
        chatView.setUserName(client.getUserName());
        chatView.getTable().setVisible(true);

        JScrollPane scrollPane = new JScrollPane(chatView.getTable(),   ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(actionWindow.getWidth(), actionWindow.getHeight()));
        actionWindow.add(scrollPane, BorderLayout.EAST);
        
        actionWindow.revalidate();
        actionWindow.repaint();
    }

    public void closeChat(){
        if (this.chatMenu.getMenu().isVisible()){
            chatMenu.hideMenu();
        }
        choiseWindow.removeAll();
        choiseWindow.add(mainMenu.getMenu());
        choiseWindow.revalidate();
        choiseWindow.repaint();

        actionWindow.removeAll();
        actionWindow.setBackground(Color.GRAY);
        actionWindow.revalidate();
        actionWindow.repaint();
        
        mainWindow.revalidate();
        mainWindow.repaint();

    }

    public void showParticipants(ArrayList<Object> newData) {
        choiseWindow.remove(chatMenu.getMenu());
        choiseWindow.add(defaultMenu.getMenu());
        defaultMenu.getMenu().setVisible(true);
        choiseWindow.revalidate();
        choiseWindow.repaint();

        actionWindow.removeAll();
        participantsView.getTable().setSize(actionWindow.getWidth(), actionWindow.getHeight());
        actionWindow.add(participantsView.getTable(), BorderLayout.CENTER);
        participantsView.printTable(newData);
        actionWindow.revalidate();
        actionWindow.repaint();
        
        mainWindow.revalidate();
        mainWindow.repaint();
    }

    public void returnToPrevView(){

        choiseWindow.removeAll();
        choiseWindow.add(chatMenu.getMenu());
        choiseWindow.revalidate();
        choiseWindow.repaint();

        actionWindow.removeAll();
        actionWindow.add(chatView.getTable());
        actionWindow.revalidate();
        actionWindow.repaint();
        
        mainWindow.revalidate();
        mainWindow.repaint();

    }

    public void sendMessage(String message) {
        client.sendChatMessageToServer(message);
    }

    public void displayMessages(ArrayList<Object> messages) {
        chatView.repaintChat(messages);
    }

    private JOptionPane sideMessage;

    public void displayError(String err) {
        if (err.equals("lost connection")) {
            sideMessage = new JOptionPane("Trying to reconnect", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
            sideMessage.setVisible(true);
            mainFrame.add(sideMessage);
            Timer timer = new Timer(3000, e -> {
                sideMessage.setVisible(false);
                mainFrame.revalidate();
                mainFrame.repaint();
            });
            timer.setRepeats(false); // Однократное срабатывание таймера
            timer.start();
        }
        if (err.equals("reconnected")) sideMessage.setVisible(false);
        else JOptionPane.showMessageDialog(mainFrame, err);
    }
}
