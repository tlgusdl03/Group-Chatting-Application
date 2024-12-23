package Client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPage extends JDialog{
    private JTextField idField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");

    public LoginPage() {
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        add(new JLabel("ID:         "));
        add(idField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);

        setVisible(true);
    }

    public void setLoginButtonActionListener(ActionListener actionListener){
        loginButton.addActionListener(actionListener);
    }

    public void setRegisterButtonActionListener(ActionListener actionListener) {
        registerButton.addActionListener(actionListener);
    }

    public String getID(){
        return idField.getText();
    }

    public String getPassword(){
        char[] passwordChars =  passwordField.getPassword();
        String password = new String(passwordChars);
        return password;
    }
}
