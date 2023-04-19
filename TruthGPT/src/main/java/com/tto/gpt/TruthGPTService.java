package com.tto.gpt;

import com.tto.gpt.common.*;
import reactor.core.publisher.Flux;

import java.util.List;

public interface TruthGPTService {

    Flux chatWithGPT(String ip, String sn, GPT35TGBean bean, int qaDeep);
    Flux shortCommand(String ip,String sn,String cmd);

    String parseGPTResponse(String ip,String sn, String json,boolean append);

    RDResult addRequestParameter(String ip, String sn, UIPrompt question);

    void clearQuestions(String ip);

    RDResult generateImage(String ip, String sn, GPTIGBean bean);

    String getNote();
    String getVersion();

    RDResult getLocalSettings(String ip);

    RDResult updateLocalSettings(String ip, GPTLocalSetting ls);

    RDResult checkClientIP(String ip);

    List<ChatBatchInfo> listChatHistory(String ip);

    RDResult listChatRecordsByBatchId(String batchId,String ip);
    RDResult deleteChatBatch(String batchId,String ip);
    RDResult updateChatBatch(ChatBatchInfo cbi,String ip);

    void removeLastQuestion(String ip);

    RDResult findBatchTitle(String ip);

    void stopQuestion(String ip,String sn);

    boolean needCheckProxy();

    String getOpenAIServiceUrl();
}
