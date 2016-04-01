import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Eric on 3/28/2016.
 * The server class for the game.
 * @author Eric Dudley
 */
public class Player {

    public Socket socket;
    public DataOutputStream out;
    public DataInputStream in;
    public String name;

    public Player() throws Exception
    {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter host address: ");
        String host = sc.nextLine();
        System.out.println("Connecting to ["+host+"]...");
        try {
            socket = new Socket(host, 7777);
            System.out.println("Connected!\n---------------");
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            String input = in.readUTF();
            System.out.println(input);
            name = sc.nextLine();
            out.writeUTF(name);
            name = in.readUTF();
            Thread thread = new Thread(new Output(socket, out, in, sc));
            thread.start();
            Thread thread2 = new Thread(new Input(socket, out, in));
            thread2.start();
        }
        catch(java.net.ConnectException exception)
        {
            System.out.println("Connection timed out.\nCould not connect to "+host);
        }
    }
    public static void main(String[] args) throws Exception
    {
        Player player = new Player();
    }

    private class Output implements Runnable
    {

        //public Socket socket;
        //public DataOutputStream out;
        //public DataInputStream in;
        public Scanner scanner;

        public Output(Socket socket, DataOutputStream out, DataInputStream in, Scanner scanner)
        {
            //this.socket = socket;
            //this.out = out;
            //this.in = in;
            this.scanner = scanner;
        }

        public void run()
        {
            String output = "";
            while(true)
            {
                //System.out.print("Message: ");
                output = this.scanner.nextLine();
                output.replace("~","");
                //System.out.println("output: "+output);
                try {
                    if(output.charAt(0) == '!')
                    {
                        output = output.substring(1);
                        switch(output)
                        {
                            case "help":
                                System.out.println("!wtp %name %message - Whispers a message to a player.\n" +
                                        "!help - Prints list and description of all commands.\n" +
                                        "!pl - Prints a list of all players connected to server.\n" +
                                        "!quit - Exits program.");
                                break;
                            case "quit":
                                System.exit(0);
                                break;
                            default:
                                outputCommand(output);
                                break;
                        }
                    }
                    else
                    {
                        outputMessage(output);
                    }
                }
                catch(Exception exception)
                {
                    System.out.println("Couldn't send packet.");
                }
            }
        }

        private void outputMessage(String message) throws Exception
        {
            out.writeUTF("m~"+name+"~"+message);
        }

        private void outputCommand(String command) throws Exception
        {
            out.writeUTF("c~"+name+"~"+command);
        }
    }

    private class Input implements Runnable
    {
        //public Socket socket;
        //public DataOutputStream out;
        //public DataInputStream in;

        public Input(Socket socket, DataOutputStream out, DataInputStream in)
        {
            //this.socket = socket;
            //this.out = out;
            //this.in = in;
        }
        public void run()
        {
            while(true) {
                try {
                    //System.out.println("Waiting for input...");
                    String input = in.readUTF();
                    if(input.equals("!kicked"))
                    {
                        socket = null;
                        in = null;
                        out = null;
                        System.out.println("Disconnecting...");
                        //System.exit(0);
                    }
                    else if(input.equals("!playsound"))
                    {
                        // open the sound file as a Java input stream
                        String gongFile = "joined.wav";
                        InputStream in = new FileInputStream(gongFile);

                        // create an audiostream from the inputstream
                        AudioStream audioStream = new AudioStream(in);

                        // play the audio clip with the audioplayer class
                        AudioPlayer.player.start(audioStream);
                    }
                    //String name = input.split("~")[0];
                    //String printed = input.split("~")[1];
                    System.out.println(input);
                }
                catch(Exception exception)
                {
                    try {
                        String gongFile = "left.wav";
                        InputStream in = new FileInputStream(gongFile);

                        // create an audiostream from the inputstream
                        AudioStream audioStream = new AudioStream(in);

                        // play the audio clip with the audioplayer class
                        AudioPlayer.player.start(audioStream);
                    }
                    catch(Exception exception2)
                    {
                        //Nothing
                    }
                    System.out.println("Disconnected from server.");
                    break;
                }
            }
        }
    }
}
