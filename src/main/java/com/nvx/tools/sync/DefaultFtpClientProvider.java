package com.nvx.tools.sync;

import org.apache.commons.net.ftp.FTPClient;

public class DefaultFtpClientProvider implements FtpClientProvider {

    @Override
    public FTPClient provide() {
        FTPClient client = new FTPClient();
        client.setConnectTimeout(10000);
        client.setControlKeepAliveReplyTimeout(3000);
        client.setControlKeepAliveTimeout(300);
        return client;
    }
    
}
