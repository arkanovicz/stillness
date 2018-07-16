package com.republicate.stillness;

import java.io.*;
import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Enumeration;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.RuntimeConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.republicate.stillness.node.RNode;

/**
 * @author Claude Brisson
 */
public class Stillness {

    protected static Logger logger = LoggerFactory.getLogger("stillness");    

    // temporaire - pour la ligne de commande
    protected static boolean _debug = true;
    protected static String _debugFile = "log/debugoutput.html";

    public Stillness() {
        _velocity = new VelocityEngine();
    }

    public void initialize() throws Exception {
        initialize(findConfigFile());
    }

    public void initialize(String sConfigFile) throws Exception {
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
        if (logger.isTraceEnabled())
        {
            SimpleNode node = (SimpleNode)template.getData();
            StringWriter tree = new StringWriter();
            node.dump("", new PrintWriter(tree));
            logger.trace("template tree:\n{}", tree.toString());
        }
        ReverseTemplate rtemplate = new ReverseTemplate(template);
        if (logger.isTraceEnabled())
        {
            RNode node = rtemplate.getData();
            StringWriter tree = new StringWriter();
            node.dump("", new PrintWriter(tree));
            logger.trace("reverse template tree:\n{}", tree.toString());
        }
        String buffer="";
        String line;
        BufferedReader reader = new BufferedReader(source);
        while((line=reader.readLine())!= null) {
            buffer += line + "\n";
        }
        // source normalization
		if (_normalize)
			buffer = StillnessUtil.normalize(buffer);
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
                logger.error("bad url", mue);
                //System.exit(1);
            } catch(IOException ioe) {
                logger.error("i/o exception", ioe);
            }
        } else {
            try {
                stream = new FileInputStream(name);
            } catch (IOException ioe) {
                logger.error("i/o exception", ioe);
            }
        }
        String buffer="";
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream)); // CB TODO - encoding
        while((line=reader.readLine())!= null) {
            buffer +=line;
        }
        return buffer;
    }

    public static void main(String args[])
    {
		InputStream source = null;
        String configFile = null;
        switch(args.length)
        {
            case 0:
                logger.error("too few parameters...");
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
                        logger.error("bad url", mue);
                    } catch(IOException ioe) {
                        logger.error("i/o exception", ioe);
                    }
                } else {
                    try {
                        source = new FileInputStream(args[1]);
                    } catch (IOException ioe) {
                        logger.error("i/o exception", ioe);
                    }
                }
                break;
            default:
                logger.error("too many parameters...");
                displayUsage();
                System.exit(1);
        }

        try {
            Stillness v = new Stillness();
            v.initialize();

            // temporaire
            if(_debug) {
                v.setDebugOutput(new PrintWriter(new FileWriter(_debugFile)));
            }


            Context c = v.scrape(new InputStreamReader(source),v.getTemplate(args[0]));
Object []ctxt = c.getKeys();
logger.debug("@@@ Contenu du context en sortie : ");
for (int i=0; i<ctxt.length; i++) {
	logger.debug(ctxt[i] + " -> '"+ c.get(""+ctxt[i]) +"'");
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
        logger.error("\nUsage :" +
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

