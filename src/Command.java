import java.util.ArrayList;

import static java.io.File.separator;

/**
 * Created by ericd on 4/1/2016.
 */
public class Command {
    public static String seperator = " ";
    private String com;
    private ArrayList<String> args;
    private String orig;
    private String issuer;

    public Command(String issuer, String input)
    {
        this.orig = input;
        this.issuer = issuer;
        this.args = new ArrayList<>();
        this.com = "";
        parseCommand(1);
    }

    public void parseCommand(int nargs)
    {
        this.args.clear();
        String[] elems = this.orig.split(Command.seperator);
        this.com = elems[0];
        int curs = 0;
        for(int i=1; i<nargs; i++)
        {
            args.add(elems[i]);
            curs = i;
        }
        String last = "";
        for(int i=curs+1; i<elems.length; i++)
        {
            last += " "+elems[i];
        }
        args.add(last.trim());
    }
    public String args(int idx){return this.args.get(idx);}
    public String com(){return this.com;}
    public String issuer(){return this.issuer;}

    public String toString()
    {
        String rstr = "Issuer: "+this.issuer+"\nCommand: "+this.com+"\nArgs: ";
        for(String $: this.args)
        {
            rstr += $+"\n";
        }
        rstr = rstr.trim();
        return rstr;
    }
}
