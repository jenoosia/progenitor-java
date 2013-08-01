package com.nvx.tools.task;

import com.nvx.bootstrap.SimpleException;


public class RetryException extends SimpleException {

    private static final long serialVersionUID = 2099087423720631038L;

    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }

}
