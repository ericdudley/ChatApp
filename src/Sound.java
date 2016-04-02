import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by ericd on 4/2/2016.
 * @author Eric Dudley
 */
public class Sound implements Runnable{
    private AudioStream stream;
    private String filename;

    public Sound(String filename) throws Exception
    {
        this.filename = filename;
        InputStream in = new FileInputStream(filename);
        this.stream = new AudioStream(in);
    }

    public void run()
    {
        System.out.println(this.filename);
        AudioPlayer.player.start(this.stream());
    }
    public AudioStream stream(){return this.stream;}
}
