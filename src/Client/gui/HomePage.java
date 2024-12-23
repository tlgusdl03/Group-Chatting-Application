package Client.gui;

import Client.communication.CommunicationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HomePage extends JPanel {
    private CommunicationManager communicationManager = null;
    private DefaultListModel<String> currentConnectedUserModel =null;
    private JList<String> currentedConnectedUserList =null;
    private DefaultListModel<String> chatRoomModel=null;
    private JList<String> chatRoomJList=null;
    public HomePage(CommunicationManager communicationManager){
        this.communicationManager = communicationManager;
        setLayout(new BorderLayout());

        //Left : User list and search
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("Connected Users"));

        currentConnectedUserModel = new DefaultListModel<>();
        currentedConnectedUserList = new JList<>(currentConnectedUserModel);
        currentedConnectedUserList.setFixedCellHeight(50);

        JButton loadUserBtn = new JButton("Refresh Users");
        loadUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicationManager.sendMessage("/listUsers");
            }
        });

        userPanel.add(loadUserBtn, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(currentedConnectedUserList), BorderLayout.CENTER);

        //Right : chatRoom List and Create
        JPanel chatRoomPanel = new JPanel(new BorderLayout());
        chatRoomPanel.setBorder(BorderFactory.createTitledBorder("Chat Room List"));

        chatRoomModel = new DefaultListModel<>();
        chatRoomJList = new JList<>(chatRoomModel);
        chatRoomJList.setFixedCellHeight(50);
        chatRoomJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    System.out.println("Room selection");
                    String selectedRoom = getSelectedChatRoom();
                    if (selectedRoom != null) {
                        communicationManager.sendMessage("/joinRoom " + selectedRoom);
                    }
                }
            }
        });

        JButton loadRoomsBtn = new JButton("Load Rooms");
        loadRoomsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicationManager.sendMessage("/listRooms");
            }
        });

        JPanel roomCreatePanel = new JPanel();
        JTextField roomCreateInput = new JTextField(15);
        JButton roomCreateButton = new JButton("Create Room");
        roomCreateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomCreateInput.getText();
                if(roomName.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please enter a room name");
                    return;
                }
                communicationManager.sendMessage("/createRoom " + roomName);
            }
        });

        roomCreatePanel.add(roomCreateInput);
        roomCreatePanel.add(roomCreateButton);

        chatRoomPanel.add(loadRoomsBtn, BorderLayout.NORTH);
        chatRoomPanel.add(new JScrollPane(chatRoomJList), BorderLayout.CENTER);
        chatRoomPanel.add(roomCreatePanel, BorderLayout.SOUTH);

        add(userPanel, BorderLayout.WEST);
        add(chatRoomPanel, BorderLayout.CENTER);

        setSize(500, 800);
        setVisible(true);
    }

    public void refreshConnectedUsers(String response) {
        currentConnectedUserModel.clear();
        String[] users = response.substring("LISTUSERS_SUCCESS:".length()).split(";");
        if (users.length == 0) {
            JOptionPane.showMessageDialog(null, "No users are currently connected.");
        } else {
            for (String user : users) {
                currentConnectedUserModel.addElement(user);
            }
        }
    }

    public void refreshChatRooms(String response) {
        chatRoomModel.clear();
        String[] rooms = response.substring("LISTROOMS_SUCCESS:".length()).split(";");
        if (rooms.length == 0) {
            JOptionPane.showMessageDialog(null, "No chat rooms available.");
        } else {
            for (String room : rooms) {
                chatRoomModel.addElement(room);
            }
        }
    }

    public String getSelectedChatRoom(){
        return chatRoomJList.getSelectedValue();
    }
}
