package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/*
client -> server message format : /[COMMAND] ?[ARG1] ?[ARG2]...
server -> client message format : [TYPE(CHAT/CMD)]:[SUBTYPE(LISTPARTICIPANTS, LISTCHATROOMS...)][SUCCESS OR FAIL(ERROR, SUCCESS)]:[DATA]
 */

public class ProgramServer {
    private ServerSocket serverSocket = null;
    private final Set<String> connectedUsers = Collections.synchronizedSet(new HashSet<>());
    private Map<String, ChatRoom> chatRooms = new HashMap<>();
    private final File userList = new File("data/userList/userList.txt");

    public ProgramServer() {

        try {
            serverSocket = new ServerSocket(9999);
            System.out.println("Server is running on port 9999...");

            initializeUserList();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUserList(){
        try {
            if (!userList.exists()) {
                userList.getParentFile().mkdirs();
                userList.createNewFile();
                System.out.println("Initialized userList: " + userList.getPath());
            }
        } catch (IOException e) {
            System.out.println("Failed to initialize userList");
            e.printStackTrace();
        }
    }

    public synchronized boolean register(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(userList));
             BufferedWriter writer = new BufferedWriter(new FileWriter(userList, true))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(username + ",")) {
                    return false;
                }
            }
            
            writer.write(username + "," + password);
            writer.newLine();
            connectedUsers.add(username);
            System.out.println("registration successful!");
            return true;

        } catch (IOException e) {
            System.out.println("Error during user registration.");
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean login(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(userList))) {
            String line;
            while ((line = reader.readLine()) != null){
                String[] user = line.split(",");
                if (user[0].equals(username) && user[1].equals(password)) {
                    connectedUsers.add(username);
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error during user login.");
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean createChatRooms(String roomName){
        if (!chatRooms.containsKey(roomName)) {
            chatRooms.put(roomName, new ChatRoom(roomName));
            System.out.println("Chat room created: " + roomName);
            return true;
        }else{
            return false;
        }
    }

    public synchronized ChatRoom joinChatRoom(String roomName, ClientHandler client){
        ChatRoom room = chatRooms.get(roomName);
        if(room!=null){
            room.addParticipant(client);
            System.out.println("Join Chatroom: " + roomName);
        }
        return room;
    }

    public synchronized List<String> getChatRoomList(){
        return new ArrayList<>(chatRooms.keySet());
    }

    public synchronized void broadcastMessage(ChatRoom room, String message){
        room.broadcastMessage(message);
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private final ProgramServer server;
        private ChatRoom currentRoom;
        private BufferedReader in;
        private BufferedWriter out;
        private String username;

        public ClientHandler(Socket socket, ProgramServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    if (message.startsWith("/register")) {
                        System.out.println("register process start");
                        handleRegister(message);
                    } else if (message.startsWith("/login")) {
                        System.out.println("login process start");
                        handleLogin(message);
                    } else if (message.startsWith("/createRoom")){
                        System.out.println("create Room process start");
                        handleCreateRoom(message);
                    } else if (message.startsWith("/joinRoom")) {
                        System.out.println("join Room process start");
                        handleJoinRoom(message);
                    } else if (message.startsWith("/broadcast")) {
                        System.out.println("broadcast process start");
                        handleBroadcast(message);
                    } else if (message.startsWith("/listUsers")) {
                        System.out.println("listUsers process start");
                        handleListUsers();
                    } else if (message.startsWith("/listRooms")) {
                        System.out.println("listRooms process start");
                        handleListRooms();
                    } else if (message.startsWith("/listParticipants")) {
                        System.out.println("listParticipants process start");
                        handleListParticipants();
                    } else {
                        sendMessage("Unknown command. Please try again.");
                    }
                }
            }catch (IOException e){
                System.out.println("Connection with client lost.");
            }finally {
                if (username != null){
                    connectedUsers.remove(username);
                    if(currentRoom != null){
                        currentRoom.removeParticipant(this);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleRegister(String message){
            String[] tokens = message.split(" ");
            if (tokens.length < 3) {
                sendMessage("CMD:REGISTER_ERROR:Usage: /register [username] [password]");
                System.out.println("Usage: /register [username] [password]");
                return;
            }
            String username = tokens[1];
            String password = tokens[2];

            if (server.register(username, password)) {
                this.username = username;
                sendMessage("CMD:REGISTER_SUCCESS: Registration successful! you can now login.");
                System.out.println("Registration successful! you can now login.");
            } else {
                sendMessage("CMD:REGISTER_ERROR: Username already exists. Please try another");
                System.out.println("Username already exists. Please try another");
            }
        }

        private void handleLogin(String message) {
            String[] tokens = message.split(" ");
            if (tokens.length < 3) {
                sendMessage("CMD:LOGIN_ERROR:Usage: /login [username] [password]");
                System.out.println("Usage: /login [username] [password]");
                return;
            }

            String username = tokens[1];
            String password = tokens[2];

            if (server.login(username, password)) {
                this.username = username;
                sendMessage("CMD:LOGIN_SUCCESS:Login successful! Welcome, " + username + "!");
                System.out.println("CMD:LOGIN_SUCCESS:Login successful! Welcome, " + username + "!");
            } else {
                sendMessage("CMD:LOGIN_ERROR:Invalid username or password. Please try again.");
                System.out.println("CMD:LOGIN_ERROR:Invalid username or password. Please try again.");
            }
        }

        private void handleCreateRoom(String message){
            String[] tokens = message.split(" ");
            if(tokens.length!=2){
                sendMessage("CMD:CREATEROOM_ERROR:Usage: /createRoom [roomName]");
                System.out.println("Usage: /createRoom [roomName]");
                return;
            }

            String roomName = tokens[1];

            if(server.createChatRooms(roomName)){
                handleListRooms();
            }

        }

        public String getUsername() {
            return username;
        }

        private void handleJoinRoom(String message){
            String[] tokens = message.split(" ");
            if(tokens.length<2){
                sendMessage("CMD:JOINROOM_ERROR:Usage: /joinRoom [roomName]");
                return;
            }

            String roomName = tokens[1];
            currentRoom = server.joinChatRoom(roomName, this);
            if (currentRoom != null) {
                sendMessage("CMD:JOINROOM_SUCCESS:Joined room " + roomName);
            } else {
                sendMessage("CMD:JOINROOM_ERROR:Room does not exist.");
            }
        }

        private void handleListRooms(){
            List<String> rooms = server.getChatRoomList();
            if(rooms.isEmpty()){
                sendMessage("CMD:LISTROOMS_ERROR: No chat rooms available");
                return;
            }

            StringBuilder messageBuilder = new StringBuilder("CMD:LISTROOMS_SUCCESS:");
            for (String room : rooms) {
                messageBuilder.append(room).append(";");
            }

            sendMessage(messageBuilder.toString().replaceAll(";$", ""));
        }

        private void handleBroadcast(String message){
            if (currentRoom == null) {
                sendMessage("CHAT:BROADCAST_ERROR: You are not in a room.");
                return;
            }

            String broadcastMessage = message.substring("/broadcast".length()).trim();
            server.broadcastMessage(currentRoom, username + ": " + broadcastMessage);
        }

        private void handleListUsers() {
            synchronized (connectedUsers) {
                if (connectedUsers.isEmpty()) {
                    sendMessage("CMD:LISTUSERS_ERROR: No users are currently connected.");
                    return;
                }

                StringBuilder messageBuilder = new StringBuilder("CMD:LISTUSERS_SUCCESS:");
                for (String user : connectedUsers) {
                    messageBuilder.append(user).append(";");
                }

                sendMessage(messageBuilder.toString().replaceAll(";$", ""));
            }
        }

        private void handleListParticipants(){
            List<String> participants = currentRoom.getParticipantNames();
            if(participants.isEmpty()){
                sendMessage("CMD:LISTPARTICIPANTS_ERROR: No user participated");
                return;
            }

            StringBuilder messageBuilder = new StringBuilder("CMD:LISTPARTICIPANTS_SUCCESS:");
            for (String participant : participants) {
                messageBuilder.append(participant).append(";");
            }

            sendMessage(messageBuilder.toString().replaceAll(";$", ""));
        }

        private void sendMessage(String message) {
            try {
                out.write(message + "\nEND\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Failed to send message to client.");
            }
        }
    }

    class ChatRoom{
        private String name;
        private List<ClientHandler> participants;
        private List<String> chatHistory;

        public ChatRoom(String name) {
            this.name = name;
            this.participants = new ArrayList<>();
            this.chatHistory = new ArrayList<>();
        }
        public synchronized void broadcastMessage(String message){
            addMessage(message);
            for (ClientHandler client : participants) {
                client.sendMessage("CHAT:" + message);
            }
        }

        public synchronized void addParticipant(ClientHandler client) {
            participants.add(client);
        }

        public synchronized void removeParticipant(ClientHandler client) {
            participants.remove(client);
        }

        public synchronized void addMessage(String message) {
            chatHistory.add(message);
        }

        public synchronized List<String> getParticipantNames() {
            List<String> names = new ArrayList<>();
            for (ClientHandler client : participants) {
                names.add(client.getUsername());
            }
            return names;
        }
    }
    public static void main(String[] args) {
        new ProgramServer();
    }
}
