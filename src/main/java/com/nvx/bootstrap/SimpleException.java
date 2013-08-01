package com.nvx.bootstrap;

public class SimpleException extends Exception {
    
    private static final long serialVersionUID = 1856564512732475062L;
    
    private String classMethodName;
    private String errorCode;

    public SimpleException(String message) {
        super(message);
    }
    
    public SimpleException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SimpleException(String message, String classMethodName) {
        this(message, classMethodName, "");
    }
    
    public SimpleException(String message, String classMethodName, String errorCode) {
        super(message);
        
        this.classMethodName = classMethodName;
        this.errorCode = errorCode;
    }
    
    public SimpleException(String message, Throwable cause, String classMethodName) {
        this(message, cause, classMethodName, "");
    }
    
    public SimpleException(String message, Throwable cause, String classMethodName, String errorCode) {
        super(message, cause);
        
        this.classMethodName = classMethodName == null ? "" : classMethodName;
        this.errorCode = errorCode == null ? "" : errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getClassMethodName() {
        return classMethodName;
    }

    public void setClassMethodName(String classMethodName) {
        this.classMethodName = classMethodName;
    }
}
