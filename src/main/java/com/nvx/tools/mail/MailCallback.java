package com.nvx.tools.mail;

public interface MailCallback {
    
    void complete(String trackingId, boolean success);
}
