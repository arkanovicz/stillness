package com.republicate.stillness;

import java.net.URL;
import java.io.*;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.velocity.tools.view.ViewContext;
import org.apache.velocity.context.Context;

public class StillnessTool {

    protected static Logger logger = LoggerFactory.getLogger("stillness");

    public static final String STILLNESS_CONFIG_FILE_KEY = "com.republicate.stillness.config";

    String _configFile = null;

    protected static Stillness _stillness = null;
    protected static Object _semaphore = new Object();
    protected Context _context = null;

    public StillnessTool()
    {
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
                        _stillness.getVelocityEngine().setApplicationAttribute("javax.servlet.ServletContext", ctx);
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
        _context = (Context)vctx;
    }


    /* alternate way of setting the context */
    public void setOutputContext(Context ctx)
    {
        _context = ctx;
    }


    public void scrape(String source,String template) {
        try {
            scrape(Stillness.getSourceReader(source), template);
        } catch (Exception e) {
            logger.error("scraping error", e);
        }
    }

    public void scrape(Reader source, String template) {
        try {
            _stillness.scrape(source,template,_context);
        } catch (Exception e) {
            logger.error("scraping error", e);
        }
    }


    public String debug(String source,String template) {
        try {
            return debug(Stillness.getSourceReader(source), template);
        } catch (Exception e) {
            logger.error("scraping error", e);
            return null;
        }
    }

    public String debug(Reader source,String template) {
        return debugThrow(source, template).getLeft();
    }

    public Pair<String, Exception> debugThrow(String source,String template) {
        try {
            return debugThrow(Stillness.getSourceReader(source), template);
        } catch (Exception e) {
            logger.error("scraping error", e);
            return Pair.of(null, e);
        }
    }

    public Pair<String, Exception> debugThrow(Reader source,String template) {
        String ret = null;
        StringWriter traceWriter = new StringWriter();
        PrintWriter trace = new PrintWriter(traceWriter);
        StringWriter errorWriter = new StringWriter();
        PrintWriter error = new PrintWriter(errorWriter);
        Exception ex = null;;
        try {
            _stillness.setDebugOutput(trace);
            _stillness.scrape(source,template,_context);
        } catch (Exception e) {
            logger.error("exception", e);
            ex = e;
        } finally {
            if (ex != null) {
                error.println("<pre>");
                ex.printStackTrace(error);
                error.println("</pre>");
            }
            trace.flush();
            error.flush();
            ret = errorWriter.toString() + traceWriter.toString();
        }
        return Pair.of(ret, ex);
    }
}
