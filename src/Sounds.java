import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by ericd on 4/2/2016.
 * @author Eric Dudley
 */
public class Sounds{
    public static HashMap<String, Sound> fx = new HashMap<String, Sound>();

    public static void addSound(String cat, String name, String filename) throws Exception
    {
        switch(cat)
        {
            case "fx":
                fx.put(name, new Sound(filename));
                break;
            default:
                 break;
        }
    }

    public static void playFx(String name)
    {
        //System.out.println("play "+name);
        new Thread(fx.get(name)).start();
    }
}
