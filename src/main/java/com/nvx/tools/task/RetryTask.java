package com.nvx.tools.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RetryTask<V, T> {
    
    protected final Logger log = LoggerFactory.getLogger(getClass()); 
    
    protected int maxTries;
    protected int retryIntervalMillis;
    protected String action;
    protected boolean success = false;
    protected String errorMessage = "";
    
    public RetryTask(int maxTries, String action, int retryIntervalMillis) {
        this.maxTries = maxTries;
        this.action = action;
        this.retryIntervalMillis = retryIntervalMillis;
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
    
    public boolean isSuccess() {
        return this.success;
    }
    
    public V execute(T inputObj) throws RetryException {
        int tryCount = 0;
        V result = null;
        while (tryCount < this.maxTries) {
            try {
                
                log.debug("Executing retry task for {}", this.action);
                
                result = doExecute(inputObj);
                
                if (result != null) {
                    return result;
                }
                
                log.debug("Execution failed for action {}. Retrying...", this.action);
                tryCount++;
            } catch (Exception e) {
                log.error("Exception encountered while executing action " + this.action + ": " + e.getMessage(), e);
                tryCount++;
                if (tryCount >= maxTries) {
                    throw new RetryException("Execution of " + this.action + 
                            " has already exceeded the max tries due to an exception.", e);
                }
            }
            
            try {
                Thread.sleep(this.retryIntervalMillis);
            } catch (InterruptedException e) {
                log.warn("Retry interval duration was not completed, and the thread was interrupted.", e);
            }
        }

        throw new RetryException("Execution of " + this.action + 
                " has exceeded the max tries, but the result is still null");
    }
    
    protected abstract V doExecute(T inputObj) throws Exception;
}
