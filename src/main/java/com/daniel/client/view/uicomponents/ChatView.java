package com.daniel.client.view.uicomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.daniel.client.view.ClientGUI;

public class ChatView {

    private JPanel panel;
    private JTextArea textArea;
    private JTextArea info;
    private JButton textEnter;

    @SuppressWarnings("unused")
    private ClientGUI clientGUI;

    public ChatView(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.GRAY);
        panel.setVisible(false);

        JPanel textInput = new JPanel(new FlowLayout());

        JTextField input = new JTextField(50);
        input.setToolTipText("Type your message here");
        input.setFont(new Font("Dialog", Font.PLAIN, 14));
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (input.getText().length() > 0) {
                    clientGUI.sendMessage(input.getText());
                    input.setText("");
                }
            }
        });

        textEnter = new JButton("Send");
        textInput.add(input);
        textInput.add(textEnter);
        textEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (input.getText().length() > 0) {
                    clientGUI.sendMessage(input.getText());
                    input.setText("");
                }
            }
        });

        panel.add(textInput, BorderLayout.SOUTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setTabSize(10);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.GRAY);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 16));
        textArea.setForeground(Color.BLACK);
        textArea.setText("SOME TEXT");
        panel.add(textArea, BorderLayout.CENTER);

        info = new JTextArea();
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.setAlignmentY(Component.CENTER_ALIGNMENT);
        info.setEditable(false);
        info.setRows(1);
        info.setWrapStyleWord(true);
        info.setBackground(Color.WHITE);
        info.setFont(new Font("Dialog", Font.PLAIN, 20));
        info.setForeground(Color.BLACK);
        panel.add(info, BorderLayout.NORTH);
    }

    public JPanel getTable() {
        return this.panel;
    }

    public void setUserName(String userName) {
        info.setText("Your user name is : " + userName);
    }

    public void repaintChat(ArrayList<Object> newChatList) {
        textArea.setText("");
        for (Object o : newChatList) {
            String s = (String) o;
            String[] arr = s.split(",");
            for (int i = 0; i < arr.length; i++) {
                textArea.append(arr[i]);
                textArea.append("   ");
            }
            textArea.append("\n");
        }
    }
}
