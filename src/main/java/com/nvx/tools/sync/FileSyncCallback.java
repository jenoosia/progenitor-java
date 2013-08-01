package com.nvx.tools.sync;

public interface FileSyncCallback {
    void onSuccess(String taskId);
    void onFailure(String taskId, Exception e);
}
