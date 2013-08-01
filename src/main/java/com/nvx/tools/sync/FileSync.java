package com.nvx.tools.sync;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileSync {
    
    public static final int DEFAULT_THREAD_POOL_SIZE = 2;
    
    private ExecutorService exec = null;
    
    private FtpClientProvider ftpProvider = null;
    public void setFtpProvider(FtpClientProvider provider) {
        this.ftpProvider = provider;
    }

    private Map<String, FtpProps> props = null;
    
    public FileSync(Map<String, FtpProps> props) {
        this(props, DEFAULT_THREAD_POOL_SIZE);
    }
    
    public FileSync(Map<String, FtpProps> props, int threads) {
        this.exec = Executors.newFixedThreadPool(threads);
        this.props = props;
    }
    
    public FileSync(Map<String, FtpProps> props, FtpClientProvider provider) {
        this(props, provider, DEFAULT_THREAD_POOL_SIZE);
    }

    public FileSync(Map<String, FtpProps> props, FtpClientProvider provider, int threads) {
        this.ftpProvider = provider;
        this.exec = Executors.newFixedThreadPool(threads);
        this.props = props;
    }
    
    public Future<String> syncFile(String propsKey, String taskId, String remotePath, String localFilePath, FileSyncCallback callback) {
        return exec.submit(new FileSyncTask(taskId, props.get(propsKey), remotePath, new File(localFilePath), ftpProvider, callback));
    }
    
    public Future<String> syncFile(String propsKey, String taskId, String remotePath, File localFile, FileSyncCallback callback) {
        return exec.submit(new FileSyncTask(taskId, props.get(propsKey), remotePath, localFile, ftpProvider, callback));
    }
    
    public void shutdown() {
        if (this.exec != null && !this.exec.isShutdown()) {
            this.exec.shutdown();
        }
    }
    
    public boolean isUp() {
        if (this.exec != null && !this.exec.isShutdown() && this.ftpProvider != null) {
            return true;
        }
        
        return false;
    }
}
