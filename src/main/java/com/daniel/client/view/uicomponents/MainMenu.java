package com.daniel.client.view.uicomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.daniel.client.Client;
import com.daniel.ctsmessages.LoginMessage;
import com.daniel.exceptions.ConnectionError;

public class MainMenu implements ActionListener {

    private Button connectButton;
    private Button exitButton;
    
    private JFrame mainFrame = null;
    private Client client = null;

    private Box sideBar;

    public MainMenu(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        sideBar = Box.createVerticalBox();

        Color defaultColor = Color.black;
        Font defaultFont = new Font("Arial", Font.BOLD, 20);
        Dimension minSize = new Dimension(300, 100);
        Dimension prefSize = new Dimension(300, 100);
        Dimension maxSize = new Dimension(300, 100);

        connectButton = new Button("Connect", defaultColor, prefSize, minSize, maxSize, defaultFont, this);
        exitButton = new Button("Exit", defaultColor, prefSize, minSize, maxSize, defaultFont, this);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dm = toolkit.getScreenSize();
        int fillerHeight = (dm.height - 400 - 25) / 3;

        Dimension fillerDim = new Dimension(5, fillerHeight);
        Box.Filler filler_1 = new Box.Filler(fillerDim, fillerDim, fillerDim);

        sideBar.add(connectButton.getButton());
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
        if (e.getSource() == connectButton.getButton()) {
            if (this.client != null)
                try {
                    LoginDialog dialog = new LoginDialog(mainFrame);
                    dialog.setVisible(true);
                    if (dialog.isSucceeded()) {
                        client.connect();
                    }
                } catch (ConnectionError e1) {
                    JOptionPane.showMessageDialog(mainFrame, e1.getMessage());
                }
        }

        if (e.getSource() == exitButton.getButton()) {
            mainFrame.dispose();
            client.closeClient();
        }
    }

    private class LoginDialog extends JDialog {

        private JTextField usernameField;
        private JTextField hostField;
        private JTextField portField;
        private JLabel lbUsername;
        private JLabel lbHost;
        private JLabel lbPort;
        private JButton btnLogin;
        private JButton btnExit;
        private boolean succeeded;
    
        public LoginDialog(JFrame parent) {
            super(parent, "Login", true);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints cs = new GridBagConstraints();
    
            cs.fill = GridBagConstraints.HORIZONTAL;
    
            lbHost = new JLabel("Set host: ");
            cs.gridx = 0;
            cs.gridy = 0;
            cs.gridwidth = 1;
            panel.add(lbHost, cs);
    
            hostField = new JTextField(20);
            cs.gridx = 1;
            cs.gridy = 0;
            cs.gridwidth = 2;
            panel.add(hostField, cs);
    
            lbPort = new JLabel("Set port: ");
            cs.gridx = 0;
            cs.gridy = 1;
            cs.gridwidth = 1;
            panel.add(lbPort, cs);
    
            portField = new JTextField(20);
            cs.gridx = 1;
            cs.gridy = 1;
            cs.gridwidth = 2;
            panel.add(portField, cs);

            lbUsername = new JLabel("Set username: ");
            cs.gridx = 0;
            cs.gridy = 2;
            cs.gridwidth = 1;
            panel.add(lbUsername, cs);
    
            usernameField = new JTextField(20);
            cs.gridx = 1;
            cs.gridy = 2;
            cs.gridwidth = 2;
            panel.add(usernameField, cs);

            panel.setBorder(new LineBorder(Color.GRAY));
    
            btnLogin = new JButton("Confirm");
            cs.gridx = 0;
            cs.gridy = 3;
            cs.gridwidth = 3;
            panel.add(btnLogin, cs);

            btnExit = new JButton("Exit");
            cs.gridx = 0;
            cs.gridy = 4;
            cs.gridwidth = 3;
            panel.add(btnExit, cs);

            getContentPane().add(panel, BorderLayout.CENTER);
    
            btnLogin.addActionListener(new ActionListener() {
    
                public void actionPerformed(ActionEvent e) {
                    if (getHost().length() == 0 || getPort().length() == 0 || getUsername().length() == 0) {
                        succeeded = false;
                        return;
                    }
                    client.setHost(getHost());
                    client.setPort(Integer.valueOf(getPort()));
                    client.setUserInfo(new LoginMessage(getUsername()));
                    succeeded = true;
                    dispose();
                }
            });

            btnExit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    succeeded = false;
                    dispose();
                }
            });

            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
        }
    
        private String getUsername() {
            return usernameField.getText();
        }
    
        private String getHost() {
            return hostField.getText();
        }

        private String getPort() {
            return portField.getText();
        }
    
        public boolean isSucceeded() {
            return succeeded;
        }
    } 
}
