package com.nvx.tools.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

public class FileSyncTest {
    
//    private String url = "10.211.55.3";
//    private Integer port = 21;
//    private String user = "simple";
//    private String pwd = "p@ssw0rd";
    private String url = "192.168.0.103";
    private Integer port = 21;
    private String user = "Jensen";
    private String pwd = "stinkgrass";
    
    @Test
    public void test() throws Exception {
        FtpProps props = new FtpProps(url, port, user, pwd);
        FileSync fs = new FileSync(Collections.singletonMap("helloe", props), new DefaultFtpClientProvider(), 2);
        List<Future<String>> futures = new ArrayList<Future<String>>();
        for (int i = 0; i < 1; i++) {
//            String remotePath = "\\temp\\hello" + (i + 1) + ".html";
            String remotePath = "/Volumes/Data/hello" + (i + 1) + ".html";
            String localPath = "/Volumes/Data/hello.html";
            Future<String> future = fs.syncFile("helloe", "hello-world-" + (i + 1), remotePath, localPath, new FileSyncCallback() {
                @Override
                public void onSuccess(String taskId) {
                    System.out.println("Successfully ran task " + taskId);
                }
                @Override
                public void onFailure(String taskId, Exception e) {
                    System.out.println("Error with task " + taskId);
                    e.printStackTrace();
                    Assert.fail();
                }
            });
            futures.add(future);
        }
        for (Future<String> f : futures) {
            f.get();
        }
    }
}
