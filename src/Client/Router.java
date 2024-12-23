package Client;

import Client.communication.CommunicationManager;
import Client.gui.ChatRoomPage;
import Client.gui.HomePage;
import Client.gui.LoginPage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Router extends JFrame {

    private CommunicationManager communicationManager = null;
    private LoginPage loginPage = null;
    private HomePage homePage = null;
    private ChatRoomPage chatRoomPage = null;

    public Router(){
        setTitle("Chatting Program");
        setSize(900, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        communicationManager = new CommunicationManager();
        communicationManager.setCommandResponseHandler(this::commandHandler);
        System.out.println("CommandHandler added!");

        loginPage = new LoginPage();
        homePage = new HomePage(communicationManager);

        loginPage.setModal(true);
        loginPage.setLoginButtonActionListener(new LoginActionListener());
        loginPage.setRegisterButtonActionListener(new RegisterActionListener());

    }

    private void commandHandler(String response){
        SwingUtilities.invokeLater(() -> {
            System.out.println("COMMAND received: " + response);
                if (response.startsWith("REGISTER_SUCCESS")) {
                    loginPage.dispose();
                    setContentPane(homePage);
                    setVisible(true);
                    revalidate();
                    repaint();
                }
                else if (response.startsWith("REGISTER_ERROR")) {
                    JOptionPane.showMessageDialog(null, "Invalid ID or password. Please try again.");
                }
                else if (response.startsWith("LOGIN_SUCCESS")) {
                    loginPage.dispose();
                    setContentPane(homePage);
                    setVisible(true);
                    revalidate();
                    repaint();
                }
                else if (response.startsWith("LOGIN_ERROR")) {
                    JOptionPane.showMessageDialog(null, "Invalid credentials. Please try again.");
                }
                else if (response.startsWith("CREATEROOM_SUCCESS")) {
                    JOptionPane.showMessageDialog(null, "Room created successfully!");
                    homePage.refreshChatRooms(response);
                }
                else if (response.startsWith("CREATEROOM_ERROR")) {
                    JOptionPane.showMessageDialog(null, "Please use another room name");
                }
                else if (response.startsWith("JOINROOM_SUCCESS")) {
                    String roomName = response.substring("JOINROOM_SUCCESS:".length());
                    chatRoomPage = new ChatRoomPage(communicationManager, roomName);
                    setContentPane(chatRoomPage);
                    setVisible(true);
                    revalidate();
                    repaint();
                }
                else if (response.startsWith("JOINROOM_ERROR")) {
                    JOptionPane.showMessageDialog(null, "Room is not available now");
                }
                else if (response.startsWith("LISTUSERS_SUCCESS")) {
                    homePage.refreshConnectedUsers(response);
                }
                else if (response.startsWith("LISTUSERS_ERROR")) {
                    JOptionPane.showMessageDialog(null, "No currently connected user");
                }
                else if (response.startsWith("LISTROOMS_SUCCESS")) {
                    homePage.refreshChatRooms(response);
                }
                else if (response.startsWith("LISTROOMS_ERROR")) {
                    JOptionPane.showMessageDialog(null, "No chat rooms available");
                }
                else {
                    System.out.println("UNKNOWN COMMAND RECEIVED : " + response);
                }
            }
        );
    }
    class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = loginPage.getID();
            String password = loginPage.getPassword();
            communicationManager.sendMessage("/login " + id + " " + password);
        }
    }

    class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loginPage.dispose();
            String username = JOptionPane.showInputDialog("Enter username:");
            String password = JOptionPane.showInputDialog("Enter password:");
            communicationManager.sendMessage("/register " + username + " " + password);
        }
    }

    public static void main(String[] args) {
        new Router();
    }
}
