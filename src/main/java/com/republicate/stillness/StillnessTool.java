package com.republicate.stillness;

import java.net.URL;
import java.io.*;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.context.Context;

public class StillnessTool {

    protected static Logger logger = LoggerFactory.getLogger("stillness");

    public static final String STILLNESS_CONFIG_FILE_KEY = "stillness.config";

    String _configFile = null;

    protected static Stillness _stillness = null;
    protected static Object _semaphore = new Object();
    protected ViewContext _context = null;

    public StillnessTool() throws Exception {
    }

    public void setServletContext(ServletContext ctx)
    {
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
				logger.info("Using config file: "+_configFile);
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
                    logger.error("Initialization problem",e);
                }
            }
        }
    }

    public void setVelocityContext(ViewContext vctx)
    {
        _context = vctx;
    }
    
    public void scrape(String source,String template) {
        try {
            _stillness.scrape(source,template,(Context)_context);
        } catch (Exception e) {
            logger.error("scraping error", e);
        }
    }

    public String debug(String source,String template) {
        Writer dbg = new StringWriter();
        try {
            PrintWriter writer = new PrintWriter(dbg);
            _stillness.setDebugOutput(writer);
            _stillness.scrape(source,template,(Context)_context);
        } catch (Exception e) {
            logger.error("exception", e);
        } finally {
            return dbg.toString();
        }
    }
}
