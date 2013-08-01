package com.nvx.tools.mail;

public class MailProperties {
    String trackingId;
    String[] toUsers;
    String[] ccUsers;
    String[] bccUsers;
    String subject;
    String body;
    boolean isBodyHtml;
    private String sender;
    private boolean hasAttach;
    String[] attachNames;
    String[] attachFilePaths;
    
    public MailProperties(String trackingId, String[] toUsers, String[] ccUsers, String[] bccUsers, String subject,
            String body, boolean isBodyHtml, String sender) {
        this.trackingId = trackingId;
        this.toUsers = toUsers;
        this.ccUsers = ccUsers;
        this.bccUsers = bccUsers;
        this.subject = subject;
        this.body = body;
        this.isBodyHtml = isBodyHtml;
        this.sender = sender;
        this.hasAttach = false;
    }
    
    public MailProperties(String trackingId, String[] toUsers, String[] ccUsers, String[] bccUsers, String subject,
            String body, boolean isBodyHtml, String sender, String[] attachNames, String[] attachFilePaths) {
        this.trackingId = trackingId;
        this.toUsers = toUsers;
        this.ccUsers = ccUsers;
        this.bccUsers = bccUsers;
        this.subject = subject;
        this.body = body;
        this.isBodyHtml = isBodyHtml;
        this.sender = sender;
        this.hasAttach = true;
        this.attachNames = attachNames;
        this.attachFilePaths = attachFilePaths;
    }

    public String[] getToUsers() {
        return toUsers;
    }

    public void setToUsers(String[] toUsers) {
        this.toUsers = toUsers;
    }

    public String[] getCcUsers() {
        return ccUsers;
    }

    public void setCcUsers(String[] ccUsers) {
        this.ccUsers = ccUsers;
    }

    public String[] getBccUsers() {
        return bccUsers;
    }

    public void setBccUsers(String[] bccUsers) {
        this.bccUsers = bccUsers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isBodyHtml() {
        return isBodyHtml;
    }

    public void setBodyHtml(boolean isBodyHtml) {
        this.isBodyHtml = isBodyHtml;
    }

    public String[] getAttachNames() {
        return attachNames;
    }

    public void setAttachNames(String[] attachNames) {
        this.attachNames = attachNames;
    }

    public String[] getAttachFilePaths() {
        return attachFilePaths;
    }

    public void setAttachFilePaths(String[] attachFilePaths) {
        this.attachFilePaths = attachFilePaths;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean isHasAttach() {
        return hasAttach;
    }

    public void setHasAttach(boolean hasAttach) {
        this.hasAttach = hasAttach;
    }
}
