package com.nvx.tools.sync;

public class FtpProps {
    
    private String url;
    private Integer port;
    private String user;
    private String pwd;
    
    public FtpProps() {
        
    }
    
    public FtpProps(String url, Integer port, String user, String pwd) {
        this.url = url;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }
    
    public String getUrl() {
        return url;
    }
    public Integer getPort() {
        return port;
    }
    public String getUser() {
        return user;
    }
    public String getPwd() {
        return pwd;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
