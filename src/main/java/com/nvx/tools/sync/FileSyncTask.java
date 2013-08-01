package com.nvx.tools.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSyncTask implements Callable<String> {
    
    private static final Logger log = LoggerFactory.getLogger(FileSyncTask.class);

    private String taskId;
    
    private FTPClient client;
    private FtpProps props;
    
    private String remotePath;
    private File localFile;
    
    private FileSyncCallback callback;
    
    public FileSyncTask(String taskId, FtpProps props, String remotePath, File localFile, 
            FtpClientProvider provider, FileSyncCallback callback) {
        this.taskId = taskId;
        this.props = props;
        this.remotePath = remotePath;
        this.localFile = localFile;
        this.client = provider.provide();
        this.callback = callback;
    }
    
    @Override
    public String call() throws Exception {
        try {

            client.connect(props.getUrl(), props.getPort());
            
            if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                String err = "Unable to connect to FTP server " + props.getUrl() + ":" + props.getPort() + ".";
                log.error(err);
                throw new IOException(err);
            }
            
            try {

                if (!client.login(props.getUser(), props.getPwd())) {
                    String err = "Unable to login to FTP server " + props.getUrl() + ":" + props.getPort().toString() + 
                            " with user " + props.getUser();
                    log.error(err);
                    throw new IOException(err);
                }

                try {
                    this.client.setFileType(FTPClient.BINARY_FILE_TYPE);
                } catch (Exception e) {
                    log.warn("Cannot set FTP Client file type to Binary. Still proceeding through. Task ID is " + taskId);
                }
                
                FileInputStream fis = new FileInputStream(localFile);
                try {
                    if (!client.storeFile(remotePath, fis)) {
                        String err = "Unable to store file due to FTP Reply Code " + client.getReplyCode();
                        log.error(err);
                        throw new IOException(err);
                    }
                } finally {
                    fis.close();
                }
            } finally {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    log.warn("Was not able to disconnect successfully from the client.", e);
                }
            }
            
            log.info("Successfully synced file to remote server. Completed task: {}", taskId);
            if (callback != null) {
                callback.onSuccess(taskId);
            }
        } catch (Exception e) {
            log.error("An error occurred while executing task {}, calling onFailure hook.", taskId);
            if (callback != null) {
                callback.onFailure(taskId, e);
            }
        }
        return "-";
    }

}
