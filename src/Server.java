import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

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

    public Server() throws Exception
    {
        id = 0;
        serversocket = new ServerSocket(7777);
        System.out.println("Starting server...");
        System.out.println("Waiting for players...");
        users = new HashMap();
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
                        if( !$.equals(this.name) )
                        {
                            DataOutputStream outstream = new DataOutputStream(users.get($).getOutputStream());
                            outstream.writeUTF("[" + name + "]: " + printed);
                        }
                    }
                }
                catch(Exception exception)
                {
                    System.out.println("didn't work");
                    break;
                }
            }
        }
    }
}
