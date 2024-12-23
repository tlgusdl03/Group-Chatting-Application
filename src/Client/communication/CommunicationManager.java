package Client.communication;


import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class CommunicationManager implements Runnable{
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9999;

    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    private Consumer<String> chatMessageHandler;
    private Consumer<String> commandResponseHandler;
    private boolean running;

    public CommunicationManager(){
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            running = true;

            new Thread(this).start();
        } catch (IOException e) {
            handleError(e.getMessage());
        }
    }

    public void sendMessage(String msg) {
        try {
            out.write(msg + '\n');
            out.flush();
        } catch (IOException e) {
            try{
                socket.close();
            }catch (IOException e2){
                handleError(e2.getMessage());
            }
            handleError(e.getMessage());
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                StringBuilder messageBuilder = new StringBuilder();
                String line;
                while (!(line = in.readLine()).equals("END")) {
                    messageBuilder.append(line).append("\n");
                }
                String receivedMessage = messageBuilder.toString().trim();
                System.out.println("received : " + receivedMessage);

                if(receivedMessage != null){
                    if (receivedMessage.startsWith("CHAT:")) {
                        if(chatMessageHandler != null){
                            chatMessageHandler.accept(receivedMessage.substring(5));
                        }
                    } else if (receivedMessage.startsWith("CMD:")) {
                        if(commandResponseHandler != null){
                            commandResponseHandler.accept(receivedMessage.substring(4));
                        }
                    }else {
                        System.out.println("Unknown message type:" + receivedMessage);
                    }
                }
            }catch (IOException e){
                handleError("Error while receiving messages: " + e.getMessage());
                stop();
            }
        }
    }

    public void setChatMessageHandler(Consumer<String> handler){
        this.chatMessageHandler = handler;
    }

    public void setCommandResponseHandler(Consumer<String> handler) {
        this.commandResponseHandler = handler;
    }

    public void stop() {
        running = false;
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleError(String msg) {
        System.out.println(msg);
        System.exit(1);
    }
}
