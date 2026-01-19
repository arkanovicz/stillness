package com.republicate.stillness;

import org.apache.velocity.exception.VelocityException;

/**
 * @author Claude Brisson
 */

/*
 * ScrapeException must extend ParseErrorException to be throwable
 * from render method.
 */
public class ScrapeException extends VelocityException
{

    public ScrapeException(String msg) {
        super(msg);
    }

    public ScrapeException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
