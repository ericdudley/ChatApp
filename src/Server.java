import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Eric on 3/28/2016.
 * The server class for the game.
 * @author Eric Dudley
 */
public class Server {
    public ServerSocket serversocket;
    public Socket socket;
    public DataOutputStream out;
    public DataInputStream in;
    public HashMap<String, Socket> users;
    public int id;
    public ArrayList<String> messages;

    public Server() throws Exception
    {
        messages = new ArrayList<>();
        id = 0;
        serversocket = new ServerSocket(7777);
        System.out.println("Starting server...");
        System.out.println("Waiting for players...");
        users = new HashMap();
        Thread thread2 = new Thread(new Output());
        thread2.start();
        while(true)
        {
            System.out.println("Waiting for next player...");
            socket = serversocket.accept();
            System.out.println("Player connected: " + socket.getInetAddress());
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            out.writeUTF("Hello!\nPlease enter an ingame-name: ");
            String name = in.readUTF()+"_"+id+"";
            users.put(name, socket);
            System.out.println("Added to list");
            Thread thread = new Thread(new Input(socket, out, in, name));
            thread.start();
            id++;
            String welcome_message = "Welcome "+name+" to the chat!";
            messages.add(welcome_message);
            for (String $ : users.keySet()) {
                if( !$.equals(name) && !users.get($).equals(null) )
                {
                    DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                    outstream.writeUTF(welcome_message);
                }
            }
            String total_str = "";
            for(int i=Math.max(0,messages.size()-21); i<messages.size(); i++)
            {
                total_str += messages.get(i)+"\n";
            }
            total_str = total_str.trim();
            //System.out.println(total_str);
            if(!total_str.equals(""))
            {
                out.writeUTF(total_str);
            }
        }
    }
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
    }
    private class Input implements Runnable
    {
        public Socket socket;
        public DataOutputStream out;
        public DataInputStream in;
        public String name;

        public Input(Socket socket, DataOutputStream out, DataInputStream in, String name)
        {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.name = name;
        }
        public void run()
        {
            while(true) {
                try {
                    //System.out.println("Waiting for input...");
                    String input = this.in.readUTF();
                    String name = input.split("~")[0];
                    String printed = input.split("~")[1];
                    //System.out.println(input);
                    String printstr = "["+ name + "]: "+printed;
                    messages.add(printstr);
                    for (String $ : users.keySet()) {
                        if( !$.equals(this.name) && !users.get($).equals(null) )
                        {
                            DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                            outstream.writeUTF(printstr);
                        }
                    }
                }
                catch(Exception exception)
                {
                    System.out.println(this.name+" disconnected.");
                    Output output = new Output();
                    output.sendServerMessage(this.name+" has disconnected");
                    this.out = null;
                    this.in = null;
                    this.socket = null;
                    users.remove(this.name);
                    break;
                }
            }
        }
    }
    private class Output implements Runnable
    {
        private void sendServerMessage(String message)
        {
            try
            {
                messages.add("[~Server~] \n\t" + message + "\n[~Server~]");
                for (String $ : users.keySet())
                {
                    DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                    outstream.writeUTF("[~Server~] \n\t" + message + "\n[~Server~]");
                }
            }
            catch(Exception exception)
            {
                System.out.println("Failed to send server message.");
            }
        }

        private void whisper(String user, String message)
        {
            try
            {
                DataOutputStream outstream2 = new DataOutputStream(users.get(user).getOutputStream());
                outstream2.writeUTF("[~ServerWhisper~] \n\t" + message + "\n[~ServerWhisper~]");
            }
            catch(Exception exception)
            {
                System.out.println("Failed to send whisper.");
            }
        }

        private void kickPlayer(String user)
        {
            try
            {
                if(users.containsKey(user))
                {
                    whisper(user, "You have been kicked.");
                    DataOutputStream outstream = new DataOutputStream(users.get(user).getOutputStream());
                    outstream.writeUTF("!kicked");
                    users.remove(user);
                    sendServerMessage(user+" has been kicked.");                
                }
            }
            catch(Exception expection)
            {
                System.out.println("Failed to kick player.");
            }
        }
        public void run()
        {
            Scanner sc = new Scanner(System.in);
            while(true) {
                try {
                    //System.out.println("Waiting for input...");
                    System.out.print("$:");
                    String output = sc.nextLine();
                    String cmd = output.split(" ")[0];
                    String[] components = output.split(" ");
                    String body = "";
                    for (int i = 1; i < output.split(" ").length; i++) {
                        body += output.split(" ")[i]+" ";
                    }
                    switch (cmd) {
                        case "sm":
                            sendServerMessage(body);
                            break;
                        case "kp":
                            kickPlayer(body.trim());
                            break;
                        case "pl":
                            for(String $: users.keySet())
                            {
                                System.out.println($);
                            }
                            break;
                        case "wtp":
                            whisper(components[1], body.replace(components[1], ""));
                            break;
                        case "shutdown":
                            sendServerMessage("Server shutting down...");
                            TimeUnit.SECONDS.sleep(1);
                            sendServerMessage("3");
                            TimeUnit.SECONDS.sleep(1);
                            sendServerMessage("2");
                            TimeUnit.SECONDS.sleep(1);
                            sendServerMessage("1");
                            System.exit(0);
                            break;
                        case "reset":
                            sendServerMessage("Server is restarting...");
                            for(String $: users.keySet())
                            {
                                kickPlayer($);
                            }
                            messages.clear();
                            break;
                        case "commands":
                            System.out.println("sm %message - Send server message to all players.\n" +
                                    "kp %name - Kick player from the server.\n" +
                                    "TODO ban %name - Ban a players ip address from the server.\n" +
                                    "TODO unban %name - Unban a players ip address from the server.\n" +
                                    "pl - Get a list of all current players.\n" +
                                    "wtp %name %message - Whisper a message to specified player.\n" +
                                    "shutdown - Notify all players server is shutting down, then shutdown.\n" +
                                    "reset - Kick all players and clear messages history.\n" +
                                    "commands - Show all available commands.");
                            break;
                        default:
                            System.out.println("Unknown command.");
                            break;
                    }
                }
                catch(Exception exception)
                {
                    System.out.println("didn't work for server");
                    break;
                }
            }
        }
    }
}

