package stillness;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;


import java.io.*;
import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Enumeration;

import stillness.node.RNode;

/**
 * @author Claude Brisson
 */
public class Stillness {


    // temporaire - pour la ligne de commande
    protected static boolean _debug = true;
    protected static String _debugFile = "log/debugoutput.html";

    public Stillness() {
    }

    public void initialize() throws Exception {
        initialize(findConfigFile());
    }

    public void initialize(String sConfigFile) throws Exception {
        _velocity = new VelocityEngine();
        _velocity.init(sConfigFile);
        _normalize = ("true".compareTo(""+_velocity.getProperty(StillnessConstants.NORMALIZE)) == 0);
    }

    protected String findConfigFile() {
        // default config file for command line tool
        return "conf/stillness.properties";
    }

    public VelocityEngine getVelocityEngine() {
        return _velocity;
    }

    public Context createContext() {
        return new VelocityContext();
    }

    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return getVelocityEngine().getTemplate(name);
    }

/*
    public Context scrape(String source) throws Exception {

    }
*/

    public Context scrape(String source,String template) throws Exception {
        return scrape(source,template,createContext());
    }

    public Context scrape(String source,String template,Context context) throws Exception {
        return scrape(getSourceReader(source),getTemplate(template),context);
    }

    public Context scrape(Reader source,Template template) throws Exception {
        return scrape(source,template,createContext());
    }

    public Context scrape(Reader source,Template template,Context context) throws Exception {
        ReverseTemplate rtemplate = new ReverseTemplate(template);
        String buffer="";
        String line;
        BufferedReader reader = new BufferedReader(source);
        while((line=reader.readLine())!= null) {
            buffer +=line;
        }
        // source normalization
		if (_normalize)
			buffer = StillnessUtil.normalize(buffer);
Logger.debug("### buffer="+buffer);        
        rtemplate.scrape(buffer,context,_normalize,getDebugOutput());
        return context;

    }

    protected String getSource(String name)  throws Exception {
        InputStream stream = null;
        if (name.indexOf("://") != -1) {
            try {
                // TODO : perfectionner le client web (user-agent, etc...)
                stream = new URL(name).openStream();
            } catch(MalformedURLException mue) {
                Logger.error("bad url: "+mue.getMessage());
                System.exit(1);
            } catch(IOException ioe) {
                Logger.error("i/o exception: "+ioe.getMessage());
            }
        } else {
            try {
                stream = new FileInputStream(name);
            } catch (IOException ioe) {
                Logger.error("i/o exception: "+ioe.getMessage());
            }
        }
        String buffer="";
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while((line=reader.readLine())!= null) {
            buffer +=line;
        }
        return buffer;
    }

    public static void main(String args[])
    {
		Logger.setWriter(System.out);
		Logger.setLogLevel(Logger.DEBUG_ID);

		InputStream source = null;
        String configFile = null;
        switch(args.length)
        {
            case 0:
                Logger.error("too few parameters...");
                displayUsage();
                System.exit(1);
            case 1:
                source = System.in;
                break;
            case 3:
                if (args[2].startsWith("conf")) {
                    source = System.in;
                    configFile = args[2].substring(args[2].indexOf("=")+1);
                } else {
                    displayUsage();
                    System.exit(1);
                }
                // no break, we fall back on case 2 to get the source.
            case 2:
                if (args[1].startsWith("conf")) {
                    source = System.in;
                    configFile = args[1].substring(args[1].indexOf("=")+1);
                } else if (args[1].indexOf("://") != -1) {
                    try {
                        source = new URL(args[1]).openStream();
                    } catch(MalformedURLException mue) {
                        Logger.error("bad url: "+mue.getMessage());
                        System.exit(1);
                    } catch(IOException ioe) {
                        Logger.error("i/o exception: "+ioe.getMessage());
                    }
                } else {
                    try {
                        source = new FileInputStream(args[1]);
                    } catch (IOException ioe) {
                        Logger.error("i/o exception: "+ioe.getMessage());
                    }
                }
                break;
            default:
                Logger.error("too many parameters...");
                displayUsage();
                System.exit(1);
        }

        try {
        //    Logger.setWriter(System.out);
		//	Logger.setLogLevel(Logger.DEBUG_ID);
            Stillness v = new Stillness();
            v.initialize();

            // temporaire
            if(_debug) {
                v.setDebugOutput(new PrintWriter(new FileWriter(_debugFile)));
            }


            Context c = v.scrape(new InputStreamReader(source),v.getTemplate(args[0]));
Object []ctxt = c.getKeys();
Logger.debug("Contenu du context en sortie : ");
for (int i=0; i<ctxt.length; i++) {
	Logger.debug(ctxt[i] + " -> '"+ c.get(""+ctxt[i]) +"'");
}
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: vraiment basique, pour l'instant, cette méthode...
    // La version surchargée de StillnessTool devrait pouvoir charger un fichier
    protected Reader getSourceReader(String source) throws MalformedURLException,IOException {
        if (source.indexOf("://") != -1)
            return new InputStreamReader(new URL(source).openStream());
        else
            return new InputStreamReader(new FileInputStream(source));
    }

    private static void displayUsage() {
        Logger.error("\nUsage :" +
                "\n\trapt <template> <url or file> [conf=path]" +
                "\nor :\n\t<command> | rapt <template> [conf=path]\n");
    }

    public void setDebugOutput(PrintWriter debugOutput) {
        _debugOutput = debugOutput;
    }

    public PrintWriter getDebugOutput() {
        return _debugOutput;
    }

    protected VelocityEngine _velocity = null;
    protected boolean _normalize = false;

    protected PrintWriter _debugOutput = null;
}

