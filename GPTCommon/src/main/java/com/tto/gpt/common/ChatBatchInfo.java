package com.tto.gpt.common;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ChatBatchInfo implements Serializable {
    private String batchTitle;
    private String batchId;

    private String systemScope;

    public ChatBatchInfo(String batchTitle, String batchId) {
        this.batchTitle = batchTitle;
        this.batchId = batchId;
    }

    public String getBatchTitle() {
        return batchTitle;
    }

    public String getBatchId() {
        return batchId;
    }
}
