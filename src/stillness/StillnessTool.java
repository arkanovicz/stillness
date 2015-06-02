package stillness;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.context.Context;

import java.net.URL;
import java.io.*;
import javax.servlet.ServletContext;

public class StillnessTool {

    public static final String STILLNESS_CONFIG_FILE_KEY = "stillness.config";

    String _configFile = null;

    protected static Stillness _stillness = null;
    protected static Object _semaphore = new Object();
    protected ViewContext _context = null;

    public StillnessTool() throws Exception {
    }

    public void init(Object inViewContext) {

        ServletContext ctx = null;
        ViewContext viewctx = null;

        if (inViewContext instanceof ServletContext) {
            ctx = (ServletContext)inViewContext;
        }
        else if (inViewContext instanceof ViewContext) {
            _context = (ViewContext)inViewContext;
            ctx = _context.getServletContext();
        }
        else {
            Logger.log2StdErr();
            Logger.error("Initialization: no valid initialization data found!");
        }

        // init log
        Logger.setWriter(new ServletLogWriter(ctx));
		Logger.setLogLevel(0);
        Logger.info("Stillness tool initialization.");

        synchronized(_semaphore) {
            if(_stillness == null) {
                // get config file
                if (_configFile == null) { // if not already given by configure()
                    // look in the servlet parameters
                    _configFile = ctx.getInitParameter(STILLNESS_CONFIG_FILE_KEY);
                }
				if (_configFile == null) {
					// default config file
					_configFile = "/WEB-INF/stillness.properties";
				}
                if (!_configFile.startsWith("/")) _configFile = "/"+_configFile;
    //            InputStream is = ctx.getResourceAsStream(mConfigFile);
				Logger.info("Using config file: "+_configFile);
                try {
					URL url = ctx.getResource(_configFile);
					String filename = url.toString();
					if(filename.startsWith("file:")) {
		                _stillness = new Stillness();
						_stillness.initialize(filename.substring(5));
					}
					else {
						throw new IOException("Initialization: '"+url+"' is not a file url!");
					}
                } catch(Exception e) {
                    Logger.log("Initialization problem",e);
                }
            }
        }
    }

    public void scrape(String source,String template) {
        try {
            _stillness.scrape(source,template,(Context)_context);
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public String debug(String source,String template) {
        Writer dbg = new StringWriter();
        try {
            PrintWriter writer = new PrintWriter(dbg);
            _stillness.setDebugOutput(writer);
            _stillness.scrape(source,template,(Context)_context);
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            return dbg.toString();
        }
    }
}
