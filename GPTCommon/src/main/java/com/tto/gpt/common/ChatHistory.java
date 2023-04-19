package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ChatHistory implements Serializable {

    ChatBatchInfo batchInfo;
    List<ChatRecord> chatRecordList;
}
