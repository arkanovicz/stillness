package com.republicate.stillness;

import org.apache.velocity.exception.ParseErrorException;

/**
 * @author Claude Brisson
 */

/*
 * ScrapeException must extend ParseErrorException to be throwable
 * from render method.
 */
public class ScrapeException extends ParseErrorException {

    public ScrapeException(String msg) {
        super(msg);
    }

}
