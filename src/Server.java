import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
            for (String $ : users.keySet()) {
                DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                String welcome_message = "Welcome "+name+" to the chat!";
                messages.add(welcome_message);
                //outstream.writeUTF(welcome_message);
            }
            String total_str = "";
            for(int i=Math.max(0,messages.size()-21); i<messages.size(); i++)
            {
                total_str += messages.get(i)+"\n";
            }
            total_str.trim();
            //System.out.println(total_str);
            out.writeUTF(total_str);
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
                    System.out.println(input);
                    for (String $ : users.keySet()) {
                        String printstr = "["+ name + "]: "+printed;
                        messages.add(printstr);
                        if( !$.equals(this.name) && !users.get($).equals(null) )
                        {
                            DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                            outstream.writeUTF(printstr);
                        }
                    }
                }
                catch(Exception exception)
                {
                    System.out.println("didn't work for "+this.name);
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
                    String outputstr = "";
                    for (int i = 1; i < output.split(" ").length; i++) {
                        body += output.split(" ")[i]+" ";
                    }
                    switch (cmd) {
                        case "sm":
                            outputstr = body;
                            break;
                        case "kp":
                            outputstr = "Kicked player: "+body;
                            DataOutputStream outstream = new DataOutputStream(users.get(body.trim()).getOutputStream());
                            outstream.writeUTF("[~Server~] \n\t" + "You have been kicked!" + "\n[~Server~]");
                            users.remove(body.trim());
                            break;
                        case "pl":
                            for(String $: users.keySet())
                            {
                                System.out.println($);
                            }
                            break;
                        case "wtp":
                            DataOutputStream outstream2 = new DataOutputStream(users.get(components[1]).getOutputStream());
                            outstream2.writeUTF("[~ServerWhisper~] \n\t" + body.replace(components[1], "") + "\n[~ServerWhisper~]");
                            break;
                        default:
                            System.out.println("Unknown command.");
                            break;
                    }
                    if (!outputstr.equals(""))
                    {
                        for (String $ : users.keySet())
                        {
                            messages.add("[~Server~] \n\t" + outputstr + "\n[~Server~]");
                            DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                            outstream.writeUTF("[~Server~] \n\t" + outputstr + "\n[~Server~]");
                        }
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

