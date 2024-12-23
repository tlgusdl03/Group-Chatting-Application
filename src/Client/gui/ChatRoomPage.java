package Client.gui;

import Client.communication.CommunicationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatRoomPage extends JPanel{
    private CommunicationManager communicationManager;
    private String roomName;

    private DefaultListModel<String> participantModel;
    private JList<String> participantList;

    private JTextArea chatArea;
    private JTextField messageInput;
    private JButton sendButton;

    private boolean running;

    public ChatRoomPage(CommunicationManager communicationManager, String roomName) {
        this.communicationManager = communicationManager;
        this.communicationManager.setCommandResponseHandler(this::commandHandler);
        this.communicationManager.setChatMessageHandler(this::chatHandler);
        this.roomName = roomName;
        this.running = true;

        setLayout(new BorderLayout());

        JPanel participantPanel = new JPanel(new BorderLayout());
        participantPanel.setBorder(BorderFactory.createTitledBorder("Participants"));

        participantModel = new DefaultListModel<>();
        participantList = new JList<>(participantModel);
        participantList.setFixedCellHeight(50);

        JButton refreshParticipantsButton = new JButton("Refresh");
        refreshParticipantsButton.addActionListener(e -> refreshParticipants());

        participantPanel.add(refreshParticipantsButton, BorderLayout.NORTH);
        participantPanel.add(new JScrollPane(participantList), BorderLayout.CENTER);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        messageInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageInput, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);

        add(participantPanel, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);

        refreshParticipants();
    }

    private void refreshParticipants() {
        communicationManager.sendMessage("/listParticipants " + roomName);
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageInput.getText().trim();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Cannot send an empty message.");
                return;
            }
            communicationManager.sendMessage("/broadcast" + message + '\n');
            messageInput.setText("");
        }
    }

    private void commandHandler(String response){
        if (response.startsWith("LISTPARTICIPANTS_SUCCESS:")) {
            participantModel.clear();
            String[] users = response.substring("LISTPARTICIPANTS_SUCCESS:".length()).split(";");
            for (String user : users) {
                participantModel.addElement(user.trim());
            }
        }
    }
    private void chatHandler(String message){
        chatArea.append(message + '\n');
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
