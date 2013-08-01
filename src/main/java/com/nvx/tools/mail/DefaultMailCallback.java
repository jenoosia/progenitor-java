package com.nvx.tools.mail;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;

public class DefaultMailCallback implements MailCallback {
    
    private WeakReference<Logger> log;
    
    public DefaultMailCallback(Logger log) {
        this.log = new WeakReference<Logger>(log);
    }
    
    @Override
    public void complete(String trackingId, boolean success) {
        final Logger l = this.log.get();
        if (l != null) {
            l.info("Email: {}, Status: {}", trackingId, success ? "Success" : "Failure");
        }
    }
}
