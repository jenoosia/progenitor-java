package com.nvx.tools.sync;

import org.apache.commons.net.ftp.FTPClient;

public interface FtpClientProvider {
    
    FTPClient provide();
}
