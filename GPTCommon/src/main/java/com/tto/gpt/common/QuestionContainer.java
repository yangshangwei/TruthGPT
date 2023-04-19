package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class QuestionContainer implements Serializable {

    private final int MaxSize = 10;
    private int index = -1;
    GPT35Message[] questions = new GPT35Message[MaxSize];
    GPT35Message[] answers = new GPT35Message[MaxSize];

    TextChatRecordManager recordManager = new TextChatRecordManager("LocalHost");

    private String systemScope = "You are a helpful assistant.";
    private boolean scopeChanged = false;
    public final static String DefaultSystemScope = "You are a helpful assistant.";

    private String batchId;
    private String batchTitle;
    private ExpiringMap<String, String> answerCache;

    public QuestionContainer() {
        this.answerCache = new ExpiringMap<>(1800);
    }

    public String getCachedAnswer(String sn) {
        return answerCache.get(sn);
    }

    public ChatBatchInfo findBatchInfo(){
        if(!CommonUtils.emptyString(batchTitle)){
            ChatBatchInfo chatBatchInfo = new ChatBatchInfo(batchTitle,batchId);
            this.batchTitle = null;
            return chatBatchInfo;
        }
        return null;
    }
    public void setSystemScope(String scope) {
        if(!this.systemScope.equals(scope)){
            this.scopeChanged = true;
            if (!CommonUtils.emptyString(scope)) {
                this.systemScope = scope;
            } else {
                this.systemScope = DefaultSystemScope;
            }
        }else {
            this.scopeChanged = false;
        }
    }

    public List<GPT35Message> getQAStack(int deep) {
        if (deep < 0) {
            deep = 1;
        }
        if (deep > MaxSize) {
            deep = MaxSize;
        }

        List<GPT35Message> messageList = new ArrayList<>();
        messageList.add(new GPT35Message(GPTRole.SYSTEM_ROLE, systemScope));

        int fromIndex = index - deep;
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        for (int i = fromIndex; i <= index; i++) {
            if (null != questions[i]) {
                messageList.add(questions[i]);
            }
            if (null != answers[i]) {
                messageList.add(answers[i]);
            }
        }
        return messageList;
    }

    public void appendAnswers(String an) {
        GPT35Message message = answers[index];
        if (null == message) {
            message = new GPT35Message(GPTRole.ASSISTANT_ROLE, an);
            answers[index] = message;
        } else {
            message.setContent(message.getContent().concat(an));
        }
    }

    public void removeLastQuestion() {
        if (index >= 0) {
            questions[index--] = null;
        }
    }

    public RDResult addQuestion(GPT35Message str) {
        if (index >= MaxSize - 1) {
            for (int i = 1; i <= index; i++) {
                questions[i - 1] = questions[i];
                answers[i - 1] = answers[i];
            }
        } else {
            index++;
        }
        questions[index] = str;
        answers[index] = null;

        return RDResult.instance(0, "");
    }

    public void logQAHistory(String message) {
        if (CommonUtils.emptyString(batchId)) {
            String cnt = questions[index].getContent();
            if(cnt.length() > 30) {
                batchTitle = cnt.substring(0, 29);
            }else {
                batchTitle = cnt;
            }
            String scope = CommonUtils.emptyString(systemScope)?DefaultSystemScope:systemScope;
            batchId = recordManager.createBatchTitle(batchTitle,scope);
        }

        ChatRecord record = new ChatRecord();
        record.setQuestion(questions[index].getContent());
        record.setAnswer(message);
        record.setTimestamp(new Date());
        recordManager.addChatRecordToBatch(batchId, record);
        updateChatBatchScope();
    }

    public void clear() {
        index = -1;
        for (int i = 0; i < MaxSize; i++) {
            questions[i] = null;
            answers[i] = null;
        }

        batchId = null;
        batchTitle = null;
        answerCache.clear();
    }

    public List<ChatBatchInfo> listChatHistory() {
        return recordManager.readJsonListFromFile();
    }

    public RDResult deleteChatBatch(String batchId) {
        List<ChatBatchInfo> list = recordManager.readJsonListFromFile();
        List<ChatBatchInfo> newList = new ArrayList<>();
        if (!CommonUtils.emptyList(list)) {
            for (ChatBatchInfo cbi : list) {
                if (!cbi.getBatchId().equalsIgnoreCase(batchId)) {
                    newList.add(cbi);
                }
            }
            recordManager.rewriteJsonToFile(newList);
            recordManager.deleteChatRecordByBatch(batchId);
        }
        if (null != this.batchId && this.batchId.equalsIgnoreCase(batchId)) {
            this.clear();
            return RDResult.instance(0, "", batchId);
        } else {
            return RDResult.instance(0, "");
        }
    }

    public ChatBatchInfo getChatBatch(String batchId) {
        List<ChatBatchInfo> list = recordManager.readJsonListFromFile();
        if (!CommonUtils.emptyList(list)) {
            for (ChatBatchInfo cbi : list) {
                if (cbi.getBatchId().equalsIgnoreCase(batchId)) {
                    return cbi;
                }
            }
        }
        return null;
    }

    public void updateChatBatchScope(){
        if(scopeChanged){
            List<ChatBatchInfo> list = recordManager.readJsonListFromFile();
            if (!CommonUtils.emptyList(list)) {
                for (ChatBatchInfo c : list) {
                    if (c.getBatchId().equalsIgnoreCase(batchId)) {
                        c.setSystemScope(systemScope);
                    }
                }
                recordManager.rewriteJsonToFile(list);
            }
        }
    }

    public RDResult updateChatBatchTitle(ChatBatchInfo cbi){
        if(null != cbi){
            List<ChatBatchInfo> list = recordManager.readJsonListFromFile();
            if (!CommonUtils.emptyList(list)) {
                for (ChatBatchInfo c : list) {
                    if (c.getBatchId().equalsIgnoreCase(cbi.getBatchId())) {
                        c.setBatchTitle(cbi.getBatchTitle());
                    }
                }
                recordManager.rewriteJsonToFile(list);
            }
        }
        return RDResult.instance(0,"");
    }

    public List<ChatRecord> listBatchChatRecords(String batchId) {
        List<ChatRecord> recordList = recordManager.searchChatRecordsByBatchId(batchId);
        if (!CommonUtils.emptyList(recordList)) {
            this.batchId = batchId;

            int fromIndex = recordList.size() > MaxSize ? recordList.size() - MaxSize : 0;
            int copySize = recordList.size() > MaxSize ? MaxSize : recordList.size();
            index = -1;
            for (int i = 0; i < copySize; i++) {
                GPT35Message q = new GPT35Message();
                GPT35Message a = new GPT35Message();

                ChatRecord record = recordList.get(fromIndex++);

                q.setRole(GPTRole.USER_ROLE);
                q.setContent(record.getQuestion());
                a.setRole(GPTRole.ASSISTANT_ROLE);
                a.setContent(record.getAnswer());

                index++;
                questions[index] = q;
                answers[index] = a;
            }

            answerCache.clear();
            for (ChatRecord record : recordList) {
                answerCache.put(record.getTmpSN(), record.getAnswer().replaceAll("\n", ""));
            }
        }
        return recordList;
    }

    public void cacheAnswer(String sn) {
        answerCache.put(sn, answers[index].getContent().replaceAll("\n", ""));
    }
}
