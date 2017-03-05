package com.example.buhalo.lazyir.Exception;

/**
 * Created by buhalo on 05.03.17.
 */

public class ParseError extends Exception {
    public ParseError(String message) {
        super(message);
    }

    public ParseError(String message, Throwable cause) {
        super(message, cause);
    }
}
